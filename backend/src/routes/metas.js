const express = require('express');
const router = express.Router();
const db = require('../db');

// POST /api/metas
router.post('/', async (req, res) => {
  const { idUsuario, tipoMeta, valorObjetivo } = req.body;
  if (!idUsuario || !tipoMeta || !valorObjetivo) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  // Convertir a minúsculas para Postgres enum
  const tipoMetaLower = tipoMeta.toLowerCase();
  
  try {
    // Validar que no exista una meta activa del mismo tipo para este usuario
    const metaActivaResult = await db.query(
      'SELECT id_metas FROM metas WHERE id_usuario = $1 AND tipo_meta = $2 AND terminada = FALSE',
      [idUsuario, tipoMetaLower]
    );

    if (metaActivaResult.rows.length > 0) {
      return res.status(400).json({ error: 'Ya tienes una meta activa de este tipo' });
    }

    const result = await db.query(
      'INSERT INTO metas (id_usuario, tipo_meta, valor_objetivo, valor_actual, terminada) VALUES ($1, $2, $3, 0, FALSE) RETURNING id_metas, id_usuario, tipo_meta, valor_objetivo, valor_actual, terminada',
      [idUsuario, tipoMetaLower, valorObjetivo]
    );

    const meta = result.rows[0];
    res.status(201).json({
      idMetas: meta.id_metas,
      idUsuario: meta.id_usuario,
      tipoMeta: meta.tipo_meta.toUpperCase(),
      valorObjetivo: parseFloat(meta.valor_objetivo),
      valorActual: parseFloat(meta.valor_actual),
      terminada: meta.terminada
    });
  } catch (error) {
    console.error('Error en /metas:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/metas/usuario/:idUsuario
router.get('/usuario/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      'SELECT id_metas, id_usuario, tipo_meta, valor_objetivo, valor_actual, terminada FROM metas WHERE id_usuario = $1 ORDER BY id_metas DESC',
      [idUsuario]
    );

    const metas = result.rows.map(meta => ({
      idMetas: meta.id_metas,
      idUsuario: meta.id_usuario,
      tipoMeta: meta.tipo_meta.toUpperCase(),
      valorObjetivo: parseFloat(meta.valor_objetivo),
      valorActual: parseFloat(meta.valor_actual),
      terminada: meta.terminada
    }));

    res.json(metas);
  } catch (error) {
    console.error('Error en /metas/usuario/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
