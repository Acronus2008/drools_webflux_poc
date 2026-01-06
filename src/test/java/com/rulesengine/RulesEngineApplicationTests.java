package com.rulesengine;

import com.rulesengine.model.Transaction;
import com.rulesengine.model.RuleResult;
import com.rulesengine.service.RulesEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RulesEngineApplicationTests {

    @Autowired
    private RulesEngineService rulesEngineService;

    @Test
    void contextLoads() {
        assertNotNull(rulesEngineService);
    }

    @Test
    void testLowComplexityRule() {
        Transaction tx = new Transaction();
        tx.setId("TEST-001");
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("PURCHASE");
        tx.setTimestamp(LocalDateTime.now());

        StepVerifier.create(rulesEngineService.evaluateTransaction(tx, "LOW"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("TEST-001", result.getTransactionId());
                    assertNotNull(result.getStatus());
                    assertNotNull(result.getProcessingTimeMs());
                })
                .verifyComplete();
    }

    @Test
    void testMediumComplexityRule() {
        Transaction tx = new Transaction();
        tx.setId("TEST-002");
        tx.setAmount(BigDecimal.valueOf(6000));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("WITHDRAWAL");
        tx.setTimestamp(LocalDateTime.now());
        tx.setIsVIP(false);
        tx.setAccountTier("BRONZE");

        StepVerifier.create(rulesEngineService.evaluateTransaction(tx, "MEDIUM"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("TEST-002", result.getTransactionId());
                    assertNotNull(result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void testHighComplexityRule() {
        Transaction tx = new Transaction();
        tx.setId("TEST-003");
        tx.setAmount(BigDecimal.valueOf(15000));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("TRANSFER");
        tx.setTimestamp(LocalDateTime.now());
        tx.setUserAge(30);
        tx.setAccountAgeDays(500);
        tx.setMonthlyTransactionVolume(BigDecimal.valueOf(8000));
        tx.setFailedTransactionsLastMonth(0);
        tx.setIsVIP(true);
        tx.setAccountTier("PLATINUM");

        StepVerifier.create(rulesEngineService.evaluateTransaction(tx, "HIGH"))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("TEST-003", result.getTransactionId());
                    assertNotNull(result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void testConcurrentEvaluations() {
        Flux<Transaction> transactions = Flux.range(1, 100)
                .map(i -> {
                    Transaction tx = new Transaction();
                    tx.setId("CONCURRENT-" + i);
                    tx.setAmount(BigDecimal.valueOf(Math.random() * 10000));
                    tx.setStatus("PENDING");
                    tx.setCurrency("USD");
                    tx.setTransactionType("PURCHASE");
                    tx.setTimestamp(LocalDateTime.now());
                    return tx;
                });

        long startTime = System.currentTimeMillis();
        
        StepVerifier.create(
                transactions
                        .flatMap(tx -> rulesEngineService.evaluateTransaction(tx, "ALL"), 50)
                        .collectList()
        )
                .assertNext(results -> {
                    assertEquals(100, results.size());
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("Processed 100 transactions in " + duration + "ms");
                    assertTrue(duration < 10000, "Should process 100 transactions in less than 10 seconds");
                })
                .verifyComplete();
    }
}

