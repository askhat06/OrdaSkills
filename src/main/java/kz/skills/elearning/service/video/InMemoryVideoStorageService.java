package kz.skills.elearning.service.video;

import kz.skills.elearning.config.VideoStorageProperties;
import kz.skills.elearning.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "app.media.video.provider", havingValue = "in-memory")
public class InMemoryVideoStorageService implements VideoStorageService {

    private final VideoStorageProperties properties;
    private final Map<String, StoredVideoObject> objects = new ConcurrentHashMap<>();

    public InMemoryVideoStorageService(VideoStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public VideoUploadSession createUploadSession(PendingVideoUpload upload) {
        return new VideoUploadSession(
                upload.objectKey(),
                "inmemory://" + properties.getBucket() + "/" + upload.objectKey(),
                Map.of("Content-Type", upload.contentType()),
                upload.expiresAt(),
                getPlaybackUrl(upload.objectKey())
        );
    }

    @Override
    public StoredVideoObject getObject(String objectKey) {
        StoredVideoObject stored = objects.get(objectKey);
        if (stored == null) {
            throw new ResourceNotFoundException("Uploaded video not found for object key: " + objectKey);
        }
        return stored;
    }

    @Override
    public String getPlaybackUrl(String objectKey) {
        String baseUrl = properties.getPublicBaseUrl();
        return (baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl) + "/" + objectKey;
    }

    @Override
    public void deleteObject(String objectKey) {
        objects.remove(objectKey);
    }

    public void putObject(String objectKey, String contentType, long sizeBytes) {
        objects.put(objectKey, new StoredVideoObject(objectKey, contentType, sizeBytes));
    }

    public void clear() {
        objects.clear();
    }
}
