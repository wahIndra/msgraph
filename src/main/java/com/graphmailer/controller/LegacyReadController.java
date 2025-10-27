package com.graphmailer.controller;

import com.graphmailer.service.ReadMailService;
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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

/**
 * Legacy Read Email Controller - Backward Compatibility
 * 
 * This controller provides backward compatibility for existing applications
 * using the old EWS-based email reading API pattern. It now uses Microsoft Graph API
 * to read emails while maintaining the same interface.
 * 
 * @deprecated Use Microsoft Graph SDK directly for new implementations
 */
@Controller
@RestController
@RequestMapping("/") // Use root path - no /legacy prefix needed  
@Tag(name = "Legacy Read Email", description = "Backward compatibility for legacy email reading using Graph API")
@Deprecated
public class LegacyReadController {

    private static final Logger logger = LoggerFactory.getLogger(LegacyReadController.class);
    
    private final ReadMailService readMailService;
    
    public LegacyReadController(ReadMailService readMailService) {
        this.readMailService = readMailService;
    }

    /**
     * Legacy read email endpoint for backward compatibility.
     * 
     * @deprecated Email reading is not implemented in this service
     */
    @CrossOrigin
    @Operation(
        summary = "Read Email (Legacy API)", 
        description = """
            Legacy email reading endpoint for backward compatibility.
            
            **DEPRECATED**: This endpoint is provided for backward compatibility only.
            New applications should use Microsoft Graph API directly.
            
            This endpoint now uses Microsoft Graph API to read emails while maintaining
            the same interface as the original EWS-based implementation.
            """,
        deprecated = true
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Validation errors")
    })
    @RequestMapping(
        value = "/reademail", 
        method = {RequestMethod.GET, RequestMethod.POST}, 
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<String> reading(
            @Parameter(description = "From email address", required = true)
            @RequestParam(required = false) String from,
            
            @Parameter(description = "Password (not used in Graph API)")
            @RequestParam(required = false) String paswd,
            
            @Parameter(description = "Subject filter")
            @RequestParam(required = false) String subject,
            
            @Parameter(description = "Sender filter", required = true)
            @RequestParam(required = false) String sender,
            
            @Parameter(description = "Output filename", required = true)
            @RequestParam(required = false) String filename,
            
            @Parameter(description = "File type", required = true)
            @RequestParam(required = false) String filetype,
            
            @Parameter(description = "Message count", required = true)
            @RequestParam(required = false) Integer counted,
            
            @Parameter(description = "CSV separator", required = true)
            @RequestParam(required = false) String separator,
            
            @Parameter(description = "Include headers")
            @RequestParam(required = false) String header
    ) throws URISyntaxException {
        
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        try {
            logger.warn("DEPRECATED API USAGE: /reademail - Email reading not implemented in this service");
            logger.info("Legacy read email request received from: {}", from);
            
            // Validate required parameters (same validation as original)
            String validationError = validateReadParameters(from, paswd, counted, sender, filetype, separator, filename);
            if (validationError != null) {
                logger.warn("Validation error: {}", validationError);
                return createErrorResponse(validationError, HttpStatus.BAD_REQUEST);
            }

            // Call the read mail service (using Graph API)
            String jsonResponse = readMailService.readEmails(from, paswd, subject, sender, filename, filetype, counted, separator, header);
            
            logger.info("Legacy read email completed successfully for mailbox: {}", from);
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error reading legacy emails", e);
            return createErrorResponse("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Validates legacy read email parameters.
     */
    private String validateReadParameters(String from, String paswd, Integer counted, 
                                        String sender, String filetype, String separator, String filename) {
        if (from == null || from.trim().isEmpty()) {
            return "{\"errors\" : \"from tidak boleh null\"}";
        }
        if (paswd == null || paswd.trim().isEmpty()) {
            return "{\"errors\" : \"password tidak boleh null\"}";
        }
        if (counted == null) {
            return "{\"errors\" : \"counted tidak boleh null\"}";
        }
        if (sender == null || sender.trim().isEmpty()) {
            return "{\"errors\" : \"sender tidak boleh null\"}";
        }
        if (filetype == null || filetype.trim().isEmpty()) {
            return "{\"errors\" : \"filetype tidak boleh null\"}";
        }
        if (separator == null || separator.trim().isEmpty()) {
            return "{\"errors\" : \"separator tidak boleh null\"}";
        }
        if (filename == null || filename.trim().isEmpty()) {
            return "{\"errors\" : \"filename tidak boleh null\"}";
        }
        return null;
    }

    /**
     * Creates error response with appropriate HTTP status based on error content.
     */
    private ResponseEntity<String> createErrorResponse(String errorMessage, HttpStatus defaultStatus) {
        // Check if it's a validation error (contains "errors" keyword like original)
        Pattern pattern = Pattern.compile("errors");
        Matcher matcher = pattern.matcher(errorMessage);
        
        if (matcher.find()) {
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(errorMessage, defaultStatus);
    }
}