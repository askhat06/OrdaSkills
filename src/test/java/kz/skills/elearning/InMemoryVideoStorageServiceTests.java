package kz.skills.elearning;

import kz.skills.elearning.config.VideoStorageProperties;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.service.video.InMemoryVideoStorageService;
import kz.skills.elearning.service.video.PendingVideoUpload;
import kz.skills.elearning.service.video.StoredVideoObject;
import kz.skills.elearning.service.video.VideoUploadSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryVideoStorageServiceTests {

    @Test
    void inMemoryStorageSupportsUploadSessionLookupAndDelete() {
        VideoStorageProperties properties = new VideoStorageProperties();
        properties.setBucket("test-bucket");
        properties.setPublicBaseUrl("https://cdn.example.test/videos");

        InMemoryVideoStorageService service = new InMemoryVideoStorageService(properties);

        VideoUploadSession session = service.createUploadSession(new PendingVideoUpload(
                "lessons/course/lesson/video.mp4",
                "video.mp4",
                "video/mp4",
                128,
                Instant.parse("2026-03-19T08:00:00Z")
        ));

        assertThat(session.uploadUrl()).isEqualTo("inmemory://test-bucket/lessons/course/lesson/video.mp4");
        assertThat(session.playbackUrl()).isEqualTo("https://cdn.example.test/videos/lessons/course/lesson/video.mp4");

        service.putObject("lessons/course/lesson/video.mp4", "video/mp4", 128);
        StoredVideoObject object = service.getObject("lessons/course/lesson/video.mp4");
        assertThat(object.contentType()).isEqualTo("video/mp4");
        assertThat(object.sizeBytes()).isEqualTo(128);

        service.deleteObject("lessons/course/lesson/video.mp4");
        assertThatThrownBy(() -> service.getObject("lessons/course/lesson/video.mp4"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
