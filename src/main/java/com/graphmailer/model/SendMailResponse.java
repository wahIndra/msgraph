package com.graphmailer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response model for email sending operations.
 * 
 * This model provides feedback about the email sending operation,
 * including success status, message identifier, and timestamp.
 */
@Schema(description = "Response model for email sending operations")
public record SendMailResponse(

        @Schema(description = "Operation status", 
                example = "SUCCESS",
                allowableValues = {"SUCCESS", "FAILED", "PARTIAL_SUCCESS"})
        @JsonProperty("status")
        String status,

        @Schema(description = "Unique message identifier from Microsoft Graph", 
                example = "AAMkAGVmMDEzM...")
        @JsonProperty("messageId")
        String messageId,

        @Schema(description = "Operation timestamp in ISO-8601 format", 
                example = "2025-10-21T08:00:00Z")
        @JsonProperty("timestamp")
        Instant timestamp,

        @Schema(description = "Optional message providing additional details")
        @JsonProperty("message")
        String message,

        @Schema(description = "Correlation ID for tracking the request")
        @JsonProperty("correlationId")
        String correlationId
) {
    /**
     * Creates a successful response.
     * 
     * @param messageId The Graph message ID
     * @param correlationId The request correlation ID
     * @return Success response
     */
    public static SendMailResponse success(String messageId, String correlationId) {
        return new SendMailResponse(
                "SUCCESS",
                messageId,
                Instant.now(),
                "Email sent successfully",
                correlationId
        );
    }

    /**
     * Creates a failed response.
     * 
     * @param message Error message
     * @param correlationId The request correlation ID
     * @return Failed response
     */
    public static SendMailResponse failed(String message, String correlationId) {
        return new SendMailResponse(
                "FAILED",
                null,
                Instant.now(),
                message,
                correlationId
        );
    }
}