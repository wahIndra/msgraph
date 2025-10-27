package com.graphmailer.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Microsoft Graph API client settings.
 * 
 * These properties are bound from application configuration and include
 * all necessary settings for Graph API authentication and request behavior.
 */
@ConfigurationProperties(prefix = "graph")
@Validated
public record GraphProperties(
        @NotBlank String tenantId,
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String scopes,
        @NotNull RequestConfig request
) {
    /**
     * Nested configuration for Graph API request settings.
     */
    public record RequestConfig(
            @Positive long timeoutMs,
            @Positive int maxRetries,
            @Positive long retryBaseDelayMs
    ) {}
}