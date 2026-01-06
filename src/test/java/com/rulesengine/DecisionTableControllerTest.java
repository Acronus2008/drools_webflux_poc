package com.rulesengine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.rulesengine.model.Transaction;
import com.rulesengine.service.DecisionTableService;

import reactor.test.StepVerifier;

@SpringBootTest
class DecisionTableControllerTest {

    @Autowired
    private DecisionTableService decisionTableService;

    @Test
    void contextLoads() {
        assertNotNull(decisionTableService);
    }

    @Test
    void testSmallTransactionWithDecisionTables() {
        Transaction tx = new Transaction();
        tx.setId("DT-TEST-001");
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("PURCHASE");
        tx.setTimestamp(LocalDateTime.now());
        tx.setCountry("USA");

        StepVerifier.create(decisionTableService.evaluateWithDecisionTables(tx))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("DT-TEST-001", result.getTransactionId());
                    assertNotNull(result.getStatus());
                    assertNotNull(result.getProcessingTimeMs());
                    assertEquals("DECISION_TABLE", result.getComplexityLevel());
                })
                .verifyComplete();
    }

    @Test
    void testMediumTransactionWithDecisionTables() {
        Transaction tx = new Transaction();
        tx.setId("DT-TEST-002");
        tx.setAmount(BigDecimal.valueOf(2500));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("PURCHASE");
        tx.setTimestamp(LocalDateTime.now());
        tx.setCountry("CANADA");
        tx.setAccountTier("SILVER");

        StepVerifier.create(decisionTableService.evaluateWithDecisionTables(tx))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("DT-TEST-002", result.getTransactionId());
                    assertNotNull(result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void testLargeTransactionWithDecisionTables() {
        Transaction tx = new Transaction();
        tx.setId("DT-TEST-003");
        tx.setAmount(BigDecimal.valueOf(15000));
        tx.setStatus("PENDING");
        tx.setCurrency("USD");
        tx.setTransactionType("PURCHASE");
        tx.setTimestamp(LocalDateTime.now());
        tx.setCountry("HIGH_RISK_COUNTRY_1");

        StepVerifier.create(decisionTableService.evaluateWithDecisionTables(tx))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("DT-TEST-003", result.getTransactionId());
                    assertNotNull(result.getStatus());
                })
                .verifyComplete();
    }
}

