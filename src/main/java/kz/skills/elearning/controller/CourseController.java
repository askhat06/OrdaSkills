package kz.skills.elearning.controller;

import kz.skills.elearning.dto.CourseLandingResponse;
import kz.skills.elearning.dto.CourseSummaryResponse;
import kz.skills.elearning.dto.LessonViewerResponse;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseSummaryResponse> getCatalog() {
        return courseService.getCatalog();
    }

    /**
     * Course landing page.
     *
     * <p>Principal is optional (route is {@code permitAll}).
     * Enrolled users can view their course even when it is not PUBLISHED.
     */
    @GetMapping("/{slug}")
    public CourseLandingResponse getCourseLanding(
            @PathVariable String slug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return courseService.getCourseLanding(slug, principal);
    }

    /**
     * Lesson viewer.
     *
     * <p>Principal is optional (route is {@code permitAll}).
     * Enrolled users retain access even when the course is not PUBLISHED.
     */
    @GetMapping("/{courseSlug}/lessons/{lessonSlug}")
    public LessonViewerResponse getLesson(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal
    ) {
        return courseService.getLessonViewer(courseSlug, lessonSlug, principal);
    }
}
