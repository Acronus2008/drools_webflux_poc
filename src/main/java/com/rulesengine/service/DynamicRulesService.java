package com.rulesengine.service;

import java.util.List;

import org.kie.api.runtime.KieContainer;


/**
 * Servicio para gestionar reglas dinámicas
 * @author Alejandro Carlos Pantaleón Urbay
 * @version 1.0
 * @since 2026-01-06
 * @see org.kie.api.runtime.KieContainer
 * @see java.util.List
 */
public interface DynamicRulesService {
    /**
     * Inicializa las reglas dinámicas después de que la aplicación esté lista
     */
    void initialize();

    /**
     * Carga y compila reglas dinámicas desde el directorio dynamic-rules
     * @return true si la compilación fue exitosa
     */
    boolean loadDynamicRules();

    /**
     * Sube un archivo de reglas y lo compila
     * @param fileContent contenido del archivo
     * @param fileName nombre del archivo
     * @return true si fue exitoso
     */
    boolean uploadAndCompileRule(byte[] fileContent, String fileName);

    /**
     * Elimina una regla
     * @param fileName nombre del archivo
     * @return true si fue exitoso
     */
    boolean deleteRule(String fileName);
    
    /**
     * Lista las reglas dinámicas
     * @return lista de nombres de archivos
     */
    List<String> listDynamicRules();

    /**
     * Obtiene el contenedor de reglas dinámicas
     * @return contenedor de reglas dinámicas
     */
    KieContainer getDynamicKieContainer();

    /**
     * Verifica si hay reglas dinámicas
     * @return true si hay reglas dinámicas
     */
    boolean hasDynamicRules();

    /**
     * Obtiene el tiempo de la última compilación
     * @return tiempo de la última compilación
     */
    long getLastCompilationTime();
}

