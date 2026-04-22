package kz.skills.elearning.service;

import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseRating;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.ResourceNotFoundException;
import kz.skills.elearning.repository.CourseRatingRepository;
import kz.skills.elearning.repository.CourseRepository;
import kz.skills.elearning.repository.EnrollmentRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseRatingService {

    private final CourseRepository courseRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PlatformUserRepository platformUserRepository;

    public CourseRatingService(CourseRepository courseRepository,
                               CourseRatingRepository courseRatingRepository,
                               EnrollmentRepository enrollmentRepository,
                               PlatformUserRepository platformUserRepository) {
        this.courseRepository = courseRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.platformUserRepository = platformUserRepository;
    }

    /**
     * Submits or updates a rating (1–5) for a course.
     * Only enrolled students can rate; one rating per student per course (upsert).
     */
    public void rateCourse(String courseSlug, int rating, PlatformUserPrincipal principal) {
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseSlug));

        if (!enrollmentRepository.existsByCourse_IdAndStudent_Id(course.getId(), principal.getId())) {
            throw new BadRequestException("You must be enrolled in this course to rate it.");
        }

        CourseRating cr = courseRatingRepository
                .findByCourse_IdAndStudent_Id(course.getId(), principal.getId())
                .orElseGet(() -> {
                    CourseRating newRating = new CourseRating();
                    newRating.setCourse(course);
                    newRating.setStudent(platformUserRepository.getReferenceById(principal.getId()));
                    return newRating;
                });

        cr.setRating(rating);
        courseRatingRepository.save(cr);
    }
}
