#!/bin/bash

# Script para recargar todas las reglas dinámicas
# Uso: ./reload-rules.sh

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Recargando reglas dinámicas..."
echo "URL: $BASE_URL/api/dynamic-rules/reload"
echo ""

response=$(curl -s -X POST "$BASE_URL/api/dynamic-rules/reload")

echo "$response" | jq '.' 2>/dev/null || echo "$response"

