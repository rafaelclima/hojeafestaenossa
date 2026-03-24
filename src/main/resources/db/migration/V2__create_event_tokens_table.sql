CREATE TABLE event_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token      VARCHAR(20) NOT NULL UNIQUE,
    event_id   UUID REFERENCES events(id) ON DELETE SET NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_event_tokens_token ON event_tokens(token);
CREATE INDEX idx_event_tokens_event_id ON event_tokens(event_id);

COMMENT ON TABLE event_tokens IS
'Tokens de autorização para criação de eventos. Um token só pode ser usado uma vez.';

COMMENT ON COLUMN event_tokens.event_id IS
'Referência ao evento criado com este token. NULL significa que ainda não foi usado.';