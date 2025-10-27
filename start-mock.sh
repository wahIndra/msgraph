#!/bin/bash

# Graph Mailer Mock Mode Startup Script
# This script starts the application in mock mode for local development/testing

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  Graph Mailer - Mock Mode Startup    ${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

echo -e "${YELLOW}Setting up mock environment...${NC}"

# Set environment variables for mock mode
export SPRING_PROFILES_ACTIVE=mock
export APP_MODE=mock

# Mock Graph API credentials (required for configuration binding)
export GRAPH_TENANT_ID=mock-tenant-id
export GRAPH_CLIENT_ID=mock-client-id
export GRAPH_CLIENT_SECRET=mock-client-secret

# Optional: Override default configurations
export GRAPH_MAILER_API_KEY="${GRAPH_MAILER_API_KEY:-demo-api-key-12345}"
export SERVER_PORT="${SERVER_PORT:-8080}"

echo -e "${GREEN}✓ Mock mode enabled${NC}"
echo -e "${GREEN}✓ Graph Tenant ID: ${GRAPH_TENANT_ID}${NC}"
echo -e "${GREEN}✓ API Key: ${GRAPH_MAILER_API_KEY}${NC}"
echo -e "${GREEN}✓ Server Port: ${SERVER_PORT}${NC}"
echo ""

echo -e "${YELLOW}Starting Graph Mailer in mock mode...${NC}"
echo -e "${YELLOW}Note: No real emails will be sent!${NC}"
echo ""

# Start the application
if [ -f "mvnw" ]; then
    ./mvnw spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=mock
else
    mvn spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=mock
fi