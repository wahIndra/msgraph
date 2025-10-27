package com.graphmailer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mock configuration that replaces Graph client configuration when running in mock mode.
 * 
 * This configuration is active when app.mode=mock, preventing the real Graph client
 * from being initialized and avoiding the need for actual Azure credentials.
 */
@Configuration
@ConditionalOnProperty(name = "app.mode", havingValue = "mock")
public class MockConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MockConfig.class);
    
    @Bean
    public String mockGraphClient() {
        logger.info("Mock mode enabled - Graph client initialization skipped");
        logger.info("No real emails will be sent. All email operations will be simulated.");
        return "mock-graph-client";
    }
}