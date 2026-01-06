package com.rulesengine.controller;

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
import com.rulesengine.service.RulesEngineService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/rules") 
public class RulesEngineController {
    
    private static final Logger log = LoggerFactory.getLogger(RulesEngineController.class);
    private final RulesEngineService rulesEngineService;
    
    public RulesEngineController(RulesEngineService rulesEngineService) {
        this.rulesEngineService = rulesEngineService;
    }

    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<RuleResult> evaluateTransaction(
            @RequestBody Transaction transaction,
            @RequestParam(defaultValue = "ALL") String complexity) {
        log.info("Evaluating transaction {} with complexity {}", transaction.getId(), complexity);
        return rulesEngineService.evaluateTransaction(transaction, complexity);
    }

    @PostMapping(value = "/evaluate/batch", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<RuleResult> evaluateBatch(
            @RequestBody List<Transaction> transactions,
            @RequestParam(defaultValue = "ALL") String complexity) {
        log.info("Evaluating batch of {} transactions with complexity {}", transactions.size(), complexity);
        return Flux.fromIterable(transactions)
                .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, complexity), 100) // Concurrencia de 100
                .doOnError(error -> log.error("Error in batch evaluation", error));
    }

    @PostMapping(value = "/evaluate/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<RuleResult> evaluateStream(
            @RequestBody Flux<Transaction> transactions,
            @RequestParam(defaultValue = "ALL") String complexity) {
        log.info("Streaming evaluation with complexity {}", complexity);
        return transactions
                .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, complexity), 100)
                .doOnError(error -> log.error("Error in stream evaluation", error));
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Rules Engine is running");
    }

    @PostMapping("/load-test/low")
    public Flux<RuleResult> loadTestLow(@RequestParam(defaultValue = "1000") int count) {
        log.info("Starting load test with {} LOW complexity transactions", count);
        return Flux.range(1, count)
                .map(i -> createTestTransaction("TX-LOW-" + i, "LOW"))
                .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, "LOW"), 200)
                .doOnNext(result -> log.debug("Processed: {}", result.getTransactionId()));
    }

    @PostMapping("/load-test/medium")
    public Flux<RuleResult> loadTestMedium(@RequestParam(defaultValue = "1000") int count) {
        log.info("Starting load test with {} MEDIUM complexity transactions", count);
        return Flux.range(1, count)
                .map(i -> createTestTransaction("TX-MED-" + i, "MEDIUM"))
                .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, "MEDIUM"), 200)
                .doOnNext(result -> log.debug("Processed: {}", result.getTransactionId()));
    }

    @PostMapping("/load-test/high")
    public Flux<RuleResult> loadTestHigh(@RequestParam(defaultValue = "1000") int count) {
        log.info("Starting load test with {} HIGH complexity transactions", count);
        return Flux.range(1, count)
                .map(i -> createTestTransaction("TX-HIGH-" + i, "HIGH"))
                .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, "HIGH"), 200)
                .doOnNext(result -> log.debug("Processed: {}", result.getTransactionId()));
    }

    @PostMapping("/load-test/mixed")
    public Flux<RuleResult> loadTestMixed(@RequestParam(defaultValue = "1000") int count) {
        log.info("Starting load test with {} MIXED complexity transactions", count);
        return Flux.range(1, count)
                .map(i -> {
                    String complexity = i % 3 == 0 ? "HIGH" : (i % 2 == 0 ? "MEDIUM" : "LOW");
                    return createTestTransaction("TX-MIX-" + i, complexity);
                })
                .flatMap(tx -> {
                    String complexity = tx.getId().contains("LOW") ? "LOW" : 
                                      (tx.getId().contains("MED") ? "MEDIUM" : "HIGH");
                    return rulesEngineService.evaluateTransaction(tx, complexity);
                }, 200)
                .doOnNext(result -> log.debug("Processed: {}", result.getTransactionId()));
    }

    private Transaction createTestTransaction(String id, String complexity) {
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setUserId("USER-" + (int)(Math.random() * 1000));
        tx.setCurrency("USD");
        tx.setTransactionType("PURCHASE");
        tx.setCountry("USA");
        tx.setTimestamp(java.time.LocalDateTime.now());
        
        // Configurar según complejidad
        switch (complexity) {
            case "LOW" -> tx.setAmount(java.math.BigDecimal.valueOf(Math.random() * 5000));
            case "MEDIUM" -> {
                tx.setAmount(java.math.BigDecimal.valueOf(5000 + Math.random() * 10000));
                tx.setIsVIP(Math.random() > 0.7);
                tx.setAccountTier(Math.random() > 0.5 ? "GOLD" : "SILVER");
            }
            case "HIGH" -> {
                tx.setAmount(java.math.BigDecimal.valueOf(10000 + Math.random() * 50000));
                tx.setUserAge((int)(18 + Math.random() * 60));
                tx.setAccountAgeDays((int)(Math.random() * 1000));
                tx.setMonthlyTransactionVolume(java.math.BigDecimal.valueOf(Math.random() * 20000));
                tx.setFailedTransactionsLastMonth((int)(Math.random() * 10));
                tx.setIsVIP(Math.random() > 0.8);
                tx.setAccountTier(Math.random() > 0.6 ? "PLATINUM" :
                        Math.random() > 0.3 ? "GOLD" : "SILVER");
            }
        }
        
        return tx;
    }
}

