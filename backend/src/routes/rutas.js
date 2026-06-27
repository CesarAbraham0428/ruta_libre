const express = require('express');
const router = express.Router();
const db = require('../db');

// POST /api/rutas/actualizar
router.post('/actualizar', async (req, res) => {
  const { idRuta, coordenadas } = req.body;
  if (!idRuta || !coordenadas) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  try {
    const coordsJson = JSON.stringify(coordenadas);
    const result = await db.query(
      'UPDATE ruta SET coordenadas = $1 WHERE id_ruta = $2 RETURNING id_ruta, coordenadas',
      [coordsJson, idRuta]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Ruta no encontrada' });
    }

    const updatedRuta = result.rows[0];
    res.json({
      idRuta: updatedRuta.id_ruta,
      coordenadas: typeof updatedRuta.coordenadas === 'string' 
        ? JSON.parse(updatedRuta.coordenadas) 
        : updatedRuta.coordenadas
    });
  } catch (error) {
    console.error('Error en /rutas/actualizar:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/rutas/:id
router.get('/:id', async (req, res) => {
  const idRuta = parseInt(req.params.id);
  if (isNaN(idRuta)) {
    return res.status(400).json({ error: 'ID de ruta no válido' });
  }

  try {
    const result = await db.query(
      'SELECT id_ruta, coordenadas FROM ruta WHERE id_ruta = $1',
      [idRuta]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Ruta no encontrada' });
    }

    const ruta = result.rows[0];
    res.json({
      idRuta: ruta.id_ruta,
      coordenadas: typeof ruta.coordenadas === 'string' 
        ? JSON.parse(ruta.coordenadas) 
        : ruta.coordenadas
    });
  } catch (error) {
    console.error('Error en /rutas/:id:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
