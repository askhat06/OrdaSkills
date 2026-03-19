package kz.skills.elearning.service;

import kz.skills.elearning.config.VideoStorageProperties;
import kz.skills.elearning.dto.AdminVideoUploadCompleteRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitResponse;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.entity.LessonVideoUpload;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.exception.VideoUploadException;
import kz.skills.elearning.repository.LessonRepository;
import kz.skills.elearning.repository.LessonVideoUploadRepository;
import kz.skills.elearning.service.video.PendingVideoUpload;
import kz.skills.elearning.service.video.StoredVideoObject;
import kz.skills.elearning.service.video.VideoStorageService;
import kz.skills.elearning.service.video.VideoUploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class AdminLessonVideoService {

    private static final Logger log = LoggerFactory.getLogger(AdminLessonVideoService.class);

    private final LessonRepository lessonRepository;
    private final LessonVideoUploadRepository lessonVideoUploadRepository;
    private final VideoStorageService videoStorageService;
    private final VideoStorageProperties videoStorageProperties;

    public AdminLessonVideoService(LessonRepository lessonRepository,
                                   LessonVideoUploadRepository lessonVideoUploadRepository,
                                   VideoStorageService videoStorageService,
                                   VideoStorageProperties videoStorageProperties) {
        this.lessonRepository = lessonRepository;
        this.lessonVideoUploadRepository = lessonVideoUploadRepository;
        this.videoStorageService = videoStorageService;
        this.videoStorageProperties = videoStorageProperties;
    }

    public AdminVideoUploadInitResponse initiateUpload(String courseSlug, String lessonSlug, AdminVideoUploadInitRequest request) {
        Lesson lesson = findLesson(courseSlug, lessonSlug);
        validateUploadRequest(request);

        String objectKey = buildObjectKey(courseSlug, lessonSlug, request.fileName());
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plus(videoStorageProperties.getPresignDuration());

        lessonVideoUploadRepository.findByLesson_Id(lesson.getId())
                .ifPresent(lessonVideoUploadRepository::delete);

        LessonVideoUpload pending = new LessonVideoUpload();
        pending.setLesson(lesson);
        pending.setObjectKey(objectKey);
        pending.setOriginalFilename(request.fileName());
        pending.setContentType(request.contentType());
        pending.setSizeBytes(request.sizeBytes());
        pending.setExpiresAt(expiresAt);
        lessonVideoUploadRepository.save(pending);

        VideoUploadSession uploadSession = videoStorageService.createUploadSession(new PendingVideoUpload(
                objectKey,
                request.fileName(),
                request.contentType(),
                request.sizeBytes(),
                expiresAt.toInstant(ZoneOffset.UTC)
        ));

        return new AdminVideoUploadInitResponse(
                uploadSession.objectKey(),
                uploadSession.uploadUrl(),
                uploadSession.requiredHeaders(),
                uploadSession.expiresAt(),
                uploadSession.playbackUrl()
        );
    }

    public void completeUpload(String courseSlug, String lessonSlug, AdminVideoUploadCompleteRequest request) {
        Lesson lesson = findLesson(courseSlug, lessonSlug);
        LessonVideoUpload pending = lessonVideoUploadRepository.findByLesson_IdAndObjectKey(lesson.getId(), request.objectKey())
                .orElseThrow(() -> new ResourceNotFoundException("Pending upload not found for object key: " + request.objectKey()));

        if (pending.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            lessonVideoUploadRepository.delete(pending);
            throw new VideoUploadException("Upload session has expired. Start a new upload.");
        }

        StoredVideoObject object = videoStorageService.getObject(request.objectKey());
        if (!pending.getContentType().equalsIgnoreCase(object.contentType())) {
            throw new VideoUploadException("Uploaded video content type does not match the initiated upload");
        }
        if (pending.getSizeBytes() != object.sizeBytes()) {
            throw new VideoUploadException("Uploaded video size does not match the initiated upload");
        }

        String previousObjectKey = lesson.getVideoStorageKey();

        lesson.setVideoStorageKey(object.objectKey());
        lesson.setVideoOriginalFilename(pending.getOriginalFilename());
        lesson.setVideoContentType(object.contentType());
        lesson.setVideoSizeBytes(object.sizeBytes());
        lesson.setVideoUploadedAt(LocalDateTime.now(ZoneOffset.UTC));
        lesson.setVideoUrl(videoStorageService.getPlaybackUrl(object.objectKey()));
        lessonRepository.save(lesson);
        lessonVideoUploadRepository.deleteByLesson_Id(lesson.getId());

        if (previousObjectKey != null && !previousObjectKey.equals(object.objectKey())) {
            deleteObjectQuietly(previousObjectKey);
        }
    }

    public void deleteVideo(String courseSlug, String lessonSlug) {
        Lesson lesson = findLesson(courseSlug, lessonSlug);
        String objectKey = lesson.getVideoStorageKey();

        lesson.setVideoUrl(null);
        lesson.setVideoStorageKey(null);
        lesson.setVideoOriginalFilename(null);
        lesson.setVideoContentType(null);
        lesson.setVideoSizeBytes(null);
        lesson.setVideoUploadedAt(null);
        lessonRepository.save(lesson);
        lessonVideoUploadRepository.deleteByLesson_Id(lesson.getId());

        if (objectKey != null) {
            deleteObjectQuietly(objectKey);
        }
    }

    private Lesson findLesson(String courseSlug, String lessonSlug) {
        return lessonRepository.findByCourse_SlugAndSlug(courseSlug, lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found: " + lessonSlug + " for course: " + courseSlug));
    }

    private void validateUploadRequest(AdminVideoUploadInitRequest request) {
        if (!videoStorageProperties.getAllowedContentTypes().contains(request.contentType())) {
            throw new BadRequestException("Unsupported video content type: " + request.contentType());
        }
        if (request.sizeBytes() > videoStorageProperties.getMaxFileSizeBytes()) {
            throw new BadRequestException("Video exceeds max file size of " + videoStorageProperties.getMaxFileSizeBytes() + " bytes");
        }
    }

    private String buildObjectKey(String courseSlug, String lessonSlug, String fileName) {
        String sanitizedFileName = fileName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        return "%s/%s/%s/%s-%s".formatted(
                trimSlashes(videoStorageProperties.getLessonPrefix()),
                courseSlug,
                lessonSlug,
                UUID.randomUUID(),
                sanitizedFileName
        );
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private void deleteObjectQuietly(String objectKey) {
        try {
            videoStorageService.deleteObject(objectKey);
        } catch (Exception ex) {
            log.warn("Failed to delete video object {}", objectKey, ex);
        }
    }
}
