package kz.skills.elearning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminVideoUploadInitRequest(
        @NotBlank(message = "fileName is required")
        String fileName,
        @NotBlank(message = "contentType is required")
        String contentType,
        @NotNull(message = "sizeBytes is required")
        @Min(value = 1, message = "sizeBytes must be greater than zero")
        Long sizeBytes
) {
}
