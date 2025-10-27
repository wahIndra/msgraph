package com.graphmailer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.service.GraphMailService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for MailController.
 */
@WebMvcTest(MailController.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GraphMailService mailService;

    @MockBean
    private ConcurrentMap<String, Bucket> rateLimitBuckets;

    @MockBean
    private Bandwidth defaultBandwidth;

    @Test
    @WithMockUser
    void sendMailWithValidRequestReturnsSuccess() throws Exception {
        // Given
        SendMailRequest request = createValidMailRequest();
        SendMailResponse response = SendMailResponse.success("msg-123", "corr-123");
        
        when(mailService.sendMail(any(SendMailRequest.class))).thenReturn(response);
        when(rateLimitBuckets.computeIfAbsent(any(), any())).thenReturn(createMockBucket());

        // When & Then
        mockMvc.perform(post("/api/v1/mail/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.messageId").value("msg-123"))
                .andExpect(jsonPath("$.correlationId").value("corr-123"));
    }

    @Test
    @WithMockUser
    void sendMailWithInvalidRequestReturnsBadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        SendMailRequest request = new SendMailRequest(
                "", // Invalid empty fromUpn
                List.of(), // Invalid empty recipients
                null,
                null,
                "", // Invalid empty subject
                null,
                null,
                null,
                null,
                null
        );

        when(rateLimitBuckets.computeIfAbsent(any(), any())).thenReturn(createMockBucket());

        // When & Then
        mockMvc.perform(post("/api/v1/mail/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    private SendMailRequest createValidMailRequest() {
        return new SendMailRequest(
                "noreply@yourtenant.com",
                List.of("test@example.com"),
                List.of(),
                List.of(),
                "Test Subject",
                "<p>Test HTML Body</p>",
                null,
                List.of(),
                true,
                "normal"
        );
    }

    private Bucket createMockBucket() {
        // Create a mock bucket that always allows requests for testing
        return Bucket.builder()
                .addLimit(Bandwidth.simple(1000, java.time.Duration.ofMinutes(1)))
                .build();
    }
}