package kz.skills.elearning.service.video;

import java.time.Instant;

public record PendingVideoUpload(
        String objectKey,
        String fileName,
        String contentType,
        long sizeBytes,
        Instant expiresAt
) {
}
