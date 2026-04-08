package kz.skills.elearning.service.email;

import kz.skills.elearning.entity.PlatformUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailVerificationEmailService {

    private static final DateTimeFormatter EXPIRES_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'");

    private final EmailTemplateService emailTemplateService;
    private final MailDeliveryService mailDeliveryService;

    public EmailVerificationEmailService(EmailTemplateService emailTemplateService,
                                         MailDeliveryService mailDeliveryService) {
        this.emailTemplateService = emailTemplateService;
        this.mailDeliveryService = mailDeliveryService;
    }

    public void sendVerificationEmail(PlatformUser user, String verificationUrl, LocalDateTime expiresAt) {
        Map<String, String> variables = Map.of(
                "FULL_NAME", safeValue(user.getFullName()),
                "EMAIL", safeValue(user.getEmail()),
                "VERIFICATION_URL", verificationUrl,
                "EXPIRES_AT", EXPIRES_AT_FORMATTER.format(expiresAt)
        );

        String htmlBody = emailTemplateService.render("mail/email-verification.html", variables);
        String textBody = emailTemplateService.render("mail/email-verification.txt", variables);

        mailDeliveryService.send(new OutgoingMail(
                user.getEmail(),
                "Verify your OrdaSkills email",
                htmlBody,
                textBody
        ));
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
