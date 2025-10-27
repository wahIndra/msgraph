package com.graphmailer.controller;

import com.graphmailer.model.AppInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;

/**
 * Controller for application information and health endpoints.
 * 
 * Provides metadata about the application including version,
 * build information, and current status.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Info", description = "Application information and metadata")
public class InfoController {

    private final BuildProperties buildProperties;
    private final Environment environment;

    public InfoController(@Autowired(required = false) BuildProperties buildProperties, Environment environment) {
        this.buildProperties = buildProperties;
        this.environment = environment;
    }

    /**
     * Returns application information and metadata.
     */
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get application information",
            description = "Returns metadata about the application including version, build time, and environment details."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Application information retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppInfoResponse.class)
            )
    )
    public ResponseEntity<AppInfoResponse> getAppInfo() {
        String activeProfile = Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse("default");

        AppInfoResponse response = new AppInfoResponse(
                buildProperties != null ? buildProperties.getName() : "graph-mailer",
                buildProperties != null ? buildProperties.getVersion() : "1.0.0-SNAPSHOT",
                buildProperties != null ? buildProperties.getTime() : Instant.now(),
                "Microsoft Graph Mail Service - Replaces EWS email sending with Graph API",
                activeProfile,
                Instant.now()
        );

        return ResponseEntity.ok(response);
    }
}