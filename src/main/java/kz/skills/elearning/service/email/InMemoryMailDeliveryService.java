package kz.skills.elearning.service.email;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryMailDeliveryService implements MailDeliveryService {

    private final List<OutgoingMail> sentMessages = new CopyOnWriteArrayList<>();

    @Override
    public void send(OutgoingMail mail) {
        sentMessages.add(mail);
    }

    public void clear() {
        sentMessages.clear();
    }

    public List<OutgoingMail> getSentMessages() {
        return List.copyOf(sentMessages);
    }
}
