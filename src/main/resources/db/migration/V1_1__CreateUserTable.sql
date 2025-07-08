CREATE TABLE users (
                    id serial PRIMARY KEY,
                    email VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    best_score INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE session_players (
                             id BIGSERIAL PRIMARY KEY,
                             session_id INTEGER NOT NULL REFERENCES active_session(id) ON DELETE CASCADE,
                             user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             score INTEGER NOT NULL DEFAULT 0,
                             UNIQUE (session_id, user_id)
);