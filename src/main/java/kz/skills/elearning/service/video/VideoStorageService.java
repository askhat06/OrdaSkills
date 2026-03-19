package kz.skills.elearning.service.video;

public interface VideoStorageService {

    VideoUploadSession createUploadSession(PendingVideoUpload upload);

    StoredVideoObject getObject(String objectKey);

    String getPlaybackUrl(String objectKey);

    void deleteObject(String objectKey);
}
