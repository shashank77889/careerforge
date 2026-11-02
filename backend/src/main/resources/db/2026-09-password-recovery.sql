CREATE TABLE IF NOT EXISTS password_recovery (
    user_id BIGINT NOT NULL PRIMARY KEY,
    token_hash VARCHAR(64) NULL UNIQUE,
    expires_at DATETIME(6) NULL,
    requested_at DATETIME(6) NULL,
    session_version BIGINT NOT NULL DEFAULT 0
);
