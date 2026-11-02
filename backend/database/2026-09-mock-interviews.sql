-- For an existing career_journeys table created before resume navigation was added:
-- ALTER TABLE career_journeys ADD COLUMN last_page VARCHAR(255) NOT NULL DEFAULT '/dashboard';
CREATE TABLE IF NOT EXISTS interview_sessions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  current_question INT NOT NULL,
  questions_json LONGTEXT NOT NULL,
  answers_json LONGTEXT NOT NULL,
  started_at DATETIME(6),
  completed_at DATETIME(6),
  version BIGINT,
  INDEX idx_interview_user_started (user_id, started_at),
  CONSTRAINT fk_interview_user FOREIGN KEY (user_id) REFERENCES users(id)
);
