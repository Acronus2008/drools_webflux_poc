#!/bin/bash

# Script para subir archivos de reglas (DRL o CSV) al Rules Engine
# Uso: ./upload-rule.sh <archivo.drl|archivo.csv>

if [ $# -eq 0 ]; then
    echo "Uso: $0 <archivo.drl|archivo.csv>"
    echo "Ejemplo: $0 my-rule.drl"
    exit 1
fi

FILE=$1
BASE_URL="${BASE_URL:-http://localhost:8080}"

if [ ! -f "$FILE" ]; then
    echo "Error: El archivo $FILE no existe"
    exit 1
fi

echo "Subiendo archivo: $FILE"
echo "URL: $BASE_URL/api/dynamic-rules/upload"
echo ""

response=$(curl -s -w "\n%{http_code}" -X POST \
  -F "file=@$FILE" \
  "$BASE_URL/api/dynamic-rules/upload")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

echo "HTTP Status: $http_code"
echo "Response:"
echo "$body" | jq '.' 2>/dev/null || echo "$body"
echo ""

if [ "$http_code" -eq 200 ]; then
    echo "✅ Archivo subido y compilado exitosamente"
    exit 0
else
    echo "❌ Error al subir el archivo"
    exit 1
fi

