package com.graphmailer.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for mail service settings.
 * 
 * These properties control mail sending behavior, security restrictions,
 * and validation rules for the Graph Mail service.
 */
@ConfigurationProperties(prefix = "mail")
@Validated
public record MailProperties(
        @NotNull String defaultFromUpn,
        @NotNull Boolean saveToSentItems,
        @Positive long maxAttachmentBytes,
        @NotEmpty List<String> allowedRecipientDomains,
        @NotEmpty List<String> allowedSenderUpns
) {}