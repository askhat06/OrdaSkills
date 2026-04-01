package kz.skills.elearning.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CourseProgressResponse(
        Long userId,
        Long courseId,
        String courseSlug,
        String courseTitle,
        String status,
        String currentStep,
        Integer completedSteps,
        Integer totalSteps,
        Integer percentComplete,
        Integer attemptCount,
        LocalDateTime startedAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime resetAt,
        List<CourseProgressStepResponse> steps
) {
}
