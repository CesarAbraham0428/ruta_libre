const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const db = require('../db');

// GET /api/usuarios/:id
router.get('/:id', async (req, res) => {
  const idUsuario = parseInt(req.params.id);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      'SELECT id_usuario, nombre, nombre_usuario, fecha_registro FROM usuario WHERE id_usuario = $1',
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
      fechaRegistro: user.fecha_registro.toISOString()
    });
  } catch (error) {
    console.error('Error en /usuarios/:id:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/usuarios/:id
router.put('/:id', async (req, res) => {
  const idUsuario = parseInt(req.params.id);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  const { nombre, password } = req.body;
  if (!nombre || nombre.trim() === '') {
    return res.status(400).json({ error: 'El nombre es obligatorio' });
  }

  try {
    let query = 'UPDATE usuario SET nombre = $1';
    let params = [nombre];

    if (password && password.trim() !== '') {
      const salt = await bcrypt.genSalt(10);
      const hashedPassword = await bcrypt.hash(password, salt);
      query += ', password = $2 WHERE id_usuario = $3';
      params.push(hashedPassword, idUsuario);
    } else {
      query += ' WHERE id_usuario = $2';
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
