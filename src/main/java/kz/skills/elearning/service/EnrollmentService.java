package kz.skills.elearning.service;

import kz.skills.elearning.dto.EnrollmentRequest;
import kz.skills.elearning.dto.EnrollmentResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.Enrollment;
import kz.skills.elearning.entity.EnrollmentStatus;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.exception.DuplicateEnrollmentException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PlatformUserRepository platformUserRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            PlatformUserRepository platformUserRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.platformUserRepository = platformUserRepository;
    }

    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Course course = courseRepository.findBySlug(request.courseSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.courseSlug()));

        String normalizedEmail = normalizeEmail(request.email());
        PlatformUser student = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> createStudent(request, normalizedEmail));

        student.setFullName(request.fullName().trim());
        student.setLocale(request.locale().trim());
        platformUserRepository.save(student);

        if (enrollmentRepository.existsByCourse_IdAndStudent_Id(course.getId(), student.getId())) {
            throw new DuplicateEnrollmentException(
                    "Student already enrolled in course: " + course.getSlug());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    public List<EnrollmentResponse> getEnrollments(String courseSlug, String email) {
        List<Enrollment> enrollments;

        if (courseSlug != null && !courseSlug.isBlank()) {
            enrollments = enrollmentRepository.findByCourse_SlugOrderByEnrolledAtDesc(courseSlug.trim());
        } else if (email != null && !email.isBlank()) {
            enrollments = enrollmentRepository.findByStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(normalizeEmail(email));
        } else {
            enrollments = enrollmentRepository.findAllByOrderByEnrolledAtDesc();
        }

        return enrollments.stream()
                .map(this::toResponse)
                .toList();
    }

    private PlatformUser createStudent(EnrollmentRequest request, String normalizedEmail) {
        PlatformUser user = new PlatformUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setLocale(request.locale().trim());
        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFullName(),
                enrollment.getStudent().getEmail(),
                enrollment.getStudent().getLocale(),
                enrollment.getCourse().getSlug(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus().name(),
                enrollment.getEnrolledAt()
        );
    }
}
