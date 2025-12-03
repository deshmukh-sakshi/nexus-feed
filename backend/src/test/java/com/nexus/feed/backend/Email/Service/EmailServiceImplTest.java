package com.nexus.feed.backend.Email.Service;

import com.nexus.feed.backend.Email.Exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Unit Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, "test@gmail.com", "password");
    }

    @Test
    @DisplayName("Should return true when email is configured")
    void shouldReturnTrueWhenConfigured() {
        assertThat(emailService.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return false when username is empty")
    void shouldReturnFalseWhenUsernameEmpty() {
        // Given
        EmailServiceImpl service = new EmailServiceImpl(mailSender, "", "password");

        // Then
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return false when password is empty")
    void shouldReturnFalseWhenPasswordEmpty() {
        // Given
        EmailServiceImpl service = new EmailServiceImpl(mailSender, "test@gmail.com", "");

        // Then
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return false when username is null")
    void shouldReturnFalseWhenUsernameNull() {
        // Given
        EmailServiceImpl service = new EmailServiceImpl(mailSender, null, "password");

        // Then
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should compose email message correctly")
    void shouldComposeEmailMessageCorrectly() {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject";
        String body = "Test body content";

        // When
        emailService.sendEmail(to, subject, body);

        // Then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("Nexus Feed <nexus.feed.help@gmail.com>");
        assertThat(message.getTo()).containsExactly(to);
        assertThat(message.getSubject()).isEqualTo(subject);
        assertThat(message.getText()).isEqualTo(body);
    }

    @Test
    @DisplayName("Should not send email when service is disabled")
    void shouldNotSendEmailWhenDisabled() {
        // Given
        EmailServiceImpl disabledService = new EmailServiceImpl(mailSender, "", "");

        // When
        disabledService.sendEmail("user@example.com", "Subject", "Body");

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should throw EmailSendException when mail sender fails")
    void shouldThrowEmailSendExceptionWhenMailSenderFails() {
        // Given
        doThrow(new MailSendException("SMTP error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendEmail("user@example.com", "Subject", "Body"))
            .isInstanceOf(EmailSendException.class)
            .hasMessageContaining("Failed to send email")
            .hasCauseInstanceOf(MailSendException.class);
    }

    @Test
    @DisplayName("Should include username in welcome email")
    void shouldIncludeUsernameInWelcomeEmail() {
        // Given
        String username = "JohnDoe";

        // When
        String body = emailService.composeWelcomeEmail(username);

        // Then
        assertThat(body).contains("Hey JohnDoe,");
        assertThat(body).contains("Welcome to Nexus Feed!");
    }

    @Test
    @DisplayName("Should include feature list in welcome email")
    void shouldIncludeFeatureListInWelcomeEmail() {
        // When
        String body = emailService.composeWelcomeEmail("TestUser");

        // Then
        assertThat(body).contains("Create and share posts");
        assertThat(body).contains("Comment on discussions");
        assertThat(body).contains("Upvote content");
        assertThat(body).contains("Earn karma and badges");
    }
}
