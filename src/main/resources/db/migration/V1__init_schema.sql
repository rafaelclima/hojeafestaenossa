-- ============================================================
-- V1__init_schema.sql
-- Initial database schema
-- Project: Event Memories
-- ============================================================

-- ------------------------------------------------------------
-- Extensions
-- ------------------------------------------------------------

-- UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- EVENTS
-- Cada evento possui um QR Code/token único
-- ------------------------------------------------------------
CREATE TABLE events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name            VARCHAR(150) NOT NULL,

    -- token usado no QR Code/link público
    access_token    VARCHAR(120) NOT NULL UNIQUE,

    -- controle de visibilidade das fotos
    is_public       BOOLEAN NOT NULL DEFAULT TRUE,

    started_at      TIMESTAMPTZ NOT NULL,
    expired_at      TIMESTAMPTZ NOT NULL,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice para busca rápida por token (entrada do sistema)
CREATE INDEX idx_events_access_token
    ON events(access_token);


-- ------------------------------------------------------------
-- UPLOADS
-- Representa qualquer mídia enviada por convidados
-- ------------------------------------------------------------
CREATE TABLE uploads (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_id        UUID NOT NULL,

    -- FOTO ou VIDEO
    media_type      VARCHAR(10) NOT NULL,

    -- caminho/URL do objeto no storage (OCI Object Storage)
    storage_key     TEXT NOT NULL,

    -- nome original do arquivo (debug/auditoria)
    original_name   TEXT,

    -- tamanho em bytes
    file_size       BIGINT NOT NULL,

    -- mensagem opcional do convidado
    message         TEXT,

    -- usado pelo slideshow
    is_visible      BOOLEAN NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_upload_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_media_type
        CHECK (media_type IN ('PHOTO', 'VIDEO'))
);

-- ------------------------------------------------------------
-- Índices estratégicos (performance slideshow)
-- ------------------------------------------------------------

-- buscar uploads por evento em ordem cronológica
CREATE INDEX idx_uploads_event_created
    ON uploads(event_id, created_at);

-- filtro rápido apenas dos visíveis
CREATE INDEX idx_uploads_visible
    ON uploads(event_id, is_visible);


-- ------------------------------------------------------------
-- Comentários (documentação viva do schema)
-- ------------------------------------------------------------

COMMENT ON TABLE events IS
'Eventos criados pelos anfitriões. O access_token é utilizado no QR Code.';

COMMENT ON TABLE uploads IS
'Mídias enviadas pelos convidados (fotos ou vídeos).';

COMMENT ON COLUMN uploads.storage_key IS
'Identificador do arquivo no Object Storage (não é URL pública).';

COMMENT ON COLUMN uploads.message IS
'Mensagem opcional enviada junto com a mídia.';