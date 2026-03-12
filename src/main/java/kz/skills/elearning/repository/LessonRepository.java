package kz.skills.elearning.repository;

import kz.skills.elearning.entity.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @EntityGraph(attributePaths = "course")
    Optional<Lesson> findByCourse_SlugAndSlug(String courseSlug, String lessonSlug);

    List<Lesson> findByCourse_SlugOrderByPositionAsc(String courseSlug);
}
