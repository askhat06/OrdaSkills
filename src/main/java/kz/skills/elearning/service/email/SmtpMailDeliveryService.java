package kz.skills.elearning.service.email;

import kz.skills.elearning.config.AppMailProperties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SmtpMailDeliveryService implements MailDeliveryService {

    private final JavaMailSender javaMailSender;
    private final AppMailProperties appMailProperties;
    private final MailProperties mailProperties;

    public SmtpMailDeliveryService(JavaMailSender javaMailSender,
                                   AppMailProperties appMailProperties,
                                   MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.appMailProperties = appMailProperties;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(OutgoingMail mail) {
        javaMailSender.send(mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(mail.to());
            helper.setFrom(resolveFromAddress());
            helper.setSubject(mail.subject());
            helper.setText(mail.textBody(), mail.htmlBody());
        });
    }

    private String resolveFromAddress() {
        if (appMailProperties.getFrom() != null && !appMailProperties.getFrom().isBlank()) {
            return appMailProperties.getFrom();
        }
        if (mailProperties.getUsername() != null && !mailProperties.getUsername().isBlank()) {
            return mailProperties.getUsername();
        }
        return "no-reply@ordaskills.local";
    }
}
