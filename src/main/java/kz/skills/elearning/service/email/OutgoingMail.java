package kz.skills.elearning.service.email;

public record OutgoingMail(
        String to,
        String subject,
        String htmlBody,
        String textBody
) {
}
