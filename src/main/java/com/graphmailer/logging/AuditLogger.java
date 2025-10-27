package com.graphmailer.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphmailer.model.SendMailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit logger for tracking email operations.
 * 
 * This component provides structured logging for email send operations,
 * ensuring no sensitive data (like message bodies) are logged while
 * maintaining audit trail for security and compliance.
 */
@Component
public class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private final ObjectMapper objectMapper;

    public AuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Logs successful email send operation.
     */
    public void logEmailSent(SendMailRequest request, String messageId, String correlationId) {
        Map<String, Object> auditEvent = createBaseAuditEvent("EMAIL_SENT", request, correlationId);
        auditEvent.put("messageId", messageId);
        auditEvent.put("status", "SUCCESS");

        logAuditEvent(auditEvent);
    }

    /**
     * Logs failed email send operation.
     */
    public void logEmailFailed(SendMailRequest request, String errorMessage, String correlationId) {
        Map<String, Object> auditEvent = createBaseAuditEvent("EMAIL_FAILED", request, correlationId);
        auditEvent.put("status", "FAILED");
        auditEvent.put("errorMessage", sanitizeErrorMessage(errorMessage));

        logAuditEvent(auditEvent);
    }

    /**
     * Creates base audit event with common fields.
     */
    private Map<String, Object> createBaseAuditEvent(String action, SendMailRequest request, String correlationId) {
        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("action", action);
        event.put("correlationId", correlationId);
        event.put("fromUpn", request.fromUpn());
        event.put("recipientCount", getTotalRecipientCount(request));
        event.put("subjectHash", hashSubject(request.subject()));
        event.put("hasAttachments", request.attachments() != null && !request.attachments().isEmpty());
        event.put("attachmentCount", request.attachments() != null ? request.attachments().size() : 0);
        
        // Add recipient domains (not full email addresses)
        event.put("toDomains", extractDomains(request.to()));
        if (request.cc() != null && !request.cc().isEmpty()) {
            event.put("ccDomains", extractDomains(request.cc()));
        }
        if (request.bcc() != null && !request.bcc().isEmpty()) {
            event.put("bccDomains", extractDomains(request.bcc()));
        }

        return event;
    }

    /**
     * Logs the audit event as structured JSON.
     */
    private void logAuditEvent(Map<String, Object> auditEvent) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(auditEvent);
            auditLog.info(jsonEvent);
        } catch (JsonProcessingException e) {
            auditLog.error("Failed to serialize audit event", e);
        }
    }

    /**
     * Gets total recipient count across TO, CC, and BCC.
     */
    private int getTotalRecipientCount(SendMailRequest request) {
        int count = request.to().size();
        if (request.cc() != null) count += request.cc().size();
        if (request.bcc() != null) count += request.bcc().size();
        return count;
    }

    /**
     * Creates a hash of the subject for audit purposes (no sensitive data).
     */
    private String hashSubject(String subject) {
        if (subject == null || subject.isBlank()) return "empty";
        return "hash_" + Integer.toHexString(subject.hashCode());
    }

    /**
     * Extracts unique domains from email list.
     */
    private List<String> extractDomains(List<String> emails) {
        return emails.stream()
                .map(email -> email.substring(email.lastIndexOf('@') + 1).toLowerCase())
                .distinct()
                .toList();
    }

    /**
     * Sanitizes error messages to remove any potential sensitive data.
     */
    private String sanitizeErrorMessage(String errorMessage) {
        if (errorMessage == null) return "Unknown error";
        
        // Remove email addresses from error messages
        String sanitized = errorMessage.replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[EMAIL]");
        
        // Truncate very long error messages
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200) + "...";
        }
        
        return sanitized;
    }
}