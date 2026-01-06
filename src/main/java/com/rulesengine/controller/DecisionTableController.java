package com.rulesengine.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;
import com.rulesengine.service.DecisionTableService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador para probar la funcionalidad de Decision Tables (CSV/Excel)
 * Este controlador demuestra cómo Drools puede cargar y ejecutar reglas
 * definidas en archivos CSV/Excel en lugar de archivos DRL tradicionales.
 */
@RestController
@RequestMapping("/api/decision-tables")
public class DecisionTableController {

    private static final Logger log = LoggerFactory.getLogger(DecisionTableController.class);
    private final DecisionTableService decisionTableService;

    public DecisionTableController(DecisionTableService decisionTableService) {
        this.decisionTableService = decisionTableService;
    }

    /**
     * Evalúa una transacción usando Decision Tables
     * POST /api/decision-tables/evaluate
     */
    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<RuleResult> evaluateTransaction(@RequestBody Transaction transaction) {
        log.info("Evaluating transaction {} with Decision Tables", transaction.getId());
        return decisionTableService.evaluateWithDecisionTables(transaction);
    }

    /**
     * Evalúa un lote de transacciones usando Decision Tables
     * POST /api/decision-tables/evaluate/batch
     */
    @PostMapping(value = "/evaluate/batch", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<RuleResult> evaluateBatch(@RequestBody List<Transaction> transactions) {
        log.info("Evaluating batch of {} transactions with Decision Tables", transactions.size());
        return decisionTableService.evaluateBatch(transactions);
    }

    /**
     * Test de carga con Decision Tables
     * POST /api/decision-tables/load-test?count=1000
     */
    @PostMapping(value = "/load-test", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<RuleResult> loadTest(@RequestParam(defaultValue = "1000") int count) {
        log.info("Starting load test with {} transactions using Decision Tables", count);
        return Flux.range(1, count)
                .map(i -> createTestTransaction("DT-TX-" + i))
                .flatMap(decisionTableService::evaluateWithDecisionTables, 200)
                .doOnNext(result -> log.debug("Processed: {}", result.getTransactionId()));
    }

    /**
     * Ejemplo de transacción pequeña (debe ser aprobada)
     * GET /api/decision-tables/example/small
     */
    @GetMapping("/example/small")
    public Mono<RuleResult> exampleSmallTransaction() {
        var transaction = createTestTransaction("EXAMPLE-SMALL");
        transaction.setAmount(BigDecimal.valueOf(50));
        transaction.setCountry("USA");
        log.info("Running example: Small transaction");
        return decisionTableService.evaluateWithDecisionTables(transaction);
    }

    /**
     * Ejemplo de transacción mediana (debe ir a revisión)
     * GET /api/decision-tables/example/medium
     */
    @GetMapping("/example/medium")
    public Mono<RuleResult> exampleMediumTransaction() {
        var transaction = createTestTransaction("EXAMPLE-MEDIUM");
        transaction.setAmount(BigDecimal.valueOf(2500));
        transaction.setCountry("CANADA");
        transaction.setAccountTier("SILVER");
        log.info("Running example: Medium transaction");
        return decisionTableService.evaluateWithDecisionTables(transaction);
    }

    /**
     * Ejemplo de transacción grande (debe ser rechazada)
     * GET /api/decision-tables/example/large
     */
    @GetMapping("/example/large")
    public Mono<RuleResult> exampleLargeTransaction() {
        var transaction = createTestTransaction("EXAMPLE-LARGE");
        transaction.setAmount(BigDecimal.valueOf(15000));
        transaction.setCountry("HIGH_RISK_COUNTRY_1");
        log.info("Running example: Large transaction from high-risk country");
        return decisionTableService.evaluateWithDecisionTables(transaction);
    }

    /**
     * Ejemplo de transacción VIP (debe tener beneficios)
     * GET /api/decision-tables/example/vip
     */
    @GetMapping("/example/vip")
    public Mono<RuleResult> exampleVIPTransaction() {
        var transaction = createTestTransaction("EXAMPLE-VIP");
        transaction.setAmount(BigDecimal.valueOf(30000));
        transaction.setCountry("USA");
        transaction.setAccountTier("PLATINUM");
        transaction.setIsVIP(true);
        log.info("Running example: VIP transaction");
        return decisionTableService.evaluateWithDecisionTables(transaction);
    }

    /**
     * Health check para Decision Tables
     * GET /api/decision-tables/health
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Decision Tables service is running");
    }

    /**
     * Crea una transacción de prueba con valores aleatorios
     */
    private Transaction createTestTransaction(String id) {
        var transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId("USER-" + (int)(Math.random() * 1000));
        transaction.setAmount(BigDecimal.valueOf(Math.random() * 20000));
        transaction.setCurrency("USD");
        transaction.setTransactionType("PURCHASE");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setCountry(getRandomCountry());
        transaction.setAccountTier(getRandomTier());
        transaction.setIsVIP(Math.random() > 0.8);
        return transaction;
    }

    private String getRandomCountry() {
        String[] countries = {"USA", "CANADA", "MEXICO", "HIGH_RISK_COUNTRY_1", "HIGH_RISK_COUNTRY_2"};
        return countries[(int)(Math.random() * countries.length)];
    }

    private String getRandomTier() {
        String[] tiers = {"BRONZE", "SILVER", "GOLD", "PLATINUM"};
        return tiers[(int)(Math.random() * tiers.length)];
    }
}

