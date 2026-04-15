package kz.skills.elearning.dto;

public record AdminCourseResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String description,
        String locale,
        String instructorName,
        String level,
        Integer durationHours,
        int lessonCount,
        String status,
        String ownerEmail
) {
}
