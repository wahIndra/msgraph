package com.graphmailer.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Configuration for rate limiting using Bucket4j.
 * 
 * This configuration sets up token bucket rate limiting to prevent
 * abuse and ensure fair usage of the mail service.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Creates a bucket registry for storing rate limit buckets per IP address.
     * 
     * @return ConcurrentMap serving as bucket registry
     */
    @Bean
    public ConcurrentMap<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates the default bandwidth configuration for rate limiting.
     * Default: 30 requests per minute per IP address.
     * 
     * @return Bandwidth configuration
     */
    @Bean
    public Bandwidth defaultBandwidth() {
        return Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
    }
}