package kz.skills.elearning.service.video;

import kz.skills.elearning.config.VideoStorageProperties;
import kz.skills.elearning.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.media.video.provider", havingValue = "s3")
public class S3VideoStorageService implements VideoStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final VideoStorageProperties properties;

    public S3VideoStorageService(S3Client s3Client, S3Presigner s3Presigner, VideoStorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public VideoUploadSession createUploadSession(PendingVideoUpload upload) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(upload.objectKey())
                .contentType(upload.contentType())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(properties.getPresignDuration())
                .putObjectRequest(request)
                .build());

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", upload.contentType());

        return new VideoUploadSession(
                upload.objectKey(),
                presignedRequest.url().toString(),
                headers,
                upload.expiresAt(),
                getPlaybackUrl(upload.objectKey())
        );
    }

    @Override
    public StoredVideoObject getObject(String objectKey) {
        try {
            var response = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build());
            return new StoredVideoObject(objectKey, response.contentType(), response.contentLength());
        } catch (NoSuchKeyException ex) {
            throw new ResourceNotFoundException("Uploaded video not found for object key: " + objectKey);
        }
    }

    @Override
    public String getPlaybackUrl(String objectKey) {
        String baseUrl = properties.getPublicBaseUrl();
        return (baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl) + "/" + objectKey;
    }

    @Override
    public void deleteObject(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build());
    }
}
