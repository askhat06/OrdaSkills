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
     * Asserts that the course metadata (title, description, etc.) can be modified.
     * Only {@code DRAFT} and {@code REJECTED} courses allow metadata edits — changes
     * to published or under-review courses require admin action first.
     *
     * @throws BadRequestException if the course is not in an editable status
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
     * Asserts that lessons and videos can be added or modified for this course.
     * Blocked only while the course is {@code PENDING_REVIEW} (locked for admin review).
     * {@code PUBLISHED} courses allow lesson/video changes so teachers can enrich
     * already-live content without going through a full re-review cycle.
     *
     * @throws BadRequestException if the course is under admin review
     */
    public void requireLessonEditable(Course course) {
        if (course.getStatus() == CourseStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Cannot modify lessons while the course is under admin review. "
                    + "Withdraw the course first.");
        }
    }

    /**
     * Convenience method: asserts ownership AND metadata-editable status.
     * Use this for course metadata updates only.
     */
    public void requireOwnerAndEditable(Course course, PlatformUserPrincipal principal) {
        requireOwner(course, principal);
        requireEditable(course);
    }

    /**
     * Convenience method: asserts ownership AND lesson-editable status.
     * Use this for all lesson and video write operations.
     */
    public void requireOwnerAndLessonEditable(Course course, PlatformUserPrincipal principal) {
        requireOwner(course, principal);
        requireLessonEditable(course);
    }
}
