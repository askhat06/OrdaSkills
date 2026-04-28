package kz.skills.elearning.service;

import kz.skills.elearning.dto.TeacherLessonRequest;
import kz.skills.elearning.dto.TeacherLessonResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.exception.ConflictException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.LessonRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.util.SlugUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TeacherLessonService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseOwnershipGuard guard;

    public TeacherLessonService(CourseRepository courseRepository,
                                LessonRepository lessonRepository,
                                CourseOwnershipGuard guard) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.guard = guard;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TeacherLessonResponse> getLessons(String courseSlug,
                                                  PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwner(course, principal);
        return lessonRepository.findByCourse_SlugOrderByPositionAsc(courseSlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    /**
     * Appends a new lesson to the course. Position is always {@code max(existing) + 1}.
     *
     * <p>Slug resolution:
     * <ol>
     *   <li>Use the explicit slug from the request if provided.</li>
     *   <li>Otherwise auto-generate from {@code title}.</li>
     *   <li>Append {@code -N} (N = 1..99) if the candidate is taken within this course.</li>
     * </ol>
     * The DB unique constraint {@code (course_id, slug)} is the final safety net for
     * concurrent inserts.
     */
    public TeacherLessonResponse createLesson(String courseSlug,
                                              TeacherLessonRequest request,
                                              PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwnerAndLessonEditable(course, principal);

        String slug = resolveUniqueSlug(request.slug(), request.title(), course.getId());
        int position = nextPosition(course);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setSlug(slug);
        lesson.setPosition(position);
        apply(lesson, request);

        try {
            return toResponse(lessonRepository.save(lesson));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Lesson slug already exists in this course: " + slug);
        }
    }

    /**
     * Updates lesson metadata.
     * Changing the slug is allowed — the new slug is validated for uniqueness within the course.
     * Position is not changed here; use a dedicated reorder endpoint if needed.
     */
    public TeacherLessonResponse updateLesson(String courseSlug,
                                              String lessonSlug,
                                              TeacherLessonRequest request,
                                              PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwnerAndLessonEditable(course, principal);

        Lesson lesson = findLesson(courseSlug, lessonSlug);

        // Slug update: validate uniqueness only when the slug actually changes
        String newSlug = (request.slug() != null && !request.slug().isBlank())
                ? request.slug().trim().toLowerCase(Locale.ROOT)
                : lesson.getSlug();

        if (!newSlug.equals(lesson.getSlug())) {
            if (lessonRepository.existsByCourse_IdAndSlug(course.getId(), newSlug)) {
                throw new ConflictException(
                        "Lesson slug already exists in this course: " + newSlug);
            }
            lesson.setSlug(newSlug);
        }

        apply(lesson, request);
        return toResponse(lessonRepository.save(lesson));
    }

    /**
     * Deletes a lesson and shifts the positions of all subsequent lessons down by 1,
     * keeping the sequence contiguous (1, 2, 3 … with no gaps).
     *
     * <p>Active {@code CourseProgressStep} rows for this lesson are handled lazily:
     * the next call to {@code ProgressService.synchronizeProgress} for any enrolled
     * student will automatically remove the orphaned step.
     */
    public void deleteLesson(String courseSlug,
                             String lessonSlug,
                             PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwnerAndLessonEditable(course, principal);

        Lesson lesson = findLesson(courseSlug, lessonSlug);
        int deletedPosition = lesson.getPosition();

        lessonRepository.delete(lesson);

        // Shift positions of all lessons that came after the deleted one
        List<Lesson> toShift = lessonRepository
                .findByCourse_IdAndPositionGreaterThanOrderByPositionAsc(
                        course.getId(), deletedPosition);
        for (Lesson l : toShift) {
            l.setPosition(l.getPosition() - 1);
        }
        if (!toShift.isEmpty()) {
            lessonRepository.saveAll(toShift);
        }
    }

    // -------------------------------------------------------------------------
    // Used by TeacherLessonController for video operations: validates ownership
    // so that the video service itself does not need to be aware of ownership rules.
    // -------------------------------------------------------------------------

    /**
     * Validates that the calling teacher owns the course and it is editable.
     * Called by the controller before delegating to AdminLessonVideoService.
     */
    public void requireOwnershipForVideoOps(String courseSlug, PlatformUserPrincipal principal) {
        Course course = findCourse(courseSlug);
        guard.requireOwnerAndLessonEditable(course, principal);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Course findCourse(String courseSlug) {
        return courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseSlug));
    }

    private Lesson findLesson(String courseSlug, String lessonSlug) {
        return lessonRepository.findByCourse_SlugAndSlug(courseSlug, lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found: " + lessonSlug + " for course: " + courseSlug));
    }

    private void apply(Lesson lesson, TeacherLessonRequest request) {
        lesson.setTitle(request.title().trim());
        lesson.setSummary(trimToNull(request.summary()));
        lesson.setContent(trimToNull(request.content()));
        lesson.setVideoUrl(trimToNull(request.videoUrl()));
        lesson.setDurationMinutes(request.durationMinutes());
    }

    /**
     * Returns max(position) + 1 across all current lessons in the course,
     * or 1 if the course has no lessons yet.
     */
    private int nextPosition(Course course) {
        return course.getLessons().stream()
                .mapToInt(Lesson::getPosition)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Finds a unique slug within the given course.
     *
     * <p>The check-then-insert pattern is not atomically race-proof, but the DB unique
     * constraint catches any concurrent collision and surfaces it as a 409.
     */
    private String resolveUniqueSlug(String requestedSlug, String title, Long courseId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? requestedSlug.trim().toLowerCase(Locale.ROOT)
                : SlugUtils.toSlug(title);

        if (!lessonRepository.existsByCourse_IdAndSlug(courseId, base)) {
            return base;
        }
        for (int i = 1; i <= 99; i++) {
            String candidate = base + "-" + i;
            if (!lessonRepository.existsByCourse_IdAndSlug(courseId, candidate)) {
                return candidate;
            }
        }
        return base + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private TeacherLessonResponse toResponse(Lesson lesson) {
        return new TeacherLessonResponse(
                lesson.getId(),
                lesson.getCourse().getSlug(),
                lesson.getSlug(),
                lesson.getTitle(),
                lesson.getSummary(),
                lesson.getContent(),
                lesson.getPosition(),
                lesson.getDurationMinutes(),
                lesson.getVideoUrl(),
                lesson.getVideoStorageKey() != null,
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
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
