#!/bin/bash
# test-analytics-endpoints.sh
# Test script for Graph Mailer Analytics and Reports APIs

BASE_URL="http://localhost:8080"
API_KEY="demo-api-key-12345"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Graph Mailer Analytics API Tests${NC}"
echo -e "${BLUE}========================================${NC}"

# Date range for testing
START_DATE="2025-10-01"
END_DATE="2025-10-21"

# Test 1: Analytics - Delivery Rates
echo -e "\n${YELLOW}1. Testing Analytics - Delivery Rates${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/analytics/delivery-rates${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/delivery-rates?from=${START_DATE}&to=${END_DATE}&domain=company.com" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 2: Analytics - Engagement Stats
echo -e "\n${YELLOW}2. Testing Analytics - Engagement Stats${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/analytics/engagement${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/engagement?from=${START_DATE}&to=${END_DATE}" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 3: Analytics - Usage Statistics
echo -e "\n${YELLOW}3. Testing Analytics - Usage Statistics${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/analytics/usage${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/usage?from=${START_DATE}&to=${END_DATE}&groupBy=DAILY" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 4: Analytics - Top Senders
echo -e "\n${YELLOW}4. Testing Analytics - Top Senders${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/analytics/top-senders${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/top-senders?limit=10&days=30" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 5: Analytics - Error Trends
echo -e "\n${YELLOW}5. Testing Analytics - Error Trends${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/analytics/error-trends${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/error-trends?from=${START_DATE}&to=${END_DATE}" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 6: Reports - Email Volumes
echo -e "\n${YELLOW}6. Testing Reports - Email Volumes${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/reports/email-volumes${NC}"
curl -s -X GET "${BASE_URL}/api/v1/reports/email-volumes?from=${START_DATE}&to=${END_DATE}&groupBy=DOMAIN&limit=20" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 7: Reports - Executive Summary
echo -e "\n${YELLOW}7. Testing Reports - Executive Summary${NC}"
echo -e "${GREEN}GET ${BASE_URL}/api/v1/reports/summary${NC}"
curl -s -X GET "${BASE_URL}/api/v1/reports/summary?days=30" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 8: Test with different parameters
echo -e "\n${YELLOW}8. Testing with Different Parameters${NC}"

# Top senders with different limits
echo -e "\n${GREEN}Top 5 senders for last 7 days:${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/top-senders?limit=5&days=7" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.[] | {senderUpn, emailsSent, successRate}'

# Usage stats with weekly grouping
echo -e "\n${GREEN}Usage stats with weekly grouping:${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/usage?from=${START_DATE}&to=${END_DATE}&groupBy=WEEKLY" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.totalRequests, .successRate, .averageResponseTime'

# Volume report grouped by user
echo -e "\n${GREEN}Email volumes grouped by user:${NC}"
curl -s -X GET "${BASE_URL}/api/v1/reports/email-volumes?from=${START_DATE}&to=${END_DATE}&groupBy=USER&limit=10" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.totalVolume, .successRate, .volumeBreakdown[0:3]'

# Test 9: Error handling
echo -e "\n${YELLOW}9. Testing Error Handling${NC}"

# Invalid date range
echo -e "\n${GREEN}Testing invalid date range:${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/delivery-rates?from=2025-12-01&to=2025-10-01" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Invalid limit parameter
echo -e "\n${GREEN}Testing invalid limit parameter:${NC}"
curl -s -X GET "${BASE_URL}/api/v1/analytics/top-senders?limit=2000&days=7" \
  -H "X-API-Key: ${API_KEY}" \
  -H "Accept: application/json" | jq '.'

# Test 10: Performance test
echo -e "\n${YELLOW}10. Performance Test${NC}"
echo -e "${GREEN}Making 5 concurrent requests to test performance:${NC}"

for i in {1..5}; do
  (
    time_start=$(date +%s%N)
    curl -s -X GET "${BASE_URL}/api/v1/analytics/delivery-rates?from=${START_DATE}&to=${END_DATE}" \
      -H "X-API-Key: ${API_KEY}" \
      -H "Accept: application/json" > /dev/null
    time_end=$(date +%s%N)
    time_diff=$((($time_end - $time_start) / 1000000))
    echo "Request $i: ${time_diff}ms"
  ) &
done
wait

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}  All Analytics API Tests Completed${NC}"
echo -e "${BLUE}========================================${NC}"

# Display summary
echo -e "\n${YELLOW}API Endpoints Tested:${NC}"
echo "✓ GET /api/v1/analytics/delivery-rates"
echo "✓ GET /api/v1/analytics/engagement"
echo "✓ GET /api/v1/analytics/usage"
echo "✓ GET /api/v1/analytics/top-senders"
echo "✓ GET /api/v1/analytics/error-trends"
echo "✓ GET /api/v1/reports/email-volumes"
echo "✓ GET /api/v1/reports/summary"

echo -e "\n${YELLOW}Features Demonstrated:${NC}"
echo "• Email delivery statistics and success rates"
echo "• API usage metrics and performance data"
echo "• Top sender analysis by volume"
echo "• Error trend analysis with recommendations"
echo "• Email volume reports with various groupings"
echo "• Executive summary with KPIs"
echo "• Parameter validation and error handling"
echo "• Performance monitoring capabilities"

echo -e "\n${GREEN}To view interactive API documentation:${NC}"
echo "Open: ${BASE_URL}/swagger-ui.html"
echo "Navigate to: 'Email Analytics' and 'Email Reports' sections"