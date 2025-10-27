package com.graphmailer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request model for sending email messages through Microsoft Graph API.
 * 
 * This model includes all necessary fields for sending emails with
 * HTML content, attachments, and multiple recipient types (TO, CC, BCC).
 */
@Schema(description = "Request model for sending email messages")
public record SendMailRequest(

        @Schema(description = "Sender email address (UPN)", 
                example = "noreply@yourtenant.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "From UPN is required")
        @Email(message = "From UPN must be a valid email address")
        @JsonProperty("fromUpn")
        String fromUpn,

        @Schema(description = "Primary recipients (TO)", 
                example = "[\"user1@example.com\", \"user2@yourtenant.com\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "At least one recipient is required")
        @Size(max = 100, message = "Maximum 100 TO recipients allowed")
        @JsonProperty("to")
        List<@Email(message = "All TO recipients must be valid email addresses") String> to,

        @Schema(description = "Carbon copy recipients (CC)", 
                example = "[\"cc@example.com\"]")
        @Size(max = 50, message = "Maximum 50 CC recipients allowed")
        @JsonProperty("cc")
        List<@Email(message = "All CC recipients must be valid email addresses") String> cc,

        @Schema(description = "Blind carbon copy recipients (BCC)", 
                example = "[\"bcc@example.com\"]")
        @Size(max = 50, message = "Maximum 50 BCC recipients allowed")
        @JsonProperty("bcc")
        List<@Email(message = "All BCC recipients must be valid email addresses") String> bcc,

        @Schema(description = "Email subject line", 
                example = "Important Notification",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must not exceed 255 characters")
        @JsonProperty("subject")
        String subject,

        @Schema(description = "HTML email body content", 
                example = "<p>Hello <strong>World</strong>!</p>")
        @Size(max = 1048576, message = "HTML body must not exceed 1MB")
        @JsonProperty("htmlBody")
        String htmlBody,

        @Schema(description = "Plain text email body (optional, will be auto-generated from HTML if not provided)")
        @Size(max = 1048576, message = "Text body must not exceed 1MB")
        @JsonProperty("textBody")
        String textBody,

        @Schema(description = "Email attachments")
        @Size(max = 10, message = "Maximum 10 attachments allowed")
        @JsonProperty("attachments")
        List<@Valid EmailAttachment> attachments,

        @Schema(description = "Whether to save the email to sender's Sent Items folder", 
                example = "true",
                defaultValue = "true")
        @JsonProperty("saveToSentItems")
        Boolean saveToSentItems,

        @Schema(description = "Email importance level", 
                example = "normal",
                allowableValues = {"low", "normal", "high"},
                defaultValue = "normal")
        @JsonProperty("importance")
        String importance
) {
    /**
     * Constructor with default values for optional fields.
     */
    public SendMailRequest {
        // Set default values for optional fields
        if (cc == null) {
            cc = List.of();
        }
        if (bcc == null) {
            bcc = List.of();
        }
        if (attachments == null) {
            attachments = List.of();
        }
        if (saveToSentItems == null) {
            saveToSentItems = true;
        }
        if (importance == null || importance.isBlank()) {
            importance = "normal";
        }
    }
}