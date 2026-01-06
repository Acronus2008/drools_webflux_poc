package com.rulesengine.service.impl;

import java.util.List;

import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;
import com.rulesengine.service.DecisionTableService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

 
@Service
public class DecisionTableServiceImpl implements DecisionTableService {


    // Usar sesión por defecto ya que el kmodule.xml no está creando la sesión con nombre
    // private static final String DECISION_TABLE_SESSION = "decisionTableSession";
    private static final Logger log = LoggerFactory.getLogger(DecisionTableServiceImpl.class);
    
    private final KieContainer decisionTableKieContainer;

    public DecisionTableServiceImpl(@Qualifier("decisionTableKieContainer") KieContainer decisionTableKieContainer) {
        this.decisionTableKieContainer = decisionTableKieContainer;
    }

    /**
     * Evalúa una transacción usando reglas definidas en Decision Tables (CSV)
     * @param transaction la transacción a evaluar
     * @return Mono con el resultado de la evaluación
     */
    @Override
    public Mono<RuleResult> evaluateWithDecisionTables(Transaction transaction) {
        return Mono.fromCallable(() -> {
            var startTime = System.currentTimeMillis();
            
            // Crear una nueva sesión de Drools para cada transacción (thread-safe)
            // Usar la sesión por defecto del KieContainer
            var kieSession = decisionTableKieContainer.newKieSession();
            
            if (kieSession == null) {
                throw new RuntimeException("KieSession is null, cannot evaluate transaction. Available KieBases: " + 
                    decisionTableKieContainer.getKieBaseNames());
            }
            
            try {
                // Configurar la transacción
                transaction.setStatus("PENDING");
                if (transaction.getRiskScore() == null) {
                    transaction.setRiskScore(0);
                }
                
                // Insertar la transacción en la sesión
                kieSession.insert(transaction);
                
                // Ejecutar todas las reglas de las Decision Tables
                var rulesFired = kieSession.fireAllRules();
                
                var processingTime = System.currentTimeMillis() - startTime;
                
                // Construir el resultado
                var result = buildResult(transaction, rulesFired, processingTime);
                
                if (transaction.getRejectionReason() != null) {
                    result.getReasons().add(transaction.getRejectionReason());
                }
                
                log.debug("Transaction {} evaluated with Decision Tables in {}ms with {} rules fired", 
                    transaction.getId(), processingTime, rulesFired);
                
                return result;
            } finally {
                if (kieSession != null) {
                    kieSession.dispose();
                }
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error evaluating transaction with Decision Tables: {}", 
            transaction.getId(), error));
    }

    /**
     * Evalúa múltiples transacciones usando Decision Tables
     * @param transactions lista de transacciones
     * @return Flux con los resultados
     */
    @Override
    public reactor.core.publisher.Flux<RuleResult> evaluateBatch(List<Transaction> transactions) {
        return reactor.core.publisher.Flux.fromIterable(transactions)
                .flatMap(this::evaluateWithDecisionTables, 100)
                .doOnError(error -> log.error("Error in batch evaluation with Decision Tables", error));
    }

    /**
     * Construye el resultado de la evaluación de una transacción
     * @param transaction la transacción evaluada
     * @param rulesFired número de reglas ejecutadas
     * @param processingTime tiempo de procesamiento en milisegundos
     * @return RuleResult con los resultados de la evaluación
     */
    private RuleResult buildResult(Transaction transaction, int rulesFired, long processingTime) {
        var result = new RuleResult();
        result.setTransactionId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setFinalRiskScore(transaction.getRiskScore());
        result.setProcessingTimeMs(processingTime);
        result.setComplexityLevel("DECISION_TABLE");
        result.setAppliedRules(List.of("Decision Table Rules fired: " + rulesFired));
        return result;
    }
}
