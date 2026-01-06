#!/bin/bash

# Script para listar reglas dinámicas cargadas
# Uso: ./list-rules.sh

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Listando reglas dinámicas..."
echo "URL: $BASE_URL/api/dynamic-rules/list"
echo ""

response=$(curl -s "$BASE_URL/api/dynamic-rules/list")

echo "$response" | jq '.' 2>/dev/null || echo "$response"

