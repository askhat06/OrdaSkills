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

    // --- Slug uniqueness check within a course (used during lesson create/update) ---
    boolean existsByCourse_IdAndSlug(Long courseId, String slug);

    // --- Used when deleting a lesson to shift subsequent positions down by 1 ---
    List<Lesson> findByCourse_IdAndPositionGreaterThanOrderByPositionAsc(Long courseId, Integer position);

    // --- Used to compute the next available position when appending a lesson ---
    List<Lesson> findByCourse_IdOrderByPositionAsc(Long courseId);
}
