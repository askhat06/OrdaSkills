package kz.skills.elearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminModerationRequest(

        @NotBlank(message = "reason is required")
        @Size(min = 10, max = 1000, message = "reason must be between 10 and 1000 characters")
        String reason

) {
}
