package kz.skills.elearning.dto;

public record LessonViewerResponse(
        String courseSlug,
        String courseTitle,
        String lessonSlug,
        String lessonTitle,
        Integer position,
        Integer durationMinutes,
        String videoUrl,
        String content
) {
}
