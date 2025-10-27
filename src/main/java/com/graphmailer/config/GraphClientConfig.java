package com.graphmailer.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration class for Microsoft Graph client setup.
 * 
 * This configuration creates a GraphServiceClient using Azure Identity
 * with Client Credentials flow for application-only authentication.
 * 
 * The client is configured with retry capabilities and timeout settings
 * as specified in the GraphProperties configuration.
 * Only active in production mode.
 */
@Configuration
@EnableRetry
@ConditionalOnProperty(name = "app.mode", havingValue = "production", matchIfMissing = true)
public class GraphClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(GraphClientConfig.class);

    private final GraphProperties graphProperties;

    public GraphClientConfig(GraphProperties graphProperties) {
        this.graphProperties = graphProperties;
    }

    /**
     * Creates and configures the Microsoft Graph service client.
     * Uses Client Secret Credential for application-only authentication.
     * 
     * @return Configured GraphServiceClient instance
     */
    @Bean
    public GraphServiceClient graphServiceClient() {
        logger.info("Initializing Microsoft Graph client for tenant: {}", 
                   graphProperties.tenantId());

        // Create Azure credentials using client secret
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(graphProperties.clientId())
                .clientSecret(graphProperties.clientSecret())
                .tenantId(graphProperties.tenantId())
                .build();

        // Build Graph service client with the credential
        GraphServiceClient graphClient = new GraphServiceClient(credential, graphProperties.scopes());

        logger.info("Microsoft Graph client initialized successfully");
        return graphClient;
    }
}