#!/bin/bash

# Test script for legacy APIs
echo "Testing Legacy APIs for graph-mailer"
echo "====================================="

BASE_URL="http://localhost:8080"
API_KEY="demo-api-key-12345"

echo ""
echo "1. Testing Legacy Send Email API..."
echo "-----------------------------------"

curl -X POST "${BASE_URL}/sendemail" \
  -H "X-API-Key: ${API_KEY}" \
  -F "from=noreply@example.com" \
  -F "to=test@example.com" \
  -F "subject=Legacy API Test" \
  -F "emailbody=<p>This is a test email from legacy API</p>" \
  -F "paswd=dummy123" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo ""
echo "2. Testing Legacy Read Email API..."
echo "-----------------------------------"

curl -X GET "${BASE_URL}/reademail?from=test@example.com&paswd=dummy&sender=test&filename=output&filetype=CSV&counted=10&separator=comma" \
  -H "X-API-Key: ${API_KEY}" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo ""
echo "3. Testing Modern API for comparison..."
echo "--------------------------------------"

curl -X POST "${BASE_URL}/api/v1/mail/send" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "fromUpn": "noreply@example.com",
    "to": ["test@example.com"],
    "subject": "Modern API Test",
    "htmlBody": "<p>This is a test email from modern API</p>",
    "saveToSentItems": true
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo ""
echo "4. Testing Health Endpoint..."
echo "-----------------------------"

curl -X GET "${BASE_URL}/actuator/health" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "Legacy API testing completed!"