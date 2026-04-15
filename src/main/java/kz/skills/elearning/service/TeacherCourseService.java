package kz.skills.elearning.service;

import kz.skills.elearning.dto.TeacherCourseRequest;
import kz.skills.elearning.dto.TeacherCourseResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.ConflictException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.util.SlugUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TeacherCourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PlatformUserRepository platformUserRepository;
    private final CourseOwnershipGuard guard;

    public TeacherCourseService(CourseRepository courseRepository,
                                EnrollmentRepository enrollmentRepository,
                                PlatformUserRepository platformUserRepository,
                                CourseOwnershipGuard guard) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.platformUserRepository = platformUserRepository;
        this.guard = guard;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TeacherCourseResponse> getMyCourses(PlatformUserPrincipal principal) {
        return courseRepository.findByOwner_IdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherCourseResponse getMyCourse(String courseSlug, PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwner(course, principal);
        return toResponse(course);
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    /**
     * Creates a new course in {@code DRAFT} status owned by the calling teacher.
     *
     * <p>Slug resolution order:
     * <ol>
     *   <li>Use the request slug if provided.</li>
     *   <li>Otherwise auto-generate from {@code title}.</li>
     *   <li>Append {@code -N} suffix (N = 1..99) if the candidate slug is taken.</li>
     *   <li>Fall back to a UUID suffix to guarantee uniqueness.</li>
     * </ol>
     *
     * <p>The DB unique constraint on {@code (slug)} is the final safety net for
     * concurrent requests that pass all checks above simultaneously.
     */
    public TeacherCourseResponse createCourse(TeacherCourseRequest request,
                                              PlatformUserPrincipal principal) {
        String baseSlug = (request.slug() != null && !request.slug().isBlank())
                ? request.slug().trim().toLowerCase(Locale.ROOT)
                : SlugUtils.toSlug(request.title());

        String resolvedSlug = resolveUniqueSlug(baseSlug);

        PlatformUser owner = platformUserRepository.getReferenceById(principal.getId());

        Course course = new Course();
        course.setOwner(owner);
        course.setStatus(CourseStatus.DRAFT);
        course.setRejectionReason(null);
        course.setInstructorName(principal.getFullName());
        apply(course, request, resolvedSlug);

        try {
            return toResponse(courseRepository.save(course));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Course slug already exists: " + resolvedSlug);
        }
    }

    /**
     * Updates course metadata. Only allowed when the course is in {@code DRAFT} or
     * {@code REJECTED} status — a course under review is locked.
     */
    public TeacherCourseResponse updateCourse(String courseSlug,
                                              TeacherCourseRequest request,
                                              PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwnerAndEditable(course, principal);

        String newSlug = (request.slug() != null && !request.slug().isBlank())
                ? request.slug().trim().toLowerCase(Locale.ROOT)
                : course.getSlug();

        if (!newSlug.equals(course.getSlug())) {
            if (courseRepository.existsBySlugIgnoreCase(newSlug)) {
                throw new ConflictException("Course slug already exists: " + newSlug);
            }
        }

        apply(course, request, newSlug);
        return toResponse(courseRepository.save(course));
    }

    /**
     * Deletes a course. Only allowed when the course is in {@code DRAFT} status and
     * has no enrollments (there can be none for a draft, but we guard defensively).
     */
    public void deleteCourse(String courseSlug, PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwner(course, principal);

        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new BadRequestException(
                    "Only DRAFT courses can be deleted. Current status: " + course.getStatus()
                    + ". Withdraw the course first if it is under review.");
        }
        if (enrollmentRepository.existsByCourse_Id(course.getId())) {
            throw new ConflictException("Cannot delete a course that already has enrollments");
        }
        courseRepository.delete(course);
    }

    /**
     * Submits a course for admin review: {@code DRAFT} or {@code REJECTED} → {@code PENDING_REVIEW}.
     */
    public TeacherCourseResponse submitForReview(String courseSlug, PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwner(course, principal);

        if (course.getStatus() != CourseStatus.DRAFT
                && course.getStatus() != CourseStatus.REJECTED) {
            throw new BadRequestException(
                    "Only DRAFT or REJECTED courses can be submitted for review. "
                    + "Current status: " + course.getStatus());
        }
        if (course.getLessons().isEmpty()) {
            throw new BadRequestException(
                    "Cannot submit a course with no lessons. Add at least one lesson before submitting.");
        }

        course.setStatus(CourseStatus.PENDING_REVIEW);
        course.setRejectionReason(null);
        return toResponse(courseRepository.save(course));
    }

    /**
     * Withdraws a course from admin review: {@code PENDING_REVIEW} → {@code DRAFT}.
     * Allows a teacher to make changes after submission but before the admin acts.
     */
    public TeacherCourseResponse withdrawFromReview(String courseSlug, PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwner(course, principal);

        if (course.getStatus() != CourseStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Only PENDING_REVIEW courses can be withdrawn. "
                    + "Current status: " + course.getStatus());
        }
        course.setStatus(CourseStatus.DRAFT);
        return toResponse(courseRepository.save(course));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Course findCourse(String courseSlug) {
        return courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseSlug));
    }

    private void apply(Course course, TeacherCourseRequest request, String resolvedSlug) {
        course.setSlug(resolvedSlug);
        course.setTitle(request.title().trim());
        course.setSubtitle(trimToNull(request.subtitle()));
        course.setDescription(request.description().trim());
        course.setLocale(request.locale().trim().toLowerCase(Locale.ROOT));
        course.setLevel(trimToNull(request.level()));
        course.setDurationHours(request.durationHours());
        // instructorName is set from the owner's account on create (see createCourse).
        // On update we do NOT touch it so that a rename of the teacher's account
        // does not silently change in-flight courses.
    }

    /**
     * Finds a unique slug by appending a numeric suffix when the base is already taken.
     * Concurrent collision after this check is caught by the DB unique constraint (→ 409).
     */
    private String resolveUniqueSlug(String base) {
        if (!courseRepository.existsBySlugIgnoreCase(base)) {
            return base;
        }
        for (int i = 1; i <= 99; i++) {
            String candidate = base + "-" + i;
            if (!courseRepository.existsBySlugIgnoreCase(candidate)) {
                return candidate;
            }
        }
        // Extremely unlikely; still guarantees no infinite loop
        return base + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private TeacherCourseResponse toResponse(Course course) {
        return new TeacherCourseResponse(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getSubtitle(),
                course.getDescription(),
                course.getLocale(),
                course.getLevel(),
                course.getDurationHours(),
                course.getStatus().name(),
                course.getRejectionReason(),
                course.getLessons().size(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
