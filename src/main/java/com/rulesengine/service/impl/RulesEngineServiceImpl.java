package com.rulesengine.service.impl;

import java.util.List;
import java.util.function.UnaryOperator;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.rule.AgendaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;
import com.rulesengine.service.RulesEngineService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class RulesEngineServiceImpl implements RulesEngineService {

    private static final String DEFAULT_RULES_SESSION = "rulesSession";

    private static final Logger log = LoggerFactory.getLogger(RulesEngineService.class);
    private final KieContainer kieContainer;

    public RulesEngineServiceImpl(@Qualifier("kieContainerRules") KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }


    @Override
    public Mono<RuleResult> evaluateTransaction(Transaction transaction, String complexityLevel) {
        return Mono.fromCallable(() -> {
            var startTime = System.currentTimeMillis();
            
            // Crear una nueva sesión de Drools para cada transacción (thread-safe)
            var kieSession = kieContainer.newKieSession(DEFAULT_RULES_SESSION);
            
            try {
                // Configurar la transacción
                transaction.setStatus("PENDING");
                if (transaction.getRiskScore() == null) {
                    transaction.setRiskScore(0);
                }
                
                // Insertar la transacción en la sesión
                kieSession.insert(transaction);
                
                // Ejecutar las reglas según el nivel de complejidad usando AgendaFilter
                
                var packageName = getPackageName.apply(complexityLevel);
                
                var rulesFired = packageName != null ? kieSession.fireAllRules(getFilter(packageName)) : kieSession.fireAllRules();
                
                var processingTime = System.currentTimeMillis() - startTime;
                
                // Construir el resultado
                var result = getResult(transaction, complexityLevel, rulesFired, processingTime);
                
                if (transaction.getRejectionReason() != null) 
                    result.getReasons().add(transaction.getRejectionReason());
                
                
                log.debug("Transaction {} evaluated in {}ms with {} rules fired", transaction.getId(), processingTime, rulesFired);
                
                return result;
            } finally {
                kieSession.dispose();
            }
        })
        .subscribeOn(Schedulers.boundedElastic()) // Ejecutar en thread pool dedicado para operaciones bloqueantes
        .doOnError(error -> log.error("Error evaluating transaction: {}", transaction.getId(), error));
    }

  
    @Override
    public Mono<RuleResult> evaluateTransactionWithAllRules(Transaction transaction) {
        return evaluateTransaction(transaction, "ALL");
    }

    /**
     * Obtiene el filtro de las reglas según el paquete de reglas
     * @param packageName el paquete de reglas
     * @return el filtro de las reglas
     */
    private AgendaFilter getFilter(String packageName) {
        AgendaFilter filter = match -> match.getRule().getPackageName().equals(packageName);
        return filter;
    }
 

    /**
     * Obtiene el paquete de reglas según el nivel de complejidad
     * @param level el nivel de complejidad
     * @return el paquete de reglas
     */
    private final UnaryOperator<String> getPackageName = level -> switch (level) {
        case "LOW" -> "com.rulesengine.rules.low";
        case "MEDIUM" -> "com.rulesengine.rules.medium";
        case "HIGH" -> "com.rulesengine.rules.high";
        default -> null;
    }; 

    /**
     * Obtiene el resultado de la evaluación de la transacción
     * @param transaction la transacción
     * @param complexityLevel el nivel de complejidad
     * @param rulesFired el número de reglas ejecutadas
     * @param processingTime el tiempo de procesamiento
     * @return el resultado de la evaluación de la transacción
     */
    private RuleResult getResult(Transaction transaction, String complexityLevel, int rulesFired, long processingTime) {
        var result = new RuleResult();
        result.setTransactionId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setFinalRiskScore(transaction.getRiskScore());
        result.setProcessingTimeMs(processingTime);
        result.setComplexityLevel(complexityLevel);
        result.setAppliedRules(List.of("Rules fired: " + rulesFired));
        return result;
    }
}
