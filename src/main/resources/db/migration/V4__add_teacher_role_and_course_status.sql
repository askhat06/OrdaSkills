-- Add course ownership: which user (teacher) owns this course.
-- Nullable so that existing admin-created/seeded courses are not broken.
ALTER TABLE courses ADD COLUMN owner_id BIGINT;
ALTER TABLE courses ADD CONSTRAINT fk_courses_owner FOREIGN KEY (owner_id) REFERENCES platform_users(id);

-- Publication lifecycle for teacher-created courses.
-- Existing rows (admin-created and seeded) default to PUBLISHED so they remain
-- visible in the catalog without any data migration.
ALTER TABLE courses ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';

-- Populated by admin when rejecting a course submitted for review.
ALTER TABLE courses ADD COLUMN rejection_reason VARCHAR(1000);
