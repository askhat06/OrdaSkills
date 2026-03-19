package kz.skills.elearning.dto;

import java.time.Instant;
import java.util.Map;

public record AdminVideoUploadInitResponse(
        String objectKey,
        String uploadUrl,
        Map<String, String> requiredHeaders,
        Instant expiresAt,
        String playbackUrl
) {
}
