package com.graphmailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the Graph Mailer service.
 * 
 * This Spring Boot application provides a REST API for sending emails using
 * Microsoft Graph API,
 * replacing traditional EWS email sending with modern Graph-based mail
 * services.
 * 
 * Key features:
 * - Send emails using Microsoft Graph Mail.Send application permission
 * - Support for HTML content and attachments
 * - Dual authentication modes: API Key or OAuth2 JWT
 * - Rate limiting and security hardening
 * - OpenAPI documentation and health monitoring
 * 
 * @author wahIndra
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@ConfigurationPropertiesScan
public class GraphMailerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphMailerApplication.class, args);
    }
}