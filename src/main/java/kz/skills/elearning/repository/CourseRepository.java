package kz.skills.elearning.repository;

import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // --- Admin: all courses regardless of status, owner loaded for admin responses ---

    @EntityGraph(attributePaths = {"lessons", "owner"})
    List<Course> findAllByOrderByCreatedAtDesc();

    // --- Admin/public: filter by status.
    //     Used by public catalog (PUBLISHED only) and admin moderation queue.
    //     Owner is loaded because admin responses include ownerEmail. ---

    @EntityGraph(attributePaths = {"lessons", "owner"})
    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);

    // --- Single course lookup used by all service layers.
    //     Owner is accessed lazily within service transactions where needed. ---

    @EntityGraph(attributePaths = "lessons")
    Optional<Course> findBySlug(String slug);

    // --- Teacher: list own courses across all statuses ---

    @EntityGraph(attributePaths = {"lessons", "owner"})
    List<Course> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    // --- Slug uniqueness check ---

    boolean existsBySlugIgnoreCase(String slug);
}
