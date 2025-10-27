package com.graphmailer.controller;

import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.service.MailService;
import com.graphmailer.service.ReadMailService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * REST controller for mail sending operations.
 * 
 * This controller provides endpoints for sending emails through Microsoft Graph API
 * with rate limiting, validation, and proper error handling.
 */
@RestController
@RequestMapping("/api/v1/mail")
@Tag(name = "Mail", description = "Email sending operations using Microsoft Graph API")
public class MailController {

    private static final Logger logger = LoggerFactory.getLogger(MailController.class);

    private final MailService mailService;
    private final ReadMailService readMailService;
    private final ConcurrentMap<String, Bucket> rateLimitBuckets;
    private final Bandwidth defaultBandwidth;

    public MailController(MailService mailService,
                         ReadMailService readMailService,
                         ConcurrentMap<String, Bucket> rateLimitBuckets,
                         Bandwidth defaultBandwidth) {
        this.mailService = mailService;
        this.readMailService = readMailService;
        this.rateLimitBuckets = rateLimitBuckets;
        this.defaultBandwidth = defaultBandwidth;
    }

    /**
     * Sends an email using application-only (app-only) authentication.
     * The email is sent from the specified mailbox using Graph Mail.Send permission.
     */
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Send email using app-only authentication",
            description = """
                    Sends an email using Microsoft Graph API with application-only authentication.
                    The email is sent from the specified 'fromUpn' mailbox using Mail.Send application permission.
                    
                    Features:
                    - HTML and plain text content support
                    - File attachments (up to 5MB total)
                    - CC/BCC recipients
                    - Domain and sender validation
                    - Rate limiting (30 requests per minute per IP)
                    """,
            security = {
                    @SecurityRequirement(name = "apiKey"),
                    @SecurityRequirement(name = "oauth2")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email sent successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SendMailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation failure",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions or sender/domain not allowed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or Graph API failure",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<SendMailResponse> sendMail(
            @Parameter(description = "Email request with all necessary details", required = true)
            @Valid @RequestBody SendMailRequest request,
            HttpServletRequest httpRequest
    ) {
        // Set up correlation ID for request tracking
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        try {
            logger.info("Received email send request from: {} for {} recipients", 
                       request.fromUpn(), getTotalRecipientCount(request));

            // Apply rate limiting
            if (!checkRateLimit(httpRequest)) {
                logger.warn("Rate limit exceeded for IP: {}", getClientIpAddress(httpRequest));
                SendMailResponse response = SendMailResponse.failed("Rate limit exceeded", correlationId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
            }

            // Process the email send request
            SendMailResponse response = mailService.sendMail(request);

            // Return appropriate HTTP status based on response
            HttpStatus status = "SUCCESS".equals(response.status()) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error for email request: {}", e.getMessage());
            SendMailResponse response = SendMailResponse.failed("Validation error: " + e.getMessage(), correlationId);
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Unexpected error processing email request: {}", e.getMessage(), e);
            SendMailResponse response = SendMailResponse.failed("Internal server error", correlationId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } finally {
            MDC.clear();
        }
    }

    /**
     * Reads emails from a mailbox using Microsoft Graph API with modern REST practices.
     * Uses query parameters for filtering and follows OData conventions.
     */
    @GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Read emails from mailbox",
            description = """
                    Reads emails from the specified mailbox using Microsoft Graph API.
                    Supports filtering by sender, subject, and date range with OData-style query parameters.
                    
                    Features:
                    - OData-style filtering ($filter, $top, $orderby)
                    - JSON response format with structured data
                    - Rate limiting (30 requests per minute per IP)
                    - Efficient field selection to minimize payload
                    - Support for both production and mock modes
                    """,
            security = {
                    @SecurityRequirement(name = "apiKey"),
                    @SecurityRequirement(name = "oauth2")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Emails retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions or mailbox access denied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or Graph API failure",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<String> readEmails(
            @Parameter(description = "Mailbox UPN (user principal name) to read from", required = true, example = "user@contoso.com")
            @RequestParam String mailbox,
            
            @Parameter(description = "Filter emails by sender address", example = "sender@example.com")
            @RequestParam(required = false) String sender,
            
            @Parameter(description = "Filter emails by subject (contains)", example = "important")
            @RequestParam(required = false) String subject,
            
            @Parameter(description = "Maximum number of emails to return (default: 10, max: 100)", example = "25")
            @RequestParam(required = false, defaultValue = "10") Integer top,
            
            @Parameter(description = "Response format: json or csv", example = "json")
            @RequestParam(required = false, defaultValue = "json") String format,
            
            @Parameter(description = "CSV separator (only for CSV format)", example = ",")
            @RequestParam(required = false, defaultValue = ",") String separator,
            
            @Parameter(description = "Include CSV headers (only for CSV format)", example = "true")
            @RequestParam(required = false, defaultValue = "true") Boolean includeHeaders,
            
            HttpServletRequest httpRequest
    ) {
        // Set up correlation ID for request tracking
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);

        try {
            logger.info("Received email read request for mailbox: {} (top: {}, format: {})", 
                       mailbox, top, format);

            // Validate parameters
            if (mailbox == null || mailbox.trim().isEmpty()) {
                logger.warn("Mailbox parameter is required");
                return ResponseEntity.badRequest()
                    .body("{\"error\":\"Mailbox parameter is required\",\"correlationId\":\"" + correlationId + "\"}");
            }

            if (top != null && (top < 1 || top > 100)) {
                logger.warn("Top parameter must be between 1 and 100, got: {}", top);
                return ResponseEntity.badRequest()
                    .body("{\"error\":\"Top parameter must be between 1 and 100\",\"correlationId\":\"" + correlationId + "\"}");
            }

            // Apply rate limiting
            if (!checkRateLimit(httpRequest)) {
                logger.warn("Rate limit exceeded for IP: {}", getClientIpAddress(httpRequest));
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("{\"error\":\"Rate limit exceeded\",\"correlationId\":\"" + correlationId + "\"}");
            }

            // Convert modern parameters to legacy format for service compatibility
            String legacyFormat = "csv".equalsIgnoreCase(format) ? "CSV" : "JSON";
            String legacyHeader = includeHeaders != null && includeHeaders ? "true" : "false";
            
            // Call the read mail service with converted parameters
            String response = readMailService.readEmails(
                mailbox,                    // from
                "not-used-in-graph-api",   // paswd (not used but required by legacy interface)
                subject,                   // subject filter
                sender,                    // sender filter
                "emails." + format.toLowerCase(), // filename
                legacyFormat,              // filetype
                top,                       // counted
                separator,                 // separator
                legacyHeader              // header
            );

            logger.info("Email read completed successfully for mailbox: {} (returned {} format)", 
                       mailbox, format);

            // Set appropriate content type based on format
            String contentType = "csv".equalsIgnoreCase(format) ? 
                "text/csv; charset=utf-8" : MediaType.APPLICATION_JSON_VALUE;

            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error for email read request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Validation error: " + e.getMessage() + "\",\"correlationId\":\"" + correlationId + "\"}");

        } catch (Exception e) {
            logger.error("Unexpected error processing email read request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"correlationId\":\"" + correlationId + "\"}");

        } finally {
            MDC.clear();
        }
    }

    /**
     * Checks rate limit for the client IP address.
     */
    private boolean checkRateLimit(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIp, ip -> 
                Bucket.builder()
                        .addLimit(defaultBandwidth)
                        .build()
        );

        return bucket.tryConsume(1);
    }

    /**
     * Extracts client IP address from request, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Calculates total recipient count for logging purposes.
     */
    private int getTotalRecipientCount(SendMailRequest request) {
        int count = request.to().size();
        if (request.cc() != null) count += request.cc().size();
        if (request.bcc() != null) count += request.bcc().size();
        return count;
    }
}