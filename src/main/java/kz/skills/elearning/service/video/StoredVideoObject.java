package kz.skills.elearning.service.video;

public record StoredVideoObject(
        String objectKey,
        String contentType,
        long sizeBytes
) {
}
