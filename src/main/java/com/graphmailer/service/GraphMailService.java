package com.graphmailer.service;

import com.graphmailer.config.GraphProperties;
import com.graphmailer.config.MailProperties;
import com.graphmailer.logging.AuditLogger;
import com.graphmailer.model.EmailAttachment;
import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.util.ValidationUtil;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.Importance;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service class for handling email operations using Microsoft Graph API.
 * 
 * This service provides the core business logic for sending emails through
 * Microsoft Graph, including validation, attachment processing, and retry logic.
 */
@Service
@ConditionalOnProperty(name = "app.mode", havingValue = "production", matchIfMissing = true)
public class GraphMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(GraphMailService.class);

    private final GraphServiceClient graphClient;
    private final MailProperties mailProperties;
    private final ValidationUtil validationUtil;
    private final AuditLogger auditLogger;

    public GraphMailService(GraphServiceClient graphClient,
                           MailProperties mailProperties,
                           ValidationUtil validationUtil,
                           AuditLogger auditLogger) {
        this.graphClient = graphClient;
        this.mailProperties = mailProperties;
        this.validationUtil = validationUtil;
        this.auditLogger = auditLogger;
    }

    /**
     * Sends an email using Microsoft Graph API with retry logic.
     * 
     * @param request The email request containing all email details
     * @return SendMailResponse with operation result
     */
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 300, multiplier = 2.0)
    )
    public SendMailResponse sendMail(SendMailRequest request) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }

        logger.info("Processing email send request from: {} to: {} recipients", 
                   request.fromUpn(), request.to().size());

        try {
            // Validate request
            validationUtil.validateMailRequest(request, mailProperties);

            // Build Graph message
            Message message = buildGraphMessage(request);

            // Send the message
            String messageId = sendGraphMessage(message, request.fromUpn(), request.saveToSentItems());

            // Log successful send
            auditLogger.logEmailSent(request, messageId, correlationId);

            logger.info("Email sent successfully with messageId: {}", messageId);
            return SendMailResponse.success(messageId, correlationId);

        } catch (Exception e) {
            logger.error("Failed to send email from: {} - {}", request.fromUpn(), e.getMessage(), e);
            auditLogger.logEmailFailed(request, e.getMessage(), correlationId);
            return SendMailResponse.failed("Failed to send email: " + e.getMessage(), correlationId);
        }
    }

    /**
     * Builds a Microsoft Graph Message object from the request.
     */
    private Message buildGraphMessage(SendMailRequest request) {
        Message message = new Message();

        // Set basic properties
        message.setSubject(request.subject());

        // Set body content
        ItemBody body = new ItemBody();
        if (request.htmlBody() != null && !request.htmlBody().isBlank()) {
            body.setContentType(BodyType.Html);
            body.setContent(request.htmlBody());
        } else if (request.textBody() != null && !request.textBody().isBlank()) {
            body.setContentType(BodyType.Text);
            body.setContent(request.textBody());
        }
        message.setBody(body);

        // Set recipients
        message.setToRecipients(buildRecipients(request.to()));
        if (request.cc() != null && !request.cc().isEmpty()) {
            message.setCcRecipients(buildRecipients(request.cc()));
        }
        if (request.bcc() != null && !request.bcc().isEmpty()) {
            message.setBccRecipients(buildRecipients(request.bcc()));
        }

        // Set importance
        message.setImportance(mapImportance(request.importance()));

        // Add attachments if present
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            message.setAttachments(buildAttachments(request.attachments()));
        }

        return message;
    }

    /**
     * Builds a list of Graph recipients from email addresses.
     */
    private List<Recipient> buildRecipients(List<String> emailAddresses) {
        return emailAddresses.stream()
                .map(email -> {
                    Recipient recipient = new Recipient();
                    EmailAddress emailAddress = new EmailAddress();
                    emailAddress.setAddress(email);
                    recipient.setEmailAddress(emailAddress);
                    return recipient;
                })
                .toList();
    }

    /**
     * Builds Graph attachments from email attachment models.
     */
    private List<Attachment> buildAttachments(List<EmailAttachment> attachments) {
        return attachments.stream()
                .map(att -> {
                    FileAttachment fileAttachment = new FileAttachment();
                    fileAttachment.setName(att.filename());
                    fileAttachment.setContentType(att.contentType());
                    fileAttachment.setContentBytes(Base64.getDecoder().decode(att.base64()));
                    return (Attachment) fileAttachment;
                })
                .toList();
    }

    /**
     * Maps string importance to Graph Importance enum.
     */
    private Importance mapImportance(String importance) {
        return switch (importance.toLowerCase()) {
            case "low" -> Importance.Low;
            case "high" -> Importance.High;
            default -> Importance.Normal;
        };
    }

    /**
     * Sends the message using Graph API.
     */
    private String sendGraphMessage(Message message, String fromUpn, Boolean saveToSentItems) {
        try {
            // Create the send mail request body
            SendMailPostRequestBody requestBody = new SendMailPostRequestBody();
            requestBody.setMessage(message);
            requestBody.setSaveToSentItems(saveToSentItems != null && saveToSentItems);
            
            // Use the Graph client to send the message from the specified mailbox
            graphClient.users().byUserId(fromUpn)
                    .sendMail()
                    .post(requestBody);

            // Generate a message ID (Graph sendMail doesn't return one directly)
            String messageId = UUID.randomUUID().toString();
            
            logger.debug("Graph API sendMail completed for user: {}", fromUpn);
            return messageId;

        } catch (Exception e) {
            logger.error("Graph API call failed for user: {} - {}", fromUpn, e.getMessage());
            throw new GraphMailException("Graph API call failed: " + e.getMessage(), e);
        }
    }
}