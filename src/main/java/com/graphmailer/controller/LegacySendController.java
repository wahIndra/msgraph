package com.graphmailer.controller;

import com.graphmailer.model.EmailAttachment;
import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Legacy Send Email Controller - Backward Compatibility
 * 
 * This controller provides backward compatibility for existing applications
 * using the old EWS-based email API pattern. It translates legacy requests
 * to the new Graph Mail API internally.
 * 
 * @deprecated Use {@link MailController} for new implementations
 */
@Controller
@RestController
@RequestMapping("/") // Use root path - no /legacy prefix needed
@Tag(name = "Legacy Send Email", description = "Backward compatibility for legacy email sending (DEPRECATED)")
@Deprecated
public class LegacySendController {

    private static final Logger logger = LoggerFactory.getLogger(LegacySendController.class);
    
    private final MailService mailService;

    public LegacySendController(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Legacy send email endpoint for backward compatibility.
     * 
     * @deprecated Use POST /api/v1/mail/send with JSON payload instead
     */
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @Operation(
        summary = "Send Email (Legacy API)", 
        description = """
            Legacy email sending endpoint for backward compatibility.
            
            **DEPRECATED**: This endpoint is provided for backward compatibility only.
            New applications should use POST /api/v1/mail/send with JSON payload.
            
            Note: 'paswd' parameter is ignored in Graph API implementation as it uses
            application-level authentication instead of user credentials.
            """,
        deprecated = true
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email sent successfully or validation error"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestMapping(
        value = "/sendemail", 
        method = RequestMethod.POST, 
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<String> sending(
            @Parameter(description = "Email subject", required = true)
            @RequestParam(required = false) String subject,
            
            @Parameter(description = "From email address", required = true) 
            @RequestParam(required = false) String from,
            
            @Parameter(description = "Password (ignored in Graph API implementation)", required = true)
            @RequestParam(required = false) String paswd,
            
            @Parameter(description = "Email body content", required = true)
            @RequestParam(required = false) String emailbody,
            
            @Parameter(description = "Recipient email addresses", required = true)
            @RequestParam(required = false) String[] to,
            
            @Parameter(description = "CC email addresses")
            @RequestParam(required = false) String[] cc,
            
            @Parameter(description = "Email attachments")
            @RequestParam(required = false) MultipartFile[] attachBytes,
            
            @Parameter(description = "Attachment names")
            @RequestParam(required = false) String[] attachName
    ) throws URISyntaxException {
        
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            logger.info("Legacy send email request received from: {}", from);
            logger.warn("DEPRECATED API USAGE: /sendemail - Please migrate to /api/v1/mail/send");
            
            // Validate required parameters (same validation as original)
            String validationError = validateSendParameters(subject, from, paswd, emailbody, to);
            if (validationError != null) {
                logger.warn("Validation error: {}", validationError);
                return new ResponseEntity<>(validationError, HttpStatus.OK);
            }

            // Convert legacy request to new format
            SendMailRequest newRequest = convertToNewFormat(subject, from, emailbody, to, cc, attachBytes, attachName);
            
            // Call new mail service
            SendMailResponse response = mailService.sendMail(newRequest);
            
            if ("SUCCESS".equals(response.status())) {
                logger.info("Legacy email sent successfully with messageId: {}", response.messageId());
                return new ResponseEntity<>("OK", HttpStatus.OK);
            } else {
                logger.error("Failed to send legacy email: {}", response.message());
                return new ResponseEntity<>(response.message(), HttpStatus.OK);
            }
            
        } catch (Exception e) {
            logger.error("Error sending legacy email", e);
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.OK);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Validates legacy send email parameters.
     */
    private String validateSendParameters(String subject, String from, String paswd, String emailbody, String[] to) {
        if (from == null || from.trim().isEmpty()) {
            return "From tidak boleh kosong!";
        }
        if (paswd == null || paswd.trim().isEmpty()) {
            return "Password tidak boleh kosong!";
        }
        if (emailbody == null || emailbody.trim().isEmpty()) {
            return "Email body tidak boleh kosong!";
        }
        if (subject == null || subject.trim().isEmpty()) {
            return "Subject tidak boleh kosong!";
        }
        if (to == null || to.length < 1) {
            return "To tidak boleh kosong!";
        }
        return null;
    }

    /**
     * Converts legacy request format to new Graph Mail format.
     */
    private SendMailRequest convertToNewFormat(String subject, String from, String emailbody, 
                                             String[] to, String[] cc, 
                                             MultipartFile[] attachBytes, String[] attachName) throws Exception {
        
        // Convert recipients
        List<String> toList = Arrays.asList(to);
        List<String> ccList = (cc != null && cc.length > 0) ? Arrays.asList(cc) : null;
        
        // Convert attachments
        List<EmailAttachment> attachments = null;
        if (attachBytes != null && attachBytes.length > 0) {
            attachments = new ArrayList<>();
            for (int i = 0; i < attachBytes.length; i++) {
                MultipartFile file = attachBytes[i];
                String filename = (attachName != null && i < attachName.length) ? 
                    attachName[i] : file.getOriginalFilename();
                
                if (file != null && !file.isEmpty()) {
                    String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
                    String contentType = file.getContentType();
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    
                    attachments.add(new EmailAttachment(filename, contentType, base64Content));
                }
            }
        }
        
        // Determine if content is HTML or text
        boolean isHtml = emailbody.toLowerCase().contains("<html>") || 
                        emailbody.toLowerCase().contains("<p>") || 
                        emailbody.toLowerCase().contains("<br>");
        
        return new SendMailRequest(
            from,                    // fromUpn
            toList,                  // to
            ccList,                  // cc  
            null,                    // bcc
            subject,                 // subject
            isHtml ? emailbody : null,  // htmlBody
            isHtml ? null : emailbody,  // textBody
            attachments,             // attachments
            true,                    // saveToSentItems
            "normal"                 // importance
        );
    }
}