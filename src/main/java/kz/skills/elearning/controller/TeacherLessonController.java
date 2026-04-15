package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.AdminVideoUploadCompleteRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitRequest;
import kz.skills.elearning.dto.AdminVideoUploadInitResponse;
import kz.skills.elearning.dto.TeacherLessonRequest;
import kz.skills.elearning.dto.TeacherLessonResponse;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.AdminLessonVideoService;
import kz.skills.elearning.service.TeacherLessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses/{courseSlug}/lessons")
public class TeacherLessonController {

    private final TeacherLessonService teacherLessonService;
    private final AdminLessonVideoService adminLessonVideoService;

    public TeacherLessonController(TeacherLessonService teacherLessonService,
                                   AdminLessonVideoService adminLessonVideoService) {
        this.teacherLessonService = teacherLessonService;
        this.adminLessonVideoService = adminLessonVideoService;
    }

    @GetMapping
    public List<TeacherLessonResponse> getLessons(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherLessonService.getLessons(courseSlug, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherLessonResponse createLesson(
            @PathVariable String courseSlug,
            @Valid @RequestBody TeacherLessonRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherLessonService.createLesson(courseSlug, request, principal);
    }

    @PutMapping("/{lessonSlug}")
    public TeacherLessonResponse updateLesson(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @Valid @RequestBody TeacherLessonRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherLessonService.updateLesson(courseSlug, lessonSlug, request, principal);
    }

    @DeleteMapping("/{lessonSlug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        teacherLessonService.deleteLesson(courseSlug, lessonSlug, principal);
    }

    // -------------------------------------------------------------------------
    // Video upload — delegates to the existing AdminLessonVideoService after
    // an ownership + editable-status check performed by TeacherLessonService.
    // The video logic itself is not duplicated.
    // -------------------------------------------------------------------------

    /**
     * Initiate a video upload for a lesson owned by this teacher.
     * Ownership and editable-status are validated before delegating to the video service.
     */
    @PostMapping("/{lessonSlug}/video-upload")
    public ResponseEntity<AdminVideoUploadInitResponse> initiateVideoUpload(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @Valid @RequestBody AdminVideoUploadInitRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        teacherLessonService.requireOwnershipForVideoOps(courseSlug, principal);
        return ResponseEntity.ok(adminLessonVideoService.initiateUpload(courseSlug, lessonSlug, request));
    }

    /** Complete a previously initiated video upload. */
    @PostMapping("/{lessonSlug}/video-upload/complete")
    public ResponseEntity<Void> completeVideoUpload(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @Valid @RequestBody AdminVideoUploadCompleteRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        teacherLessonService.requireOwnershipForVideoOps(courseSlug, principal);
        adminLessonVideoService.completeUpload(courseSlug, lessonSlug, request);
        return ResponseEntity.noContent().build();
    }

    /** Remove the video from a lesson. */
    @DeleteMapping("/{lessonSlug}/video")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        teacherLessonService.requireOwnershipForVideoOps(courseSlug, principal);
        adminLessonVideoService.deleteVideo(courseSlug, lessonSlug);
        return ResponseEntity.noContent().build();
    }
}
