package kz.skills.elearning.dto;

import java.util.List;

public record CourseLandingResponse(
        String slug,
        String title,
        String subtitle,
        String description,
        String locale,
        String instructorName,
        String level,
        Integer durationHours,
        List<LessonOutlineResponse> lessons
) {
}
