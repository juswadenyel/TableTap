package edu.cit.dingding.tabletap.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// This service sends password reset links through configured SMTP.
@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final ObjectProvider<JavaMailSender> mailSender;
    private final Environment environment;
    private final String host;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, Environment environment,
            @Value("${spring.mail.host:}") String host, @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.environment = environment;
        this.host = host;
        this.from = from;
    }

    public void sendPasswordReset(String email, String link, int expirationMinutes) {
        JavaMailSender sender = host.isBlank() ? null : mailSender.getIfAvailable();
        if (sender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            if (!from.isBlank()) message.setFrom(from);
            message.setSubject("Reset your TableTap password");
            message.setText("Use this link within " + expirationMinutes + " minutes to reset your password: " + link);
            sender.send(message);
        } else if (environment.acceptsProfiles(Profiles.of("dev"))) {
            logger.info("Development password reset link: {}", link);
        } else {
            logger.warn("Password reset email could not be sent because SMTP is not configured");
        }
    }
}
