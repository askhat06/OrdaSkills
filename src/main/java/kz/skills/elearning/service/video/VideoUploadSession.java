package kz.skills.elearning.service.video;

import java.time.Instant;
import java.util.Map;

public record VideoUploadSession(
        String objectKey,
        String uploadUrl,
        Map<String, String> requiredHeaders,
        Instant expiresAt,
        String playbackUrl
) {
}
