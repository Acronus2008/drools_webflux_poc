package com.rulesengine.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    private static final String RULES_PATH = "rules/";
    private final KieServices kieServices = KieServices.Factory.get();

    @Bean("kieContainerRules")
    public KieContainer kieContainer() {
        var kieFileSystem = getKieFileSystem();
        
        var kieBuilder = getKieBuilder(kieFileSystem);

        var kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    private KieBuilder getKieBuilder(KieFileSystem kieFileSystem) {
        var kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            var errors = kieBuilder.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR);
            throw new RuntimeException("Error building rules: " + errors);
        }
        return kieBuilder;
    }

    private KieFileSystem getKieFileSystem() {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Cargar kmodule.xml
        kieFileSystem.write(ResourceFactory.newClassPathResource("META-INF/kmodule.xml"));
        
        // Cargar reglas de diferentes niveles de complejidad
        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + "low-complexity-rules.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + "medium-complexity-rules.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + "high-complexity-rules.drl"));
        return kieFileSystem;
    }
}

