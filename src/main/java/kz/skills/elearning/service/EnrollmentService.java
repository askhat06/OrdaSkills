package kz.skills.elearning.service;

import kz.skills.elearning.dto.EnrollmentRequest;
import kz.skills.elearning.dto.EnrollmentResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.entity.Enrollment;
import kz.skills.elearning.entity.EnrollmentStatus;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.DuplicateEnrollmentException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.time.ZoneOffset;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PlatformUserRepository platformUserRepository;
    private final ProgressService progressService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            PlatformUserRepository platformUserRepository,
            ProgressService progressService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.platformUserRepository = platformUserRepository;
        this.progressService = progressService;
    }

    public EnrollmentResponse enrollAuthenticated(String courseSlug, PlatformUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseSlug));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("Enrollment is only allowed for published courses");
        }

        PlatformUser student = platformUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (enrollmentRepository.existsByCourse_IdAndStudent_Id(course.getId(), student.getId())) {
            throw new DuplicateEnrollmentException("Student already enrolled in course: " + course.getSlug());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now(ZoneOffset.UTC));

        Enrollment saved = enrollmentRepository.save(enrollment);
        progressService.initializeProgressForEnrollment(saved);
        return toResponse(saved);
    }

    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Course course = courseRepository.findBySlug(request.courseSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.courseSlug()));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("Enrollment is only allowed for published courses");
        }

        String normalizedEmail = normalizeEmail(request.email());
        PlatformUser student = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        if (student != null && enrollmentRepository.existsByCourse_IdAndStudent_Id(course.getId(), student.getId())) {
            throw new DuplicateEnrollmentException(
                    "Student already enrolled in course: " + course.getSlug());
        }

        if (student == null) {
            student = createStudent(request, normalizedEmail);
        } else if (student.getPasswordHash() == null || student.getPasswordHash().isBlank()) {
            mergeLeadProfile(student, request);
        }

        if (student.getId() == null || student.getPasswordHash() == null || student.getPasswordHash().isBlank()) {
            student = platformUserRepository.save(student);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now(ZoneOffset.UTC));

        Enrollment saved = enrollmentRepository.save(enrollment);
        progressService.initializeProgressForEnrollment(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollments(String courseSlug, String email, PlatformUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (principal.getRole() == UserRole.ADMIN) {
            return getEnrollmentsForAdmin(courseSlug, email);
        }

        String normalizedPrincipalEmail = normalizeEmail(principal.getUsername());
        if (email != null && !email.isBlank() && !normalizedPrincipalEmail.equals(normalizeEmail(email))) {
            throw new AccessDeniedException("Students can only view their own enrollments");
        }

        List<Enrollment> enrollments;
        if (courseSlug != null && !courseSlug.isBlank()) {
            enrollments = enrollmentRepository.findByCourse_SlugAndStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(
                    courseSlug.trim(),
                    normalizedPrincipalEmail
            );
        } else {
            enrollments = enrollmentRepository.findByStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(normalizedPrincipalEmail);
        }

        return enrollments.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<EnrollmentResponse> getEnrollmentsForAdmin(String courseSlug, String email) {
        List<Enrollment> enrollments;

        boolean hasCourseFilter = courseSlug != null && !courseSlug.isBlank();
        boolean hasEmailFilter = email != null && !email.isBlank();

        if (hasCourseFilter && hasEmailFilter) {
            enrollments = enrollmentRepository.findByCourse_SlugAndStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(
                    courseSlug.trim(),
                    normalizeEmail(email)
            );
        } else if (hasCourseFilter) {
            enrollments = enrollmentRepository.findByCourse_SlugOrderByEnrolledAtDesc(courseSlug.trim());
        } else if (hasEmailFilter) {
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
        user.setLead(true);
        return user;
    }

    private void mergeLeadProfile(PlatformUser student, EnrollmentRequest request) {
        if (student.getFullName() == null || student.getFullName().isBlank()) {
            student.setFullName(request.fullName().trim());
        }
        if (student.getLocale() == null || student.getLocale().isBlank()) {
            student.setLocale(request.locale().trim());
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
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
