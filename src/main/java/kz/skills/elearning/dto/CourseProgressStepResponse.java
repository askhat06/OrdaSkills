package kz.skills.elearning.dto;

import java.time.LocalDateTime;

public record CourseProgressStepResponse(
        String lessonSlug,
        String lessonTitle,
        Integer position,
        String status,
        LocalDateTime completedAt
) {
}
