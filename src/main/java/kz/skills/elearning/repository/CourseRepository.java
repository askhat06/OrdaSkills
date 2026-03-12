package kz.skills.elearning.repository;

import kz.skills.elearning.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "lessons")
    Optional<Course> findBySlug(String slug);
}
