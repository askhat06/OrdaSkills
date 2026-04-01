package kz.skills.elearning.repository;

import jakarta.persistence.LockModeType;
import kz.skills.elearning.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);

    @Query("""
            select distinct progress from CourseProgress progress
            left join fetch progress.course course
            left join fetch progress.currentLesson currentLesson
            left join fetch progress.steps steps
            left join fetch steps.lesson lesson
            where progress.student.id = :studentId and progress.course.slug = :courseSlug
            """)
    Optional<CourseProgress> findDetailedByStudentIdAndCourseSlug(Long studentId, String courseSlug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct progress from CourseProgress progress
            left join fetch progress.course course
            left join fetch progress.currentLesson currentLesson
            left join fetch progress.steps steps
            left join fetch steps.lesson lesson
            where progress.student.id = :studentId and progress.course.id = :courseId
            """)
    Optional<CourseProgress> findForUpdateByStudentIdAndCourseId(Long studentId, Long courseId);
}
