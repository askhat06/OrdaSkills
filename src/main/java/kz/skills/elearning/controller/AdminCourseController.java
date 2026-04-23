package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.AdminCourseRequest;
import kz.skills.elearning.dto.AdminCourseResponse;
import kz.skills.elearning.dto.AdminModerationRequest;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.AdminCourseService;
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
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    public AdminCourseController(AdminCourseService adminCourseService) {
        this.adminCourseService = adminCourseService;
    }

    @GetMapping
    public List<AdminCourseResponse> getCourses() {
        return adminCourseService.getCourses();
    }

    /** Moderation queue — courses waiting for admin review. */
    @GetMapping("/pending")
    public List<AdminCourseResponse> getPendingCourses() {
        return adminCourseService.getPendingCourses();
    }

    @GetMapping("/{courseId}")
    public AdminCourseResponse getCourse(@PathVariable Long courseId) {
        return adminCourseService.getCourse(courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCourseResponse createCourse(@Valid @RequestBody AdminCourseRequest request,
                                            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return adminCourseService.createCourse(request, principal);
    }

    @PutMapping("/{courseId}")
    public AdminCourseResponse updateCourse(@PathVariable Long courseId,
                                            @Valid @RequestBody AdminCourseRequest request,
                                            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return adminCourseService.updateCourse(courseId, request, principal);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long courseId,
                             @AuthenticationPrincipal PlatformUserPrincipal principal) {
        adminCourseService.deleteCourse(courseId, principal);
    }

    /**
     * Approve a course in PENDING_REVIEW → PUBLISHED.
     * Returns 400 if the course is not in PENDING_REVIEW.
     */
    @PostMapping("/{courseId}/publish")
    public AdminCourseResponse publishCourse(@PathVariable Long courseId,
                                             @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return adminCourseService.publishCourse(courseId, principal);
    }

    /**
     * Reject a course in PENDING_REVIEW → REJECTED.
     * Returns 400 if the course is not in PENDING_REVIEW.
     * The reason field is mandatory and will be shown to the teacher.
     */
    @PostMapping("/{courseId}/reject")
    public AdminCourseResponse rejectCourse(@PathVariable Long courseId,
                                            @Valid @RequestBody AdminModerationRequest request,
                                            @AuthenticationPrincipal PlatformUserPrincipal principal) {
        return adminCourseService.rejectCourse(courseId, request, principal);
    }
}
