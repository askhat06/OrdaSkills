package kz.skills.elearning.dto;

import java.math.BigDecimal;

public record CourseSummaryResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String locale,
        String level,
        Integer durationHours,
        Integer lessonCount,
        String instructorName,
        long enrollmentCount,
        BigDecimal price,
        Double averageRating,
        long ratingCount
) {
}
