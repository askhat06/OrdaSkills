package kz.skills.elearning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollmentRequest(
        @NotBlank(message = "courseSlug is required")
        String courseSlug,

        @NotBlank(message = "fullName is required")
        @Size(max = 120, message = "fullName must be at most 120 characters")
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 180, message = "email must be at most 180 characters")
        String email,

        @NotBlank(message = "locale is required")
        @Size(max = 20, message = "locale must be at most 20 characters")
        String locale
) {
}
