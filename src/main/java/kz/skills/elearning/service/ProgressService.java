package kz.skills.elearning.service;

import kz.skills.elearning.dto.CourseProgressResponse;
import kz.skills.elearning.dto.CourseProgressStepResponse;
import kz.skills.elearning.dto.UpdateCourseProgressRequest;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseProgress;
import kz.skills.elearning.entity.CourseProgressStep;
import kz.skills.elearning.entity.Enrollment;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.ProgressStatus;
import kz.skills.elearning.entity.ProgressStepStatus;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseProgressRepository;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class ProgressService {

    private static final Comparator<Lesson> LESSON_ORDER =
            Comparator.comparing(Lesson::getPosition).thenComparing(Lesson::getId);

    private static final Comparator<CourseProgressStep> STEP_ORDER =
            Comparator.comparing(CourseProgressStep::getStepOrder).thenComparing(step -> step.getLesson().getId());

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final PlatformUserRepository platformUserRepository;

    public ProgressService(CourseRepository courseRepository,
                           EnrollmentRepository enrollmentRepository,
                           CourseProgressRepository courseProgressRepository,
                           PlatformUserRepository platformUserRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.platformUserRepository = platformUserRepository;
    }

    public CourseProgressResponse getCourseProgress(String courseSlug, PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, false);
        synchronizeProgress(context.progress(), context.lessons());
        return toResponse(context.progress(), context.course(), principal.getId());
    }

    public CourseProgressResponse startCourseProgress(String courseSlug, PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, true);
        CourseProgress progress = context.progress();
        synchronizeProgress(progress, context.lessons());

        if (progress.getStatus() != ProgressStatus.COMPLETED) {
            beginAttemptIfNeeded(progress);
            if (progress.getCurrentLesson() == null) {
                progress.setCurrentLesson(findNextIncompleteLesson(progress));
            }
            updatePercentComplete(progress);
        }

        return toResponse(progress, context.course(), principal.getId());
    }

    public CourseProgressResponse updateCurrentStep(String courseSlug,
                                                    UpdateCourseProgressRequest request,
                                                    PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, true);
        CourseProgress progress = context.progress();
        synchronizeProgress(progress, context.lessons());

        if (progress.getStatus() != ProgressStatus.COMPLETED) {
            beginAttemptIfNeeded(progress);
            Lesson lesson = findLesson(context.course(), request.lessonSlug(), courseSlug);
            progress.setCurrentLesson(lesson);
            if (progress.getCompletedSteps() < progress.getTotalSteps()) {
                progress.setStatus(ProgressStatus.IN_PROGRESS);
            }
            updatePercentComplete(progress);
        }

        return toResponse(progress, context.course(), principal.getId());
    }

    public CourseProgressResponse markStepCompleted(String courseSlug,
                                                    String lessonSlug,
                                                    PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, true);
        CourseProgress progress = context.progress();
        synchronizeProgress(progress, context.lessons());

        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            return toResponse(progress, context.course(), principal.getId());
        }

        beginAttemptIfNeeded(progress);
        Lesson lesson = findLesson(context.course(), lessonSlug, courseSlug);
        CourseProgressStep step = findProgressStep(progress, lesson);
        if (step.getStatus() != ProgressStepStatus.COMPLETED) {
            step.setStatus(ProgressStepStatus.COMPLETED);
            step.setCompletedAt(nowUtc());
        }

        recalculateMetrics(progress);
        if (progress.getCompletedSteps() >= progress.getTotalSteps() && progress.getTotalSteps() > 0) {
            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setCompletedAt(progress.getCompletedAt() == null ? nowUtc() : progress.getCompletedAt());
            progress.setCurrentLesson(null);
        } else {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
            progress.setCompletedAt(null);
            if (progress.getCurrentLesson() == null
                    || Objects.equals(progress.getCurrentLesson().getId(), lesson.getId())) {
                progress.setCurrentLesson(findNextIncompleteLesson(progress));
            }
        }
        updatePercentComplete(progress);

        return toResponse(progress, context.course(), principal.getId());
    }

    public CourseProgressResponse completeCourseProgress(String courseSlug, PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, true);
        CourseProgress progress = context.progress();
        synchronizeProgress(progress, context.lessons());

        boolean hasIncompleteSteps = progress.getSteps().stream()
                .anyMatch(s -> s.getStatus() != ProgressStepStatus.COMPLETED);
        if (hasIncompleteSteps) {
            throw new BadRequestException("Cannot mark course as complete: not all lessons have been completed");
        }

        if (progress.getStatus() != ProgressStatus.COMPLETED) {
            beginAttemptIfNeeded(progress);
            LocalDateTime completedAt = nowUtc();
            recalculateMetrics(progress);
            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setCompletedAt(completedAt);
            progress.setCurrentLesson(null);
            updatePercentComplete(progress);
        }

        return toResponse(progress, context.course(), principal.getId());
    }

    public CourseProgressResponse resetCourseProgress(String courseSlug, PlatformUserPrincipal principal) {
        ProgressContext context = loadProgressContext(courseSlug, principal, true);
        CourseProgress progress = context.progress();
        synchronizeProgress(progress, context.lessons());

        for (CourseProgressStep step : progress.getSteps()) {
            step.setStatus(ProgressStepStatus.NOT_STARTED);
            step.setCompletedAt(null);
        }

        progress.setStatus(ProgressStatus.RESET);
        progress.setStartedAt(null);
        progress.setCompletedAt(null);
        progress.setCurrentLesson(null);
        progress.setResetAt(nowUtc());

        recalculateMetrics(progress);
        updatePercentComplete(progress);

        return toResponse(progress, context.course(), principal.getId());
    }

    public void initializeProgressForEnrollment(Enrollment enrollment) {
        if (courseProgressRepository.existsByCourse_IdAndStudent_Id(
                enrollment.getCourse().getId(),
                enrollment.getStudent().getId()
        )) {
            return;
        }

        try {
            courseProgressRepository.save(buildNewProgress(enrollment.getCourse(), enrollment.getStudent()));
        } catch (DataIntegrityViolationException ignored) {
            // Another transaction initialized the same progress row concurrently.
        }
    }

    private ProgressContext loadProgressContext(String courseSlug,
                                                PlatformUserPrincipal principal,
                                                boolean forUpdate) {
        PlatformUserPrincipal authenticatedPrincipal = requireAuthenticatedPrincipal(principal);
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseSlug));

        if (!enrollmentRepository.existsByCourse_IdAndStudent_Id(course.getId(), authenticatedPrincipal.getId())) {
            throw new BadRequestException("User is not enrolled in course: " + courseSlug);
        }

        CourseProgress progress = forUpdate
                ? courseProgressRepository.findForUpdateByStudentIdAndCourseId(authenticatedPrincipal.getId(), course.getId())
                .orElseGet(() -> createProgress(course, authenticatedPrincipal.getId()))
                : courseProgressRepository.findDetailedByStudentIdAndCourseSlug(authenticatedPrincipal.getId(), courseSlug)
                .orElseGet(() -> createProgress(course, authenticatedPrincipal.getId()));

        return new ProgressContext(course, progress, sortLessons(course.getLessons()));
    }

    private CourseProgress createProgress(Course course, Long studentId) {
        PlatformUser student = platformUserRepository.getReferenceById(studentId);
        try {
            return courseProgressRepository.save(buildNewProgress(course, student));
        } catch (DataIntegrityViolationException ex) {
            return courseProgressRepository.findForUpdateByStudentIdAndCourseId(studentId, course.getId())
                    .orElseGet(() -> courseProgressRepository.findDetailedByStudentIdAndCourseSlug(studentId, course.getSlug())
                            .orElseThrow(() -> ex));
        }
    }

    private CourseProgress buildNewProgress(Course course, PlatformUser student) {
        CourseProgress progress = new CourseProgress();
        progress.setCourse(course);
        progress.setStudent(student);
        progress.setStatus(ProgressStatus.NOT_STARTED);
        progress.setAttemptCount(0);
        progress.setPercentComplete(0);
        progress.setCompletedSteps(0);
        progress.setTotalSteps(0);

        for (Lesson lesson : sortLessons(course.getLessons())) {
            CourseProgressStep step = new CourseProgressStep();
            step.setLesson(lesson);
            step.setStepOrder(lesson.getPosition());
            step.setStatus(ProgressStepStatus.NOT_STARTED);
            progress.addStep(step);
        }

        recalculateMetrics(progress);
        updatePercentComplete(progress);
        return progress;
    }

    private void synchronizeProgress(CourseProgress progress, List<Lesson> lessons) {
        syncStepStructure(progress, lessons);
        recalculateMetrics(progress);

        if (progress.getCurrentLesson() != null && lessons.stream()
                .noneMatch(lesson -> Objects.equals(lesson.getId(), progress.getCurrentLesson().getId()))) {
            progress.setCurrentLesson(null);
        }

        if (progress.getTotalSteps() > 0 && progress.getCompletedSteps() >= progress.getTotalSteps()) {
            progress.setStatus(ProgressStatus.COMPLETED);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(nowUtc());
            }
            progress.setCurrentLesson(null);
        } else if (progress.getStatus() == ProgressStatus.COMPLETED && progress.getCompletedSteps() < progress.getTotalSteps()) {
            progress.setStatus(progress.getCompletedSteps() > 0 || progress.getStartedAt() != null
                    ? ProgressStatus.IN_PROGRESS
                    : ProgressStatus.NOT_STARTED);
            progress.setCompletedAt(null);
        } else if (progress.getCompletedSteps() > 0
                && (progress.getStatus() == ProgressStatus.NOT_STARTED || progress.getStatus() == ProgressStatus.RESET)) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }

        if (progress.getStatus() == ProgressStatus.IN_PROGRESS && progress.getCurrentLesson() == null) {
            progress.setCurrentLesson(findNextIncompleteLesson(progress));
        }

        if ((progress.getStatus() == ProgressStatus.NOT_STARTED || progress.getStatus() == ProgressStatus.RESET)
                && progress.getCompletedSteps() == 0) {
            progress.setCurrentLesson(null);
            progress.setCompletedAt(null);
        }

        updatePercentComplete(progress);
    }

    private void syncStepStructure(CourseProgress progress, List<Lesson> lessons) {
        Set<Long> lessonIds = new HashSet<>();
        for (Lesson lesson : lessons) {
            lessonIds.add(lesson.getId());
        }

        List<CourseProgressStep> obsoleteSteps = progress.getSteps().stream()
                .filter(step -> !lessonIds.contains(step.getLesson().getId()))
                .toList();

        for (CourseProgressStep obsoleteStep : obsoleteSteps) {
            if (progress.getCurrentLesson() != null
                    && Objects.equals(progress.getCurrentLesson().getId(), obsoleteStep.getLesson().getId())) {
                progress.setCurrentLesson(null);
            }
            progress.removeStep(obsoleteStep);
        }

        for (Lesson lesson : lessons) {
            CourseProgressStep step = progress.getSteps().stream()
                    .filter(existing -> Objects.equals(existing.getLesson().getId(), lesson.getId()))
                    .findFirst()
                    .orElse(null);
            if (step == null) {
                CourseProgressStep newStep = new CourseProgressStep();
                newStep.setLesson(lesson);
                newStep.setStepOrder(lesson.getPosition());
                newStep.setStatus(ProgressStepStatus.NOT_STARTED);
                progress.addStep(newStep);
                continue;
            }
            if (!Objects.equals(step.getStepOrder(), lesson.getPosition())) {
                step.setStepOrder(lesson.getPosition());
            }
        }
    }

    private void beginAttemptIfNeeded(CourseProgress progress) {
        if (progress.getStatus() == ProgressStatus.NOT_STARTED || progress.getStatus() == ProgressStatus.RESET) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
            progress.setStartedAt(nowUtc());
            progress.setCompletedAt(null);
            progress.setAttemptCount(progress.getAttemptCount() + 1);
        }
    }

    private Lesson findLesson(Course course, String lessonSlug, String courseSlug) {
        String normalizedLessonSlug = normalizeSlug(lessonSlug);
        return course.getLessons().stream()
                .filter(lesson -> normalizedLessonSlug.equals(normalizeSlug(lesson.getSlug())))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found: " + lessonSlug + " for course: " + courseSlug));
    }

    private CourseProgressStep findProgressStep(CourseProgress progress, Lesson lesson) {
        return progress.getSteps().stream()
                .filter(step -> Objects.equals(step.getLesson().getId(), lesson.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress step not found for lesson: " + lesson.getSlug()));
    }

    private Lesson findNextIncompleteLesson(CourseProgress progress) {
        return progress.getSteps().stream()
                .sorted(STEP_ORDER)
                .filter(step -> step.getStatus() != ProgressStepStatus.COMPLETED)
                .map(CourseProgressStep::getLesson)
                .findFirst()
                .orElse(null);
    }

    private void recalculateMetrics(CourseProgress progress) {
        int totalSteps = progress.getSteps().size();
        int completedSteps = (int) progress.getSteps().stream()
                .filter(step -> step.getStatus() == ProgressStepStatus.COMPLETED)
                .count();

        progress.setTotalSteps(totalSteps);
        progress.setCompletedSteps(completedSteps);
    }

    private void updatePercentComplete(CourseProgress progress) {
        if (progress.getTotalSteps() == 0) {
            progress.setPercentComplete(progress.getStatus() == ProgressStatus.COMPLETED ? 100 : 0);
            return;
        }

        int percent = (int) Math.round((progress.getCompletedSteps() * 100.0d) / progress.getTotalSteps());
        progress.setPercentComplete(Math.min(100, Math.max(0, percent)));
    }

    private CourseProgressResponse toResponse(CourseProgress progress, Course course, Long userId) {
        List<CourseProgressStepResponse> steps = progress.getSteps().stream()
                .sorted(STEP_ORDER)
                .map(step -> new CourseProgressStepResponse(
                        step.getLesson().getSlug(),
                        step.getLesson().getTitle(),
                        step.getStepOrder(),
                        step.getStatus().name(),
                        step.getCompletedAt()
                ))
                .toList();

        return new CourseProgressResponse(
                userId,
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                progress.getStatus().name(),
                progress.getCurrentLesson() == null ? null : progress.getCurrentLesson().getSlug(),
                progress.getCompletedSteps(),
                progress.getTotalSteps(),
                progress.getPercentComplete(),
                progress.getAttemptCount(),
                progress.getStartedAt(),
                progress.getUpdatedAt(),
                progress.getCompletedAt(),
                progress.getResetAt(),
                steps
        );
    }

    private PlatformUserPrincipal requireAuthenticatedPrincipal(PlatformUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal;
    }

    private List<Lesson> sortLessons(List<Lesson> lessons) {
        return lessons.stream()
                .sorted(LESSON_ORDER)
                .toList();
    }

    private String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private record ProgressContext(Course course, CourseProgress progress, List<Lesson> lessons) {
    }
}
