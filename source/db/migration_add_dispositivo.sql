CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS dispositivo (
    id_dispositivo      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario          INTEGER REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    tipo                VARCHAR(20) NOT NULL CHECK (tipo IN ('movil', 'wear', 'tv')),
    nombre              VARCHAR(100),
    codigo_vinculacion  VARCHAR(10) UNIQUE,
    codigo_expira       TIMESTAMPTZ,
    token_hash          VARCHAR(255) UNIQUE,
    vinculado           BOOLEAN NOT NULL DEFAULT FALSE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_vinculacion   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario
ON dispositivo(id_usuario);
