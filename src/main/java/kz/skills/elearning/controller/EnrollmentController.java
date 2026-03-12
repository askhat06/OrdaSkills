package kz.skills.elearning.controller;

import jakarta.validation.Valid;
import kz.skills.elearning.dto.EnrollmentRequest;
import kz.skills.elearning.dto.EnrollmentResponse;
import kz.skills.elearning.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest request) {
        return enrollmentService.enroll(request);
    }

    @GetMapping
    public List<EnrollmentResponse> getEnrollments(
            @RequestParam(required = false) String courseSlug,
            @RequestParam(required = false) String email
    ) {
        return enrollmentService.getEnrollments(courseSlug, email);
    }
}
