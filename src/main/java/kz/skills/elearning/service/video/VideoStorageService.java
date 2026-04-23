package kz.skills.elearning.service.video;

import java.time.Duration;

public interface VideoStorageService {

    VideoUploadSession createUploadSession(PendingVideoUpload upload);

    StoredVideoObject getObject(String objectKey);

    /**
     * Returns a permanent or cached public URL for the object.
     * Only valid when the bucket is publicly accessible (e.g. CDN).
     * Prefer {@link #generatePresignedGetUrl} for private buckets.
     */
    String getPlaybackUrl(String objectKey);

    /**
     * Generates a short-lived presigned GET URL that allows the caller to stream
     * the video directly from storage without exposing credentials.
     */
    String generatePresignedGetUrl(String objectKey, Duration validity);

    void deleteObject(String objectKey);
}
