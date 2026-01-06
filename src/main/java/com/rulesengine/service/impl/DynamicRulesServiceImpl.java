package com.rulesengine.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.rulesengine.service.DynamicRulesService;

@Service
public class DynamicRulesServiceImpl implements DynamicRulesService {
    private static final Logger log = LoggerFactory.getLogger(DynamicRulesServiceImpl.class);
    private static final String DYNAMIC_RULES_DIR = "dynamic-rules";
    private final KieServices kieServices = KieServices.Factory.get();
    
    private KieContainer dynamicKieContainer;
    private long lastCompilationTime = 0;

    public DynamicRulesServiceImpl() {
        // Crear directorio si no existe
        createDynamicRulesDirectory();
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void initialize() {
        // Cargar reglas dinámicas iniciales
        loadDynamicRules();
    }

    @Override
    public synchronized boolean loadDynamicRules() {
        try {
            Path rulesDir = Paths.get(DYNAMIC_RULES_DIR);
            if (!Files.exists(rulesDir) || !Files.isDirectory(rulesDir)) {
                log.warn("Dynamic rules directory does not exist: {}", DYNAMIC_RULES_DIR);
                return false;
            }

            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            List<String> loadedFiles = new ArrayList<>();

            // Cargar archivos DRL
            try (Stream<Path> paths = Files.walk(rulesDir)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".drl"))
                    .forEach(drlFile -> {
                        try {
                            String relativePath = rulesDir.relativize(drlFile).toString();
                            kieFileSystem.write(ResourceFactory.newFileResource(drlFile.toFile()));
                            loadedFiles.add(relativePath);
                            log.info("Loaded DRL file: {}", relativePath);
                        } catch (Exception e) {
                            log.error("Error loading DRL file: {}", drlFile, e);
                        }
                    });
            }

            // Cargar archivos CSV (Decision Tables)
            try (Stream<Path> paths = Files.walk(rulesDir)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(csvFile -> {
                        try {
                            String relativePath = rulesDir.relativize(csvFile).toString();
                            // Para CSV, necesitamos compilarlos primero o usar como DRL
                            // Por ahora, los tratamos como recursos que se compilarán
                            kieFileSystem.write(ResourceFactory.newFileResource(csvFile.toFile()));
                            loadedFiles.add(relativePath);
                            log.info("Loaded CSV file: {}", relativePath);
                        } catch (Exception e) {
                            log.error("Error loading CSV file: {}", csvFile, e);
                        }
                    });
            }

            if (loadedFiles.isEmpty()) {
                log.warn("No rule files found in {}", DYNAMIC_RULES_DIR);
                return false;
            }

            // Compilar reglas
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            // Verificar errores
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
                log.error("Errors compiling dynamic rules:");
                errors.forEach(e -> log.error("  {} at {}", e.getText(), e.getPath()));
                return false;
            }

            // Crear nuevo KieContainer
            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            // Reemplazar el contenedor anterior
            if (dynamicKieContainer != null) {
                dynamicKieContainer.dispose();
            }
            dynamicKieContainer = newContainer;
            lastCompilationTime = System.currentTimeMillis();

            log.info("Successfully loaded {} dynamic rule files", loadedFiles.size());
            return true;

        } catch (Exception e) {
            log.error("Failed to load dynamic rules", e);
            return false;
        }
    }

    /**
     * Sube un archivo de reglas y lo compila
     * @param fileContent contenido del archivo
     * @param fileName nombre del archivo
     * @return true si fue exitoso
     */
    @Override
    public synchronized boolean uploadAndCompileRule(byte[] fileContent, String fileName) {
        try {
            Path rulesDir = Paths.get(DYNAMIC_RULES_DIR);
            createDynamicRulesDirectory();

            Path targetFile = rulesDir.resolve(fileName);
            Files.write(targetFile, fileContent);

            log.info("Uploaded rule file: {}", fileName);
            
            // Recargar todas las reglas
            return loadDynamicRules();

        } catch (IOException e) {
            log.error("Error uploading rule file: {}", fileName, e);
            return false;
        }
    }

    /**
     * Elimina un archivo de reglas
     * @param fileName nombre del archivo
     * @return true si fue exitoso
     */
    @Override
    public synchronized boolean deleteRule(String fileName) {
        try {
            Path rulesDir = Paths.get(DYNAMIC_RULES_DIR);
            Path targetFile = rulesDir.resolve(fileName);

            if (!Files.exists(targetFile)) {
                log.warn("Rule file not found: {}", fileName);
                return false;
            }

            Files.delete(targetFile);
            log.info("Deleted rule file: {}", fileName);

            // Recargar todas las reglas
            return loadDynamicRules();

        } catch (IOException e) {
            log.error("Error deleting rule file: {}", fileName, e);
            return false;
        }
    }

    /**
     * Lista todos los archivos de reglas dinámicas
     * @return lista de nombres de archivos
     */
    @Override
    public List<String> listDynamicRules() {
        List<String> files = new ArrayList<>();
        try {
            Path rulesDir = Paths.get(DYNAMIC_RULES_DIR);
            if (!Files.exists(rulesDir)) {
                return files;
            }

            try (Stream<Path> paths = Files.walk(rulesDir)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".drl") || p.toString().endsWith(".csv"))
                    .forEach(file -> {
                        String relativePath = rulesDir.relativize(file).toString();
                        files.add(relativePath);
                    });
            }
        } catch (IOException e) {
            log.error("Error listing dynamic rules", e);
        }
        return files;
    }

    /**
     * Obtiene el KieContainer para reglas dinámicas
     * @return KieContainer o null si no hay reglas cargadas
     */
    @Override
    public KieContainer getDynamicKieContainer() {
        return dynamicKieContainer;
    }

    /**
     * Verifica si hay reglas dinámicas cargadas
     * @return true si hay reglas cargadas
     */
    @Override
    public boolean hasDynamicRules() {
        return dynamicKieContainer != null;
    }

    /**
     * Obtiene el tiempo de la última compilación
     * @return timestamp de última compilación
     */
    @Override
    public long getLastCompilationTime() {
        return lastCompilationTime;
    }

    private void createDynamicRulesDirectory() {
        try {
            Path rulesDir = Paths.get(DYNAMIC_RULES_DIR);
            if (!Files.exists(rulesDir)) {
                Files.createDirectories(rulesDir);
                log.info("Created dynamic rules directory: {}", DYNAMIC_RULES_DIR);
            }
        } catch (IOException e) {
            log.error("Error creating dynamic rules directory", e);
        }
    }
}
