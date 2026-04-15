package kz.skills.elearning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TeacherCourseRequest(

        /**
         * Optional slug. If not provided, the service auto-generates one from {@code title}.
         * Must be lowercase, alphanumeric, with hyphens between words.
         */
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                 message = "slug must use lowercase letters, numbers, and hyphens only")
        @Size(max = 120, message = "slug must be at most 120 characters")
        String slug,

        @NotBlank(message = "title is required")
        @Size(max = 180, message = "title must be at most 180 characters")
        String title,

        @Size(max = 240, message = "subtitle must be at most 240 characters")
        String subtitle,

        @NotBlank(message = "description is required")
        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        @NotBlank(message = "locale is required")
        @Size(max = 20, message = "locale must be at most 20 characters")
        String locale,

        @Size(max = 50, message = "level must be at most 50 characters")
        String level,

        @NotNull(message = "durationHours is required")
        @Min(value = 1, message = "durationHours must be at least 1")
        @Max(value = 1000, message = "durationHours must be at most 1000")
        Integer durationHours

) {
}
