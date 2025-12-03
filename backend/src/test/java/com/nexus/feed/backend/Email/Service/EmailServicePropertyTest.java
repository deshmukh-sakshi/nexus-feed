package com.nexus.feed.backend.Email.Service;

import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServicePropertyTest {

    @Property(tries = 100)
    void welcomeEmailShouldContainUsername(@ForAll("usernames") String username) {
        // Given
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
        EmailServiceImpl emailService = new EmailServiceImpl(mailSender, "", "");

        // When
        String emailBody = emailService.composeWelcomeEmail(username);

        // Then
        assert emailBody.contains(username);
    }

    @Property(tries = 100)
    void missingConfigShouldDisableService(@ForAll("missingConfigs") ConfigPair config) {
        // Given
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);

        // When
        EmailServiceImpl emailService = new EmailServiceImpl(
            mailSender, 
            config.username(), 
            config.password()
        );

        // Then
        assert !emailService.isEnabled();
    }

    @Property(tries = 100)
    void validConfigShouldEnableService(@ForAll("validConfigs") ConfigPair config) {
        // Given
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);

        // When
        EmailServiceImpl emailService = new EmailServiceImpl(
            mailSender, 
            config.username(), 
            config.password()
        );

        // Then
        assert emailService.isEnabled();
    }

    @Provide
    Arbitrary<String> usernames() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(50);
    }

    @Provide
    Arbitrary<ConfigPair> missingConfigs() {
        Arbitrary<String> emptyOrNull = Arbitraries.of("", "   ", null);
        Arbitrary<String> validValue = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
        
        return Arbitraries.oneOf(
            emptyOrNull.flatMap(u -> emptyOrNull.map(p -> new ConfigPair(u, p))),
            emptyOrNull.flatMap(u -> validValue.map(p -> new ConfigPair(u, p))),
            validValue.flatMap(u -> emptyOrNull.map(p -> new ConfigPair(u, p)))
        );
    }

    @Provide
    Arbitrary<ConfigPair> validConfigs() {
        Arbitrary<String> validValue = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
        return validValue.flatMap(u -> validValue.map(p -> new ConfigPair(u, p)));
    }

    record ConfigPair(String username, String password) {}
}
