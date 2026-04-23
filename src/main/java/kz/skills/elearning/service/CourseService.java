package kz.skills.elearning.service;

import kz.skills.elearning.dto.CourseLandingResponse;
import kz.skills.elearning.dto.CourseSummaryResponse;
import kz.skills.elearning.dto.LessonOutlineResponse;
import kz.skills.elearning.dto.LessonViewerResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRatingRepository;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.LessonRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.video.VideoStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private static final Duration VIDEO_PRESIGN_VALIDITY = Duration.ofMinutes(30);

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final VideoStorageService videoStorageService;

    public CourseService(CourseRepository courseRepository,
                         LessonRepository lessonRepository,
                         EnrollmentRepository enrollmentRepository,
                         CourseRatingRepository courseRatingRepository,
                         VideoStorageService videoStorageService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.videoStorageService = videoStorageService;
    }

    /**
     * Public course catalog — only PUBLISHED courses are visible.
     */
    public List<CourseSummaryResponse> getCatalog() {
        return courseRepository.findByStatusOrderByCreatedAtDesc(CourseStatus.PUBLISHED)
                .stream()
                .map(course -> new CourseSummaryResponse(
                        course.getId(),
                        course.getSlug(),
                        course.getTitle(),
                        course.getSubtitle(),
                        course.getLocale(),
                        course.getLevel(),
                        course.getDurationHours(),
                        course.getLessons().size(),
                        course.getInstructorName(),
                        enrollmentRepository.countByCourse_Id(course.getId()),
                        course.getPrice(),
                        courseRatingRepository.findAverageRatingByCourseId(course.getId()),
                        courseRatingRepository.countByCourse_Id(course.getId())
                ))
                .toList();
    }

    /**
     * Course landing page.
     *
     * <p>Visibility rules:
     * <ul>
     *   <li>PUBLISHED course → visible to everyone, no auth required.</li>
     *   <li>Non-published course + user is enrolled → visible (protects users who enrolled
     *       before the course was rejected or unpublished).</li>
     *   <li>Non-published course + unauthenticated or not enrolled → 404.
     *       We return 404 (not 403) to avoid leaking that a draft exists.</li>
     * </ul>
     */
    public CourseLandingResponse getCourseLanding(String slug, PlatformUserPrincipal principal) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + slug));

        requireVisible(course, principal, slug);

        List<LessonOutlineResponse> lessonResponses = course.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getPosition))
                .map(this::toLessonOutline)
                .toList();

        return new CourseLandingResponse(
                course.getSlug(),
                course.getTitle(),
                course.getSubtitle(),
                course.getDescription(),
                course.getLocale(),
                course.getInstructorName(),
                course.getLevel(),
                course.getDurationHours(),
                lessonResponses
        );
    }

    /**
     * Lesson viewer.
     *
     * <p>Applies the same visibility rules as {@link #getCourseLanding}: a student
     * who is enrolled can always view lessons regardless of the course's current status.
     * This prevents a situation where a course is temporarily rejected and enrolled
     * students suddenly lose access to content they were already studying.
     */
    public LessonViewerResponse getLessonViewer(String courseSlug,
                                                String lessonSlug,
                                                PlatformUserPrincipal principal) {
        Lesson lesson = lessonRepository.findByCourse_SlugAndSlug(courseSlug, lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found: " + lessonSlug + " for course: " + courseSlug));

        requireVisible(lesson.getCourse(), principal, courseSlug);

        String videoUrl = resolveVideoUrl(lesson);
        return new LessonViewerResponse(
                lesson.getCourse().getSlug(),
                lesson.getCourse().getTitle(),
                lesson.getSlug(),
                lesson.getTitle(),
                lesson.getPosition(),
                lesson.getDurationMinutes(),
                videoUrl,
                lesson.getContent()
        );
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Enforces course visibility for the public-facing endpoints.
     *
     * <p>PUBLISHED → always accessible.
     * Non-published → accessible only if the caller is authenticated AND enrolled.
     * Otherwise throws {@link ResourceNotFoundException} (→ 404) to avoid
     * leaking the existence of unpublished content.
     */
    private void requireVisible(Course course, PlatformUserPrincipal principal, String slug) {
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return;
        }
        if (principal != null
                && enrollmentRepository.existsByCourse_IdAndStudent_Id(
                        course.getId(), principal.getId())) {
            return;
        }
        // Return 404, not 403 — do not reveal that this slug exists but is unpublished.
        throw new ResourceNotFoundException("Course not found: " + slug);
    }

    /**
     * When a lesson has an S3 storage key, generate a short-lived presigned GET URL
     * so clients stream directly from storage without the bucket being publicly readable.
     * Falls back to the stored videoUrl for externally-hosted videos (e.g., YouTube).
     */
    private String resolveVideoUrl(Lesson lesson) {
        if (lesson.getVideoStorageKey() != null) {
            return videoStorageService.generatePresignedGetUrl(lesson.getVideoStorageKey(), VIDEO_PRESIGN_VALIDITY);
        }
        return lesson.getVideoUrl();
    }

    private LessonOutlineResponse toLessonOutline(Lesson lesson) {
        return new LessonOutlineResponse(
                lesson.getSlug(),
                lesson.getTitle(),
                lesson.getPosition(),
                lesson.getDurationMinutes(),
                lesson.getSummary()
        );
    }
}
