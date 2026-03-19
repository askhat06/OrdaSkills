package kz.skills.elearning.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminVideoUploadCompleteRequest(
        @NotBlank(message = "objectKey is required")
        String objectKey
) {
}
