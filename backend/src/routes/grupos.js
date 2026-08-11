const express = require('express');
const router = express.Router();
const db = require('../db');

const DAY_MAP = {
  1: 'Lun',
  2: 'Mar',
  3: 'Mie',
  4: 'Jue',
  5: 'Vie',
  6: 'Sab',
  7: 'Dom'
};

/** Genera un código alfanumérico corto para identificar un grupo. */
function generateGroupCode() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

// POST /api/grupos (Crear grupo)
/** Crea un grupo, registra opcionalmente a su creador y confirma la transacción. */
router.post('/', async (req, res) => {
  const idUsuarioRaw = req.body.idUsuario ?? req.body.id_usuario ?? req.body.idCreador ?? req.body.id_creador;
  const idUsuario = idUsuarioRaw !== undefined && idUsuarioRaw !== null ? parseInt(idUsuarioRaw, 10) : undefined;
  const { nombre, descripcion } = req.body;
  
  if (!nombre) {
    return res.status(400).json({ error: 'El nombre del grupo es obligatorio' });
  }

  if (idUsuario !== undefined && (isNaN(idUsuario) || !Number.isInteger(idUsuario) || idUsuario <= 0)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  let code = generateGroupCode();
  let attempts = 0;
  
  // Reintentar si el código ya existe (límite de 5 intentos)
  while (attempts < 5) {
    const client = await db.pool.connect();
    try {
      await client.query('BEGIN');
      const result = await client.query(
        'INSERT INTO grupo (nombre, codigo, descripcion, id_creador) VALUES ($1, $2, $3, $4) RETURNING id_grupo, nombre, codigo, descripcion, id_creador',
        [nombre, code, descripcion, idUsuario !== undefined ? idUsuario : null]
      );
      const grupo = result.rows[0];

      // El creador se integra automáticamente al grupo para que aparezca
      // inmediatamente en sus grupos y pueda consultar sus estadísticas.
      if (idUsuario !== undefined) {
        await client.query(
          'INSERT INTO usuario_grupo (id_usuario, id_grupo, fecha_union) VALUES ($1, $2, NOW())',
          [idUsuario, grupo.id_grupo]
        );
      }

      await client.query('COMMIT');
      return res.status(201).json({
        idGrupo: grupo.id_grupo,
        nombre: grupo.nombre,
        codigo: grupo.codigo,
        descripcion: grupo.descripcion,
        idCreador: grupo.id_creador
      });
    } catch (err) {
      await client.query('ROLLBACK').catch(() => {});
      if (err.code === '23505') { // Error de clave única duplicada en PostgreSQL
        code = generateGroupCode();
        attempts++;
      } else {
        console.error('Error al insertar grupo:', err);
        return res.status(500).json({ error: 'Error interno del servidor' });
      }
    } finally {
      client.release();
    }
  }
  res.status(500).json({ error: 'No se pudo generar un código único para el grupo' });
});

// POST /api/grupos/unirse (Unirse a grupo)
/** Añade al usuario a un grupo mediante su código de invitación. */
router.post('/unirse', async (req, res) => {
  const idUsuarioRaw = req.body.idUsuario ?? req.body.id_usuario ?? req.body.idCreador ?? req.body.id_creador;
  const idUsuario = idUsuarioRaw !== undefined && idUsuarioRaw !== null ? parseInt(idUsuarioRaw, 10) : undefined;
  const { codigo } = req.body;
  if (!idUsuario || !codigo || isNaN(idUsuario)) {
    return res.status(400).json({ error: 'Faltan campos obligatorios o son inválidos' });
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

// DELETE /api/grupos/:idGrupo/miembros/:idUsuario (Salir del grupo)
/** Elimina la relación entre un usuario y el grupo indicado. */
router.delete('/:idGrupo/miembros/:idUsuario', async (req, res) => {
  const idGrupo = parseInt(req.params.idGrupo);
  const idUsuario = parseInt(req.params.idUsuario);

  if (isNaN(idGrupo) || isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de grupo o usuario no válido' });
  }

  try {
    const result = await db.query(
      'DELETE FROM usuario_grupo WHERE id_grupo = $1 AND id_usuario = $2',
      [idGrupo, idUsuario]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'El usuario no pertenece al grupo' });
    }

    res.status(204).send();
  } catch (error) {
    console.error('Error en /grupos/:idGrupo/miembros/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/usuario/:idUsuario (Grupos del usuario)
/** Lista los grupos a los que pertenece un usuario. */
router.get('/usuario/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      `SELECT g.id_grupo, g.nombre, g.codigo, g.descripcion, g.id_creador 
       FROM grupo g 
       JOIN usuario_grupo ug ON g.id_grupo = ug.id_grupo 
       WHERE ug.id_usuario = $1`,
      [idUsuario]
    );

    const grupos = result.rows.map(grupo => ({
      idGrupo: grupo.id_grupo,
      nombre: grupo.nombre,
      codigo: grupo.codigo,
      descripcion: grupo.descripcion,
      idCreador: grupo.id_creador
    }));

    res.json(grupos);
  } catch (error) {
    console.error('Error en /grupos/usuario/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/:idGrupo/miembros (Miembros y rendimiento semanal)
/** Devuelve los miembros de un grupo junto con sus métricas semanales. */
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

// GET /api/grupos/:idGrupo/miembros/:idUsuario/estadisticas
/** Obtiene las estadísticas diarias y totales de un miembro del grupo. */
router.get('/:idGrupo/miembros/:idUsuario/estadisticas', async (req, res) => {
  const idGrupo = parseInt(req.params.idGrupo, 10);
  const idUsuario = parseInt(req.params.idUsuario, 10);

  if (!Number.isInteger(idGrupo) || idGrupo <= 0 || !Number.isInteger(idUsuario) || idUsuario <= 0) {
    return res.status(400).json({ error: 'ID de grupo o usuario no valido' });
  }

  try {
    const membershipResult = await db.query(
      'SELECT 1 FROM usuario_grupo WHERE id_grupo = $1 AND id_usuario = $2',
      [idGrupo, idUsuario]
    );

    if (membershipResult.rows.length === 0) {
      return res.status(404).json({ error: 'El usuario no pertenece al grupo' });
    }

    const [totalResult, dailyResult] = await Promise.all([
      db.query(
        `SELECT COALESCE(SUM(distancia), 0) AS distancia_total,
                COALESCE(SUM(pasos), 0) AS pasos_totales,
                COALESCE(SUM(calorias), 0) AS calorias_totales,
                COALESCE(SUM(tiempo), 0) AS tiempo_total
         FROM entrenamiento
         WHERE id_usuario = $1
           AND fecha_inicio >= date_trunc('week', current_date)`,
        [idUsuario]
      ),
      db.query(
        `SELECT EXTRACT(ISODOW FROM fecha_inicio) AS dia_num,
                COALESCE(SUM(distancia), 0) AS distancia,
                COALESCE(SUM(pasos), 0) AS pasos,
                COALESCE(SUM(calorias), 0) AS calorias,
                COALESCE(SUM(tiempo), 0) AS tiempo
         FROM entrenamiento
         WHERE id_usuario = $1
           AND fecha_inicio >= date_trunc('week', current_date)
         GROUP BY EXTRACT(ISODOW FROM fecha_inicio)
         ORDER BY dia_num`,
        [idUsuario]
      )
    ]);

    const totals = totalResult.rows[0];
    const rendimientoDiario = Array.from({ length: 7 }, (_, indice) => ({
      dia: DAY_MAP[indice + 1],
      distancia: 0.0,
      pasos: 0,
      calorias: 0,
      tiempo: 0
    }));

    for (const row of dailyResult.rows) {
      const indice = parseInt(row.dia_num, 10) - 1;
      if (indice >= 0 && indice < rendimientoDiario.length) {
        rendimientoDiario[indice] = {
          dia: DAY_MAP[indice + 1],
          distancia: parseFloat(row.distancia),
          pasos: parseInt(row.pasos, 10),
          calorias: parseInt(row.calorias, 10),
          tiempo: parseInt(row.tiempo, 10)
        };
      }
    }

    res.json({
      distanciaTotal: parseFloat(totals.distancia_total),
      pasosTotales: parseInt(totals.pasos_totales, 10),
      caloriasTotales: parseInt(totals.calorias_totales, 10),
      tiempoTotal: parseInt(totals.tiempo_total, 10),
      rendimientoDiario
    });
  } catch (error) {
    console.error('Error en estadisticas semanales del miembro:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/grupos/:idGrupo/ranking (Miembros ordenados por distancia semanal)
/** Ordena a los miembros del grupo según su distancia semanal acumulada. */
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

// DELETE /api/grupos/:idGrupo (Eliminar grupo)
/** Elimina un grupo después de verificar que el usuario sea su creador. */
router.delete('/:idGrupo', async (req, res) => {
  const idGrupo = parseInt(req.params.idGrupo);
  const idUsuarioRaw = req.query.idUsuario ?? req.body.idUsuario ?? req.query.id_usuario ?? req.body.id_usuario ?? req.query.idCreador ?? req.body.idCreador;
  const idUsuario = idUsuarioRaw !== undefined && idUsuarioRaw !== null ? parseInt(idUsuarioRaw, 10) : NaN;

  if (isNaN(idGrupo) || isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de grupo o usuario no válido' });
  }

  try {
    // Verificar si el usuario es el creador del grupo
    const groupResult = await db.query(
      'SELECT id_creador FROM grupo WHERE id_grupo = $1',
      [idGrupo]
    );

    if (groupResult.rows.length === 0) {
      return res.status(404).json({ error: 'Grupo no encontrado' });
    }

    const idCreador = groupResult.rows[0].id_creador;
    if (idCreador !== idUsuario) {
      return res.status(403).json({ error: 'No tienes permisos para eliminar este grupo' });
    }

    // Eliminar el grupo (las tablas relacionadas tienen ON DELETE CASCADE)
    await db.query('DELETE FROM grupo WHERE id_grupo = $1', [idGrupo]);

    res.status(204).send();
  } catch (error) {
    console.error('Error en DELETE /grupos/:idGrupo:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
