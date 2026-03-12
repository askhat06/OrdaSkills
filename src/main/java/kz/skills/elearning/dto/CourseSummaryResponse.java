package kz.skills.elearning.dto;

public record CourseSummaryResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String locale,
        String level,
        Integer durationHours,
        Integer lessonCount
) {
}
