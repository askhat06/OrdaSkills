package kz.skills.elearning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TeacherLessonRequest(

        /**
         * Optional slug. Auto-generated from {@code title} if not provided.
         * Only honoured on create; on update, provide it explicitly to rename.
         */
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                 message = "slug must use lowercase letters, numbers, and hyphens only")
        @Size(max = 120, message = "slug must be at most 120 characters")
        String slug,

        @NotBlank(message = "title is required")
        @Size(min = 3, max = 180, message = "title must be between 3 and 180 characters")
        String title,

        @Size(max = 500, message = "summary must be at most 500 characters")
        String summary,

        @Size(max = 8000, message = "content must be at most 8000 characters")
        String content,

        /** Optional external video URL. Platform-hosted videos use the video-upload workflow. */
        @Size(max = 400, message = "videoUrl must be at most 400 characters")
        String videoUrl,

        @Min(value = 1, message = "durationMinutes must be at least 1")
        @Max(value = 600, message = "durationMinutes must be at most 600")
        Integer durationMinutes

) {
}
