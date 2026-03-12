package kz.skills.elearning.dto;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        String studentName,
        String email,
        String locale,
        String courseSlug,
        String courseTitle,
        String status,
        LocalDateTime enrolledAt
) {
}
