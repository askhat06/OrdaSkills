package kz.skills.elearning.controller;

import kz.skills.elearning.dto.AdminVideoUploadCompleteRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitResponse;
import kz.skills.elearning.service.AdminLessonVideoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}")
public class AdminLessonVideoController {

    private final AdminLessonVideoService adminLessonVideoService;

    public AdminLessonVideoController(AdminLessonVideoService adminLessonVideoService) {
        this.adminLessonVideoService = adminLessonVideoService;
    }

    @PostMapping("/video-upload")
    public ResponseEntity<AdminVideoUploadInitResponse> initiateUpload(@PathVariable String courseSlug,
                                                                       @PathVariable String lessonSlug,
                                                                       @Valid @RequestBody AdminVideoUploadInitRequest request) {
        return ResponseEntity.ok(adminLessonVideoService.initiateUpload(courseSlug, lessonSlug, request));
    }

    @PostMapping("/video-upload/complete")
    public ResponseEntity<Void> completeUpload(@PathVariable String courseSlug,
                                               @PathVariable String lessonSlug,
                                               @Valid @RequestBody AdminVideoUploadCompleteRequest request) {
        adminLessonVideoService.completeUpload(courseSlug, lessonSlug, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/video")
    public ResponseEntity<Void> deleteVideo(@PathVariable String courseSlug,
                                            @PathVariable String lessonSlug) {
        adminLessonVideoService.deleteVideo(courseSlug, lessonSlug);
        return ResponseEntity.noContent().build();
    }
}
