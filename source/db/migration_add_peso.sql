-- Ejecutar una sola vez en bases de datos de Ruta Libre ya existentes.
ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS peso_kg NUMERIC(5,2);

ALTER TABLE usuario
DROP CONSTRAINT IF EXISTS usuario_peso_valido;

ALTER TABLE usuario
ADD CONSTRAINT usuario_peso_valido
CHECK (peso_kg IS NULL OR peso_kg BETWEEN 20 AND 300);
