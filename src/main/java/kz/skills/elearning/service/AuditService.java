package kz.skills.elearning.service;

import kz.skills.elearning.entity.AuditEvent;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.repository.AuditEventRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final PlatformUserRepository platformUserRepository;

    public AuditService(AuditEventRepository auditEventRepository,
                        PlatformUserRepository platformUserRepository) {
        this.auditEventRepository = auditEventRepository;
        this.platformUserRepository = platformUserRepository;
    }

    /**
     * Persists an audit event in the same transaction as the caller.
     * Use {@code Propagation.MANDATORY} so it never silently succeeds without a transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(PlatformUserPrincipal actor,
                       String action,
                       String entityType,
                       String entityId,
                       String payloadJson) {
        AuditEvent event = new AuditEvent();

        if (actor != null) {
            event.setActor(platformUserRepository.getReferenceById(actor.getId()));
        }

        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setPayload(payloadJson);
        event.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));

        auditEventRepository.save(event);
    }
}
