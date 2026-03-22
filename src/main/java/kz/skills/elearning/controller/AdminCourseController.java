package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.AdminCourseRequest;
import kz.skills.elearning.dto.AdminCourseResponse;
import kz.skills.elearning.service.AdminCourseService;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{courseId}")
    public AdminCourseResponse getCourse(@PathVariable Long courseId) {
        return adminCourseService.getCourse(courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCourseResponse createCourse(@Valid @RequestBody AdminCourseRequest request) {
        return adminCourseService.createCourse(request);
    }

    @PutMapping("/{courseId}")
    public AdminCourseResponse updateCourse(@PathVariable Long courseId,
                                            @Valid @RequestBody AdminCourseRequest request) {
        return adminCourseService.updateCourse(courseId, request);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long courseId) {
        adminCourseService.deleteCourse(courseId);
    }
}
