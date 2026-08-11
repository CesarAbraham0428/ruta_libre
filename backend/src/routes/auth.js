const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const db = require('../db');
const { signToken } = require('../authToken');

// POST /api/auth/register
/** Registra un usuario nuevo después de validar y cifrar su contraseña. */
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
      return res.status(400).json({ error: 'Nombre de usuario no disponible cambia tu nombre de usuario' });
    }

    // Hashear contraseña e insertar nuevo usuario
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    await db.query(
      'INSERT INTO usuario (nombre, nombre_usuario, password, fecha_registro) VALUES ($1, $2, $3, NOW())',
      [nombre, nombreUsuario, hashedPassword]
    );

    res.status(201).send();
  } catch (error) {
    console.error('Error en /auth/register:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// POST /api/auth/login
/** Autentica al usuario y devuelve sus datos junto con un token JWT. */
router.post('/login', async (req, res) => {
  const { nombreUsuario, password } = req.body;
  if (!nombreUsuario || !password) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  try {
    const result = await db.query(
      'SELECT id_usuario, nombre, nombre_usuario, password, peso_kg FROM usuario WHERE nombre_usuario = $1',
      [nombreUsuario]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'El usuario o la contraseña es incorrecto' });
    }

    const user = result.rows[0];
    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) {
      return res.status(401).json({ error: 'El usuario o la contraseña es incorrecto' });
    }

    const token = signToken({
      tipoToken: 'usuario',
      idUsuario: user.id_usuario,
      nombreUsuario: user.nombre_usuario
    });

    res.json({
      idUsuario: user.id_usuario,
      nombre: user.nombre,
      nombreUsuario: user.nombre_usuario,
      pesoKg: user.peso_kg === null ? null : parseFloat(user.peso_kg),
      token
    });
  } catch (error) {
    console.error('Error en /auth/login:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
