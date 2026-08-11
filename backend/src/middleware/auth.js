const { verifyToken } = require('../authToken');

/** Protege una ruta exigiendo un token JWT de usuario válido. */
function requireUser(req, res, next) {
  const authorization = req.get('authorization') || '';
  const [scheme, token] = authorization.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'Se requiere iniciar sesión' });
  }

  try {
    const payload = verifyToken(token);
    if (payload.tipoToken !== 'usuario' || !payload.idUsuario) {
      return res.status(403).json({ error: 'Token de usuario requerido' });
    }
    req.auth = payload;
    next();
  } catch (error) {
    return res.status(401).json({ error: error.message });
  }
}

/** Protege una ruta exigiendo un token JWT emitido para un dispositivo. */
function requireDevice(req, res, next) {
  const authorization = req.get('authorization') || '';
  const [scheme, token] = authorization.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'Se requiere la sesión del dispositivo' });
  }
  try {
    const payload = verifyToken(token);
    if (payload.tipoToken !== 'dispositivo' || !payload.idDispositivo || !payload.idUsuario) {
      return res.status(403).json({ error: 'Token de dispositivo requerido' });
    }
    req.auth = payload;
    next();
  } catch (error) {
    return res.status(401).json({ error: error.message });
  }
}

module.exports = { requireUser, requireDevice };
