#!/bin/bash

# Script de demostración completa del sistema contenerizado
# Muestra el uso de reglas estáticas y dinámicas

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=========================================="
echo "Rules Engine POC - Docker Demo"
echo "=========================================="
echo ""

# 1. Verificar que el contenedor está corriendo
echo "1. Verificando health check..."
health=$(curl -s "$BASE_URL/actuator/health")
if [ $? -eq 0 ]; then
    echo "✅ Aplicación está corriendo"
    echo "$health" | jq '.' 2>/dev/null || echo "$health"
else
    echo "❌ La aplicación no está corriendo. Ejecuta: docker-compose up"
    exit 1
fi
echo ""

# 2. Probar reglas estáticas
echo "2. Probando reglas ESTÁTICAS (DRL)..."
echo "POST $BASE_URL/api/rules/evaluate?complexity=ALL"
response=$(curl -s -X POST "$BASE_URL/api/rules/evaluate?complexity=ALL" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "STATIC-TEST-001",
    "amount": 5000,
    "currency": "USD",
    "transactionType": "PURCHASE",
    "country": "USA",
    "status": "PENDING"
  }')
echo "$response" | jq '.' 2>/dev/null || echo "$response"
echo ""

# 3. Listar reglas dinámicas actuales
echo "3. Listando reglas DINÁMICAS actuales..."
echo "GET $BASE_URL/api/dynamic-rules/list"
list=$(curl -s "$BASE_URL/api/dynamic-rules/list")
echo "$list" | jq '.' 2>/dev/null || echo "$list"
echo ""

# 4. Subir una regla dinámica
echo "4. Subiendo regla DINÁMICA..."
if [ -f "scripts/example-dynamic-rule.drl" ]; then
    echo "POST $BASE_URL/api/dynamic-rules/upload"
    upload=$(curl -s -X POST "$BASE_URL/api/dynamic-rules/upload" \
      -F "file=@scripts/example-dynamic-rule.drl")
    echo "$upload" | jq '.' 2>/dev/null || echo "$upload"
else
    echo "⚠️  Archivo scripts/example-dynamic-rule.drl no encontrado"
fi
echo ""

# 5. Listar reglas dinámicas después de subir
echo "5. Listando reglas DINÁMICAS después de subir..."
list=$(curl -s "$BASE_URL/api/dynamic-rules/list")
echo "$list" | jq '.' 2>/dev/null || echo "$list"
echo ""

# 6. Evaluar con reglas dinámicas
echo "6. Evaluando transacción con reglas DINÁMICAS..."
echo "POST $BASE_URL/api/dynamic-rules/evaluate"
response=$(curl -s -X POST "$BASE_URL/api/dynamic-rules/evaluate" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "DYNAMIC-TEST-001",
    "amount": 60000,
    "currency": "USD",
    "transactionType": "PURCHASE",
    "country": "USA",
    "accountTier": "PLATINUM",
    "status": "PENDING"
  }')
echo "$response" | jq '.' 2>/dev/null || echo "$response"
echo ""

# 7. Comparar resultados
echo "7. Resumen:"
echo "   - Reglas ESTÁTICAS: Compiladas en la imagen Docker"
echo "   - Reglas DINÁMICAS: Subidas y compiladas sin reiniciar"
echo ""

echo "=========================================="
echo "Demo completada!"
echo "=========================================="

