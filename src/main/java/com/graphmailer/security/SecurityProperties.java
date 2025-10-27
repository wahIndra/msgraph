package com.graphmailer.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for security settings.
 * 
 * These properties control authentication modes, API key settings,
 * and CORS configuration for the application.
 */
@ConfigurationProperties(prefix = "security")
@Validated
public record SecurityProperties(
        InboundConfig inbound,
        CorsConfig cors
) {
    /**
     * Inbound security configuration.
     */
    public record InboundConfig(
            @NotBlank String mode,  // "api-key" or "oauth2"
            @NotBlank String apiKeyHeader,
            String apiKeyValue
    ) {}

    /**
     * CORS configuration.
     */
    public record CorsConfig(
            List<String> allowedOrigins
    ) {}
}