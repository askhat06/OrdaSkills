package kz.skills.elearning.service;

import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
public class DataSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;

    public DataSeeder(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            return;
        }

        Course course = new Course();
        course.setSlug("digital-skills-kz");
        course.setTitle("Digital Skills for Career Growth in Kazakhstan");
        course.setSubtitle("MVP demo course for skills development and employability");
        course.setDescription("An introductory course that helps learners explore digital literacy, online collaboration, and modern workplace tools in Kazakhstan. This seeded course supports the MVP demo: landing page, enrollment flow, and lesson viewer.");
        course.setLocale("en-KZ");
        course.setInstructorName("Aruzhan Sadykova");
        course.setLevel("Beginner");
        course.setDurationHours(6);
        course.setStatus(CourseStatus.PUBLISHED);

        Lesson lesson = new Lesson();
        lesson.setSlug("intro-to-digital-skills");
        lesson.setTitle("Introduction to Digital Skills");
        lesson.setSummary("Why digital skills matter for learners and job seekers in Kazakhstan.");
        lesson.setPosition(1);
        lesson.setDurationMinutes(25);
        lesson.setVideoUrl("https://example.com/videos/intro-to-digital-skills");
        lesson.setContent("This lesson viewer can render the first course lesson for your MVP.\\n\\nSuggested frontend behavior:\\n1. Load the course landing page from GET /api/courses/{slug}.\\n2. Submit the enrollment form to POST /api/enrollments.\\n3. Open this lesson with GET /api/courses/{courseSlug}/lessons/{lessonSlug}.\\n\\nFor the demo, you can enroll a test student, open the lesson viewer, and then inspect the enrollment record via GET /api/enrollments or in the H2 console.");

        Lesson lessonTwo = new Lesson();
        lessonTwo.setSlug("online-collaboration-basics");
        lessonTwo.setTitle("Online Collaboration Basics");
        lessonTwo.setSummary("Core tools and habits for remote learning and team communication.");
        lessonTwo.setPosition(2);
        lessonTwo.setDurationMinutes(30);
        lessonTwo.setVideoUrl("https://example.com/videos/online-collaboration-basics");
        lessonTwo.setContent("This second lesson is present so the course landing page shows a small catalog of lessons. Later ISC-2 features can extend this model with localized bodies, progress tracking, quizzes, and instructor tools.");

        course.addLesson(lesson);
        course.addLesson(lessonTwo);

        courseRepository.save(course);
    }
}
