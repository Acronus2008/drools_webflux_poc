#!/bin/bash

# Script de ejemplo para pruebas de carga del Rules Engine POC
# Requiere: curl, jq (opcional para formatear JSON)

BASE_URL="http://localhost:8080/api/rules"

echo "=========================================="
echo "Rules Engine POC - Load Test Examples"
echo "=========================================="
echo ""

# Test 1: Evaluar una transacción simple
echo "1. Testing single transaction evaluation..."
curl -X POST "${BASE_URL}/evaluate?complexity=LOW" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TX-001",
    "userId": "USER-123",
    "amount": 500,
    "currency": "USD",
    "transactionType": "PURCHASE",
    "country": "USA",
    "timestamp": "2024-01-15T10:00:00"
  }' | jq '.' 2>/dev/null || echo ""
echo ""

# Test 2: Load test con complejidad baja
echo "2. Load test - LOW complexity (100 transactions)..."
time curl -X POST "${BASE_URL}/load-test/low?count=100" \
  -H "Content-Type: application/json" \
  -s -o /dev/null -w "Time: %{time_total}s\n"
echo ""

# Test 3: Load test con complejidad media
echo "3. Load test - MEDIUM complexity (100 transactions)..."
time curl -X POST "${BASE_URL}/load-test/medium?count=100" \
  -H "Content-Type: application/json" \
  -s -o /dev/null -w "Time: %{time_total}s\n"
echo ""

# Test 4: Load test con complejidad alta
echo "4. Load test - HIGH complexity (100 transactions)..."
time curl -X POST "${BASE_URL}/load-test/high?count=100" \
  -H "Content-Type: application/json" \
  -s -o /dev/null -w "Time: %{time_total}s\n"
echo ""

# Test 5: Load test mixto
echo "5. Load test - MIXED complexity (100 transactions)..."
time curl -X POST "${BASE_URL}/load-test/mixed?count=100" \
  -H "Content-Type: application/json" \
  -s -o /dev/null -w "Time: %{time_total}s\n"
echo ""

# Test 6: Health check
echo "6. Health check..."
curl -X GET "${BASE_URL}/health" 2>/dev/null || echo ""
echo ""

echo "=========================================="
echo "Load tests completed!"
echo "=========================================="

