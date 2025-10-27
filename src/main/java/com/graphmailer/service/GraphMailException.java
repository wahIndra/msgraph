package com.graphmailer.service;

/**
 * Custom exception for Microsoft Graph mail operations.
 * 
 * This exception is thrown when Graph API operations fail,
 * providing specific context about mail service errors.
 */
public class GraphMailException extends RuntimeException {

    public GraphMailException(String message) {
        super(message);
    }

    public GraphMailException(String message, Throwable cause) {
        super(message, cause);
    }
}