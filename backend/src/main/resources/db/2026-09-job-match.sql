-- Additive release migration: safe to run again without changing saved rows.
CREATE TABLE IF NOT EXISTS job_match_analyses (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  job_title VARCHAR(160) NOT NULL,
  job_description LONGTEXT NOT NULL,
  resume_label VARCHAR(255) NOT NULL,
  score INT NOT NULL,
  result_json LONGTEXT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  INDEX idx_job_match_user_updated (user_id, updated_at)
);
