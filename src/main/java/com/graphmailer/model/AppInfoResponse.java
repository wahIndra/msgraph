package com.graphmailer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response model for application information endpoint.
 * 
 * This model provides metadata about the application including
 * version, build information, and health status.
 */
@Schema(description = "Application information response")
public record AppInfoResponse(

        @Schema(description = "Application name", 
                example = "graph-mailer")
        @JsonProperty("name")
        String name,

        @Schema(description = "Application version", 
                example = "1.0.0")
        @JsonProperty("version")
        String version,

        @Schema(description = "Build timestamp", 
                example = "2025-10-21T08:00:00Z")
        @JsonProperty("buildTime")
        Instant buildTime,

        @Schema(description = "Application description")
        @JsonProperty("description")
        String description,

        @Schema(description = "Environment profile", 
                example = "production")
        @JsonProperty("profile")
        String profile,

        @Schema(description = "Current server timestamp", 
                example = "2025-10-21T08:00:00Z")
        @JsonProperty("timestamp")
        Instant timestamp
) {}