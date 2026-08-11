const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Inicializar la aplicación Express
const app = express();
const PORT = process.env.PORT || 3000;
const mqttService = require('./mqtt');

/** Configura los middlewares globales para CORS y lectura de cuerpos HTTP. */
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

/** Carga los módulos de rutas que exponen las funcionalidades de Ruta Libre. */
const authRoutes = require('./routes/auth');
const usuarioRoutes = require('./routes/usuarios');
const entrenamientoRoutes = require('./routes/entrenamientos');
const rutaRoutes = require('./routes/rutas');
const metaRoutes = require('./routes/metas');
const grupoRoutes = require('./routes/grupos');
const notificacionRoutes = require('./routes/notificaciones');
const dispositivoRoutes = require('./routes/dispositivos');

/** Publica los módulos de rutas bajo el prefijo común de la API. */
app.use('/api/auth', authRoutes);
app.use('/api/usuarios', usuarioRoutes);
app.use('/api/entrenamientos', entrenamientoRoutes);
app.use('/api/rutas', rutaRoutes);
app.use('/api/metas', metaRoutes);
app.use('/api/grupos', grupoRoutes);
app.use('/api/notificaciones', notificacionRoutes);
app.use('/api/dispositivos', dispositivoRoutes);

// Ruta de estado de la API
/** Devuelve el estado del servidor REST y de la conexión MQTT. */
app.get('/api/status', (req, res) => {
  res.json({
    status: 'online',
    timestamp: new Date().toISOString(),
    service: 'Ruta Libre REST API',
    mqtt: mqttService.estaConectado() ? 'connected' : 'disconnected'
  });
});

// Manejo de errores global
/** Convierte los errores no controlados en una respuesta HTTP uniforme. */
app.use((err, req, res, next) => {
  console.error('Error global:', err.stack);
  res.status(500).json({ error: 'Algo salió mal en el servidor' });
});

// Iniciar servidor
/** Inicia el servidor HTTP y el cliente MQTT del backend. */
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Servidor de Ruta Libre escuchando en http://0.0.0.0:${PORT}`);
  console.log(`Endpoints disponibles bajo /api/`);
  mqttService.iniciarMqtt();
});
