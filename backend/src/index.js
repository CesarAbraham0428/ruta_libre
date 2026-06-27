const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Inicializar la aplicación Express
const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Importar rutas
const authRoutes = require('./routes/auth');
const usuarioRoutes = require('./routes/usuarios');
const entrenamientoRoutes = require('./routes/entrenamientos');
const rutaRoutes = require('./routes/rutas');
const metaRoutes = require('./routes/metas');
const grupoRoutes = require('./routes/grupos');
const notificacionRoutes = require('./routes/notificaciones');

// Registrar rutas
app.use('/api/auth', authRoutes);
app.use('/api/usuarios', usuarioRoutes);
app.use('/api/entrenamientos', entrenamientoRoutes);
app.use('/api/rutas', rutaRoutes);
app.use('/api/metas', metaRoutes);
app.use('/api/grupos', grupoRoutes);
app.use('/api/notificaciones', notificacionRoutes);

// Ruta de estado de la API
app.get('/api/status', (req, res) => {
  res.json({
    status: 'online',
    timestamp: new Date().toISOString(),
    service: 'Ruta Libre REST API'
  });
});

// Manejo de errores global
app.use((err, req, res, next) => {
  console.error('Error global:', err.stack);
  res.status(500).json({ error: 'Algo salió mal en el servidor' });
});

// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Servidor de Ruta Libre escuchando en http://0.0.0.0:${PORT}`);
  console.log(`Endpoints disponibles bajo /api/`);
});
