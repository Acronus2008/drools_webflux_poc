package com.rulesengine.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;
import com.rulesengine.service.DynamicRulesService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Controlador para gestionar reglas dinámicas
 * Permite subir, compilar y ejecutar reglas desde archivos sin reiniciar la aplicación
 */
@RestController
@RequestMapping("/api/dynamic-rules")
public class DynamicRulesController {

    private static final Logger log = LoggerFactory.getLogger(DynamicRulesController.class);
    private final DynamicRulesService dynamicRulesService;

    public DynamicRulesController(DynamicRulesService dynamicRulesService) {
        this.dynamicRulesService = dynamicRulesService;
    }

    /**
     * Sube un archivo de reglas (DRL o CSV) y lo compila
     * POST /api/dynamic-rules/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> uploadRule(@RequestPart("file") FilePart filePart) {
        String fileName = filePart.filename();
        log.info("Uploading rule file: {}", fileName);

        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .flatMap(fileContent -> Mono.fromCallable(() -> {
                    boolean success = dynamicRulesService.uploadAndCompileRule(fileContent, fileName);

                    Map<String, Object> response = new HashMap<>();
                    if (success) {
                        response.put("status", "success");
                        response.put("message", "Rule file uploaded and compiled successfully");
                        response.put("fileName", fileName);
                        response.put("compilationTime", LocalDateTime.now());
                        return ResponseEntity.ok(response);
                    } else {
                        response.put("status", "error");
                        response.put("message", "Failed to compile rule file. Check logs for details.");
                        response.put("fileName", fileName);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }).subscribeOn(Schedulers.boundedElastic()))
                .onErrorResume(error -> {
                    log.error("Error uploading rule file", error);
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Error uploading file: " + error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    /**
     * Lista todos los archivos de reglas dinámicas
     * GET /api/dynamic-rules/list
     */
    @GetMapping("/list")
    public Mono<ResponseEntity<Map<String, Object>>> listRules() {
        return Mono.fromCallable(() -> {
            List<String> files = dynamicRulesService.listDynamicRules();
            Map<String, Object> response = new HashMap<>();
            response.put("files", files);
            response.put("count", files.size());
            response.put("hasRules", dynamicRulesService.hasDynamicRules());
            response.put("lastCompilationTime", dynamicRulesService.getLastCompilationTime());
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Elimina un archivo de reglas
     * DELETE /api/dynamic-rules/{fileName}
     */
    @DeleteMapping("/{fileName}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteRule(@PathVariable String fileName) {
        return Mono.fromCallable(() -> {
            log.info("Deleting rule file: {}", fileName);
            boolean success = dynamicRulesService.deleteRule(fileName);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Rule file deleted and rules reloaded");
                response.put("fileName", fileName);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to delete rule file");
                response.put("fileName", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Recarga todas las reglas dinámicas
     * POST /api/dynamic-rules/reload
     */
    @PostMapping("/reload")
    public Mono<ResponseEntity<Map<String, Object>>> reloadRules() {
        return Mono.fromCallable(() -> {
            log.info("Reloading dynamic rules");
            boolean success = dynamicRulesService.loadDynamicRules();

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Dynamic rules reloaded successfully");
                response.put("compilationTime", LocalDateTime.now());
                response.put("files", dynamicRulesService.listDynamicRules());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to reload dynamic rules. Check logs for details.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Evalúa una transacción usando reglas dinámicas
     * POST /api/dynamic-rules/evaluate
     */
    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<RuleResult> evaluateWithDynamicRules(@RequestBody Transaction transaction) {
        return Mono.fromCallable(() -> {
            KieContainer container = dynamicRulesService.getDynamicKieContainer();
            if (container == null) {
                throw new RuntimeException("No dynamic rules loaded. Please upload rule files first.");
            }

            long startTime = System.currentTimeMillis();
            KieSession kieSession = container.newKieSession();

            try {
                transaction.setStatus("PENDING");
                if (transaction.getRiskScore() == null) {
                    transaction.setRiskScore(0);
                }

                kieSession.insert(transaction);
                int rulesFired = kieSession.fireAllRules();

                long processingTime = System.currentTimeMillis() - startTime;

                RuleResult result = new RuleResult();
                result.setTransactionId(transaction.getId());
                result.setStatus(transaction.getStatus());
                result.setFinalRiskScore(transaction.getRiskScore());
                result.setProcessingTimeMs(processingTime);
                result.setComplexityLevel("DYNAMIC");
                result.setAppliedRules(List.of("Dynamic Rules fired: " + rulesFired));

                if (transaction.getRejectionReason() != null) {
                    result.getReasons().add(transaction.getRejectionReason());
                }

                log.debug("Transaction {} evaluated with dynamic rules in {}ms with {} rules fired",
                    transaction.getId(), processingTime, rulesFired);

                return result;
            } finally {
                kieSession.dispose();
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error evaluating transaction with dynamic rules: {}", 
            transaction.getId(), error));
    }

    /**
     * Health check para reglas dinámicas
     * GET /api/dynamic-rules/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "running");
            response.put("hasRules", dynamicRulesService.hasDynamicRules());
            response.put("ruleFiles", dynamicRulesService.listDynamicRules().size());
            response.put("lastCompilationTime", dynamicRulesService.getLastCompilationTime());
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

