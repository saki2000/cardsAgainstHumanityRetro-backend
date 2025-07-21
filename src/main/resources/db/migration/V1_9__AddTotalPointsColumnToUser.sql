-- ALTER TABLE users ADD COLUMN total_points INTEGER NOT NULL DEFAULT 0;
-- ALTER TABLE active_session ADD COLUMN round_number INTEGER NOT NULL DEFAULT 0;

CREATE TABLE session_history(
                                 id BIGSERIAL PRIMARY KEY,
                                 session_id INTEGER NOT NULL REFERENCES active_session(id) ON DELETE CASCADE,
                                 user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 score INTEGER NOT NULL DEFAULT 0,
                                 UNIQUE (session_id, user_id)
);