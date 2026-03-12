package kz.skills.elearning.controller;

import kz.skills.elearning.dto.CourseLandingResponse;
import kz.skills.elearning.dto.CourseSummaryResponse;
import kz.skills.elearning.dto.LessonViewerResponse;
import kz.skills.elearning.service.CourseService;
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

    @GetMapping("/{slug}")
    public CourseLandingResponse getCourseLanding(@PathVariable String slug) {
        return courseService.getCourseLanding(slug);
    }

    @GetMapping("/{courseSlug}/lessons/{lessonSlug}")
    public LessonViewerResponse getLesson(
            @PathVariable String courseSlug,
            @PathVariable String lessonSlug
    ) {
        return courseService.getLessonViewer(courseSlug, lessonSlug);
    }
}
