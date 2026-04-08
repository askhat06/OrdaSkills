package kz.skills.elearning.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMailDeliveryService implements MailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailDeliveryService.class);

    @Override
    public void send(OutgoingMail mail) {
        log.info("Email delivery fallback for {} with subject '{}'. Text body:\n{}", mail.to(), mail.subject(), mail.textBody());
    }
}
