const express = require('express');
const crypto = require('crypto');
const db = require('../db');
const { signToken } = require('../authToken');
const { requireUser, requireDevice } = require('../middleware/auth');
const mqttService = require('../mqtt');

const router = express.Router();
const CODE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

function randomCode(length = 6) {
  return Array.from(
    crypto.randomBytes(length),
    byte => CODE_ALPHABET[byte % CODE_ALPHABET.length]
  ).join('');
}

function hashSecret(secret) {
  return crypto.createHash('sha256').update(secret).digest('hex');
}

function secretsMatch(secret, expectedHash) {
  const received = Buffer.from(hashSecret(secret));
  const expected = Buffer.from(expectedHash || '');
  return received.length === expected.length && crypto.timingSafeEqual(received, expected);
}

// TV solicita un código temporal y conserva el secreto para consultar el resultado.
router.post('/solicitar-vinculacion', async (req, res) => {
  const { tipo = 'tv', nombre = 'Ruta Libre TV' } = req.body;
  if (tipo !== 'tv') {
    return res.status(400).json({ error: 'Este endpoint solo genera códigos para TV' });
  }

  const deviceSecret = crypto.randomBytes(32).toString('base64url');
  const secretHash = hashSecret(deviceSecret);

  try {
    for (let attempt = 0; attempt < 5; attempt += 1) {
      const code = randomCode();
      try {
        const result = await db.query(
          `INSERT INTO dispositivo
             (tipo, nombre, codigo_vinculacion, codigo_expira, token_hash)
           VALUES ($1, $2, $3, NOW() + INTERVAL '10 minutes', $4)
           RETURNING id_dispositivo, codigo_vinculacion, codigo_expira`,
          [tipo, nombre, code, secretHash]
        );
        const device = result.rows[0];
        return res.status(201).json({
          idDispositivo: device.id_dispositivo,
          codigo: device.codigo_vinculacion,
          expira: device.codigo_expira.toISOString(),
          secreto: deviceSecret
        });
      } catch (error) {
        if (error.code !== '23505') throw error;
      }
    }
    return res.status(503).json({ error: 'No se pudo generar un código único' });
  } catch (error) {
    console.error('Error al solicitar vinculación:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// La TV consulta si el celular ya autorizó el código.
router.post('/estado-vinculacion', async (req, res) => {
  const { idDispositivo, secreto } = req.body;
  if (!idDispositivo || !secreto) {
    return res.status(400).json({ error: 'Faltan datos del dispositivo' });
  }

  try {
    const result = await db.query(
      `SELECT id_dispositivo, id_usuario, tipo, token_hash, vinculado, activo, codigo_expira
       FROM dispositivo WHERE id_dispositivo = $1`,
      [idDispositivo]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Dispositivo no encontrado' });
    }

    const device = result.rows[0];
    if (!device.activo || !secretsMatch(secreto, device.token_hash)) {
      return res.status(401).json({ error: 'Credenciales de dispositivo no válidas' });
    }
    if (!device.vinculado) {
      if (device.codigo_expira && device.codigo_expira <= new Date()) {
        return res.json({ estado: 'expirado' });
      }
      return res.json({ estado: 'pendiente' });
    }

    const token = signToken({
      tipoToken: 'dispositivo',
      idDispositivo: device.id_dispositivo,
      idUsuario: device.id_usuario,
      tipo: device.tipo
    }, 30 * 24 * 60 * 60);

    return res.json({
      estado: 'vinculado',
      idUsuario: device.id_usuario,
      token
    });
  } catch (error) {
    console.error('Error al consultar vinculación:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// El celular autoriza el código usando el usuario autenticado, nunca un ID del body.
router.post('/vincular', requireUser, async (req, res) => {
  const code = String(req.body.codigo || '').trim().toUpperCase();
  if (!code) return res.status(400).json({ error: 'El código es obligatorio' });

  try {
    const result = await db.query(
      `UPDATE dispositivo
       SET id_usuario = $1,
           vinculado = TRUE,
           fecha_vinculacion = NOW(),
           codigo_vinculacion = NULL,
           codigo_expira = NULL
       WHERE codigo_vinculacion = $2
         AND vinculado = FALSE
         AND activo = TRUE
         AND codigo_expira > NOW()
       RETURNING id_dispositivo, tipo, nombre, fecha_vinculacion`,
      [req.auth.idUsuario, code]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Código inválido o expirado' });
    }
    const device = result.rows[0];
    return res.json({
      idDispositivo: device.id_dispositivo,
      tipo: device.tipo,
      nombre: device.nombre,
      fechaVinculacion: device.fecha_vinculacion.toISOString()
    });
  } catch (error) {
    console.error('Error al vincular dispositivo:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

// El celular crea directamente la identidad del reloj emparejado.
router.post('/vincular-wear', requireUser, async (req, res) => {
  const name = String(req.body.nombre || 'Ruta Libre Wear OS').trim();
  const deviceSecret = crypto.randomBytes(32).toString('base64url');
  try {
    const existing = await db.query(
      `SELECT id_dispositivo FROM dispositivo
       WHERE id_usuario = $1 AND tipo = 'wear' AND activo = TRUE
       ORDER BY fecha_vinculacion DESC NULLS LAST LIMIT 1`,
      [req.auth.idUsuario]
    );
    const result = existing.rows.length > 0
      ? existing
      : await db.query(
          `INSERT INTO dispositivo
             (id_usuario, tipo, nombre, token_hash, vinculado, fecha_vinculacion)
           VALUES ($1, 'wear', $2, $3, TRUE, NOW())
           RETURNING id_dispositivo`,
          [req.auth.idUsuario, name, hashSecret(deviceSecret)]
        );
    const idDispositivo = result.rows[0].id_dispositivo;
    const token = signToken({
      tipoToken: 'dispositivo',
      idDispositivo,
      idUsuario: req.auth.idUsuario,
      tipo: 'wear'
    }, 30 * 24 * 60 * 60);
    return res.status(201).json({
      idDispositivo,
      idUsuario: req.auth.idUsuario,
      token
    });
  } catch (error) {
    console.error('Error al vincular Wear OS:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

router.get('/', requireUser, async (req, res) => {
  try {
    const result = await db.query(
      `SELECT id_dispositivo, tipo, nombre, activo, fecha_vinculacion
       FROM dispositivo
       WHERE id_usuario = $1 AND vinculado = TRUE AND activo = TRUE
       ORDER BY fecha_vinculacion DESC`,
      [req.auth.idUsuario]
    );
    return res.json(result.rows.map(device => ({
      idDispositivo: device.id_dispositivo,
      tipo: device.tipo,
      nombre: device.nombre,
      activo: device.activo,
      fechaVinculacion: device.fecha_vinculacion?.toISOString() || null
    })));
  } catch (error) {
    console.error('Error al listar dispositivos:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

router.delete('/sesion/actual', requireDevice, async (req, res) => {
  try {
    await db.query(
      `UPDATE dispositivo SET activo = FALSE
       WHERE id_dispositivo = $1 AND id_usuario = $2`,
      [req.auth.idDispositivo, req.auth.idUsuario]
    );
    return res.status(204).send();
  } catch (error) {
    console.error('Error al cerrar sesión del dispositivo:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

router.get('/sesion/actual', requireDevice, async (req, res) => {
  try {
    const result = await db.query(
      `SELECT 1 FROM dispositivo
       WHERE id_dispositivo = $1 AND id_usuario = $2 AND activo = TRUE AND vinculado = TRUE`,
      [req.auth.idDispositivo, req.auth.idUsuario]
    );
    if (result.rows.length === 0) return res.status(401).json({ error: 'Sesión revocada' });
    return res.status(204).send();
  } catch (error) {
    console.error('Error al validar sesión del dispositivo:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

router.delete('/sesion/todos', requireUser, async (req, res) => {
  try {
    await db.query(
      `UPDATE dispositivo SET activo = FALSE
       WHERE id_usuario = $1 AND activo = TRUE`,
      [req.auth.idUsuario]
    );
    mqttService.publicarEvento(
      `rutalibre/usuarios/${req.auth.idUsuario}/sesion/cerrada`,
      { idUsuario: req.auth.idUsuario }
    );
    return res.status(204).send();
  } catch (error) {
    console.error('Error al cerrar todas las sesiones:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

router.delete('/:idDispositivo', requireUser, async (req, res) => {
  try {
    const result = await db.query(
      `UPDATE dispositivo SET activo = FALSE
       WHERE id_dispositivo = $1 AND id_usuario = $2
       RETURNING id_dispositivo`,
      [req.params.idDispositivo, req.auth.idUsuario]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Dispositivo no encontrado' });
    }
    mqttService.publicarEvento(
      `rutalibre/usuarios/${req.auth.idUsuario}/dispositivos/${req.params.idDispositivo}/desvinculado`,
      { idDispositivo: req.params.idDispositivo }
    );
    return res.status(204).send();
  } catch (error) {
    console.error('Error al desvincular dispositivo:', error);
    return res.status(500).json({ error: 'Error interno del servidor' });
  }
});

module.exports = router;
