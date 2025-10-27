package com.graphmailer.util;

import com.graphmailer.config.MailProperties;
import com.graphmailer.model.SendMailRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating mail requests and related data.
 * 
 * This utility performs business rule validation beyond basic
 * bean validation annotations, including domain restrictions
 * and content size limits.
 */
@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "text/plain", "text/html", "text/csv",
            "application/pdf", "application/zip",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg", "image/png", "image/gif");

    /**
     * Validates a mail request against business rules and configuration.
     * 
     * @param request        The mail request to validate
     * @param mailProperties Mail configuration properties
     * @throws IllegalArgumentException if validation fails
     */
    public void validateMailRequest(SendMailRequest request, MailProperties mailProperties) {
        validateSenderUpn(request.fromUpn(), mailProperties);
        validateRecipientDomains(request.to(), mailProperties);
        validateRecipientDomains(request.cc(), mailProperties);
        validateRecipientDomains(request.bcc(), mailProperties);
        validateAttachments(request.attachments(), mailProperties);
        validateContentSize(request);
    }

    /**
     * Validates that the sender UPN is allowed.
     */
    private void validateSenderUpn(String fromUpn, MailProperties mailProperties) {
        if (!mailProperties.allowedSenderUpns().contains(fromUpn)) {
            throw new IllegalArgumentException(
                    "Sender UPN '" + fromUpn + "' is not in the allowed senders list");
        }
    }

    /**
     * Validates that recipient domains are allowed.
     */
    private void validateRecipientDomains(List<String> recipients, MailProperties mailProperties) {
        if (recipients == null)
            return;

        for (String recipient : recipients) {
            String domain = extractDomain(recipient);
            if (!mailProperties.allowedRecipientDomains().contains(domain)) {
                throw new IllegalArgumentException(
                        "Recipient domain '" + domain + "' is not in the allowed domains list");
            }
        }
    }

    /**
     * Validates attachments against size and type restrictions.
     */
    private void validateAttachments(List<com.graphmailer.model.EmailAttachment> attachments,
            MailProperties mailProperties) {
        if (attachments == null)
            return;

        long totalSize = 0;
        for (var attachment : attachments) {
            // Validate MIME type
            if (!ALLOWED_MIME_TYPES.contains(attachment.contentType())) {
                throw new IllegalArgumentException(
                        "Attachment MIME type '" + attachment.contentType() + "' is not allowed");
            }

            // Calculate attachment size
            byte[] decodedContent = Base64.getDecoder().decode(attachment.base64());
            totalSize += decodedContent.length;

            if (totalSize > mailProperties.maxAttachmentBytes()) {
                throw new IllegalArgumentException(
                        "Total attachment size exceeds limit of " + mailProperties.maxAttachmentBytes() + " bytes");
            }
        }
    }

    /**
     * Validates content size limits.
     */
    private void validateContentSize(SendMailRequest request) {
        if (request.htmlBody() != null && request.htmlBody().length() > 1048576) {
            throw new IllegalArgumentException("HTML body exceeds 1MB limit");
        }
        if (request.textBody() != null && request.textBody().length() > 1048576) {
            throw new IllegalArgumentException("Text body exceeds 1MB limit");
        }
    }

    /**
     * Extracts domain from email address.
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email.substring(atIndex + 1).toLowerCase();
    }

    /**
     * Validates email format.
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}