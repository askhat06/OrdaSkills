ALTER TABLE platform_users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE platform_users ADD COLUMN verification_token VARCHAR(64);
ALTER TABLE platform_users ADD COLUMN token_expires_at TIMESTAMP;
