CREATE TABLE comments (
                             id SERIAL PRIMARY KEY,
                             session_card_id INTEGER NOT NULL REFERENCES session_card(id) ON DELETE CASCADE,
                             author_player_id INTEGER NOT NULL REFERENCES session_players(id) ON DELETE CASCADE,
                             content TEXT NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);