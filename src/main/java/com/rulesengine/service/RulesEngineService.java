package com.rulesengine.service;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;

import reactor.core.publisher.Mono;

 
/**
 * Servicio para evaluar transacciones usando reglas definidas en Decision Tables (CSV)
 * @author Alejandro Carlos Pantaleón Urbay
 * @version 1.0
 * @since 2026-01-06
 * @see com.rulesengine.model.RuleResult
 * @see com.rulesengine.model.Transaction
 * @see reactor.core.publisher.Mono
 */
public interface RulesEngineService {
    /**
     * Evalúa una transacción usando reglas definidas en Decision Tables (CSV)
     * @param transaction la transacción a evaluar
     * @param complexityLevel nivel de complejidad de la transacción
     * @return Mono con el resultado de la evaluación
     */
    Mono<RuleResult> evaluateTransaction(Transaction transaction, String complexityLevel);

    /**
     * Evalúa una transacción usando todas las reglas definidas en Decision Tables (CSV)
     * @param transaction la transacción a evaluar
     * @return Mono con el resultado de la evaluación
     */
    Mono<RuleResult> evaluateTransactionWithAllRules(Transaction transaction);
}

