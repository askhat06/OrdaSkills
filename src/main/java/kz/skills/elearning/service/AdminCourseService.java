package kz.skills.elearning.service;

import kz.skills.elearning.dto.AdminCourseRequest;
import kz.skills.elearning.dto.AdminCourseResponse;
import kz.skills.elearning.dto.AdminModerationRequest;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.ConflictException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Admin course management service.
 *
 * <p><strong>Technical debt note:</strong> Admin mutations (create, update, publish) take effect
 * immediately with no audit trail, version history, or rollback mechanism. This is intentional
 * for the MVP but should be addressed before the platform scales — consider an event log or
 * optimistic-locking revision table.
 */
@Service
@Transactional
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AdminCourseService(CourseRepository courseRepository,
                              EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCourseResponse> getCourses() {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCourseResponse> getPendingCourses() {
        return courseRepository.findByStatusOrderByCreatedAtDesc(CourseStatus.PENDING_REVIEW)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCourseResponse getCourse(Long courseId) {
        return toResponse(findCourse(courseId));
    }

    /**
     * Admin-created courses are published immediately — admins do not self-moderate.
     */
    public AdminCourseResponse createCourse(AdminCourseRequest request) {
        String normalizedSlug = normalizeSlug(request.slug());
        ensureSlugAvailable(normalizedSlug, null);

        Course course = new Course();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setOwner(null);
        apply(course, request, normalizedSlug);
        return toResponse(courseRepository.save(course));
    }

    public AdminCourseResponse updateCourse(Long courseId, AdminCourseRequest request) {
        Course course = findCourse(courseId);
        String normalizedSlug = normalizeSlug(request.slug());
        ensureSlugAvailable(normalizedSlug, courseId);

        apply(course, request, normalizedSlug);
        return toResponse(courseRepository.save(course));
    }

    public void deleteCourse(Long courseId) {
        Course course = findCourse(courseId);
        if (enrollmentRepository.existsByCourse_Id(courseId)) {
            throw new ConflictException("Cannot delete a course that already has enrollments");
        }
        courseRepository.delete(course);
    }

    /**
     * Moves a course from {@code PENDING_REVIEW} to {@code PUBLISHED}.
     * Only pending courses can be published — attempting to publish an already-published
     * or rejected course returns 400 to catch accidental double-approvals.
     */
    public AdminCourseResponse publishCourse(Long courseId) {
        Course course = findCourse(courseId);
        if (course.getStatus() != CourseStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Only PENDING_REVIEW courses can be published. Current status: " + course.getStatus());
        }
        course.setStatus(CourseStatus.PUBLISHED);
        course.setRejectionReason(null);
        return toResponse(courseRepository.save(course));
    }

    /**
     * Moves a course from {@code PENDING_REVIEW} to {@code REJECTED} and records the reason.
     * The teacher will see the reason and can edit then resubmit.
     */
    public AdminCourseResponse rejectCourse(Long courseId, AdminModerationRequest request) {
        Course course = findCourse(courseId);
        if (course.getStatus() != CourseStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Only PENDING_REVIEW courses can be rejected. Current status: " + course.getStatus());
        }
        course.setStatus(CourseStatus.REJECTED);
        course.setRejectionReason(request.reason());
        return toResponse(courseRepository.save(course));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private void ensureSlugAvailable(String slug, Long currentCourseId) {
        boolean slugTaken = courseRepository.existsBySlugIgnoreCase(slug);
        if (!slugTaken) {
            return;
        }
        if (currentCourseId != null) {
            Course existing = courseRepository.findById(currentCourseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + currentCourseId));
            if (slug.equalsIgnoreCase(existing.getSlug())) {
                return;
            }
        }
        throw new ConflictException("Course slug already exists: " + slug);
    }

    private void apply(Course course, AdminCourseRequest request, String normalizedSlug) {
        course.setSlug(normalizedSlug);
        course.setTitle(request.title().trim());
        course.setSubtitle(trimToNull(request.subtitle()));
        course.setDescription(request.description().trim());
        course.setLocale(request.locale().trim().toLowerCase(Locale.ROOT));
        course.setInstructorName(trimToNull(request.instructorName()));
        course.setLevel(trimToNull(request.level()));
        course.setDurationHours(request.durationHours());
    }

    private AdminCourseResponse toResponse(Course course) {
        return new AdminCourseResponse(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getSubtitle(),
                course.getDescription(),
                course.getLocale(),
                course.getInstructorName(),
                course.getLevel(),
                course.getDurationHours(),
                course.getLessons().size(),
                course.getStatus().name(),
                course.getOwner() != null ? course.getOwner().getEmail() : null
        );
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
