#!/bin/bash

# Graph Mailer Mock Mode Test Script
# This script tests the Graph Mailer API running in mock mode

# Configuration
BASE_URL="${GRAPH_MAILER_URL:-http://localhost:8080}"
API_KEY="${GRAPH_MAILER_API_KEY:-demo-api-key-12345}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_title() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

# Test health endpoint
test_health() {
    log_title "Testing health endpoint..."
    
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "Health check passed"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        # Check if it's in mock mode
        if echo "$body" | grep -q "mock\|MOCK\|Mock"; then
            log_info "✓ Confirmed running in MOCK mode"
        fi
    else
        log_error "Health check failed (HTTP $http_code)"
        return 1
    fi
    echo ""
}

# Test info endpoint to confirm mock mode
test_info() {
    log_title "Testing info endpoint (checking mode)..."
    
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/info")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "Info endpoint accessible"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        # Check mode
        if echo "$body" | grep -q '"mode".*"MOCK"'; then
            log_info "✓ Confirmed application is in MOCK/DEMO mode"
        fi
    else
        log_warn "Info endpoint not accessible (HTTP $http_code)"
    fi
    echo ""
}

# Test simple mock email
test_mock_simple() {
    log_title "Testing simple mock email..."
    
    payload=$(cat <<EOF
{
  "fromUpn": "demo@mock.com",
  "to": ["recipient@example.com"],
  "subject": "Mock Test Email",
  "htmlBody": "<h2>Mock Email Test</h2><p>This is a mock email sent at $(date)</p><p>No real email will be delivered!</p>"
}
EOF
)
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/api/v1/mail/send" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: $API_KEY" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "✓ Mock email send successful"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        # Check for mock message ID
        if echo "$body" | grep -q '"messageId".*"mock-'; then
            log_info "✓ Received mock message ID (no real email sent)"
        fi
    else
        log_error "Mock email send failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
    echo ""
}

# Test mock email with all features
test_mock_full() {
    log_title "Testing full mock email with attachments..."
    
    # Create a test attachment
    test_content="Mock attachment content - $(date)"
    test_base64=$(echo "$test_content" | base64 -w 0)
    
    payload=$(cat <<EOF
{
  "fromUpn": "test@example.com",
  "to": ["user1@example.com", "user2@example.com"],
  "cc": ["manager@example.com"],
  "bcc": ["admin@example.com"],
  "subject": "Full Mock Email Test",
  "htmlBody": "<h1>Full Mock Email Test</h1><p>This tests all features in mock mode:</p><ul><li>Multiple recipients</li><li>CC and BCC</li><li>HTML content</li><li>Attachments</li><li>High importance</li></ul><p><strong>Timestamp:</strong> $(date)</p>",
  "textBody": "Full Mock Email Test\\n\\nThis tests all features in mock mode:\\n- Multiple recipients\\n- CC and BCC\\n- HTML content\\n- Attachments\\n- High importance\\n\\nTimestamp: $(date)",
  "importance": "high",
  "saveToSentItems": true,
  "attachments": [
    {
      "name": "mock-test.txt",
      "contentType": "text/plain",
      "contentBytes": "$test_base64"
    }
  ]
}
EOF
)
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/api/v1/mail/send" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: $API_KEY" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "✓ Full mock email send successful"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        # Check recipient count
        if echo "$body" | grep -q '"recipientCount".*4'; then
            log_info "✓ Correct recipient count (4 total: 2 TO + 1 CC + 1 BCC)"
        fi
    else
        log_error "Full mock email send failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
    echo ""
}

# Test validation in mock mode
test_validation() {
    log_title "Testing validation in mock mode..."
    
    # Test invalid email
    payload='{"fromUpn":"invalid-email","to":["test@test.com"],"subject":"Test","htmlBody":"Test"}'
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/api/v1/mail/send" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: $API_KEY" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "400" ]; then
        log_info "✓ Validation working correctly in mock mode"
    else
        log_warn "Expected validation error for invalid email, got HTTP $http_code"
    fi
    echo ""
}

# Check logs for mock messages
check_logs() {
    log_title "Checking for mock-specific log messages..."
    
    # This assumes the application is logging to console or accessible logs
    log_info "Check your application logs for messages like:"
    echo "  - 'Mock Graph Mail Service initialized - NO REAL EMAILS WILL BE SENT'"
    echo "  - '=== MOCK EMAIL SENT ==='"
    echo "  - Mock message details with Message ID starting with 'mock-'"
    echo ""
}

# Display mock information
show_mock_info() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  Graph Mailer Mock Mode Test   ${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
    echo -e "${YELLOW}This test suite verifies the Graph Mailer running in MOCK mode.${NC}"
    echo -e "${YELLOW}No real emails will be sent during these tests.${NC}"
    echo ""
    echo -e "${GREEN}Base URL:${NC} $BASE_URL"
    echo -e "${GREEN}API Key:${NC} $API_KEY"
    echo ""
}

# Main test runner
run_mock_tests() {
    show_mock_info
    
    test_health
    test_info
    test_mock_simple
    test_validation
    
    if [ "$1" = "--full" ]; then
        test_mock_full
    fi
    
    check_logs
    
    echo -e "${GREEN}==============================${NC}"
    echo -e "${GREEN}  Mock Mode Tests Completed   ${NC}"
    echo -e "${GREEN}==============================${NC}"
    echo ""
    echo -e "${YELLOW}Remember: This was mock mode - no real emails were sent!${NC}"
}

# Help function
show_help() {
    cat <<EOF
Graph Mailer Mock Mode Test Script

Usage: $0 [OPTIONS]

Options:
  --full          Run full test suite including attachment tests
  --help          Show this help message

Environment Variables:
  GRAPH_MAILER_URL           Base URL (default: http://localhost:8080)
  GRAPH_MAILER_API_KEY       API key (default: demo-api-key-12345)

Examples:
  # Basic mock tests
  ./test-mock.sh
  
  # Full mock tests
  ./test-mock.sh --full
  
  # Test against different URL
  GRAPH_MAILER_URL=http://localhost:8090 ./test-mock.sh
EOF
}

# Main script logic
case "$1" in
    --full)
        run_mock_tests --full
        ;;
    --help)
        show_help
        ;;
    *)
        run_mock_tests
        ;;
esac