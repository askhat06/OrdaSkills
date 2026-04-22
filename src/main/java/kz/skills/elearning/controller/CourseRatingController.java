package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.RatingRequest;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.CourseRatingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseRatingController {

    private final CourseRatingService courseRatingService;

    public CourseRatingController(CourseRatingService courseRatingService) {
        this.courseRatingService = courseRatingService;
    }

    /**
     * Submit or update a star rating (1–5) for a course.
     * The caller must be authenticated and enrolled in the course.
     */
    @PostMapping("/{slug}/ratings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rateCourse(
            @PathVariable String slug,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        courseRatingService.rateCourse(slug, request.rating(), principal);
    }
}
