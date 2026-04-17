package kz.skills.elearning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;
    private final String appBaseUrl;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:noreply@example.com}") String fromAddress,
                        @Value("${app.email.from-name:Oyan Platform}") String fromName,
                        @Value("${app.email.base-url:http://localhost:7777}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.appBaseUrl = appBaseUrl;
    }

    public void sendVerificationEmail(String to, String token) {
        String link = appBaseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromName + " <" + fromAddress + ">");
        message.setTo(to);
        message.setSubject("Verify your email — Oyan Platform");
        message.setText(
                "Hello!\n\n" +
                "Please verify your email address by clicking the link below:\n\n" +
                link + "\n\n" +
                "The link expires in 24 hours.\n\n" +
                "If you did not register on Oyan Platform, you can ignore this email."
        );

        try {
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}: {}", to, ex.getMessage());
            throw ex;
        }
    }
}
