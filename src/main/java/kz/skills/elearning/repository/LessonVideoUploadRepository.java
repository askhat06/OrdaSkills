package kz.skills.elearning.repository;

import kz.skills.elearning.entity.LessonVideoUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonVideoUploadRepository extends JpaRepository<LessonVideoUpload, Long> {

    Optional<LessonVideoUpload> findByLesson_Id(Long lessonId);

    Optional<LessonVideoUpload> findByLesson_IdAndObjectKey(Long lessonId, String objectKey);

    void deleteByLesson_Id(Long lessonId);
}
