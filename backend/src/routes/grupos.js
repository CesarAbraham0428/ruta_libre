const express = require('express');
const router = express.Router();
const db = require('../db');

// Generador de código único de 6 caracteres
function generateGroupCode() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

// POST /api/grupos (Crear grupo)
router.post('/', async (req, res) => {
  const { nombre, descripcion } = req.body;
  if (!nombre) {
    return res.status(400).json({ error: 'El nombre del grupo es obligatorio' });
  }

  let code = generateGroupCode();
  let attempts = 0;
  
  // Reintentar si el código ya existe (límite de 5 intentos)
  while (attempts < 5) {
    try {
      const result = await db.query(
        'INSERT INTO grupo (nombre, codigo, descripcion) VALUES ($1, $2, $3) RETURNING id_grupo, nombre, codigo, descripcion',
        [nombre, code, descripcion]
      );
      const grupo = result.rows[0];
      return res.status(201).json({
        idGrupo: grupo.id_grupo,
        nombre: grupo.nombre,
        codigo: grupo.codigo,
        descripcion: grupo.descripcion
      });
    } catch (err) {
      if (err.code === '23505') { // Error de clave única duplicada en PostgreSQL
        code = generateGroupCode();
        attempts++;
      } else {
        console.error('Error al insertar grupo:', err);
        return res.status(500).json({ error: 'Error interno del servidor' });
      }
    }
  }
  res.status(500).json({ error: 'No se pudo generar un código único para el grupo' });
});

// POST /api/grupos/unirse (Unirse a grupo)
router.post('/unirse', async (req, res) => {
  const { idUsuario, codigo } = req.body;
  if (!idUsuario || !codigo) {
    return res.status(400).json({ error: 'Faltan campos obligatorios' });
  }

  try {
    // Buscar el grupo por código
    const groupResult = await db.query(
      'SELECT id_grupo FROM grupo WHERE codigo = $1',
      [codigo.toUpperCase()]
    );

    if (groupResult.rows.length === 0) {
      return res.status(404).json({ error: 'Código de grupo no encontrado' });
    }

    const idGrupo = groupResult.rows[0].id_grupo;

    // Relacionar usuario con el grupo
    await db.query(
      'INSERT INTO usuario_grupo (id_usuario, id_grupo, fecha_union) VALUES ($1, $2, NOW()) ON CONFLICT (id_usuario, id_grupo) DO NOTHING',
      [idUsuario, idGrupo]
    );

    res.status(200).send();
  } catch (error) {
    console.error('Error en /grupos/unirse:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/usuario/:idUsuario (Grupos del usuario)
router.get('/usuario/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      `SELECT g.id_grupo, g.nombre, g.codigo, g.descripcion 
       FROM grupo g 
       JOIN usuario_grupo ug ON g.id_grupo = ug.id_grupo 
       WHERE ug.id_usuario = $1`,
      [idUsuario]
    );

    const grupos = result.rows.map(grupo => ({
      idGrupo: grupo.id_grupo,
      nombre: grupo.nombre,
      codigo: grupo.codigo,
      descripcion: grupo.descripcion
    }));

    res.json(grupos);
  } catch (error) {
    console.error('Error en /grupos/usuario/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/:idGrupo/miembros (Miembros y rendimiento semanal)
router.get('/:idGrupo/miembros', async (req, res) => {
  const idGrupo = parseInt(req.params.idGrupo);
  if (isNaN(idGrupo)) {
    return res.status(400).json({ error: 'ID de grupo no válido' });
  }

  try {
    const query = `
      SELECT 
        u.id_usuario, 
        u.nombre, 
        u.nombre_usuario,
        COALESCE(SUM(e.distancia), 0) AS distancia,
        COALESCE(SUM(e.pasos), 0) AS pasos,
        COALESCE(SUM(e.calorias), 0) AS calorias,
        COALESCE(SUM(e.tiempo), 0) AS tiempo
      FROM usuario u
      JOIN usuario_grupo ug ON u.id_usuario = ug.id_usuario
      LEFT JOIN entrenamiento e ON u.id_usuario = e.id_usuario 
        AND e.fecha_inicio >= date_trunc('week', current_date)
      WHERE ug.id_grupo = $1
      GROUP BY u.id_usuario, u.nombre, u.nombre_usuario
      ORDER BY u.nombre ASC
    `;

    const result = await db.query(query, [idGrupo]);
    
    const miembros = result.rows.map(row => ({
      idUsuario: row.id_usuario,
      nombre: row.nombre,
      nombreUsuario: row.nombre_usuario,
      distancia: parseFloat(row.distancia),
      pasos: parseInt(row.pasos),
      calorias: parseInt(row.calorias),
      tiempo: parseInt(row.tiempo)
    }));

    res.json(miembros);
  } catch (error) {
    console.error('Error en /grupos/:idGrupo/miembros:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/:idGrupo/ranking (Miembros ordenados por distancia semanal)
router.get('/:idGrupo/ranking', async (req, res) => {
  const idGrupo = parseInt(req.params.idGrupo);
  if (isNaN(idGrupo)) {
    return res.status(400).json({ error: 'ID de grupo no válido' });
  }

  try {
    const query = `
      SELECT 
        u.id_usuario, 
        u.nombre, 
        u.nombre_usuario,
        COALESCE(SUM(e.distancia), 0) AS distancia,
        COALESCE(SUM(e.pasos), 0) AS pasos,
        COALESCE(SUM(e.calorias), 0) AS calorias,
        COALESCE(SUM(e.tiempo), 0) AS tiempo
      FROM usuario u
      JOIN usuario_grupo ug ON u.id_usuario = ug.id_usuario
      LEFT JOIN entrenamiento e ON u.id_usuario = e.id_usuario 
        AND e.fecha_inicio >= date_trunc('week', current_date)
      WHERE ug.id_grupo = $1
      GROUP BY u.id_usuario, u.nombre, u.nombre_usuario
      ORDER BY distancia DESC
    `;

    const result = await db.query(query, [idGrupo]);
    
    const miembros = result.rows.map(row => ({
      idUsuario: row.id_usuario,
      nombre: row.nombre,
      nombreUsuario: row.nombre_usuario,
      distancia: parseFloat(row.distancia),
      pasos: parseInt(row.pasos),
      calorias: parseInt(row.calorias),
      tiempo: parseInt(row.tiempo)
    }));

    res.json({ miembros });
  } catch (error) {
    console.error('Error en /grupos/:idGrupo/ranking:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
