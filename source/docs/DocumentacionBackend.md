# Backend API REST y MQTT — Ruta Libre

Documentación técnica del backend desarrollado en Node.js + Express + PostgreSQL con PostGIS, JWT Auth y comunicación MQTT en tiempo real con HiveMQ Cloud.

---

## Índice

1. [Descripción general](#descripción-general)
2. [Tecnologías](#tecnologías)
3. [Configuración del proyecto](#configuración-del-proyecto)
4. [Arquitectura](#arquitectura)
5. [Servicio MQTT](#servicio-mqtt)
6. [Base de datos](#base-de-datos)
7. [Autenticación y Seguridad (JWT)](#autenticación-y-seguridad-jwt)
8. [Endpoints](#endpoints)
   - [Estado de la API](#estado-de-la-api)
   - [Auth](#auth)
   - [Usuarios](#usuarios)
   - [Dispositivos y Vinculación](#dispositivos-y-vinculación)
   - [Entrenamientos](#entrenamientos)
   - [Rutas](#rutas)
   - [Metas](#metas)
   - [Grupos](#grupos)
   - [Notificaciones](#notificaciones)
9. [Flujos del sistema](#flujos-del-sistema)
   - [Flujo de vinculación de dispositivos (Android TV y Wear OS)](#flujo-de-vinculación-de-dispositivos-android-tv-y-wear-os)
   - [Flujo de registro y finalización de entrenamiento](#flujo-de-registro-y-finalización-de-entrenamiento)
10. [Manejo de errores](#manejo-de-errores)
11. [Consideraciones sobre Contenido Multimedia y APIs de Terceros](#consideraciones-sobre-contenido-multimedia-y-apis-de-terceros)

---

## Descripción general

Backend monolítico en Node.js + Express que expone una API REST e integra pub/sub en tiempo real mediante MQTT (HiveMQ Cloud) para las tres aplicaciones del ecosistema **Ruta Libre**:
- **Aplicación Móvil (Android):** Registro, seguimiento GPS, gestión de perfil/metas/grupos y vinculación de pantallas/relojes.
- **Smartwatch (Wear OS):** Monitoreo de sensores de salud, registro de entrenamientos y notificaciones instantáneas.
- **Smart TV (Android TV):** Visualización de métricas en pantalla grande mediante sincronización de sesión por código temporal.

Se comunica con PostgreSQL 16 + PostGIS (soporta hosting local o en la nube como Neon DB) para el almacenamiento relacional, consultas geoespaciales y persistencia de métricas.

---

## Tecnologías

| Componente | Tecnología | Versión / Detalle |
|---|---|---|
| Runtime | Node.js | 20+ |
| Framework web | Express | 4.19.x |
| Cliente PostgreSQL | `pg` | 8.11.x |
| Extensión Geoespacial | PostGIS | 3.x |
| Mensajería MQTT | `mqtt` | 5.14.x (Conexión segura TLS/MQTTS con HiveMQ Cloud) |
| Cifrado de Contraseñas | `bcryptjs` | 3.0.x |
| Autenticación | Custom JWT (HMAC-SHA256 via `crypto`) | Tokens sign/verify |
| Variables de Entorno | `dotenv` | 16.4.x |
| CORS | `cors` | 2.8.x |
| Dev | `nodemon` | 3.1.x |
| Base de Datos Cloud | Neon PostgreSQL / Local | PostgreSQL 16 |

---

## Configuración del proyecto

### Archivo `.env`

```env
# Conexión a Base de Datos (Neon DB Cloud o PostgreSQL local)
DATABASE_URL=url_database
channel_binding=require

# Variables para conexión local alternativa
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=********
DB_NAME=rutaLibre

# Puerto del servidor backend
PORT=3000

# Secreto para firma de tokens JWT (mínimo 32 caracteres)
JWT_SECRET=clave_secreta

# Configuración del Broker MQTT (HiveMQ Cloud TLS)
MQTT_HOST=host_mqtt
MQTT_PORT=8883
MQTT_USERNAME=rutalibre
MQTT_PASSWORD=rutalibre
```

### Scripts disponibles

```bash
npm start       # Inicia el servidor en producción (node src/index.js)
npm run dev     # Inicia en modo desarrollo con nodemon (recarga automática)
```

### Inicialización

```bash
cd backend
npm install
npm run dev
```

El servidor escucha en `http://0.0.0.0:3000` (aceptando peticiones locales y desde emuladores Android `10.0.2.2`).

---

## Arquitectura

```
backend/
├── .env                        # Variables de entorno
├── package.json
├── src/
│   ├── index.js                # Punto de entrada, middlewares global y servidor HTTP/MQTT
│   ├── db.js                   # Conexión adaptativa a PostgreSQL (Pool / Neon Cloud)
│   ├── authToken.js            # Firma y verificación de Tokens JWT (crypto HMAC-SHA256)
│   ├── mqtt.js                 # Cliente MQTT (conexion segura MQTTS y publicación de eventos)
│   ├── api_test.js             # Script de prueba de integración de endpoints
│   ├── middleware/
│   │   └── auth.js             # Middlewares `requireUser` y `requireDevice`
│   └── routes/
│       ├── auth.js             # Registro e inicio de sesión (bcrypt + JWT)
│       ├── usuarios.js         # Perfil de usuario, actualización de peso y credenciales
│       ├── dispositivos.js     # Gestión de dispositivos, vinculación TV/Wear OS y sesiones
│       ├── entrenamientos.js   # CRUD de entrenamientos (transaccional + PostGIS + eventos MQTT)
│       ├── rutas.js            # Coordenadas geoespaciales de rutas (JSONB)
│       ├── metas.js            # Metas diarias personalizadas
│       ├── grupos.js           # Grupos, miembros y rankings semanales
│       └── notificaciones.js   # Notificaciones de logros
```

### Middlewares globales (`index.js`)

```javascript
app.use(cors());                                    // Habilitar peticiones origen cruzado
app.use(express.json());                            // Parsear cuerpos JSON
app.use(express.urlencoded({ extended: true }));     // Parsear urlencoded
```

---

## Servicio MQTT

El backend incluye un cliente MQTT integrado (`src/mqtt.js`) que se conecta automáticamente a **HiveMQ Cloud** sobre TLS (`mqtts://`, puerto 8883).

### Características
- **Reconexión automática:** Reintenta la conexión cada 5 segundos si se interrumpe el canal.
- **Publicación de eventos:** Los eventos se publican con `QoS 1` en formato JSON estructurado.

### Estructura estándar del Payload MQTT

```json
{
  "version": 1,
  "eventId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fechaEvento": "2026-07-23T02:00:00.000Z",
  "data": { ... }
}
```

### Tópicos y Eventos Notificados

| Evento | Tópico | Generado En |
|---|---|---|
| Entrenamiento iniciado | `rutalibre/usuarios/{idUsuario}/entrenamientos/iniciado` | `POST /api/entrenamientos/iniciar` |
| Entrenamiento finalizado | `rutalibre/usuarios/{idUsuario}/entrenamientos/finalizado` | `PUT /api/entrenamientos/finalizar` |
| Meta completada | `rutalibre/usuarios/{idUsuario}/metas/completada` | Transacción de finalización de entrenamiento |
| Cierre masivo de sesiones | `rutalibre/usuarios/{idUsuario}/sesion/cerrada` | `DELETE /api/dispositivos/sesion/todos` |
| Dispositivo desvinculado | `rutalibre/usuarios/{idUsuario}/dispositivos/{idDispositivo}/desvinculado` | `DELETE /api/dispositivos/:idDispositivo` |

---

## Base de datos

### Conexión Adaptativa (`db.js`)

Detecta automáticamente la presencia de la variable `DATABASE_URL` (Neon PostgreSQL Cloud con SSL) o utiliza la configuración por parámetros (`DB_HOST`, `DB_USER`, etc.).

```javascript
const pool = new Pool(
  process.env.DATABASE_URL
    ? { connectionString: process.env.DATABASE_URL, max: 5, idleTimeoutMillis: 30000 }
    : { host: process.env.DB_HOST, port: process.env.DB_PORT, user: process.env.DB_USER, password: process.env.DB_PASSWORD, database: process.env.DB_NAME }
);
```

### Tablas del Sistema

| Tabla | Descripción |
|---|---|
| `usuario` | Usuarios registrados, credenciales y datos antropométricos (peso). |
| `dispositivo` | Pantallas TV, relojes Wear OS y dispositivos vinculados con estado de sesión. |
| `grupo` | Grupos comunitarios para competir y compartir estadísticas. |
| `usuario_grupo` | Relación M:N usuario-grupo (PK compuesta `(id_usuario, id_grupo)`). |
| `ruta` | Almacenamiento de coordenadas GPS continuas en formato `JSONB`. |
| `entrenamiento` | Sesiones de actividad física con geometría de inicio/fin PostGIS. |
| `metas` | Metas diarias personalizadas (distancia, pasos, calorías, tiempo). |
| `notificacion` | Notificaciones de logros y su estado de lectura por dispositivo. |

### Esquema de `usuario`

```sql
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    peso_kg NUMERIC(5,2) DEFAULT NULL,
    fecha_registro TIMESTAMP DEFAULT NOW()
);
```

### Esquema de `dispositivo`

```sql
CREATE TABLE dispositivo (
    id_dispositivo SERIAL PRIMARY KEY,
    id_usuario INTEGER REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL, -- 'tv', 'wear', 'movil'
    nombre VARCHAR(100) NOT NULL,
    codigo_vinculacion VARCHAR(10) UNIQUE,
    codigo_expira TIMESTAMP,
    token_hash VARCHAR(255),
    vinculado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_vinculacion TIMESTAMP
);
```

### Esquema de `entrenamiento`

```sql
CREATE TABLE entrenamiento (
    id_entrenamiento SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    id_ruta INTEGER REFERENCES ruta(id_ruta),
    pasos INTEGER DEFAULT 0,
    calorias INTEGER DEFAULT 0,
    distancia NUMERIC(10,2) DEFAULT 0,
    tiempo INTEGER DEFAULT 0, -- en segundos (0 = activo/no finalizado)
    fecha_inicio TIMESTAMP DEFAULT NOW(),
    punto_inicio GEOMETRY(Point, 4326),
    punto_fin GEOMETRY(Point, 4326)
);
```

---

## Autenticación y Seguridad (JWT)

El backend implementa autenticación basada en tokens JWT firmados mediante **HMAC-SHA256** (`src/authToken.js`).

### Tipos de Tokens

1. **Token de Usuario (`tipoToken: "usuario"`):** Se emite al realizar Login (`POST /api/auth/login`). Válido por 7 días.
2. **Token de Dispositivo (`tipoToken: "dispositivo"`):** Se emite a la Smart TV o Smartwatch al completar el proceso de vinculación. Válido por 30 días. Contiene `idDispositivo`, `idUsuario` y `tipo`.

### Middlewares de Protección (`src/middleware/auth.js`)

- `requireUser`: Exige la cabecera `Authorization: Bearer <token_usuario>`. Inyecta `req.auth = { idUsuario, nombreUsuario, ... }`.
- `requireDevice`: Exige la cabecera `Authorization: Bearer <token_dispositivo>`. Inyecta `req.auth = { idDispositivo, idUsuario, tipo, ... }`.

---

## Endpoints

### Estado de la API

#### `GET /api/status`

Consulta el estado operativo del servicio REST y del broker MQTT.

**Respuesta `200 OK`:**
```json
{
  "status": "online",
  "timestamp": "2026-07-23T02:47:00.000Z",
  "service": "Ruta Libre REST API",
  "mqtt": "connected"
}
```

---

### Auth

`base: /api/auth`

#### `POST /register`

Registra un nuevo usuario en la plataforma.

**Request Body:**
```json
{
  "nombre": "Juan Pérez",
  "nombreUsuario": "juanp",
  "password": "miPassword123"
}
```

**Respuestas:**
- `201 Created`: Usuario registrado con éxito.
- `400 Bad Request`: Faltan campos u el nombre de usuario ya está registrado (`"Nombre de usuario no disponible cambia tu nombre de usuario"`).

---

#### `POST /login`

Autentica al usuario mediante contraseña hasheada con bcrypt y genera un token JWT de usuario.

**Request Body:**
```json
{
  "nombreUsuario": "juanp",
  "password": "miPassword123"
}
```

**Respuesta `200 OK`:**
```json
{
  "idUsuario": 1,
  "nombre": "Juan Pérez",
  "nombreUsuario": "juanp",
  "pesoKg": 75.5,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuestas de error:**
- `400 Bad Request`: Faltan campos obligatorios.
- `401 Unauthorized`: `"El usuario o la contraseña es incorrecto"`.

---

### Usuarios

`base: /api/usuarios`

#### `GET /:id`

Obtiene la información del perfil del usuario.

**Respuesta `200 OK`:**
```json
{
  "idUsuario": 1,
  "nombre": "Juan Pérez",
  "nombreUsuario": "juanp",
  "pesoKg": 75.5,
  "fechaRegistro": "2026-01-15T10:30:00.000Z"
}
```

---

#### `PUT /:id/peso`

Actualiza el peso corporal en kilogramos del usuario (utilizado para el cálculo dinámico de calorías).

**Request Body:**
```json
{
  "pesoKg": 72.0
}
```

**Validaciones:** `pesoKg` debe ser un número finito entre 20 y 300 kg.

**Respuesta `200 OK`:**
```json
{
  "pesoKg": 72.0
}
```

---

#### `PUT /:id`

Actualiza la información general del perfil del usuario (nombre, peso y opcionalmente contraseña).

**Request Body:**
```json
{
  "nombre": "Juan Carlos Pérez",
  "pesoKg": 74.0,
  "password": "nuevaContrasena123"
}
```

**Respuesta `200 OK`:**
```json
{
  "message": "Usuario actualizado correctamente"
}
```

---

### Dispositivos y Vinculación

`base: /api/dispositivos`

#### `POST /solicitar-vinculacion`

Llamado por **Smart TV** para generar un código temporal de 6 caracteres alfanuméricos de 10 minutos de vigencia.

**Request Body:**
```json
{
  "tipo": "tv",
  "nombre": "Ruta Libre TV Sala"
}
```

**Respuesta `201 Created`:**
```json
{
  "idDispositivo": 12,
  "codigo": "A7X3K9",
  "expira": "2026-07-23T02:57:00.000Z",
  "secreto": "dGhpcy1pcy1hLXNlY3JldC10b2tlbg..."
}
```

---

#### `POST /estado-vinculacion`

Sondeo (polling) realizado por la **Smart TV** utilizando el `idDispositivo` y `secreto` obtenidos al solicitar la vinculación.

**Request Body:**
```json
{
  "idDispositivo": 12,
  "secreto": "dGhpcy1pcy1hLXNlY3JldC10b2tlbg..."
}
```

**Respuestas:**
- **Pendiente `200 OK`:** `{ "estado": "pendiente" }`
- **Expirado `200 OK`:** `{ "estado": "expirado" }`
- **Vinculado `200 OK`:**
  ```json
  {
    "estado": "vinculado",
    "idUsuario": 1,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

---

#### `POST /vincular` `[Requiere Token Usuario]`

Llamado por la **Aplicación Móvil** para autorizar un código ingresado por el usuario.

**Request Body:**
```json
{
  "codigo": "A7X3K9"
}
```

**Respuesta `200 OK`:**
```json
{
  "idDispositivo": 12,
  "tipo": "tv",
  "nombre": "Ruta Libre TV Sala",
  "fechaVinculacion": "2026-07-23T02:50:00.000Z"
}
```

---

#### `POST /vincular-wear` `[Requiere Token Usuario]`

Llamado por la **Aplicación Móvil** para registrar directamente el reloj smartwatch (Wear OS) asociado.

**Request Body:**
```json
{
  "nombre": "Galaxy Watch 6"
}
```

**Respuesta `201 Created`:**
```json
{
  "idDispositivo": 15,
  "idUsuario": 1,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

#### `GET /` `[Requiere Token Usuario]`

Obtiene el listado de dispositivos vinculados activos del usuario.

**Respuesta `200 OK`:**
```json
[
  {
    "idDispositivo": 12,
    "tipo": "tv",
    "nombre": "Ruta Libre TV Sala",
    "activo": true,
    "fechaVinculacion": "2026-07-23T02:50:00.000Z"
  }
]
```

---

#### `GET /sesion/actual` `[Requiere Token Dispositivo]`

Valida si la sesión del dispositivo actual sigue estando activa en el sistema.

**Respuestas:**
- `204 No Content`: Sesión válida.
- `401 Unauthorized`: `"Sesión revocada"`.

---

#### `DELETE /sesion/actual` `[Requiere Token Dispositivo]`

Cierra la sesión del dispositivo actual marcando `activo = FALSE`.

**Respuesta:** `204 No Content`.

---

#### `DELETE /sesion/todos` `[Requiere Token Usuario]`

Cierra todas las sesiones de dispositivos pertenecientes al usuario y emite el evento MQTT `rutalibre/usuarios/{idUsuario}/sesion/cerrada`.

**Respuesta:** `204 No Content`.

---

#### `DELETE /:idDispositivo` `[Requiere Token Usuario]`

Desvincula un dispositivo específico por su ID y publica el evento MQTT de desvinculación.

**Respuesta:** `204 No Content`.

---

### Entrenamientos

`base: /api/entrenamientos`

#### `POST /iniciar`

Crea una sesión de entrenamiento vacía (`pasos=0, calorias=0, distancia=0, tiempo=0`) y emite el evento MQTT `entrenamientos/iniciado`.

**Request Body:**
```json
{
  "idUsuario": 1
}
```

**Respuesta `201 Created`:**
```json
{
  "idEntrenamiento": 5,
  "idUsuario": 1,
  "idRuta": null,
  "pasos": 0,
  "calorias": 0,
  "distancia": 0.0,
  "fechaInicio": "2026-07-23T02:00:00.000Z",
  "tiempo": 0,
  "puntoInicioLat": null,
  "puntoInicioLng": null,
  "puntoFinLat": null,
  "puntoFinLng": null
}
```

---

#### `PUT /finalizar`

Finaliza el entrenamiento en una **transacción atómica SQL (BEGIN / COMMIT)**:
1. Guarda la lista de coordenadas GPS en la tabla `ruta`.
2. Actualiza métricas en `entrenamiento` y puntos geoespaciales PostGIS.
3. Actualiza el progreso de todas las metas activas del usuario.
4. Genera registros en `notificacion` y emite eventos MQTT por cada meta completada.
5. Emite evento MQTT `entrenamientos/finalizado`.

**Request Body:**
```json
{
  "idEntrenamiento": 5,
  "pasos": 3500,
  "calorias": 280,
  "distancia": 4.25,
  "tiempo": 1800,
  "coordenadas": [
    { "longitud": -99.1332, "latitud": 19.4326 },
    { "longitud": -99.1340, "latitud": 19.4330 }
  ],
  "puntoInicio": { "longitud": -99.1332, "latitud": 19.4326 },
  "puntoFin": { "longitud": -99.1340, "latitud": 19.4330 }
}
```

**Respuesta `200 OK`:**
```json
{
  "idEntrenamiento": 5,
  "idUsuario": 1,
  "idRuta": 3,
  "pasos": 3500,
  "calorias": 280,
  "distancia": 4.25,
  "fechaInicio": "2026-07-23T02:00:00.000Z",
  "tiempo": 1800,
  "puntoInicioLat": 19.4326,
  "puntoInicioLng": -99.1332,
  "puntoFinLat": 19.4330,
  "puntoFinLng": -99.1340
}
```

---

#### `GET /activo/:idUsuario`

Obtiene el entrenamiento en curso del usuario (con `tiempo = 0`).

**Respuesta `200 OK`:**
```json
{
  "idEntrenamiento": 5,
  "fechaInicio": "2026-07-23T02:00:00.000Z",
  "pasos": 0,
  "calorias": 0,
  "distancia": 0.0
}
```
**Respuesta `404 Not Found`:** `{ "error": "No hay entrenamiento activo" }`

---

#### `GET /usuario/:idUsuario`

Historial completo de entrenamientos del usuario ordenado descendentemente por fecha.

**Respuesta `200 OK`:** Array de entrenamientos finalizados.

---

#### `GET /semana/:idUsuario`

Dashboard de rendimiento de la semana actual (desde el lunes). Retorna los 7 días de la semana (Lun–Dom).

**Respuesta `200 OK`:**
```json
{
  "distanciaTotal": 15.3,
  "pasosTotales": 12000,
  "caloriasTotales": 950,
  "tiempoTotal": 5400,
  "rendimientoDiario": [
    { "dia": "Lun", "distancia": 5.0, "pasos": 4000, "calorias": 300, "tiempo": 1800 },
    { "dia": "Mar", "distancia": 0.0, "pasos": 0, "calorias": 0, "tiempo": 0 },
    ...
  ]
}
```

---

#### `GET /comparacion/:idUsuario`

Porcentaje de cambio entre la semana actual y la semana anterior.

**Respuesta `200 OK`:**
```json
{
  "distanciaMejora": 25.5,
  "pasosMejora": -10.0,
  "caloriasMejora": 50.0,
  "tiempoMejora": 0.0
}
```

---

#### `GET /mes/:idUsuario`

Dashboard mensual agrupado por semanas calendario del mes actual.

**Respuesta `200 OK`:**
```json
{
  "distanciaTotal": 42.71,
  "pasosTotales": 56842,
  "caloriasTotales": 3215,
  "tiempoTotal": 19938,
  "rendimientoSemanal": [
    { "semana": "Semana 1", "distancia": 10.5, "pasos": 12000, "calorias": 750, "tiempo": 4200 },
    { "semana": "Semana 2", "distancia": 12.0, "pasos": 15000, "calorias": 900, "tiempo": 5000 },
    ...
  ]
}
```

---

#### `GET /comparacion-mes/:idUsuario`

Porcentaje de cambio entre el mes actual y el mes anterior.

**Respuesta `200 OK`:**
```json
{
  "distanciaMejora": 12.5,
  "pasosMejora": 8.0,
  "caloriasMejora": 10.0,
  "tiempoMejora": 9.2
}
```

---

### Rutas

`base: /api/rutas`

#### `POST /actualizar`

Actualiza el arreglo de coordenadas `JSONB` de una ruta existente.

**Request Body:**
```json
{
  "idRuta": 3,
  "coordenadas": [{ "longitud": -99.1332, "latitud": 19.4326 }]
}
```

---

#### `GET /:id`

Obtiene los puntos geográficos guardados de una ruta.

---

### Metas

`base: /api/metas`

#### `POST /`

Crea una nueva meta diaria. Valida que el usuario no posea una meta activa no terminada del mismo tipo (`distancia`, `pasos`, `calorias`, `tiempo`).

**Request Body:**
```json
{
  "idUsuario": 1,
  "tipoMeta": "DISTANCIA",
  "valorObjetivo": 5.0
}
```

**Respuesta `201 Created`:**
```json
{
  "idMetas": 10,
  "idUsuario": 1,
  "tipoMeta": "DISTANCIA",
  "valorObjetivo": 5.0,
  "valorActual": 0.0,
  "terminada": false
}
```

---

#### `PUT /:idMetas`

Actualiza de manera parcial los valores de una meta.

---

#### `DELETE /:idMetas`

Elimina una meta por su ID. Respuesta `204 No Content`.

---

#### `GET /usuario/:idUsuario`

Obtiene todas las metas registradas del usuario.

---

### Grupos

`base: /api/grupos`

#### `POST /`

Crea un grupo con un código único de 6 caracteres. Si el creador envía `idUsuario`, este se registra en la columna `id_creador` de la tabla `grupo` (propietario) y se le inscribe de inmediato en `usuario_grupo`.

**Request Body:**
```json
{
  "nombre": "Corredores Matutinos",
  "descripcion": "Grupo de entrenamiento matutino",
  "idUsuario": 1
}
```

**Respuesta `201 Created`:**
```json
{
  "idGrupo": 3,
  "nombre": "Corredores Matutinos",
  "codigo": "A7X3K9",
  "descripcion": "Grupo de entrenamiento matutino",
  "idCreador": 1
}
```

---

#### `POST /unirse`

Une al usuario a un grupo mediante su código de 6 caracteres.

**Request Body:**
```json
{
  "idUsuario": 2,
  "codigo": "A7X3K9"
}
```

---

#### `DELETE /:idGrupo/miembros/:idUsuario`

Permite a un usuario salir de un grupo. Respuesta `204 No Content`.

---

#### `GET /usuario/:idUsuario`

Obtiene la lista de grupos a los que está unido el usuario. Cada grupo incluye su identificador, nombre, código, descripción y el `idCreador` (identificador del creador/dueño).

**Respuesta `200 OK`:**
```json
[
  {
    "idGrupo": 3,
    "nombre": "Corredores Matutinos",
    "codigo": "A7X3K9",
    "descripcion": "Grupo de entrenamiento matutino",
    "idCreador": 1
  }
]
```

---

#### `GET /:idGrupo/miembros`

Obtiene la lista de integrantes del grupo con sus métricas acumuladas durante la semana actual.

---

#### `GET /:idGrupo/ranking`

Obtiene la tabla de clasificación del grupo ordenada descendentemente por distancia acumulada en la semana.

---

#### `DELETE /:idGrupo`

Elimina un grupo completo de la plataforma. Este endpoint valida que el usuario que realiza la petición sea el creador/dueño del grupo (`id_creador`). Si se confirma la eliminación, los registros de membresías en `usuario_grupo` son eliminados automáticamente en cascada.

**Parámetros de Consulta (Query Params) o Cuerpo (Request Body):**
- `idUsuario` (Integer, Obligatorio): ID del usuario que solicita la eliminación (debe ser el creador).

**Respuestas:**
- `204 No Content`: Grupo eliminado con éxito.
- `400 Bad Request`: ID de grupo o usuario inválido.
- `403 Forbidden`: El usuario no es el creador del grupo.
- `404 Not Found`: Grupo no encontrado.

---

### Notificaciones

`base: /api/notificaciones`

#### `GET /usuario/:idUsuario`

Obtiene las notificaciones generadas por el sistema.

**Respuesta `200 OK`:**
```json
[
  {
    "idNotificacion": 1,
    "idUsuario": 1,
    "idMetas": 10,
    "mensaje": "¡Felicidades! Has completado tu meta diaria de Distancia (5).",
    "fechaCreacion": "2026-07-23T02:30:00.000Z",
    "leidaMovil": false,
    "leidaSmartwatch": false
  }
]
```

---

#### `PUT /:id/leer-movil`

Marca la notificación como leída desde la App Móvil (`200 OK`).

---

#### `PUT /:id/leer-wear`

Marca la notificación como leída desde el Smartwatch Wear OS (`200 OK`).

---

## Flujos del sistema

### Flujo de vinculación de dispositivos (Android TV y Wear OS)

```
Android TV                              Backend                              App Móvil
    │                                      │                                     │
    │  1. POST /solicitar-vinculacion     │                                     │
    │ ───────────────────────────────────► │                                     │
    │ ◄─────────────────────────────────── │                                     │
    │   { codigo: "A7X3K9", secreto }      │                                     │
    │                                      │                                     │
    │  2. Muestra código en pantalla       │    3. Usuario ingresa "A7X3K9"       │
    │                                      │ ◄────────────────────────────────── │
    │  4. Polling POST /estado-vinculacion │       POST /api/dispositivos/vincular│
    │ ───────────────────────────────────► │       (con JWT de Usuario)          │
    │                                      │ ──────────────────────────────────► │
    │ ◄─────────────────────────────────── │   UPDATE dispositivo                │
    │   { estado: "vinculado", token:JWT } │   SET vinculado=TRUE, id_usuario=1  │
    │                                      │                                     │
```

---

### Flujo de registro y finalización de entrenamiento

```
Smartwatch / App Móvil                     Backend                             MQTT Broker
        │                                     │                                     │
        │  1. POST /entrenamientos/iniciar    │                                     │
        │ ──────────────────────────────────► │                                     │
        │ ◄────────────────────────────────── │  2. Evento MQTT publicado           │
        │   { idEntrenamiento: 5 }            │ ──────────────────────────────────► │
        │                                     │     Topic: .../iniciado             │
        │                                     │                                     │
        │  (Registro en tiempo real local)    │                                     │
        │                                     │                                     │
        │  3. PUT /entrenamientos/finalizar   │                                     │
        │ ──────────────────────────────────► │                                     │
        │                                     │  4. Transacción SQL (BEGIN)         │
        │                                     │     - UPDATE entrenamiento          │
        │                                     │     - UPDATE metas (progreso)       │
        │                                     │     - INSERT notificacion (si logra)│
        │                                     │     (COMMIT)                        │
        │                                     │                                     │
        │ ◄────────────────────────────────── │  5. Publica eventos MQTT            │
        │   { idEntrenamiento: 5, ... }       │ ──────────────────────────────────► │
        │                                     │     Topic: .../finalizado           │
        │                                     │     Topic: .../metas/completada     │
```

---

## Manejo de errores

### Formato de respuesta de error

```json
{
  "error": "Descripción legible del error"
}
```

### Códigos de estado HTTP

| Código | Significado | Ejemplos |
|---|---|---|
| `200` | OK | Petición exitosa, consultas y actualizaciones. |
| `201` | Created | Recursos creados (Usuario, Entrenamiento, Meta, Grupo, Vinculación). |
| `204` | No Content | Eliminación exitosa o validación de sesión activa. |
| `400` | Bad Request | Parámetros obligatorios faltantes o valores fuera de rango. |
| `401` | Unauthorized | Credenciales/Tokens inválidos o caducados. |
| `403` | Forbidden | Tipo de token incorrecto (ej. se requiere token de dispositivo). |
| `404` | Not Found | Recurso no encontrado (Usuario, Grupo, Ruta, Entrenamiento). |
| `500` | Internal Server Error | Error en base de datos PostgreSQL o excepciones no capturadas. |

---

## Consideraciones sobre Contenido Multimedia y APIs de Terceros

### Módulo de Contenido de TV (Visualización de Videos de Running)

El módulo de Smart TV cuenta con una sección de **Contenido** para la visualización de videos recomendados sobre running:
1. **Consumo Descentralizado:** Este módulo no consume endpoints del backend de Ruta Libre para obtener los metadatos o reproducir los videos.
2. **Conexión Directa:** El cliente de Smart TV consume de manera directa la **API v3 de YouTube** utilizando peticiones HTTP remotas mediante Retrofit (`https://www.googleapis.com/youtube/v3/search`).
3. **Resiliencia Local:** Si la API de YouTube falla por red, excede su cuota o no hay una clave API configurada, el cliente de Smart TV cuenta con resiliencia local inmediata. Implementa timeouts explícitos de 5 segundos y realiza un fallback automático a una lista estática simulada de videos (`obtenerVideosSimulados`), evitando cargas infinitas o bloqueos.
