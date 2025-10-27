#!/bin/bash

# Graph Mailer Test Script
# This script provides easy testing of the Graph Mailer API

# Configuration
BASE_URL="${GRAPH_MAILER_URL:-http://localhost:8080}"
API_KEY="${GRAPH_MAILER_API_KEY:-your-api-key}"
FROM_UPN="${GRAPH_TEST_FROM_UPN:-noreply@yourdomain.com}"
TO_EMAIL="${GRAPH_TEST_TO_EMAIL:-test@example.com}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check if required environment variables are set
check_config() {
    log_info "Checking configuration..."
    
    if [ "$API_KEY" = "your-api-key" ]; then
        log_error "Please set GRAPH_MAILER_API_KEY environment variable"
        exit 1
    fi
    
    if [ "$FROM_UPN" = "noreply@yourdomain.com" ]; then
        log_warn "Using default FROM_UPN. Set GRAPH_TEST_FROM_UPN for your domain"
    fi
    
    if [ "$TO_EMAIL" = "test@example.com" ]; then
        log_warn "Using default TO_EMAIL. Set GRAPH_TEST_TO_EMAIL for actual testing"
    fi
    
    log_info "Configuration check completed"
}

# Test health endpoint
test_health() {
    log_info "Testing health endpoint..."
    
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "Health check passed"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        log_error "Health check failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
}

# Test info endpoint
test_info() {
    log_info "Testing info endpoint..."
    
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/info")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "200" ]; then
        log_info "Info endpoint accessible"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        log_warn "Info endpoint not accessible (HTTP $http_code)"
        echo "$body"
    fi
}

# Test send email with minimal payload
test_send_simple() {
    log_info "Testing simple email send..."
    
    payload=$(cat <<EOF
{
  "fromUpn": "$FROM_UPN",
  "to": ["$TO_EMAIL"],
  "subject": "Test Email from Graph Mailer",
  "htmlBody": "<p>This is a test email sent from Graph Mailer microservice.</p><p>Timestamp: $(date)</p>"
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
        log_info "Simple email send successful"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        log_error "Simple email send failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
}

# Test send email with full payload
test_send_full() {
    log_info "Testing full email send with attachments..."
    
    # Create a small test file in base64
    test_content="This is a test attachment content created at $(date)"
    test_base64=$(echo "$test_content" | base64 -w 0)
    
    payload=$(cat <<EOF
{
  "fromUpn": "$FROM_UPN",
  "to": ["$TO_EMAIL"],
  "cc": [],
  "bcc": [],
  "subject": "Full Test Email from Graph Mailer",
  "htmlBody": "<h2>Full Test Email</h2><p>This email tests all features:</p><ul><li>HTML content</li><li>Text content</li><li>Attachments</li><li>High importance</li></ul><p>Sent at: $(date)</p>",
  "textBody": "Full Test Email\\n\\nThis email tests all features:\\n- HTML content\\n- Text content\\n- Attachments\\n- High importance\\n\\nSent at: $(date)",
  "importance": "high",
  "saveToSentItems": true,
  "attachments": [
    {
      "name": "test-file.txt",
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
        log_info "Full email send successful"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        log_error "Full email send failed (HTTP $http_code)"
        echo "$body"
        return 1
    fi
}

# Test authentication failure
test_auth_failure() {
    log_info "Testing authentication failure..."
    
    payload='{"fromUpn":"test@test.com","to":["test@test.com"],"subject":"Test","htmlBody":"Test"}'
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/api/v1/mail/send" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: invalid-key" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_info "Authentication failure test passed (HTTP $http_code)"
    else
        log_warn "Expected authentication failure, got HTTP $http_code"
        echo "$body"
    fi
}

# Test invalid payload
test_invalid_payload() {
    log_info "Testing invalid payload..."
    
    payload='{"invalid": "payload"}'
    
    response=$(curl -s -w "\n%{http_code}" \
        -X POST "$BASE_URL/api/v1/mail/send" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: $API_KEY" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "400" ]; then
        log_info "Invalid payload test passed (HTTP $http_code)"
    else
        log_warn "Expected validation error, got HTTP $http_code"
        echo "$body"
    fi
}

# Main test runner
run_tests() {
    log_info "Starting Graph Mailer API tests..."
    log_info "Base URL: $BASE_URL"
    log_info "From UPN: $FROM_UPN"
    log_info "To Email: $TO_EMAIL"
    echo
    
    # Check configuration
    check_config
    echo
    
    # Run tests
    test_health
    echo
    
    test_info
    echo
    
    test_auth_failure
    echo
    
    test_invalid_payload
    echo
    
    test_send_simple
    echo
    
    if [ "$1" = "--full" ]; then
        test_send_full
        echo
    fi
    
    log_info "Test run completed!"
}

# Help function
show_help() {
    cat <<EOF
Graph Mailer Test Script

Usage: $0 [OPTIONS]

Options:
  --full          Run full test suite including attachment tests
  --health        Test only health endpoints
  --send          Test only email sending
  --help          Show this help message

Environment Variables:
  GRAPH_MAILER_URL           Base URL (default: http://localhost:8080)
  GRAPH_MAILER_API_KEY       API key for authentication (required)
  GRAPH_TEST_FROM_UPN        Sender email address (default: noreply@yourdomain.com)
  GRAPH_TEST_TO_EMAIL        Recipient email address (default: test@example.com)

Examples:
  # Basic test
  GRAPH_MAILER_API_KEY=your-key ./test-api.sh
  
  # Full test with attachments
  GRAPH_MAILER_API_KEY=your-key ./test-api.sh --full
  
  # Test against remote instance
  GRAPH_MAILER_URL=https://graph-mailer.example.com GRAPH_MAILER_API_KEY=your-key ./test-api.sh
EOF
}

# Main script logic
case "$1" in
    --health)
        check_config
        test_health
        test_info
        ;;
    --send)
        check_config
        test_send_simple
        ;;
    --full)
        run_tests --full
        ;;
    --help)
        show_help
        ;;
    *)
        run_tests
        ;;
esac