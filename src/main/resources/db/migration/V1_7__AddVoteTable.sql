ALTER TABLE comments ADD COLUMN voteCount INTEGER NOT NULL DEFAULT 0;

CREATE TABLE votes (
                       id SERIAL PRIMARY KEY,
                       comment_id INTEGER NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
                       voter_player_id INTEGER NOT NULL REFERENCES session_players(id) ON DELETE CASCADE,
                       UNIQUE (comment_id, voter_player_id)
);