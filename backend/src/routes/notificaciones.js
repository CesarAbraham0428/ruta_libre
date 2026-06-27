const express = require('express');
const router = express.Router();
const db = require('../db');

// GET /api/notificaciones/usuario/:idUsuario
router.get('/usuario/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      `SELECT id_notificacion, id_usuario, id_metas, mensaje, fecha_creacion, leida_movil, leida_smartwatch 
       FROM notificacion 
       WHERE id_usuario = $1 
       ORDER BY fecha_creacion DESC`,
      [idUsuario]
    );

    const notificaciones = result.rows.map(n => ({
      idNotificacion: n.id_notificacion,
      idUsuario: n.id_usuario,
      idMetas: n.id_metas,
      mensaje: n.mensaje,
      fechaCreacion: n.fecha_creacion.toISOString(),
      leidaMovil: n.leida_movil,
      leidaSmartwatch: n.leida_smartwatch
    }));

    res.json(notificaciones);
  } catch (error) {
    console.error('Error en /notificaciones/usuario/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/notificaciones/:id/leer-movil
router.put('/:id/leer-movil', async (req, res) => {
  const idNotificacion = parseInt(req.params.id);
  if (isNaN(idNotificacion)) {
    return res.status(400).json({ error: 'ID de notificación no válido' });
  }

  try {
    const result = await db.query(
      'UPDATE notificacion SET leida_movil = TRUE WHERE id_notificacion = $1 RETURNING id_notificacion',
      [idNotificacion]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Notificación no encontrada' });
    }

    res.status(200).send();
  } catch (error) {
    console.error('Error en /notificaciones/:id/leer-movil:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/notificaciones/:id/leer-wear
router.put('/:id/leer-wear', async (req, res) => {
  const idNotificacion = parseInt(req.params.id);
  if (isNaN(idNotificacion)) {
    return res.status(400).json({ error: 'ID de notificación no válido' });
  }

  try {
    const result = await db.query(
      'UPDATE notificacion SET leida_smartwatch = TRUE WHERE id_notificacion = $1 RETURNING id_notificacion',
      [idNotificacion]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Notificación no encontrada' });
    }

    res.status(200).send();
  } catch (error) {
    console.error('Error en /notificaciones/:id/leer-wear:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
