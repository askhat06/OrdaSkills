package kz.skills.elearning.dto;

import java.time.LocalDateTime;

public record TeacherLessonResponse(
        Long id,
        String courseSlug,
        String slug,
        String title,
        String summary,
        String content,
        Integer position,
        Integer durationMinutes,
        String videoUrl,
        boolean hasVideo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
