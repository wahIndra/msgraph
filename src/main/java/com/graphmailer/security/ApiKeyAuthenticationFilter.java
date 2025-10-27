package com.graphmailer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key authentication filter.
 * 
 * This filter checks for a valid API key in the request headers
 * and sets up the security context if the key is valid.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private final SecurityProperties securityProperties;

    public ApiKeyAuthenticationFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // Skip API key authentication if not in api-key mode
        if (!"api-key".equals(securityProperties.inbound().mode())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for health checks and info endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(securityProperties.inbound().apiKeyHeader());
        String expectedApiKey = securityProperties.inbound().apiKeyValue();

        if (apiKey != null && expectedApiKey != null && apiKey.equals(expectedApiKey)) {
            // Valid API key - set up authentication
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken("api-user", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("API key authentication successful for request: {}", requestPath);
        } else {
            logger.warn("Invalid or missing API key for request: {} from IP: {}", 
                       requestPath, getClientIpAddress(request));
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "type": "https://example.com/problems/authentication-required",
                    "title": "Authentication Required",
                    "status": 401,
                    "detail": "Valid API key required in %s header"
                }
                """.formatted(securityProperties.inbound().apiKeyHeader()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the endpoint is public and doesn't require authentication.
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/api/v1/info");
    }

    /**
     * Extracts client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}