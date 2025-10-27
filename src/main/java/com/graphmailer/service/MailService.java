package com.graphmailer.service;

import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;

/**
 * Mail service interface for sending emails.
 * 
 * This interface is implemented by both the real Graph Mail Service
 * and the Mock Mail Service for testing purposes.
 */
public interface MailService {
    
    /**
     * Send an email using the configured mail provider.
     * 
     * @param request the email request containing recipients, content, and settings
     * @return response containing message ID and delivery status
     * @throws IllegalArgumentException if the request is invalid
     * @throws RuntimeException if email sending fails
     */
    SendMailResponse sendMail(SendMailRequest request);
}