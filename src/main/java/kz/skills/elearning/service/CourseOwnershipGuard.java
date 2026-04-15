package kz.skills.elearning.service;

import kz.skills.elearning.entity.Course;
import kz.skills.elearning.entity.CourseStatus;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Centralized ownership and lifecycle enforcement for teacher-owned courses.
 *
 * <p>Every teacher-facing service method that touches a course MUST call the relevant
 * guard method before performing any mutation. Keeping this logic in one place means
 * a forgotten check is immediately visible during code review rather than silently
 * allowing cross-teacher edits.
 *
 * <p>Guard methods throw standard Spring exceptions so that the existing exception
 * handlers produce consistent HTTP responses:
 * <ul>
 *   <li>{@link AccessDeniedException} → 403 Forbidden</li>
 *   <li>{@link BadRequestException} → 400 Bad Request</li>
 * </ul>
 */
@Component
public class CourseOwnershipGuard {

    /**
     * Asserts that the given principal is the owner of the course.
     *
     * <p>A course with {@code owner == null} is a platform/admin-created course and
     * is never owned by any teacher. Attempting to claim ownership of such a course
     * is denied.
     *
     * @throws AccessDeniedException if the principal is not the owner
     */
    public void requireOwner(Course course, PlatformUserPrincipal principal) {
        if (course.getOwner() == null
                || !course.getOwner().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not own this course");
        }
    }

    /**
     * Asserts that the course is in a status that allows content modifications.
     *
     * <p>Only {@code DRAFT} and {@code REJECTED} courses can be edited. A course that
     * is {@code PENDING_REVIEW} is locked while the admin reviews it. A {@code PUBLISHED}
     * course must be withdrawn first (teacher action) or unpublished by admin.
     *
     * @throws BadRequestException if the course is not editable
     */
    public void requireEditable(Course course) {
        if (course.getStatus() != CourseStatus.DRAFT
                && course.getStatus() != CourseStatus.REJECTED) {
            throw new BadRequestException(
                    "Course must be in DRAFT or REJECTED status to modify content. "
                    + "Current status: " + course.getStatus());
        }
    }

    /**
     * Convenience method: asserts ownership AND editable status in one call.
     * Use this for all lesson write operations and course metadata updates.
     */
    public void requireOwnerAndEditable(Course course, PlatformUserPrincipal principal) {
        requireOwner(course, principal);
        requireEditable(course);
    }
}
