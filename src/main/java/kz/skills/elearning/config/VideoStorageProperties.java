package kz.skills.elearning.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.media.video")
public class VideoStorageProperties {

    @NotBlank
    private String provider;

    @NotBlank
    private String bucket;

    private String endpoint;

    @NotBlank
    private String publicBaseUrl;

    private String accessKey;

    private String secretKey;

    private String region = "us-east-1";

    @NotBlank
    private String lessonPrefix = "lessons";

    @Min(1)
    private long maxFileSizeBytes = 536_870_912L;

    private List<String> allowedContentTypes = new ArrayList<>(List.of("video/mp4", "video/webm"));

    private Duration presignDuration = Duration.ofMinutes(15);

    private boolean pathStyleAccessEnabled = true;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLessonPrefix() {
        return lessonPrefix;
    }

    public void setLessonPrefix(String lessonPrefix) {
        this.lessonPrefix = lessonPrefix;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public Duration getPresignDuration() {
        return presignDuration;
    }

    public void setPresignDuration(Duration presignDuration) {
        this.presignDuration = presignDuration;
    }

    public boolean isPathStyleAccessEnabled() {
        return pathStyleAccessEnabled;
    }

    public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
        this.pathStyleAccessEnabled = pathStyleAccessEnabled;
    }

    @AssertTrue(message = "app.media.video.provider must be either 's3' or 'in-memory'")
    public boolean isProviderSupported() {
        return "s3".equals(provider) || "in-memory".equals(provider);
    }

    @AssertTrue(message = "S3 video storage requires endpoint, accessKey, secretKey, and region")
    public boolean isS3ConfigurationValid() {
        if (!"s3".equals(provider)) {
            return true;
        }

        return hasText(endpoint)
                && hasText(accessKey)
                && hasText(secretKey)
                && hasText(region);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
