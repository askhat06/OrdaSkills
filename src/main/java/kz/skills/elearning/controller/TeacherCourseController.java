package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.TeacherCourseRequest;
import kz.skills.elearning.dto.TeacherCourseResponse;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.TeacherCourseService;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    public TeacherCourseController(TeacherCourseService teacherCourseService) {
        this.teacherCourseService = teacherCourseService;
    }

    /** List all courses owned by the calling teacher (all statuses). */
    @GetMapping
    public List<TeacherCourseResponse> getMyCourses(
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.getMyCourses(principal);
    }

    /** Get a single owned course by slug. Returns 403 if the teacher doesn't own it. */
    @GetMapping("/{courseSlug}")
    public TeacherCourseResponse getMyCourse(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.getMyCourse(courseSlug, principal);
    }

    /** Create a new course in DRAFT status. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherCourseResponse createCourse(
            @Valid @RequestBody TeacherCourseRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.createCourse(request, principal);
    }

    /** Update course metadata. Only allowed in DRAFT or REJECTED status. */
    @PutMapping("/{courseSlug}")
    public TeacherCourseResponse updateCourse(
            @PathVariable String courseSlug,
            @Valid @RequestBody TeacherCourseRequest request,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.updateCourse(courseSlug, request, principal);
    }

    /** Delete a DRAFT course with no enrollments. */
    @DeleteMapping("/{courseSlug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        teacherCourseService.deleteCourse(courseSlug, principal);
    }

    /** Submit course for admin review: DRAFT or REJECTED → PENDING_REVIEW. */
    @PostMapping("/{courseSlug}/submit")
    public TeacherCourseResponse submitForReview(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.submitForReview(courseSlug, principal);
    }

    /** Withdraw from review before admin acts: PENDING_REVIEW → DRAFT. */
    @PostMapping("/{courseSlug}/withdraw")
    public TeacherCourseResponse withdrawFromReview(
            @PathVariable String courseSlug,
            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return teacherCourseService.withdrawFromReview(courseSlug, principal);
    }
}
