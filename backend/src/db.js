const { Pool } = require('pg');
require('dotenv').config();

const useNeon = Boolean(process.env.DATABASE_URL);

const pool = new Pool(
  useNeon
    ? {
        connectionString: process.env.DATABASE_URL,
        max: 5,
        idleTimeoutMillis: 30_000,
        connectionTimeoutMillis: 10_000,
      }
    : {
        host: process.env.DB_HOST,
        port: parseInt(process.env.DB_PORT || '5432', 10),
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        database: process.env.DB_NAME,
      },
);

/** Informa cuando PostgreSQL acepta una nueva conexión del pool. */
pool.on('connect', () => {
  console.log(
    `Conectado a PostgreSQL correctamente (${useNeon ? 'Neon' : 'local'}).`,
  );
});

/** Registra errores inesperados producidos por el pool de PostgreSQL. */
pool.on('error', (err) => {
  console.error('Error inesperado en el cliente de PostgreSQL:', err);
});

module.exports = {
  /** Ejecuta una consulta parametrizada contra la base de datos. */
  query: (text, params) => pool.query(text, params),
  pool,
};
