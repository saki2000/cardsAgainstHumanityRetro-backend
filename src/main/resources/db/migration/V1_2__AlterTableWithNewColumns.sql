ALTER TABLE active_session ADD COLUMN host_user_id INTEGER REFERENCES users(id) NULL;
ALTER TABLE active_session ADD COLUMN current_player_id INTEGER REFERENCES users(id) NULL;
ALTER TABLE session_players ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE session_players ADD COLUMN turn_order INTEGER NOT NULL;
ALTER TABLE active_session DROP COLUMN email;
ALTER TABLE active_session DROP COLUMN username;
