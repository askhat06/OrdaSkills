package kz.skills.elearning.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCourseProgressRequest(
        @NotBlank(message = "lessonSlug is required")
        String lessonSlug
) {
}
