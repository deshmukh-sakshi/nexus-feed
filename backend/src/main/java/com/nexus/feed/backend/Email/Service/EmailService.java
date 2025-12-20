package com.nexus.feed.backend.Email.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendWelcomeEmail(String to, String username);
    void sendBadgeAwardedEmail(String to, String username, String badgeName, String badgeDescription, String badgeIcon);
    boolean isEnabled();
}
