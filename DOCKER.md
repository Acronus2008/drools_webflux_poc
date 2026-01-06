# Docker - Rules Engine POC

Esta guía explica cómo ejecutar la POC en un entorno contenerizado con Docker.

## Requisitos

- Docker 20.10+
- Docker Compose 2.0+
- Al menos 2GB de RAM disponible

## Inicio Rápido

### 1. Construir y ejecutar con Docker Compose

```bash
# Construir y levantar el contenedor
docker-compose up --build

# O en modo detached (background)
docker-compose up -d --build
```

### 2. Verificar que está funcionando

```bash
# Health check
curl http://localhost:8080/actuator/health

# Ver logs
docker-compose logs -f rules-engine
```

### 3. Acceder a la aplicación

- **API Base**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

## Reglas Dinámicas

### Directorio de Reglas Dinámicas

El contenedor monta el directorio `./dynamic-rules` que permite:

1. **Cargar reglas al iniciar**: Coloca archivos `.drl` o `.csv` en `./dynamic-rules/` antes de iniciar
2. **Subir reglas vía API**: Usa el endpoint `/api/dynamic-rules/upload` para subir reglas sin reiniciar

### Subir Reglas Dinámicamente

#### Usando curl

```bash
# Subir un archivo DRL
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@scripts/example-dynamic-rule.drl"

# Subir un archivo CSV
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@src/main/resources/decisiontables/transaction-rules.csv"
```

#### Usando scripts

```bash
# Subir regla
./scripts/upload-rule.sh scripts/example-dynamic-rule.drl

# Listar reglas cargadas
./scripts/list-rules.sh

# Recargar todas las reglas
./scripts/reload-rules.sh
```

### Endpoints de Reglas Dinámicas

- `POST /api/dynamic-rules/upload` - Subir y compilar archivo de reglas
- `GET /api/dynamic-rules/list` - Listar reglas cargadas
- `DELETE /api/dynamic-rules/{fileName}` - Eliminar regla
- `POST /api/dynamic-rules/reload` - Recargar todas las reglas
- `POST /api/dynamic-rules/evaluate` - Evaluar transacción con reglas dinámicas
- `GET /api/dynamic-rules/health` - Health check de reglas dinámicas

## Reglas Estáticas vs Dinámicas

### Reglas Estáticas

- Ubicación: `src/main/resources/rules/*.drl`
- Se compilan al construir la imagen Docker
- Requieren rebuild para cambios
- Endpoints: `/api/rules/*`

### Reglas Dinámicas

- Ubicación: `./dynamic-rules/*.drl` o `./dynamic-rules/*.csv`
- Se cargan al iniciar o se suben vía API
- No requieren rebuild para cambios
- Endpoints: `/api/dynamic-rules/*`

## Ejemplos de Uso

### 1. Evaluar con reglas estáticas

```bash
curl -X POST http://localhost:8080/api/rules/evaluate?complexity=ALL \
  -H "Content-Type: application/json" \
  -d @example-transaction.json
```

### 2. Subir y usar reglas dinámicas

```bash
# 1. Subir regla
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@scripts/example-dynamic-rule.drl"

# 2. Verificar que se cargó
curl http://localhost:8080/api/dynamic-rules/list

# 3. Evaluar con reglas dinámicas
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d @example-transaction.json
```

### 3. Usar Decision Tables (CSV)

```bash
# Subir CSV como regla dinámica
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@src/main/resources/decisiontables/transaction-rules.csv"

# Evaluar
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "id": "DT-001",
    "amount": 5000,
    "status": "PENDING"
  }'
```

## Volúmenes Docker

El `docker-compose.yml` monta los siguientes volúmenes:

- `./dynamic-rules` → `/app/dynamic-rules` - Reglas dinámicas
- `./logs` → `/app/logs` - Logs de la aplicación

## Comandos Útiles

```bash
# Ver logs
docker-compose logs -f rules-engine

# Detener
docker-compose down

# Reconstruir
docker-compose build --no-cache

# Ejecutar comandos en el contenedor
docker-compose exec rules-engine sh

# Ver uso de recursos
docker stats rules-engine-poc
```

## Troubleshooting

### El contenedor no inicia

```bash
# Ver logs detallados
docker-compose logs rules-engine

# Verificar puerto
netstat -an | grep 8080
```

### Las reglas dinámicas no se cargan

1. Verificar que el directorio `dynamic-rules` existe
2. Verificar permisos del directorio
3. Revisar logs: `docker-compose logs rules-engine | grep -i "dynamic"`
4. Verificar formato del archivo (debe ser DRL válido)

### Error al subir archivos

1. Verificar tamaño del archivo (máx 10MB)
2. Verificar formato (debe ser .drl o .csv válido)
3. Revisar logs para errores de compilación

## Próximos Pasos

- [ ] Implementar watch automático del directorio `dynamic-rules`
- [ ] Agregar validación de reglas antes de compilar
- [ ] Implementar versionado de reglas dinámicas
- [ ] Agregar UI para gestión de reglas
- [ ] Implementar rollback de reglas

