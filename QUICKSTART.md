# Quick Start - Rules Engine POC

Guía rápida para empezar con la POC en Docker.

## Inicio Rápido (5 minutos)

### 1. Construir y ejecutar

```bash
# Opción A: Usando Make
make all

# Opción B: Usando Docker Compose directamente
docker-compose up --build -d
```

### 2. Verificar que está funcionando

```bash
# Opción A: Usando Make
make health

# Opción B: Usando curl
curl http://localhost:8080/actuator/health
```

### 3. Probar reglas estáticas

```bash
curl -X POST http://localhost:8080/api/rules/evaluate?complexity=ALL \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEST-001",
    "amount": 5000,
    "currency": "USD",
    "transactionType": "PURCHASE",
    "country": "USA",
    "status": "PENDING"
  }'
```

### 4. Subir y probar reglas dinámicas

```bash
# Subir regla
curl -X POST http://localhost:8080/api/dynamic-rules/upload \
  -F "file=@scripts/example-dynamic-rule.drl"

# Evaluar con reglas dinámicas
curl -X POST http://localhost:8080/api/dynamic-rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "id": "DYNAMIC-001",
    "amount": 60000,
    "currency": "USD",
    "country": "USA",
    "accountTier": "PLATINUM",
    "status": "PENDING"
  }'
```

### 5. Demo completa

```bash
./scripts/docker-demo.sh
```

## Comandos Útiles

```bash
# Ver logs
make logs
# o
docker-compose logs -f

# Detener
make down
# o
docker-compose down

# Reiniciar
make restart
```

## Endpoints Principales

### Reglas Estáticas
- `POST /api/rules/evaluate?complexity={LOW|MEDIUM|HIGH|ALL}`
- `POST /api/rules/load-test/{low|medium|high|mixed}?count=1000`

### Reglas Dinámicas
- `POST /api/dynamic-rules/upload` - Subir regla
- `GET /api/dynamic-rules/list` - Listar reglas
- `POST /api/dynamic-rules/evaluate` - Evaluar con reglas dinámicas
- `POST /api/dynamic-rules/reload` - Recargar reglas

### Decision Tables
- `POST /api/decision-tables/evaluate` - Evaluar con Decision Tables
- `GET /api/decision-tables/example/{small|medium|large|vip}`

## Próximos Pasos

1. Lee [DOCKER.md](DOCKER.md) para más detalles sobre Docker
2. Lee [README.md](README.md) para documentación completa
3. Explora los scripts en `scripts/` para ejemplos

