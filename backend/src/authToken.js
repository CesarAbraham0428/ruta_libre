const crypto = require('crypto');

/** Obtiene el secreto configurado y valida su longitud mínima. */
function getSecret() {
  const secret = process.env.JWT_SECRET;
  if (!secret || secret.length < 32) {
    throw new Error('JWT_SECRET debe tener al menos 32 caracteres');
  }
  return secret;
}

/** Convierte un valor JSON a la representación base64url usada por el token. */
function encode(value) {
  return Buffer.from(JSON.stringify(value)).toString('base64url');
}

/** Firma un token JWT con HMAC-SHA256 y añade sus fechas de emisión y expiración. */
function signToken(payload, expiresInSeconds = 7 * 24 * 60 * 60) {
  const now = Math.floor(Date.now() / 1000);
  const header = encode({ alg: 'HS256', typ: 'JWT' });
  const body = encode({ ...payload, iat: now, exp: now + expiresInSeconds });
  const signature = crypto
    .createHmac('sha256', getSecret())
    .update(`${header}.${body}`)
    .digest('base64url');
  return `${header}.${body}.${signature}`;
}

/** Verifica la estructura, firma y vigencia de un token JWT. */
function verifyToken(token) {
  const parts = token?.split('.') || [];
  if (parts.length !== 3) throw new Error('Token no válido');

  const [header, body, signature] = parts;
  const expected = crypto
    .createHmac('sha256', getSecret())
    .update(`${header}.${body}`)
    .digest('base64url');

  const receivedBuffer = Buffer.from(signature);
  const expectedBuffer = Buffer.from(expected);
  if (
    receivedBuffer.length !== expectedBuffer.length ||
    !crypto.timingSafeEqual(receivedBuffer, expectedBuffer)
  ) {
    throw new Error('Firma de token no válida');
  }

  const payload = JSON.parse(Buffer.from(body, 'base64url').toString('utf8'));
  if (!payload.exp || payload.exp <= Math.floor(Date.now() / 1000)) {
    throw new Error('Token expirado');
  }
  return payload;
}

module.exports = { signToken, verifyToken };
