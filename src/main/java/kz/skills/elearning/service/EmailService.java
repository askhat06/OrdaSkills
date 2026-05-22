package kz.skills.elearning.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String apiKey;

    @Value("${app.email.from-address}")
    private String fromAddress;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.base-url:http://localhost:5173}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify-email?token=" + token;
        send(toEmail,
                "Подтвердите ваш email — Oyan",
                """
                <h2>Добро пожаловать на Oyan!</h2>
                <p>Нажмите кнопку ниже для подтверждения email:</p>
                <a href="%s" style="background:#f5a623;color:white;padding:12px 24px;
                   text-decoration:none;border-radius:6px;display:inline-block;">
                   Подтвердить email
                </a>
                <p>Ссылка действует 24 часа.</p>
                """.formatted(link));
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        send(toEmail,
                "Сброс пароля — Oyan",
                """
                <h2>Сброс пароля</h2>
                <p>Нажмите кнопку ниже для установки нового пароля:</p>
                <a href="%s" style="background:#f5a623;color:white;padding:12px 24px;
                   text-decoration:none;border-radius:6px;display:inline-block;">
                   Сбросить пароль
                </a>
                <p>Ссылка действует 1 час.</p>
                """.formatted(link));
    }

    private void send(String toEmail, String subject, String html) {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(fromName + " <" + fromAddress + ">")
                .to(List.of(toEmail))
                .subject(subject)
                .html(html)
                .build();
        try {
            resend.emails().send(request);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }
}
