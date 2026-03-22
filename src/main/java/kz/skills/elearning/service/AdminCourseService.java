package kz.skills.elearning.service;

import kz.skills.elearning.dto.AdminCourseRequest;
import kz.skills.elearning.dto.AdminCourseResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.exception.ConflictException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AdminCourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
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
    public AdminCourseResponse getCourse(Long courseId) {
        return toResponse(findCourse(courseId));
    }

    public AdminCourseResponse createCourse(AdminCourseRequest request) {
        String normalizedSlug = normalizeSlug(request.slug());
        ensureSlugAvailable(normalizedSlug, null);

        Course course = new Course();
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
                course.getLessons().size()
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
