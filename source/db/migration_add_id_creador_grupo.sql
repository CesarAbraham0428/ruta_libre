-- Agrega el campo id_creador a la tabla grupo para soporte de eliminación y trazabilidad de propiedad de grupos.

ALTER TABLE grupo 
ADD COLUMN IF NOT EXISTS id_creador INTEGER REFERENCES usuario(id_usuario) ON DELETE SET NULL;

-- Asignar el creador inicial por defecto a los grupos existentes 
-- (asocia al miembro más antiguo que se unió al grupo).
UPDATE grupo g
SET id_creador = (
  SELECT id_usuario 
  FROM usuario_grupo ug 
  WHERE ug.id_grupo = g.id_grupo 
  ORDER BY fecha_union ASC 
  LIMIT 1
)
WHERE id_creador IS NULL;
