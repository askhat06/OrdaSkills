package kz.skills.elearning.dto;

public record LessonOutlineResponse(
        String slug,
        String title,
        Integer position,
        Integer durationMinutes,
        String summary
) {
}
