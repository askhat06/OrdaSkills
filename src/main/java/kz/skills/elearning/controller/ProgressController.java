package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.CourseProgressResponse;
import kz.skills.elearning.dto.UpdateCourseProgressRequest;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.ProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress/courses")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/{courseSlug}")
    public CourseProgressResponse getCourseProgress(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.getCourseProgress(courseSlug, principal);
    }

    @PostMapping("/{courseSlug}/start")
    public CourseProgressResponse startCourseProgress(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.startCourseProgress(courseSlug, principal);
    }

    @PutMapping("/{courseSlug}/current-step")
    public CourseProgressResponse updateCurrentStep(
            @PathVariable String courseSlug,
            @Valid @RequestBody UpdateCourseProgressRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.updateCurrentStep(courseSlug, request, principal);
    }

    @PostMapping("/{courseSlug}/steps/{lessonSlug}/complete")
    public CourseProgressResponse markStepCompleted(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.markStepCompleted(courseSlug, lessonSlug, principal);
    }

    @PostMapping("/{courseSlug}/complete")
    public CourseProgressResponse completeCourseProgress(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.completeCourseProgress(courseSlug, principal);
    }

    @PostMapping("/{courseSlug}/reset")
    public CourseProgressResponse resetCourseProgress(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return progressService.resetCourseProgress(courseSlug, principal);
    }
}
