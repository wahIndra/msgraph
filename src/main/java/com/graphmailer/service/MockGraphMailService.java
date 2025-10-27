package com.graphmailer.service;

import com.graphmailer.config.GraphProperties;
import com.graphmailer.config.MailProperties;
import com.graphmailer.logging.AuditLogger;
import com.graphmailer.model.EmailAttachment;
import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock implementation of the Graph Mail Service for development and testing.
 * 
 * This service simulates email sending without actually connecting to Microsoft Graph API.
 * It provides realistic behavior including validation, logging, and response simulation.
 */
@Service
@ConditionalOnProperty(name = "app.mode", havingValue = "mock", matchIfMissing = false)
public class MockGraphMailService implements MailService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockGraphMailService.class);
    
    private final MailProperties mailProperties;
    private final AuditLogger auditLogger;
    private final ValidationUtil validationUtil;
    
    public MockGraphMailService(MailProperties mailProperties, 
                               GraphProperties graphProperties,
                               AuditLogger auditLogger,
                               ValidationUtil validationUtil) {
        this.mailProperties = mailProperties;
        this.auditLogger = auditLogger;
        this.validationUtil = validationUtil;
        
        logger.info("Mock Graph Mail Service initialized - NO REAL EMAILS WILL BE SENT");
    }
    
    @Override
    public SendMailResponse sendMail(SendMailRequest request) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            logger.info("Mock email send request received from: {}", request.fromUpn());
            
            // Perform all the same validations as the real service
            validateEmailRequest(request);
            
            // Simulate processing delay
            simulateProcessingDelay();
            
            // Create mock response
            SendMailResponse response = createMockResponse();
            
            // Log the mock email details
            logMockEmailDetails(request, response);
            
            // Audit logging (same as real service)
            auditLogger.logEmailSent(request, response.messageId(), correlationId);
            
            logger.info("Mock email send completed successfully. MessageId: {}", response.messageId());
            return response;
            
        } catch (Exception e) {
            auditLogger.logEmailFailed(request, "MOCK_ERROR: " + e.getMessage(), correlationId);
            throw e;
        } finally {
            MDC.clear();
        }
    }
    
    private void validateEmailRequest(SendMailRequest request) {
        // Same validation as real service
        if (request.fromUpn() == null || request.fromUpn().trim().isEmpty()) {
            throw new IllegalArgumentException("From UPN is required");
        }
        
        if (request.to() == null || request.to().isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required");
        }
        
        if (request.subject() == null || request.subject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        
        if ((request.htmlBody() == null || request.htmlBody().trim().isEmpty()) &&
            (request.textBody() == null || request.textBody().trim().isEmpty())) {
            throw new IllegalArgumentException("Either HTML body or text body is required");
        }
        
        // Validate email addresses
        if (!validationUtil.isValidEmail(request.fromUpn())) {
            throw new IllegalArgumentException("Invalid from email address: " + request.fromUpn());
        }
        
        for (String email : request.to()) {
            if (!validationUtil.isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid recipient email address: " + email);
            }
        }
        
        // Validate sender UPN if configured
        if (mailProperties.allowedSenderUpns() != null && 
            !mailProperties.allowedSenderUpns().isEmpty() &&
            !mailProperties.allowedSenderUpns().contains(request.fromUpn())) {
            throw new IllegalArgumentException("Sender UPN not allowed: " + request.fromUpn());
        }
        
        // Validate recipient domains if configured
        if (mailProperties.allowedRecipientDomains() != null && 
            !mailProperties.allowedRecipientDomains().isEmpty()) {
            for (String email : request.to()) {
                String domain = email.substring(email.indexOf('@') + 1);
                if (!mailProperties.allowedRecipientDomains().contains(domain)) {
                    throw new IllegalArgumentException("Recipient domain not allowed: " + domain);
                }
            }
        }
        
        // Validate attachments
        if (request.attachments() != null) {
            for (EmailAttachment attachment : request.attachments()) {
                // Validate attachment size using the utility
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(attachment.base64());
                    if (decodedBytes.length > mailProperties.maxAttachmentBytes()) {
                        throw new IllegalArgumentException(
                            String.format("Attachment '%s' exceeds maximum size of %d bytes", 
                                        attachment.filename(), mailProperties.maxAttachmentBytes()));
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid base64 content in attachment: " + attachment.filename());
                }
            }
        }
    }
    
    private void simulateProcessingDelay() {
        try {
            // Simulate realistic processing time (100-500ms)
            int delay = ThreadLocalRandom.current().nextInt(100, 501);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Mock processing delay interrupted");
        }
    }
    
    private SendMailResponse createMockResponse() {
        String messageId = "mock-" + UUID.randomUUID().toString();
        String correlationId = MDC.get("correlationId");
        
        return SendMailResponse.success(messageId, correlationId);
    }
    
    private void logMockEmailDetails(SendMailRequest request, SendMailResponse response) {
        logger.info("=== MOCK EMAIL SENT ===");
        logger.info("Message ID: {}", response.messageId());
        logger.info("From: {}", request.fromUpn());
        logger.info("To: {}", String.join(", ", request.to()));
        
        if (request.cc() != null && !request.cc().isEmpty()) {
            logger.info("CC: {}", String.join(", ", request.cc()));
        }
        
        if (request.bcc() != null && !request.bcc().isEmpty()) {
            logger.info("BCC: {}", String.join(", ", request.bcc()));
        }
        
        logger.info("Subject: {}", request.subject());
        logger.info("Content Type: {}", request.htmlBody() != null ? "HTML" : "Text");
        
        int contentLength = 0;
        if (request.htmlBody() != null) {
            contentLength = request.htmlBody().length();
        } else if (request.textBody() != null) {
            contentLength = request.textBody().length();
        }
        logger.info("Content Length: {} characters", contentLength);
        
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            logger.info("Attachments: {} files", request.attachments().size());
            for (EmailAttachment attachment : request.attachments()) {
                logger.info("  - {}: {} bytes ({})", 
                    attachment.filename(), 
                    attachment.base64() != null ? attachment.base64().length() : 0,
                    attachment.contentType());
            }
        }
        
        logger.info("Importance: {}", request.importance() != null ? request.importance() : "normal");
        logger.info("Save to Sent Items: {}", Boolean.TRUE.equals(request.saveToSentItems()));
        logger.info("========================");
    }
}