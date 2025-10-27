package com.graphmailer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) documentation configuration for the Graph Mailer API.
 * 
 * This configuration sets up comprehensive API documentation including
 * security schemes for both API Key and OAuth2 authentication modes.
 */
@Configuration
public class OpenApiConfig {

        private final BuildProperties buildProperties;

        public OpenApiConfig(@Autowired(required = false) BuildProperties buildProperties) {
                this.buildProperties = buildProperties;
        }

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .components(new Components()
                                                .addSecuritySchemes("apiKey", apiKeySecurityScheme())
                                                .addSecuritySchemes("oauth2", oauth2SecurityScheme()))
                                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
        }

        private Info apiInfo() {
                return new Info()
                                .title("Graph Mailer API")
                                .description("""
                                                Microsoft Graph Mail Service API

                                                This service provides REST endpoints for sending emails using Microsoft Graph API,
                                                replacing traditional EWS email sending with modern Graph-based mail services.

                                                Features:
                                                - Send emails with HTML content and attachments
                                                - Support for CC/BCC recipients
                                                - Dual authentication: API Key or OAuth2 JWT
                                                - Rate limiting and security controls
                                                - Domain and sender restrictions
                                                """)
                                .version(buildProperties != null ? buildProperties.getVersion() : "1.0.0")
                                .contact(new Contact()
                                                .name("wahIndra")
                                                .url("https://github.com/wahIndra"))
                                .license(new License()
                                                .name("MIT License")
                                                .url("https://github.com/wahIndra/msgraph/blob/main/LICENSE"));
        }

        private SecurityScheme apiKeySecurityScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key authentication. Provide your API key in the X-API-Key header.");
        }

        private SecurityScheme oauth2SecurityScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("OAuth2 JWT Bearer token authentication. Provide your JWT token in the Authorization header.");
        }
}