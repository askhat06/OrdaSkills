package kz.skills.elearning.repository;

import kz.skills.elearning.entity.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {

    Optional<CourseRating> findByCourse_IdAndStudent_Id(Long courseId, Long studentId);

    long countByCourse_Id(Long courseId);

    @Query("SELECT AVG(r.rating) FROM CourseRating r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
