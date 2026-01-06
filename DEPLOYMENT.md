# Guía de Despliegue - Rules Engine POC

Esta guía explica cómo desplegar y usar la POC en un entorno contenerizado.

## Arquitectura

La POC soporta tres tipos de reglas:

1. **Reglas Estáticas (DRL)**: Compiladas en la imagen Docker
2. **Reglas Dinámicas (DRL/CSV)**: Subidas y compiladas en tiempo de ejecución
3. **Decision Tables (CSV)**: Reglas tabulares desde CSV

## Despliegue con Docker

### Paso 1: Construir la imagen

```bash
docker-compose build
# o
make build
```

### Paso 2: Iniciar el contenedor

```bash
docker-compose up -d
# o
make up
```

### Paso 3: Verificar

```bash
curl http://localhost:8080/actuator/health
# o
make health
```

## Uso de Reglas Dinámicas

### Subir Reglas

#### Opción 1: Vía API (Recomendado)

```bash
# Subir archivo DRL
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@path/to/rule.drl"

# Subir archivo CSV
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@path/to/rules.csv"
```

#### Opción 2: Colocar en directorio

Coloca archivos `.drl` o `.csv` en `./dynamic-rules/` y recarga:

```bash
# Recargar reglas desde el directorio
curl -X POST http://localhost:8080/api/dynamic-rules/reload
```

### Gestionar Reglas

```bash
# Listar reglas cargadas
curl http://localhost:8080/api/dynamic-rules/list

# Eliminar regla
curl -X DELETE http://localhost:8080/api/dynamic-rules/{fileName}

# Recargar todas las reglas
curl -X POST http://localhost:8080/api/dynamic-rules/reload
```

### Evaluar con Reglas Dinámicas

```bash
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TX-001",
    "amount": 5000,
    "currency": "USD",
    "country": "USA",
    "status": "PENDING"
  }'
```

## Flujo de Trabajo Recomendado

### Desarrollo Local

1. **Reglas Estáticas**: Editar `src/main/resources/rules/*.drl`
2. **Rebuild**: `docker-compose build`
3. **Reiniciar**: `docker-compose restart`

### Producción (Reglas Dinámicas)

1. **Crear regla**: Escribir archivo `.drl` o `.csv`
2. **Subir regla**: `POST /api/dynamic-rules/upload`
3. **Verificar**: `GET /api/dynamic-rules/list`
4. **Probar**: `POST /api/dynamic-rules/evaluate`
5. **Usar**: Las reglas están activas inmediatamente

## Ejemplos Completos

### Ejemplo 1: Regla Dinámica Simple

```bash
# 1. Crear archivo de regla
cat > my-rule.drl << 'EOF'
package com.rulesengine.rules.dynamic

import com.rulesengine.model.Transaction

rule "My Dynamic Rule"
    when
        $t : Transaction(amount > 10000, status == "PENDING")
    then
        $t.setStatus("REJECTED");
        $t.setRejectionReason("Amount too high");
end
EOF

# 2. Subir regla
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@my-rule.drl"

# 3. Probar
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{"id": "TEST", "amount": 15000, "status": "PENDING"}'
```

### Ejemplo 2: Usar Scripts

```bash
# Subir regla
./scripts/upload-rule.sh scripts/example-dynamic-rule.drl

# Listar
./scripts/list-rules.sh

# Evaluar
./scripts/evaluate-dynamic.sh example-transaction.json
```

## Monitoreo

### Health Checks

```bash
# Aplicación
curl http://localhost:8080/actuator/health

# Reglas dinámicas
curl http://localhost:8080/api/dynamic-rules/health
```

### Métricas

```bash
# Prometheus
curl http://localhost:8080/actuator/prometheus

# Métricas generales
curl http://localhost:8080/actuator/metrics
```

## Troubleshooting

### Las reglas dinámicas no se cargan

1. Verificar logs: `docker-compose logs rules-engine | grep -i dynamic`
2. Verificar directorio: `ls -la dynamic-rules/`
3. Verificar formato: El archivo debe ser DRL o CSV válido
4. Verificar compilación: Revisar errores en la respuesta del upload

### Error al subir archivo

1. Verificar tamaño (máx 10MB)
2. Verificar formato del archivo
3. Revisar logs del contenedor
4. Verificar permisos del directorio

### El contenedor no inicia

1. Ver logs: `docker-compose logs rules-engine`
2. Verificar puerto 8080 disponible
3. Verificar recursos del sistema (RAM, CPU)

## Mejores Prácticas

1. **Versionado**: Mantén versiones de tus reglas dinámicas
2. **Validación**: Valida reglas antes de subirlas a producción
3. **Backup**: Haz backup de reglas dinámicas importantes
4. **Testing**: Prueba reglas en ambiente de desarrollo primero
5. **Monitoreo**: Monitorea el uso y rendimiento de reglas dinámicas

