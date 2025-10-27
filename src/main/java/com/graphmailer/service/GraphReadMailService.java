package com.graphmailer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of ReadMailService using Microsoft Graph API.
 * 
 * This service reads emails from Exchange Online using Graph API,
 * providing the same interface as the original EWS-based implementation.
 */
@Service
public class GraphReadMailService implements ReadMailService {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphReadMailService.class);
    
    private final GraphServiceClient graphServiceClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.mode:production}")
    private String appMode;
    
    public GraphReadMailService(@Autowired(required = false) GraphServiceClient graphServiceClient, 
                               ObjectMapper objectMapper) {
        this.graphServiceClient = graphServiceClient;
        this.objectMapper = objectMapper;
        logger.info("GraphReadMailService initialized with GraphServiceClient: {}", 
                   graphServiceClient != null ? "available" : "not available (mock mode)");
    }
    
    @Override
    public String readEmails(String from, String paswd, String subject, String sender, 
                           String filename, String filetype, Integer counted, 
                           String separator, String header) {
        
        logger.info("Reading emails from mailbox: {}", from);
        
        try {
            // In mock mode or when GraphServiceClient is not available, return sample data
            if ("mock".equals(appMode) || graphServiceClient == null) {
                return generateMockEmailData(from, subject, sender, counted, filetype);
            }
            
            // Build the request to Graph API
            var messagesResponse = graphServiceClient
                .users()
                .byUserId(from)
                .messages()
                .get(requestConfiguration -> {
                    if (counted != null && counted > 0) {
                        requestConfiguration.queryParameters.top = counted;
                    }
                    
                    // Build filter based on parameters
                    List<String> filters = new ArrayList<>();
                    if (sender != null && !sender.trim().isEmpty()) {
                        filters.add(String.format("from/emailAddress/address eq '%s'", sender));
                    }
                    if (subject != null && !subject.trim().isEmpty()) {
                        filters.add(String.format("contains(subject, '%s')", subject));
                    }
                    
                    if (!filters.isEmpty()) {
                        requestConfiguration.queryParameters.filter = String.join(" and ", filters);
                    }
                    
                    // Select specific fields to reduce payload
                    requestConfiguration.queryParameters.select = new String[]{
                        "subject", "from", "receivedDateTime", "bodyPreview", 
                        "isRead", "hasAttachments", "internetMessageId"
                    };
                    
                    // Order by received date (newest first)
                    requestConfiguration.queryParameters.orderby = new String[]{"receivedDateTime desc"};
                });
            
            List<Message> messages = messagesResponse != null ? messagesResponse.getValue() : new ArrayList<>();
            
            // Convert to the expected format
            return convertToExpectedFormat(messages, filetype, separator, header);
            
        } catch (Exception e) {
            logger.error("Error reading emails from mailbox: {}", from, e);
            return createErrorResponse("Error reading emails: " + e.getMessage());
        }
    }
    
    /**
     * Generates mock email data for testing.
     */
    private String generateMockEmailData(String from, String subject, String sender, Integer counted, String filetype) {
        logger.info("Generating mock email data for mailbox: {}", from);
        
        List<Map<String, Object>> mockEmails = new ArrayList<>();
        int emailCount = (counted != null && counted > 0) ? counted : 5;
        
        for (int i = 1; i <= emailCount; i++) {
            Map<String, Object> email = new HashMap<>();
            email.put("messageId", "mock-msg-" + i);
            email.put("subject", (subject != null ? subject + " " : "") + "Mock Email " + i);
            email.put("from", sender != null ? sender : "mock.sender" + i + "@example.com");
            email.put("receivedDateTime", "2025-10-21T" + String.format("%02d", 8 + i) + ":00:00Z");
            email.put("bodyPreview", "This is a mock email body preview for testing purposes");
            email.put("isRead", i % 2 == 0);
            email.put("hasAttachments", i % 3 == 0);
            mockEmails.add(email);
        }
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("mailbox", from);
            response.put("totalCount", emailCount);
            response.put("emails", mockEmails);
            response.put("note", "MOCK DATA - Real emails would be retrieved from Microsoft Graph API");
            
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Error creating mock response", e);
            return createErrorResponse("Error creating mock response");
        }
    }
    
    /**
     * Converts Graph API message response to the expected format.
     */
    private String convertToExpectedFormat(List<Message> messages, String filetype, String separator, String header) {
        try {
            if ("CSV".equalsIgnoreCase(filetype)) {
                return convertToCSV(messages, separator, header);
            } else {
                return convertToJSON(messages);
            }
        } catch (Exception e) {
            logger.error("Error converting messages to format: {}", filetype, e);
            return createErrorResponse("Error formatting response");
        }
    }
    
    /**
     * Converts messages to JSON format.
     */
    private String convertToJSON(List<Message> messages) throws JsonProcessingException {
        List<Map<String, Object>> emailList = new ArrayList<>();
        
        for (Message message : messages) {
            Map<String, Object> email = new HashMap<>();
            email.put("messageId", message.getInternetMessageId());
            email.put("subject", message.getSubject());
            email.put("from", message.getFrom() != null ? message.getFrom().getEmailAddress().getAddress() : "");
            email.put("receivedDateTime", message.getReceivedDateTime() != null ? 
                message.getReceivedDateTime().format(DateTimeFormatter.ISO_INSTANT) : "");
            email.put("bodyPreview", message.getBodyPreview());
            email.put("isRead", message.getIsRead());
            email.put("hasAttachments", message.getHasAttachments());
            emailList.add(email);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("totalCount", emailList.size());
        response.put("emails", emailList);
        
        return objectMapper.writeValueAsString(response);
    }
    
    /**
     * Converts messages to CSV format.
     */
    private String convertToCSV(List<Message> messages, String separator, String header) {
        StringBuilder csv = new StringBuilder();
        String sep = (separator != null && !separator.isEmpty()) ? 
            (separator.equals("comma") ? "," : separator) : ",";
        
        // Add header if requested
        if ("true".equalsIgnoreCase(header)) {
            csv.append("MessageId").append(sep)
               .append("Subject").append(sep)
               .append("From").append(sep)
               .append("ReceivedDateTime").append(sep)
               .append("IsRead").append(sep)
               .append("HasAttachments").append("\n");
        }
        
        // Add data rows
        for (Message message : messages) {
            csv.append(escapeCSV(message.getInternetMessageId())).append(sep)
               .append(escapeCSV(message.getSubject())).append(sep)
               .append(escapeCSV(message.getFrom() != null ? message.getFrom().getEmailAddress().getAddress() : "")).append(sep)
               .append(escapeCSV(message.getReceivedDateTime() != null ? message.getReceivedDateTime().toString() : "")).append(sep)
               .append(message.getIsRead()).append(sep)
               .append(message.getHasAttachments()).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Escapes CSV values to handle commas and quotes.
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Creates an error response in JSON format.
     */
    private String createErrorResponse(String errorMessage) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", errorMessage);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            return "{\"status\":\"ERROR\",\"message\":\"" + errorMessage + "\"}";
        }
    }
}