package com.graphmailer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphmailer.model.SendMailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Graph Mailer application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "api-key"})
@TestPropertySource(properties = {
        "graph.tenant-id=test-tenant",
        "graph.client-id=test-client",
        "graph.client-secret=test-secret",
        "security.inbound.api-key-value=test-key",
        "mail.allowed-sender-upns=test@example.com",
        "mail.allowed-recipient-domains=example.com"
})
class GraphMailerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoEndpointShouldReturnAppInfo() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.version").exists());
    }

    @Test
    void sendMailWithoutApiKeyShouldReturnUnauthorized() throws Exception {
        SendMailRequest request = createValidMailRequest();

        mockMvc.perform(post("/api/v1/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendMailWithValidApiKeyShouldProcessRequest() throws Exception {
        SendMailRequest request = createValidMailRequest();

        mockMvc.perform(post("/api/v1/mail/send")
                .header("X-API-Key", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Expected since Graph client is not configured
    }

    private SendMailRequest createValidMailRequest() {
        return new SendMailRequest(
                "test@example.com",
                List.of("recipient@example.com"),
                List.of(),
                List.of(),
                "Integration Test Subject",
                "<p>Integration Test Body</p>",
                null,
                List.of(),
                true,
                "normal"
        );
    }
}