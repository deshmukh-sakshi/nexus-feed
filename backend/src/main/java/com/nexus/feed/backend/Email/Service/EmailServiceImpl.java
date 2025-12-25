package com.nexus.feed.backend.Email.Service;

import com.nexus.feed.backend.Email.Exception.EmailSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final String FROM_ADDRESS = "Nexus Feed <nexus.feed.help@gmail.com>";

    private final JavaMailSender mailSender;
    private final boolean enabled;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.mailSender = mailSender;
        this.enabled = isConfigured(mailUsername, mailPassword);
        
        if (!enabled) {
            log.warn("Email service is disabled - MAIL_USERNAME or MAIL_PASSWORD not configured");
        } else {
            log.info("Email service initialized with sender: {}", FROM_ADDRESS);
        }
    }

    private boolean isConfigured(String username, String password) {
        return username != null && !username.isBlank() 
            && password != null && !password.isBlank();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        doSendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Nexus Feed!";
        String body = composeWelcomeEmail(username);
        doSendEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendBadgeAwardedEmail(String to, String username, String badgeName, String badgeDescription, String badgeIcon) {
        String subject = "🎉 You earned a new badge: " + badgeName;
        String body = composeBadgeEmail(username, badgeName, badgeDescription, badgeIcon);
        doSendEmail(to, subject, body);
    }

    private void doSendEmail(String to, String subject, String body) {
        if (!enabled) {
            log.debug("Email not sent (service disabled): to={}, subject={}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent: to={}, subject={}", to, subject);
        } catch (MailException e) {
            log.error("Failed to send email: to={}, subject={}, error={}", to, subject, e.getMessage());
            throw new EmailSendException("Failed to send email to " + to, e);
        }
    }

    String composeWelcomeEmail(String username) {
        return String.format(
            "Hey %s,\n\n" +
            "Welcome to Nexus Feed! We're excited to have you join our community.\n\n" +
            "You can now:\n" +
            "- Create and share posts\n" +
            "- Comment on discussions\n" +
            "- Upvote content you like\n" +
            "- Earn karma and badges\n\n" +
            "Happy posting!\n\n" +
            "The Nexus Feed Team",
            username
        );
    }

    String composeBadgeEmail(String username, String badgeName, String badgeDescription, String badgeIcon) {
        return String.format(
            "Hey %s,\n\n" +
            "Congratulations! You've earned a new badge on Nexus Feed!\n\n" +
            "%s %s\n" +
            "%s\n\n" +
            "Keep up the great work and continue engaging with the community to earn more badges!\n\n" +
            "The Nexus Feed Team",
            username, badgeIcon, badgeName, badgeDescription
        );
    }
}
