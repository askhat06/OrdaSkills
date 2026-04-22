package kz.skills.elearning.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TeacherCourseResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String description,
        String locale,
        String level,
        Integer durationHours,
        BigDecimal price,
        String status,
        /** Populated by admin when the course is rejected. Null otherwise. */
        String rejectionReason,
        int lessonCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
