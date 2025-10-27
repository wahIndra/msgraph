package com.graphmailer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Model representing an email attachment.
 * 
 * This model encapsulates file attachment data including the filename,
 * content type, and base64-encoded content.
 */
@Schema(description = "Email attachment model")
public record EmailAttachment(

        @Schema(description = "Attachment filename", 
                example = "document.pdf",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Filename is required")
        @Size(max = 255, message = "Filename must not exceed 255 characters")
        @Pattern(regexp = "^[^<>:\"/\\\\|?*]+$", 
                message = "Filename contains invalid characters")
        @JsonProperty("filename")
        String filename,

        @Schema(description = "MIME content type", 
                example = "application/pdf",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Content type is required")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9!#$&\\-\\^_]*\\/[a-zA-Z0-9!#$&\\-\\^_]+$",
                message = "Content type must be a valid MIME type")
        @JsonProperty("contentType")
        String contentType,

        @Schema(description = "Base64-encoded file content", 
                example = "JVBERi0xLjQKJe...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Base64 content is required")
        @Pattern(regexp = "^[A-Za-z0-9+/]*={0,2}$", 
                message = "Content must be valid base64 encoding")
        @JsonProperty("base64")
        String base64
) {}