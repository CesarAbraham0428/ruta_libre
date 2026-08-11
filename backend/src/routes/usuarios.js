const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const db = require('../db');

// GET /api/usuarios/:id
/** Obtiene los datos públicos del perfil de un usuario. */
router.get('/:id', async (req, res) => {
  const idUsuario = parseInt(req.params.id);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      'SELECT id_usuario, nombre, nombre_usuario, peso_kg, fecha_registro FROM usuario WHERE id_usuario = $1',
      [idUsuario]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    const user = result.rows[0];
    res.json({
      idUsuario: user.id_usuario,
      nombre: user.nombre,
      nombreUsuario: user.nombre_usuario,
      pesoKg: user.peso_kg === null ? null : parseFloat(user.peso_kg),
      fechaRegistro: user.fecha_registro.toISOString()
    });
  } catch (error) {
    console.error('Error en /usuarios/:id:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/usuarios/:id/peso
/** Actualiza el peso del usuario después de validar su rango permitido. */
router.put('/:id/peso', async (req, res) => {
  const idUsuario = parseInt(req.params.id);
  const pesoKg = Number(req.body.pesoKg);

  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }
  if (!Number.isFinite(pesoKg) || pesoKg < 20 || pesoKg > 300) {
    return res.status(400).json({ error: 'El peso debe estar entre 20 y 300 kg' });
  }

  try {
    const result = await db.query(
      'UPDATE usuario SET peso_kg = $1 WHERE id_usuario = $2 RETURNING peso_kg',
      [pesoKg, idUsuario]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }
    res.json({ pesoKg: parseFloat(result.rows[0].peso_kg) });
  } catch (error) {
    console.error('Error en PUT /usuarios/:id/peso:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/usuarios/:id
/** Actualiza el perfil y, si se proporciona, la contraseña del usuario. */
router.put('/:id', async (req, res) => {
  const idUsuario = parseInt(req.params.id);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  const { nombre, password, pesoKg } = req.body;
  if (!nombre || nombre.trim() === '') {
    return res.status(400).json({ error: 'El nombre es obligatorio' });
  }
  if (pesoKg !== null && pesoKg !== undefined && (!Number.isFinite(Number(pesoKg)) || Number(pesoKg) < 20 || Number(pesoKg) > 300)) {
    return res.status(400).json({ error: 'El peso debe estar entre 20 y 300 kg' });
  }

  try {
    let query = 'UPDATE usuario SET nombre = $1, peso_kg = $2';
    let params = [nombre];
    params.push(pesoKg ?? null);

    if (password && password.trim() !== '') {
      const salt = await bcrypt.genSalt(10);
      const hashedPassword = await bcrypt.hash(password, salt);
      query += ', password = $3 WHERE id_usuario = $4';
      params.push(hashedPassword, idUsuario);
    } else {
      query += ' WHERE id_usuario = $3';
      params.push(idUsuario);
    }

    const result = await db.query(query, params);
    
    res.json({ message: 'Usuario actualizado correctamente' });
  } catch (error) {
    console.error('Error en PUT /usuarios/:id:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
