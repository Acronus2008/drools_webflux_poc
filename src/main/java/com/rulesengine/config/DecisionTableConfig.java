package com.rulesengine.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DecisionTableConfig {

    private static final String DECISION_TABLES_PATH = "decisiontables/";
    private final KieServices kieServices = KieServices.Factory.get();

    @Bean("decisionTableKieContainer")
    public KieContainer decisionTableKieContainer() {
        try {
            var kieFileSystem = getKieFileSystem();
            
            var kieBuilder = getKieBuilder(kieFileSystem);
            
            var kieModule = kieBuilder.getKieModule();
            return kieServices.newKieContainer(kieModule.getReleaseId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Decision Table KieContainer", e);
        }
    }

    private KieFileSystem getKieFileSystem() {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Cargar kmodule.xml para Decision Tables
        kieFileSystem.write(ResourceFactory.newClassPathResource("META-INF/decisiontable-kmodule.xml"));
        
        // Cargar reglas DRL compiladas desde Decision Tables CSV
        // NOTA: Los archivos CSV (transaction-rules.csv, country-risk-rules.csv, account-tier-rules.csv)
        // son la fuente de verdad para reglas dinámicas. Estos CSV pueden ser modificados sin recompilar.
        // Los archivos DRL correspondientes son la versión compilada de los CSV.
        // 
        // En producción, se podría usar SpreadsheetCompiler para compilar CSV a DRL en tiempo de ejecución,
        // pero para esta POC mantenemos ambos: CSV (fuente) y DRL (compilado) para demostrar
        // la diferencia entre reglas estáticas (DRL directo) y dinámicas (desde CSV).
        kieFileSystem.write(ResourceFactory.newClassPathResource(
            DECISION_TABLES_PATH + "transaction-rules.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource(
            DECISION_TABLES_PATH + "country-risk-rules.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource(
            DECISION_TABLES_PATH + "account-tier-rules.drl"));
        return kieFileSystem;
    }

    private KieBuilder getKieBuilder(KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        var results = kieBuilder.getResults();
        
        // Log warnings
        if (results.hasMessages(org.kie.api.builder.Message.Level.WARNING)) {
            var warnings = results.getMessages(org.kie.api.builder.Message.Level.WARNING);
            System.err.println("Warnings building Decision Tables:");
            warnings.forEach(w -> System.err.println("  " + w.getText()));
        }
        
        // Throw exception on errors
        if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            var errors = results.getMessages(org.kie.api.builder.Message.Level.ERROR);
            System.err.println("Errors building Decision Tables:");
            errors.forEach(e -> System.err.println("  " + e.getText() + " at " + e.getPath()));
            throw new RuntimeException("Error building Decision Tables: " + errors);
        }
        
        return kieBuilder;
    }
}

