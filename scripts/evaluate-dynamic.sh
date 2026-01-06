#!/bin/bash

# Script para evaluar una transacción usando reglas dinámicas
# Uso: ./evaluate-dynamic.sh <transaction.json>

if [ $# -eq 0 ]; then
    echo "Uso: $0 <transaction.json>"
    echo "Ejemplo: $0 example-transaction.json"
    exit 1
fi

FILE=$1
BASE_URL="${BASE_URL:-http://localhost:8080}"

if [ ! -f "$FILE" ]; then
    echo "Error: El archivo $FILE no existe"
    exit 1
fi

echo "Evaluando transacción con reglas dinámicas..."
echo "URL: $BASE_URL/api/dynamic-rules/evaluate"
echo ""

response=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "@$FILE" \
  "$BASE_URL/api/dynamic-rules/evaluate")

echo "$response" | jq '.' 2>/dev/null || echo "$response"

