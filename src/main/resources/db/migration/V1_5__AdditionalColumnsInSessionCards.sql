CREATE TYPE card_status AS ENUM ('FREE', 'PLAYED');

ALTER TABLE session_card
    ADD COLUMN status card_status NOT NULL DEFAULT 'FREE';