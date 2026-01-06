# Rules Engine POC - Spring WebFlux + Drools

## Descripción

Esta es una Prueba de Concepto (POC) que demuestra la integración de **Spring WebFlux** (programación reactiva no bloqueante) con **Drools** (motor de reglas de negocio) para evaluar la factibilidad de uso de reglas simples y complejas, así como la escalabilidad del sistema.

## Objetivos de la POC

1. **Escalabilidad**: Demostrar que el sistema puede manejar alto volumen de transacciones
2. **No Blocking**: Utilizar programación reactiva para evitar bloqueos de threads
3. **Alto Volumen de Consultas**: Procesar miles de transacciones concurrentemente
4. **Reglas de Diferentes Complejidades**: 
   - **Baja**: Validaciones simples y directas
   - **Media**: Múltiples condiciones y cálculos
   - **Alta**: Lógica compleja con múltiples factores y cálculos avanzados

## Tecnologías

- **Spring Boot 3.2.0**
- **Spring WebFlux** (Programación Reactiva)
- **Drools 8.44.0** (Motor de Reglas)
- **Drools Decision Tables** (Reglas desde CSV/Excel)
- **Java 17**
- **Maven**
- **Docker & Docker Compose** (Contenerización)
- **Micrometer + Prometheus** (Métricas)
- **Apache POI** (Soporte para Excel)

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/rulesengine/
│   │   ├── RulesEngineApplication.java
│   │   ├── config/
│   │   │   └── DroolsConfig.java
│   │   ├── controller/
│   │   │   └── RulesEngineController.java
│   │   ├── model/
│   │   │   ├── Transaction.java
│   │   │   └── RuleResult.java
│   │   └── service/
│   │       └── RulesEngineService.java
│   └── resources/
│       ├── application.yml
│       ├── META-INF/
│       │   └── kmodule.xml
│       ├── META-INF/
│       │   ├── kmodule.xml
│       │   └── decisiontable-kmodule.xml
│       ├── rules/
│       │   ├── low-complexity-rules.drl
│       │   ├── medium-complexity-rules.drl
│       │   └── high-complexity-rules.drl
│       └── decisiontables/
│           ├── transaction-rules.csv
│           ├── country-risk-rules.csv
│           └── account-tier-rules.csv
└── test/
    └── java/com/rulesengine/
        └── RulesEngineApplicationTests.java
```

## Reglas Implementadas

### Complejidad Baja (low-complexity-rules.drl)
- Rechazar transacciones mayores a $10,000
- Aprobar transacciones pequeñas (≤ $100)
- Rechazar montos negativos
- Establecer score de riesgo para montos medianos

### Complejidad Media (medium-complexity-rules.drl)
- Evaluación de riesgo basada en monto y tipo de transacción
- Restricciones basadas en país
- Beneficios para usuarios VIP
- Límites basados en tier de cuenta
- Riesgo de conversión de moneda

### Complejidad Alta (high-complexity-rules.drl)
- Evaluación compleja de riesgo con múltiples factores
- Análisis avanzado de perfil de usuario
- Detección de patrones de transacción complejos
- Cálculo de riesgo multi-nivel con umbrales
- Combinación compleja de VIP y tier

## Decision Tables (CSV/Excel)

Drools soporta **Decision Tables** (Tablas de Decisión) que permiten definir reglas en formato tabular (CSV o Excel). Esto facilita la gestión de reglas por parte de usuarios de negocio sin necesidad de conocer DRL.

### Decision Tables Implementadas (Reglas Dinámicas)

Los archivos CSV son la **fuente de verdad** para reglas dinámicas. Se mantienen en el proyecto y se compilan a DRL para ejecución:

1. **transaction-rules.csv** → `transaction-rules.drl`
   - Reglas basadas en el monto de la transacción
   - Aprobación automática para montos pequeños
   - Revisión pendiente para montos medianos
   - Rechazo para montos muy grandes

2. **country-risk-rules.csv** → `country-risk-rules.drl`
   - Reglas de evaluación de riesgo por país
   - Diferentes niveles de riesgo según el país
   - Límites de monto ajustados por país
   - Restricciones especiales para países de alto riesgo

3. **account-tier-rules.csv** → `account-tier-rules.drl`
   - Reglas basadas en tier de cuenta y estado VIP
   - Límites diferentes según el tier (BRONZE, SILVER, GOLD, PLATINUM)
   - Beneficios adicionales para usuarios VIP
   - Combinación de tier y VIP para límites extendidos

**Nota**: Los archivos CSV se mantienen como referencia y fuente de verdad. Los DRL correspondientes son la versión compilada que se ejecuta. En producción, se podría implementar compilación automática de CSV a DRL en tiempo de ejecución.

### Endpoints de Decision Tables

#### Evaluar con Decision Tables
```bash
POST /api/decision-tables/evaluate
Content-Type: application/json

{
  "id": "DT-001",
  "amount": 5000,
  "country": "USA",
  "accountTier": "GOLD",
  "isVIP": true
}
```

#### Ejemplos predefinidos
```bash
# Transacción pequeña (aprobada)
GET /api/decision-tables/example/small

# Transacción mediana (revisión)
GET /api/decision-tables/example/medium

# Transacción grande (rechazada)
GET /api/decision-tables/example/large

# Transacción VIP (beneficios)
GET /api/decision-tables/example/vip
```

#### Test de carga con Decision Tables
```bash
POST /api/decision-tables/load-test?count=1000
```

#### Health check
```bash
GET /api/decision-tables/health
```

## Endpoints API

### Evaluación de Transacciones

#### Evaluar una transacción
```bash
POST /api/rules/evaluate?complexity=ALL
Content-Type: application/json

{
  "id": "TX-001",
  "userId": "USER-123",
  "amount": 5000,
  "currency": "USD",
  "transactionType": "PURCHASE",
  "country": "USA"
}
```

#### Evaluar lote de transacciones
```bash
POST /api/rules/evaluate/batch?complexity=ALL
Content-Type: application/json

[
  { "id": "TX-001", "amount": 1000, ... },
  { "id": "TX-002", "amount": 5000, ... }
]
```

#### Evaluación en streaming
```bash
POST /api/rules/evaluate/stream?complexity=ALL
Content-Type: application/x-ndjson
```

### Tests de Carga

#### Test de complejidad baja
```bash
POST /api/rules/load-test/low?count=1000
```

#### Test de complejidad media
```bash
POST /api/rules/load-test/medium?count=1000
```

#### Test de complejidad alta
```bash
POST /api/rules/load-test/high?count=1000
```

#### Test mixto
```bash
POST /api/rules/load-test/mixed?count=1000
```

### Health Check
```bash
GET /api/rules/health
```

## Decision Tables

### Endpoints de Decision Tables

Todos los endpoints de Decision Tables están bajo `/api/decision-tables/`:

- `POST /api/decision-tables/evaluate` - Evaluar transacción con Decision Tables
- `POST /api/decision-tables/evaluate/batch` - Evaluar lote de transacciones
- `POST /api/decision-tables/load-test?count=1000` - Test de carga
- `GET /api/decision-tables/example/small` - Ejemplo: transacción pequeña
- `GET /api/decision-tables/example/medium` - Ejemplo: transacción mediana
- `GET /api/decision-tables/example/large` - Ejemplo: transacción grande
- `GET /api/decision-tables/example/vip` - Ejemplo: transacción VIP
- `GET /api/decision-tables/health` - Health check

## Compilación y Ejecución

### Opción 1: Ejecución Local

#### Requisitos
- Java 17 o superior
- Maven 3.6+

#### Compilar
```bash
mvn clean compile
```

#### Ejecutar
```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

#### Ejecutar Tests
```bash
mvn test
```

### Opción 2: Ejecución con Docker (Recomendado)

#### Requisitos
- Docker 20.10+
- Docker Compose 2.0+

#### Construir y ejecutar
```bash
# Construir y levantar el contenedor
docker-compose up --build

# O en modo detached
docker-compose up -d --build
```

#### Verificar
```bash
curl http://localhost:8080/actuator/health
```

Para más información sobre Docker, ver [DOCKER.md](DOCKER.md)

## Métricas y Monitoreo

La aplicación expone métricas a través de Actuator:

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

## Arquitectura No Bloqueante

### Características Clave

1. **Spring WebFlux**: Maneja requests de forma reactiva usando Project Reactor
2. **Schedulers.boundedElastic()**: Ejecuta operaciones bloqueantes de Drools en un thread pool dedicado
3. **Flux/Mono**: Permite procesamiento asíncrono y no bloqueante
4. **Backpressure**: Control automático de la presión de datos

### Flujo de Procesamiento

```
HTTP Request (Reactive)
    ↓
Controller (Mono/Flux)
    ↓
Service (Mono.fromCallable)
    ↓
Schedulers.boundedElastic() (Thread Pool)
    ↓
Drools Engine (Blocking Operation)
    ↓
Response (Reactive)
```

## Tests de Escalabilidad

### Ejemplo de Test de Carga con curl

```bash
# Test de 1000 transacciones de baja complejidad
curl -X POST "http://localhost:8080/api/rules/load-test/low?count=1000" \
  -H "Content-Type: application/json"

# Test de 1000 transacciones de alta complejidad
curl -X POST "http://localhost:8080/api/rules/load-test/high?count=1000" \
  -H "Content-Type: application/json"
```

### Script de Pruebas de Carga

Se incluye un script de ejemplo (`load-test-example.sh`) que ejecuta varios tipos de pruebas:

```bash
./load-test-example.sh
```

Este script ejecuta:
- Evaluación de transacción individual
- Tests de carga para cada nivel de complejidad
- Test de carga mixto
- Health check

### Ejemplo de Test con Apache Bench

```bash
# 1000 requests, 100 concurrentes
ab -n 1000 -c 100 -p transaction.json -T application/json \
   http://localhost:8080/api/rules/evaluate?complexity=ALL
```

### Ejemplo de Test con JMeter

1. Crear un Thread Group con 1000 threads
2. Configurar HTTP Request Sampler apuntando a `/api/rules/load-test/mixed?count=1000`
3. Ejecutar y analizar resultados

## Consideraciones de Rendimiento

### Optimizaciones Implementadas

1. **KieSession por Request**: Cada transacción obtiene su propia sesión de Drools (thread-safe)
2. **Thread Pool Dedicado**: Operaciones bloqueantes se ejecutan en `boundedElastic()`
3. **Concurrencia Controlada**: Uso de `flatMap` con límite de concurrencia (100-200)
4. **Dispose de Sesiones**: Liberación adecuada de recursos después de cada evaluación

### Limitaciones

- Drools es inherentemente bloqueante, por lo que se ejecuta en un thread pool dedicado
- El rendimiento depende del número de reglas y su complejidad
- Para alto volumen, considerar clustering o distribución de carga

## Archivos de Ejemplo

- `example-transaction.json`: Ejemplo de transacción JSON para pruebas
- `load-test-example.sh`: Script bash para ejecutar pruebas de carga

## Reglas Dinámicas (Nuevo)

La POC ahora soporta **reglas dinámicas** que pueden subirse y compilarse sin reiniciar la aplicación.

### Características

- ✅ **Subir reglas vía API**: Endpoint para subir archivos DRL o CSV
- ✅ **Compilación automática**: Las reglas se compilan automáticamente al subirlas
- ✅ **Recarga sin reinicio**: No es necesario reiniciar la aplicación
- ✅ **Gestión de reglas**: Listar, eliminar y recargar reglas dinámicas
- ✅ **Directorio persistente**: Reglas en `./dynamic-rules/` se cargan al iniciar

### Endpoints de Reglas Dinámicas

- `POST /api/dynamic-rules/upload` - Subir y compilar archivo de reglas
- `GET /api/dynamic-rules/list` - Listar reglas cargadas
- `DELETE /api/dynamic-rules/{fileName}` - Eliminar regla
- `POST /api/dynamic-rules/reload` - Recargar todas las reglas
- `POST /api/dynamic-rules/evaluate` - Evaluar transacción con reglas dinámicas
- `GET /api/dynamic-rules/health` - Health check

### Ejemplo de Uso

```bash
# Subir regla dinámica
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@scripts/example-dynamic-rule.drl"

# Listar reglas
curl http://localhost:8080/api/dynamic-rules/list

# Evaluar con reglas dinámicas
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d @example-transaction.json
```

Ver [DOCKER.md](DOCKER.md) para más detalles sobre el uso en contenedores.

## Próximos Pasos

1. **Clustering**: Implementar Drools con Kie Server para distribución
2. **Caching**: Cachear resultados de evaluaciones similares
3. **Persistencia**: Guardar resultados en base de datos reactiva (R2DBC)
4. **Monitoring Avanzado**: Integración con Grafana para visualización de métricas
5. **Load Balancing**: Configurar múltiples instancias con balanceador de carga
6. **JMeter Tests**: Crear plan de pruebas JMeter para análisis detallado de rendimiento
7. **Watch Automático**: Implementar watch del directorio `dynamic-rules` para recarga automática

## Autor
Alejandro Carlos Pantaleón Urbay

POC desarrollada para demostrar la factibilidad de integración de Spring WebFlux con Drools.

## Licencia

Este proyecto es una POC de demostración.

