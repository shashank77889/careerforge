-- Additive MySQL migration for an existing CareerForge database.
-- Back up the database and apply on staging before production startup with ddl-auto=validate.
CREATE TABLE IF NOT EXISTS career_journeys (
  user_id BIGINT NOT NULL PRIMARY KEY,
  target_role VARCHAR(255) NOT NULL,
  last_page VARCHAR(255) NOT NULL DEFAULT '/dashboard',
  weekly_hours INT NOT NULL,
  version BIGINT,
  CONSTRAINT fk_journey_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS learning_steps (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  skill VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  reflection VARCHAR(2000),
  completed_at DATETIME(6),
  version BIGINT,
  CONSTRAINT uq_learning_step UNIQUE (user_id, role, skill),
  CONSTRAINT fk_learning_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS readiness_snapshots (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  score INT NOT NULL,
  recorded_at DATETIME(6),
  INDEX idx_readiness_user_role (user_id, role),
  CONSTRAINT fk_snapshot_user FOREIGN KEY (user_id) REFERENCES users(id)
);

