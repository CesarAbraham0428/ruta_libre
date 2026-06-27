const express = require('express');
const router = express.Router();
const db = require('../db');

// POST /api/auth/register
router.post('/register', async (req, res) => {
  const { nombre, nombreUsuario, password } = req.body;
  if (!nombre || !nombreUsuario || !password) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  try {
    // Verificar si el usuario ya existe
    const checkUser = await db.query(
      'SELECT id_usuario FROM usuario WHERE nombre_usuario = $1',
      [nombreUsuario]
    );
    if (checkUser.rows.length > 0) {
      return res.status(400).json({ error: 'El nombre de usuario ya está registrado' });
    }

    // Insertar nuevo usuario
    await db.query(
      'INSERT INTO usuario (nombre, nombre_usuario, password, fecha_registro) VALUES ($1, $2, $3, NOW())',
      [nombre, nombreUsuario, password]
    );

    res.status(201).send();
  } catch (error) {
    console.error('Error en /auth/register:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  const { nombreUsuario, password } = req.body;
  if (!nombreUsuario || !password) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  try {
    const result = await db.query(
      'SELECT id_usuario, nombre, nombre_usuario, password FROM usuario WHERE nombre_usuario = $1',
      [nombreUsuario]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Usuario no encontrado' });
    }

    const user = result.rows[0];
    if (user.password !== password) {
      return res.status(401).json({ error: 'Contraseña incorrecta' });
    }

    // Generar un token simulado para cumplir con la interfaz
    const dummyToken = `token_simulado_${user.id_usuario}_${Date.now()}`;

    res.json({
      idUsuario: user.id_usuario,
      nombre: user.nombre,
      nombreUsuario: user.nombre_usuario,
      token: dummyToken
    });
  } catch (error) {
    console.error('Error en /auth/login:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
