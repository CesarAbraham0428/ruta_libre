-- ============================================================
-- Script PostgreSQL - Ruta Libre
-- Basado en el diagrama de clases proporcionado
-- ============================================================

-- Extensión necesaria para los campos geometry (punto_inicio, punto_fin)
CREATE EXTENSION IF NOT EXISTS postgis;

-- ENUM para tipo_meta (definido en la entidad Metas)
CREATE TYPE tipo_meta_enum AS ENUM ('pasos', 'calorias', 'distancia', 'tiempo');

-- ============================================================
-- Tabla: usuario
-- ============================================================
CREATE TABLE usuario (
    id_usuario      SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    nombre_usuario  VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
-- Tabla: grupo
-- ============================================================
CREATE TABLE grupo (
    id_grupo     SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    codigo       VARCHAR(20)  NOT NULL UNIQUE,
    descripcion  TEXT
);

-- ============================================================
-- Tabla intermedia: usuario_grupo (relación M:N Usuario - Grupo)
-- ============================================================
CREATE TABLE usuario_grupo (
    id_usuario_grupo  SERIAL PRIMARY KEY,
    id_usuario        INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_grupo          INTEGER NOT NULL REFERENCES grupo(id_grupo)     ON DELETE CASCADE,
    fecha_union       TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (id_usuario, id_grupo)
);

-- ============================================================
-- Tabla: ruta
-- coordenadas: jsonb con formato [{"longitud": ..., "latitud": ...}, ...]
-- ============================================================
CREATE TABLE ruta (
    id_ruta      SERIAL PRIMARY KEY,
    coordenadas  JSONB NOT NULL
);

-- ============================================================
-- Tabla: entrenamiento
-- Relación 1:1 con ruta (id_ruta UNIQUE)
-- Relación N:1 con usuario
-- ============================================================
CREATE TABLE entrenamiento (
    id_entrenamiento  SERIAL PRIMARY KEY,
    id_usuario        INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_ruta           INTEGER UNIQUE REFERENCES ruta(id_ruta) ON DELETE SET NULL,
    pasos             INTEGER NOT NULL DEFAULT 0,
    calorias          INTEGER NOT NULL DEFAULT 0,
    distancia         FLOAT   NOT NULL DEFAULT 0,
    fecha_inicio      TIMESTAMP NOT NULL,
    tiempo            INTEGER NOT NULL DEFAULT 0,  -- duración en segundos
    punto_inicio      geometry(Point, 4326),
    punto_fin         geometry(Point, 4326)
);

-- ============================================================
-- Tabla: metas
-- Relación N:1 con usuario
-- ============================================================
CREATE TABLE metas (
    id_metas        SERIAL PRIMARY KEY,
    id_usuario      INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    tipo_meta       tipo_meta_enum NOT NULL,
    valor_objetivo  FLOAT NOT NULL,
    valor_actual    FLOAT NOT NULL DEFAULT 0,
    terminada       BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================
-- Tabla: notificacion (necesaria para RF-06, no está en el diagrama)
-- ============================================================
CREATE TABLE notificacion (
    id_notificacion    SERIAL PRIMARY KEY,
    id_usuario         INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_metas           INTEGER REFERENCES metas(id_metas) ON DELETE CASCADE,
    mensaje            VARCHAR(255) NOT NULL,
    fecha_creacion     TIMESTAMP NOT NULL DEFAULT now(),
    leida_movil        BOOLEAN NOT NULL DEFAULT FALSE,
    leida_smartwatch   BOOLEAN NOT NULL DEFAULT FALSE
);
