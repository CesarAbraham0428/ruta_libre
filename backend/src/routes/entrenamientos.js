const express = require('express');
const router = express.Router();
const db = require('../db');

// Helper para mapear número de día ISO (1=Lunes, 7=Domingo) a abreviación en español
const DAY_MAP = {
  1: 'Lun',
  2: 'Mar',
  3: 'Mie',
  4: 'Jue',
  5: 'Vie',
  6: 'Sab',
  7: 'Dom'
};

// POST /api/entrenamientos/iniciar (Iniciar entrenamiento)
router.post('/iniciar', async (req, res) => {
  const { idUsuario } = req.body;
  if (!idUsuario) {
    return res.status(400).json({ error: 'El ID de usuario es obligatorio' });
  }

  try {
    const result = await db.query(
      `INSERT INTO entrenamiento (id_usuario, fecha_inicio, pasos, calorias, distancia, tiempo) 
       VALUES ($1, NOW(), 0, 0, 0, 0) 
       RETURNING id_entrenamiento, id_usuario, id_ruta, pasos, calorias, distancia, fecha_inicio, tiempo`,
      [idUsuario]
    );

    const ent = result.rows[0];
    res.status(201).json({
      idEntrenamiento: ent.id_entrenamiento,
      idUsuario: ent.id_usuario,
      idRuta: ent.id_ruta,
      pasos: ent.pasos,
      calorias: ent.calorias,
      distancia: parseFloat(ent.distancia),
      fechaInicio: ent.fecha_inicio.toISOString(),
      tiempo: ent.tiempo,
      puntoInicioLat: null,
      puntoInicioLng: null,
      puntoFinLat: null,
      puntoFinLng: null
    });
  } catch (error) {
    console.error('Error en /entrenamientos/iniciar:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// PUT /api/entrenamientos/finalizar (Finalizar entrenamiento y actualizar metas)
router.put('/finalizar', async (req, res) => {
  const { 
    idEntrenamiento, 
    pasos, 
    calorias, 
    distancia, 
    tiempo, 
    coordenadas, 
    puntoInicio, 
    puntoFin 
  } = req.body;

  if (idEntrenamiento === undefined) {
    return res.status(400).json({ error: 'El ID de entrenamiento es obligatorio' });
  }

  try {
    await db.query('BEGIN'); // Iniciar transacción

    // 1. Guardar la ruta si contiene coordenadas
    let idRuta = null;
    if (coordenadas && coordenadas.length > 0) {
      const rutaResult = await db.query(
        'INSERT INTO ruta (coordenadas) VALUES ($1) RETURNING id_ruta',
        [JSON.stringify(coordenadas)]
      );
      idRuta = rutaResult.rows[0].id_ruta;
    }

    // 2. Preparar puntos geométricos (PostGIS)
    let puntoInicioQuery = null;
    let puntoFinQuery = null;
    const params = [pasos, calorias, distancia, tiempo, idRuta, idEntrenamiento];

    if (puntoInicio && (puntoInicio.longitud !== 0 || puntoInicio.latitud !== 0)) {
      puntoInicioQuery = 'ST_SetSRID(ST_MakePoint($7, $8), 4326)';
      params.push(puntoInicio.longitud, puntoInicio.latitud);
    }
    
    const nextIndex = params.length + 1;
    if (puntoFin && (puntoFin.longitud !== 0 || puntoFin.latitud !== 0)) {
      puntoFinQuery = `ST_SetSRID(ST_MakePoint($${nextIndex}, $${nextIndex + 1}), 4326)`;
      params.push(puntoFin.longitud, puntoFin.latitud);
    }

    // 3. Actualizar entrenamiento
    const updateQuery = `
      UPDATE entrenamiento 
      SET pasos = $1, 
          calorias = $2, 
          distancia = $3, 
          tiempo = $4, 
          id_ruta = $5
          ${puntoInicioQuery ? `, punto_inicio = ${puntoInicioQuery}` : ''}
          ${puntoFinQuery ? `, punto_fin = ${puntoFinQuery}` : ''}
      WHERE id_entrenamiento = $6
      RETURNING id_entrenamiento, id_usuario, id_ruta, pasos, calorias, distancia, fecha_inicio, tiempo,
                ST_Y(punto_inicio) AS lat_ini, ST_X(punto_inicio) AS lng_ini,
                ST_Y(punto_fin) AS lat_fin, ST_X(punto_fin) AS lng_fin
    `;

    const entResult = await db.query(updateQuery, params);

    if (entResult.rows.length === 0) {
      await db.query('ROLLBACK');
      return res.status(404).json({ error: 'Entrenamiento no encontrado' });
    }

    const ent = entResult.rows[0];
    const idUsuario = ent.id_usuario;

    // 4. Actualizar metas activas de este usuario
    const metasResult = await db.query(
      'SELECT id_metas, tipo_meta, valor_objetivo, valor_actual FROM metas WHERE id_usuario = $1 AND terminada = FALSE',
      [idUsuario]
    );

    for (const meta of metasResult.rows) {
      let incremento = 0;
      if (meta.tipo_meta === 'distancia') incremento = distancia;
      else if (meta.tipo_meta === 'pasos') incremento = pasos;
      else if (meta.tipo_meta === 'calorias') incremento = calorias;
      else if (meta.tipo_meta === 'tiempo') incremento = tiempo / 60.0; // segundos a minutos

      if (incremento > 0) {
        const nuevoValor = parseFloat(meta.valor_actual) + incremento;
        const terminada = nuevoValor >= parseFloat(meta.valor_objetivo);

        // Guardar progreso en la base de datos
        await db.query(
          'UPDATE metas SET valor_actual = $1, terminada = $2 WHERE id_metas = $3',
          [nuevoValor, terminada, meta.id_metas]
        );

        // Si se completa la meta, generar notificación de logro
        if (terminada) {
          const tipoLabel = meta.tipo_meta.charAt(0).toUpperCase() + meta.tipo_meta.slice(1);
          const mensaje = `¡Felicidades! Has completado tu meta diaria de ${tipoLabel} (${meta.valor_objetivo}).`;
          
          await db.query(
            'INSERT INTO notificacion (id_usuario, id_metas, mensaje, fecha_creacion, leida_movil, leida_smartwatch) VALUES ($1, $2, $3, NOW(), FALSE, FALSE)',
            [idUsuario, meta.id_metas, mensaje]
          );
        }
      }
    }

    await db.query('COMMIT'); // Confirmar transacción

    res.json({
      idEntrenamiento: ent.id_entrenamiento,
      idUsuario: ent.id_usuario,
      idRuta: ent.id_ruta,
      pasos: ent.pasos,
      calorias: ent.calorias,
      distancia: parseFloat(ent.distancia),
      fechaInicio: ent.fecha_inicio.toISOString(),
      tiempo: ent.tiempo,
      puntoInicioLat: ent.lat_ini ? parseFloat(ent.lat_ini) : null,
      puntoInicioLng: ent.lng_ini ? parseFloat(ent.lng_ini) : null,
      puntoFinLat: ent.lat_fin ? parseFloat(ent.lat_fin) : null,
      puntoFinLng: ent.lng_fin ? parseFloat(ent.lng_fin) : null
    });

  } catch (error) {
    await db.query('ROLLBACK');
    console.error('Error en /entrenamientos/finalizar:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/entrenamientos/activo/:idUsuario
router.get('/activo/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    // Un entrenamiento se considera activo si su tiempo registrado es 0
    const result = await db.query(
      `SELECT id_entrenamiento, fecha_inicio, pasos, calorias, distancia 
       FROM entrenamiento 
       WHERE id_usuario = $1 AND tiempo = 0 
       ORDER BY fecha_inicio DESC LIMIT 1`,
      [idUsuario]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'No hay entrenamiento activo' });
    }

    const ent = result.rows[0];
    res.json({
      idEntrenamiento: ent.id_entrenamiento,
      fechaInicio: ent.fecha_inicio.toISOString(),
      pasos: ent.pasos,
      calorias: ent.calorias,
      distancia: parseFloat(ent.distancia)
    });
  } catch (error) {
    console.error('Error en /entrenamientos/activo/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/entrenamientos/usuario/:idUsuario (Historial de entrenamientos)
router.get('/usuario/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    const result = await db.query(
      `SELECT id_entrenamiento, id_usuario, id_ruta, pasos, calorias, distancia, fecha_inicio, tiempo,
              ST_Y(punto_inicio) AS lat_ini, ST_X(punto_inicio) AS lng_ini,
              ST_Y(punto_fin) AS lat_fin, ST_X(punto_fin) AS lng_fin
       FROM entrenamiento 
       WHERE id_usuario = $1 
       ORDER BY fecha_inicio DESC`,
      [idUsuario]
    );

    const historial = result.rows.map(ent => ({
      idEntrenamiento: ent.id_entrenamiento,
      idUsuario: ent.id_usuario,
      idRuta: ent.id_ruta,
      pasos: ent.pasos,
      calorias: ent.calorias,
      distancia: parseFloat(ent.distancia),
      fechaInicio: ent.fecha_inicio.toISOString(),
      tiempo: ent.tiempo,
      puntoInicioLat: ent.lat_ini ? parseFloat(ent.lat_ini) : null,
      puntoInicioLng: ent.lng_ini ? parseFloat(ent.lng_ini) : null,
      puntoFinLat: ent.lat_fin ? parseFloat(ent.lat_fin) : null,
      puntoFinLng: ent.lng_fin ? parseFloat(ent.lng_fin) : null
    }));

    res.json(historial);
  } catch (error) {
    console.error('Error en /entrenamientos/usuario/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/entrenamientos/semana/:idUsuario (Dashboard semanal)
router.get('/semana/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    // 1. Obtener totales acumulados de la semana actual (desde el lunes)
    const totalResult = await db.query(
      `SELECT COALESCE(SUM(distancia), 0) AS dist_tot,
              COALESCE(SUM(pasos), 0) AS pasos_tot,
              COALESCE(SUM(calorias), 0) AS cal_tot,
              COALESCE(SUM(tiempo), 0) AS tiempo_tot
       FROM entrenamiento
       WHERE id_usuario = $1 AND fecha_inicio >= date_trunc('week', current_date)`,
      [idUsuario]
    );

    const totals = totalResult.rows[0];

    // 2. Obtener el rendimiento por día de la semana actual
    const dailyResult = await db.query(
      `SELECT EXTRACT(ISODOW FROM fecha_inicio) AS dia_num,
              COALESCE(SUM(distancia), 0) AS distancia,
              COALESCE(SUM(pasos), 0) AS pasos,
              COALESCE(SUM(calorias), 0) AS calorias,
              COALESCE(SUM(tiempo), 0) AS tiempo
       FROM entrenamiento
       WHERE id_usuario = $1 AND fecha_inicio >= date_trunc('week', current_date)
       GROUP BY EXTRACT(ISODOW FROM fecha_inicio)
       ORDER BY dia_num`,
      [idUsuario]
    );

    // Inicializar el rendimiento diario con valores en cero para todos los 7 días
    const rendimientoDiario = [];
    for (let i = 1; i <= 7; i++) {
      rendimientoDiario.push({
        dia: DAY_MAP[i],
        distancia: 0.0,
        pasos: 0,
        calorias: 0,
        tiempo: 0
      });
    }

    // Sobrescribir con los datos reales obtenidos de las consultas
    for (const row of dailyResult.rows) {
      const idx = parseInt(row.dia_num) - 1;
      if (idx >= 0 && idx < 7) {
        rendimientoDiario[idx] = {
          dia: DAY_MAP[row.dia_num],
          distancia: parseFloat(row.distancia),
          pasos: parseInt(row.pasos),
          calorias: parseInt(row.calorias),
          tiempo: parseInt(row.tiempo)
        };
      }
    }

    res.json({
      distanciaTotal: parseFloat(totals.dist_tot),
      pasosTotales: parseInt(totals.pasos_tot),
      caloriasTotales: parseInt(totals.cal_tot),
      tiempoTotal: parseInt(totals.tiempo_tot),
      rendimientoDiario
    });

  } catch (error) {
    console.error('Error en /entrenamientos/semana/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// GET /api/entrenamientos/comparacion/:idUsuario (Comparación de rendimiento semanal)
router.get('/comparacion/:idUsuario', async (req, res) => {
  const idUsuario = parseInt(req.params.idUsuario);
  if (isNaN(idUsuario)) {
    return res.status(400).json({ error: 'ID de usuario no válido' });
  }

  try {
    // 1. Semana actual (desde el lunes de esta semana)
    const currentWeekResult = await db.query(
      `SELECT COALESCE(SUM(distancia), 0) AS dist,
              COALESCE(SUM(pasos), 0) AS pasos,
              COALESCE(SUM(calorias), 0) AS cal,
              COALESCE(SUM(tiempo), 0) AS tiempo
       FROM entrenamiento
       WHERE id_usuario = $1 AND fecha_inicio >= date_trunc('week', current_date)`,
      [idUsuario]
    );
    const curr = currentWeekResult.rows[0];

    // 2. Semana anterior (desde el lunes de la semana pasada hasta el lunes de esta semana)
    const previousWeekResult = await db.query(
      `SELECT COALESCE(SUM(distancia), 0) AS dist,
              COALESCE(SUM(pasos), 0) AS pasos,
              COALESCE(SUM(calorias), 0) AS cal,
              COALESCE(SUM(tiempo), 0) AS tiempo
       FROM entrenamiento
       WHERE id_usuario = $1 
         AND fecha_inicio >= date_trunc('week', current_date - interval '1 week')
         AND fecha_inicio < date_trunc('week', current_date)`,
      [idUsuario]
    );
    const prev = previousWeekResult.rows[0];

    // Función auxiliar para calcular porcentaje de mejora
    const calcMejora = (c, p) => {
      const currentVal = parseFloat(c);
      const prevVal = parseFloat(p);
      if (prevVal === 0) {
        return currentVal > 0 ? 100.0 : 0.0;
      }
      return parseFloat((((currentVal - prevVal) / prevVal) * 100).toFixed(2));
    };

    res.json({
      distanciaMejora: calcMejora(curr.dist, prev.dist),
      pasosMejora: calcMejora(curr.pasos, prev.pasos),
      caloriasMejora: calcMejora(curr.cal, prev.cal),
      tiempoMejora: calcMejora(curr.tiempo, prev.tiempo)
    });

  } catch (error) {
    console.error('Error en /entrenamientos/comparacion/:idUsuario:', error);
    res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
