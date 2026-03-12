package kz.skills.elearning.service;

import kz.skills.elearning.dto.CourseLandingResponse;
import kz.skills.elearning.dto.CourseSummaryResponse;
import kz.skills.elearning.dto.LessonOutlineResponse;
import kz.skills.elearning.dto.LessonViewerResponse;
import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<CourseSummaryResponse> getCatalog() {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(course -> new CourseSummaryResponse(
                        course.getId(),
                        course.getSlug(),
                        course.getTitle(),
                        course.getSubtitle(),
                        course.getLocale(),
                        course.getLevel(),
                        course.getDurationHours(),
                        course.getLessons().size()
                ))
                .toList();
    }

    public CourseLandingResponse getCourseLanding(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + slug));

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

    public LessonViewerResponse getLessonViewer(String courseSlug, String lessonSlug) {
        Lesson lesson = lessonRepository.findByCourse_SlugAndSlug(courseSlug, lessonSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found: " + lessonSlug + " for course: " + courseSlug));

        return new LessonViewerResponse(
                lesson.getCourse().getSlug(),
                lesson.getCourse().getTitle(),
                lesson.getSlug(),
                lesson.getTitle(),
                lesson.getPosition(),
                lesson.getDurationMinutes(),
                lesson.getVideoUrl(),
                lesson.getContent()
        );
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
