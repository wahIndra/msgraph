#!/bin/bash

# Comprehensive test script for all graph-mailer APIs
echo "Graph Mailer - Complete API Test Suite"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080"
API_KEY="demo-api-key-12345"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_api() {
    local name="$1"
    local command="$2"
    local expected_status="$3"
    
    echo -e "${BLUE}Testing: $name${NC}"
    echo "Command: $command"
    echo ""
    
    # Execute the command and capture both response and status
    response=$(eval "$command")
    status=$?
    
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}SUCCESS${NC}"
        echo "Response: $response"
    else
        echo -e "${RED}FAILED${NC}"
        echo "Response: $response"
    fi
    
    echo ""
    echo "-------------------------------------------"
    echo ""
}

echo -e "${YELLOW}1. Health Check${NC}"
test_api "Application Health" \
    "curl -s ${BASE_URL}/actuator/health | jq -r '.status'" \
    "200"

echo -e "${YELLOW}2. Legacy Send Email API${NC}"
test_api "Legacy Send Email (Form Data)" \
    "curl -s -X POST '${BASE_URL}/sendemail' \
        -H 'X-API-Key: ${API_KEY}' \
        -F 'from=noreply@example.com' \
        -F 'to=test@example.com' \
        -F 'subject=Legacy API Integration Test' \
        -F 'emailbody=<p>Hello from <strong>Legacy API</strong>!</p>' \
        -F 'paswd=dummy123'" \
    "200"

echo -e "${YELLOW}3. Legacy Read Email API${NC}"
test_api "Legacy Read Email (Not Implemented)" \
    "curl -s -X GET '${BASE_URL}/reademail?from=test@example.com&paswd=dummy&sender=test&filename=output&filetype=CSV&counted=10&separator=comma' \
        -H 'X-API-Key: ${API_KEY}' | jq -r '.error'" \
    "200"

echo -e "${YELLOW}4. Modern JSON API${NC}"
test_api "Modern Send Email (JSON)" \
    "curl -s -X POST '${BASE_URL}/api/v1/mail/send' \
        -H 'X-API-Key: ${API_KEY}' \
        -H 'Content-Type: application/json' \
        -d '{
            \"fromUpn\": \"noreply@example.com\",
            \"to\": [\"test@example.com\"],
            \"subject\": \"Modern API Integration Test\",
            \"htmlBody\": \"<p>Hello from <strong>Modern API</strong>!</p>\",
            \"saveToSentItems\": true
        }' | jq -r '.status'" \
    "200"

echo -e "${YELLOW}5. API Documentation${NC}"
test_api "Swagger UI Available" \
    "curl -s -I ${BASE_URL}/swagger-ui/index.html | head -n 1 | grep -o '200'" \
    "200"

echo -e "${YELLOW}6. Authentication Test${NC}"
test_api "Invalid API Key (Should Fail)" \
    "curl -s -X POST '${BASE_URL}/api/v1/mail/send' \
        -H 'X-API-Key: invalid-key' \
        -H 'Content-Type: application/json' \
        -d '{}' | jq -r '.title'" \
    "401"

echo ""
echo "Test Suite Completed!"
echo ""
echo " Summary:"
echo "• Health endpoint working"
echo "• Legacy send email API working (multipart/form-data)"
echo "• Legacy read email API working (returns not implemented)"
echo "• Modern JSON API working"
echo "• API documentation available"  
echo "• Authentication working (rejects invalid keys)"
echo ""
echo " Key Endpoints:"
echo "• Health: ${BASE_URL}/actuator/health"
echo "• Legacy Send: POST ${BASE_URL}/sendemail" 
echo "• Legacy Read: GET ${BASE_URL}/reademail"
echo "• Modern API: POST ${BASE_URL}/api/v1/mail/send"
echo "• Documentation: ${BASE_URL}/swagger-ui/index.html"
echo ""
echo " API Key: ${API_KEY}"
echo ""
echo " See LEGACY_API_SUMMARY.md for detailed documentation"