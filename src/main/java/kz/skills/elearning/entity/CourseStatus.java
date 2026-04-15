package kz.skills.elearning.entity;

public enum CourseStatus {
    /** Teacher is still building the course. Not visible to students. */
    DRAFT,

    /** Teacher submitted for admin review. Locked for edits until reviewed. */
    PENDING_REVIEW,

    /** Admin approved. Visible in the public catalog. */
    PUBLISHED,

    /** Admin rejected. Teacher can edit and resubmit. Rejection reason is stored on the course. */
    REJECTED
}
