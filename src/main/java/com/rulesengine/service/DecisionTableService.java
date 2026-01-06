package com.rulesengine.service;

import java.util.List;

import com.rulesengine.model.RuleResult;
import com.rulesengine.model.Transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio para evaluar transacciones usando Decision Tables (CSV)
 * @author Alejandro Carlos Pantaleón Urbay
 * @version 1.0
 * @since 2026-01-06
 * @see com.rulesengine.model.RuleResult
 * @see com.rulesengine.model.Transaction
 * @see reactor.core.publisher.Mono
 * @see reactor.core.publisher.Flux
 */
public interface DecisionTableService {

    /**
     * Evalúa una transacción usando reglas definidas en Decision Tables (CSV)
     * @param transaction la transacción a evaluar
     * @return Mono con el resultado de la evaluación
     */
     Mono<RuleResult> evaluateWithDecisionTables(Transaction transaction);

    /**
     * Evalúa múltiples transacciones usando Decision Tables
     * @param transactions lista de transacciones
     * @return Flux con los resultados
     */
     Flux<RuleResult> evaluateBatch(List<Transaction> transactions); 
}

