-- Distinguish unauthenticated "lead" accounts created by the enrollment flow
-- from fully-registered users who own their email address.
ALTER TABLE platform_users ADD COLUMN is_lead BOOLEAN NOT NULL DEFAULT FALSE;

-- Index for O(1) token lookups instead of full table scans.
-- Partial WHERE clause omitted for H2 compatibility; a full index still achieves O(1) lookups.
CREATE INDEX idx_platform_users_verification_token
    ON platform_users (verification_token);
