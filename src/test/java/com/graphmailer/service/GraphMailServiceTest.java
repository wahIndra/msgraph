package com.graphmailer.service;

import com.graphmailer.config.MailProperties;
import com.graphmailer.logging.AuditLogger;
import com.graphmailer.model.SendMailRequest;
import com.graphmailer.model.SendMailResponse;
import com.graphmailer.util.ValidationUtil;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphMailService.
 */
@ExtendWith(MockitoExtension.class)
class GraphMailServiceTest {

    @Mock
    private GraphServiceClient graphClient;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private AuditLogger auditLogger;

    private GraphMailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new GraphMailService(graphClient, mailProperties, validationUtil, auditLogger);
        MDC.clear();
    }

    @Test
    void sendMailWithValidRequestReturnsSuccessResponse() {
        // Given
        SendMailRequest request = createValidMailRequest();

        // Mock validation to pass
        doNothing().when(validationUtil).validateMailRequest(any(), any());

        // Mock audit logger
        doNothing().when(auditLogger).logEmailSent(any(), any(), any());

        // When
        SendMailResponse response = mailService.sendMail(request);

        // Then
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.messageId()).isNotNull();
        assertThat(response.correlationId()).isNotNull();

        verify(validationUtil).validateMailRequest(eq(request), eq(mailProperties));
        verify(auditLogger).logEmailSent(eq(request), any(), any());
    }

    @Test
    void sendMailWithValidationFailureReturnsFailedResponse() {
        // Given
        SendMailRequest request = createValidMailRequest();
        String errorMessage = "Invalid sender UPN";

        // Mock validation to throw exception
        when(validationUtil.validateMailRequest(any(), any()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Mock audit logger
        doNothing().when(auditLogger).logEmailFailed(any(), any(), any());

        // When
        SendMailResponse response = mailService.sendMail(request);

        // Then
        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.message()).contains("Failed to send email");
        assertThat(response.correlationId()).isNotNull();

        verify(auditLogger).logEmailFailed(eq(request), any(), any());
    }

    private SendMailRequest createValidMailRequest() {
        return new SendMailRequest(
                "noreply@yourtenant.com",
                List.of("test@example.com"),
                List.of(),
                List.of(),
                "Test Subject",
                "<p>Test HTML Body</p>",
                "Test Text Body",
                List.of(),
                true,
                "normal");
    }
}