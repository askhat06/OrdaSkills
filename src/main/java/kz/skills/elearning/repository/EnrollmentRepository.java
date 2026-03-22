package kz.skills.elearning.repository;

import kz.skills.elearning.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);

    boolean existsByCourse_Id(Long courseId);

    @EntityGraph(attributePaths = {"course", "student"})
    List<Enrollment> findAllByOrderByEnrolledAtDesc();

    @EntityGraph(attributePaths = {"course", "student"})
    List<Enrollment> findByCourse_SlugOrderByEnrolledAtDesc(String courseSlug);

    @EntityGraph(attributePaths = {"course", "student"})
    List<Enrollment> findByStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(String email);

    @EntityGraph(attributePaths = {"course", "student"})
    List<Enrollment> findByCourse_SlugAndStudent_EmailIgnoreCaseOrderByEnrolledAtDesc(String courseSlug, String email);

    Optional<Enrollment> findByCourse_IdAndStudent_Id(Long courseId, Long studentId);
}
