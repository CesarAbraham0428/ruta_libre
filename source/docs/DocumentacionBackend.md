# Backend API REST — Ruta Libre

Documentación técnica del backend desarrollado en Node.js + Express + PostgreSQL con PostGIS.

---

## Índice

1. [Descripción general](#descripción-general)
2. [Tecnologías](#tecnologías)
3. [Configuración del proyecto](#configuración-del-proyecto)
4. [Arquitectura](#arquitectura)
5. [Base de datos](#base-de-datos)
6. [Endpoints](#endpoints)
   - [Auth](#auth)
   - [Usuarios](#usuarios)
   - [Entrenamientos](#entrenamientos)
   - [Rutas](#rutas)
   - [Metas](#metas)
   - [Grupos](#grupos)
   - [Notificaciones](#notificaciones)
7. [Flujo de registro de un entrenamiento](#flujo-de-registro-de-un-entrenamiento)
8. [Manejo de errores](#manejo-de-errores)

---

## Descripción general

Backend monolítico en Node.js que expone una API REST para las tres aplicaciones del ecosistema Ruta Libre: móvil (Android), smartwatch (Wear OS) y Smart TV (Android TV).

Se comunica con PostgreSQL 16 + PostGIS para almacenamiento de datos y consultas geoespaciales.

---

## Tecnologías

| Componente | Tecnología | Versión |
|---|---|---|
| Runtime | Node.js | 20+ |
| Framework web | Express | 4.19.x |
| Cliente PostgreSQL | `pg` | 8.11.x |
| Variables de entorno | `dotenv` | 16.4.x |
| CORS | `cors` | 2.8.x |
| Hashing de contraseñas | `bcryptjs` | 3.0.x |
| Dev | `nodemon` | 3.1.x |
| Base de datos | PostgreSQL + PostGIS | 16 |

---

## Configuración del proyecto

### Archivo `.env`

```
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=********
DB_NAME=rutaLibre
```

### Scripts disponibles

```bash
npm start       # Inicia el servidor en producción
npm run dev     # Inicia con nodemon (recarga automática)
npm run test-api # Ejecuta prueba de integración (src/api_test.js)
```

### Inicialización

```bash
cd backend
npm install
npm run dev
```

El servidor se levanta en `http://0.0.0.0:3000` y muestra:

```
Servidor de Ruta Libre escuchando en http://0.0.0.0:3000
Endpoints disponibles bajo /api/
```

---

## Arquitectura

```
backend/
├── .env                        # Variables de entorno
├── package.json
├── src/
│   ├── index.js                # Punto de entrada, middlewares y montado de rutas
│   ├── db.js                   # Pool de conexión a PostgreSQL
│   ├── api_test.js             # Script de prueba de integración
│   └── routes/
│       ├── auth.js             # Registro e inicio de sesión (bcrypt)
│       ├── usuarios.js         # Consulta de perfil de usuario
│       ├── entrenamientos.js   # CRUD de sesiones de entrenamiento (transaccional)
│       ├── rutas.js            # Coordenadas de rutas (JSONB)
│       ├── metas.js            # Metas diarias personalizadas
│       ├── grupos.js           # Grupos y rankings semanales
│       └── notificaciones.js   # Notificaciones de logros
```

### Middlewares globales (index.js)

```javascript
app.use(cors());                     # Habilitar CORS para peticiones cruzadas
app.use(express.json());             # Parsear body JSON
app.use(express.urlencoded({ extended: true }));  # Parsear form-urlencoded
```

El servidor escucha en `0.0.0.0` para aceptar conexiones desde cualquier interfaz de red, incluyendo las que vienen del emulador Android (`10.0.2.2`).

### Endpoint de estado

```http
GET /api/status
→ { "status": "online", "timestamp": "...", "service": "Ruta Libre REST API" }
```

---

## Base de datos

### Conexión (`db.js`)

```javascript
const pool = new Pool({
  host: process.env.DB_HOST,       // localhost
  port: parseInt(process.env.DB_PORT || '5432'),
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
});
```

Se utiliza un **Pool** de conexiones de `pg` para manejar múltiples consultas concurrentes. Se exporta `{ query: (text, params) => pool.query(text, params), pool }`.

### Tablas

| Tabla | Descripción |
|---|---|
| `usuario` | Usuarios del sistema |
| `grupo` | Grupos para compartir resultados |
| `usuario_grupo` | Relación M:N usuario-grupo (PK compuesta) |
| `ruta` | Coordenadas JSONB del recorrido |
| `entrenamiento` | Sesiones de actividad física |
| `metas` | Metas diarias personalizadas |
| `notificacion` | Notificaciones de logros |

### Esquema de `entrenamiento`

| Columna | Tipo | Descripción |
|---|---|---|
| `id_entrenamiento` | `SERIAL PRIMARY KEY` | ID autogenerado |
| `id_usuario` | `INTEGER NOT NULL` | FK → usuario |
| `id_ruta` | `INTEGER` | FK → ruta (nullable) |
| `pasos` | `INTEGER DEFAULT 0` | Pasos totales |
| `calorias` | `INTEGER DEFAULT 0` | Calorías quemadas |
| `distancia` | `NUMERIC(10,2) DEFAULT 0` | Distancia en km |
| `tiempo` | `INTEGER DEFAULT 0` | Tiempo en segundos (0 = no finalizado) |
| `fecha_inicio` | `TIMESTAMP DEFAULT NOW()` | Inicio del entrenamiento |
| `punto_inicio` | `GEOMETRY(Point, 4326)` | Punto de inicio (PostGIS) |
| `punto_fin` | `GEOMETRY(Point, 4326)` | Punto de fin (PostGIS) |

### Columnas calculadas con PostGIS

Los puntos de inicio y fin se almacenan como geometrías PostGIS usando `ST_MakePoint(longitud, latitud)` con SRID 4326. Al leer se extraen con:

```sql
ST_Y(punto_inicio) AS lat_ini,
ST_X(punto_inicio) AS lng_ini
```

---

## Endpoints

### Auth

`base: /api/auth`

#### `POST /register`

Registra un nuevo usuario con contraseña hasheada (bcrypt, salt rounds = 10).

**Request:**
```json
{
  "nombre": "string",
  "nombreUsuario": "string",
  "password": "string"
}
```

**Validaciones:**
- Todos los campos son obligatorios → 400
- `nombre_usuario` debe ser único → 400 `"Nombre de usuario no disponible cambia tu nombre de usuario"`

**SQL:** `SELECT id_usuario FROM usuario WHERE nombre_usuario = $1` (pre-validación), luego `INSERT INTO usuario (nombre, nombre_usuario, password, fecha_registro) VALUES ($1, $2, $3, NOW())`

**Response:** `201` (sin cuerpo)

---

#### `POST /login`

Inicia sesión verificando la contraseña con bcrypt.

**Request:**
```json
{
  "nombreUsuario": "string",
  "password": "string"
}
```

**Validaciones:**
- Ambos campos obligatorios → 400
- Usuario no encontrado o contraseña incorrecta → 401 `"El usuario o la contraseña es incorrecto"`

**SQL:** `SELECT id_usuario, nombre, nombre_usuario, password FROM usuario WHERE nombre_usuario = $1`

**Autenticación:** `bcrypt.compare(password, user.password)` contra el hash almacenado.

**Token:** `token_simulado_{idUsuario}_{timestamp}` (no es JWT, no se verifica en requests posteriores).

**Response `200`:**
```json
{
  "idUsuario": 1,
  "nombre": "string",
  "nombreUsuario": "string",
  "token": "token_simulado_1_1689123456789"
}
```

---

### Usuarios

`base: /api/usuarios`

#### `GET /:id`

Obtiene datos del perfil de un usuario.

**Response `200`:**
```json
{
  "idUsuario": 1,
  "nombre": "string",
  "nombreUsuario": "string",
  "fechaRegistro": "2026-01-15T10:30:00.000Z"
}
```

---

### Entrenamientos

`base: /api/entrenamientos`

#### `POST /iniciar`

Crea un nuevo entrenamiento con valores en cero y retorna el ID asignado.

**Request:**
```json
{
  "idUsuario": 1
}
```

**SQL:**
```sql
INSERT INTO entrenamiento (id_usuario, fecha_inicio, pasos, calorias, distancia, tiempo) 
VALUES ($1, NOW(), 0, 0, 0, 0) 
RETURNING id_entrenamiento, id_usuario, id_ruta, pasos, calorias, distancia, fecha_inicio, tiempo
```

**Response `201`:**
```json
{
  "idEntrenamiento": 5,
  "idUsuario": 1,
  "idRuta": null,
  "pasos": 0,
  "calorias": 0,
  "distancia": 0.0,
  "fechaInicio": "2026-06-27T15:00:00.000Z",
  "tiempo": 0,
  "puntoInicioLat": null,
  "puntoInicioLng": null,
  "puntoFinLat": null,
  "puntoFinLng": null
}
```

---

#### `PUT /finalizar`

Actualiza las métricas del entrenamiento, guarda la ruta (si hay coordenadas), actualiza las metas activas del usuario y genera notificaciones de logros. **Operación transaccional** (BEGIN/COMMIT/ROLLBACK).

**Request:**
```json
{
  "idEntrenamiento": 5,
  "pasos": 1500,
  "calorias": 120,
  "distancia": 2.5,
  "tiempo": 1800,
  "coordenadas": [{ "longitud": -99.0, "latitud": 19.0 }],
  "puntoInicio": { "longitud": -99.0, "latitud": 19.0 },
  "puntoFin": { "longitud": -99.1, "latitud": 19.1 }
}
```

**Proceso interno (transacción):**

```
1. BEGIN
2. Si coordenadas.length > 0 → INSERT en ruta (JSON.stringify) → obtener idRuta
3. UPDATE entrenamiento SET pasos, calorias, distancia, tiempo, id_ruta
4. Si puntoInicio no es (0,0) → ST_SetSRID(ST_MakePoint(lng, lat), 4326)
5. Si puntoFin no es (0,0) → ST_SetSRID(ST_MakePoint(lng, lat), 4326)
6. Para cada meta activa del usuario (terminada = FALSE):
   a. Incrementar valor_actual según tipo:
      - 'distancia' → +distancia (km)
      - 'pasos' → +pasos
      - 'calorias' → +calorias
      - 'tiempo' → +tiempo/60.0 (segundos → minutos)
   b. Si nuevo valor >= valor_objetivo → marcar terminada = TRUE
   c. Si se completó → INSERT en notificacion con mensaje:
      "¡Felicidades! Has completado tu meta diaria de {Tipo} ({valorObjetivo})."
7. COMMIT
8. En caso de error → ROLLBACK
```

**Response `200`:**
```json
{
  "idEntrenamiento": 5,
  "idUsuario": 1,
  "idRuta": null,
  "pasos": 1500,
  "calorias": 120,
  "distancia": 2.5,
  "fechaInicio": "2026-06-27T15:00:00.000Z",
  "tiempo": 1800,
  "puntoInicioLat": 19.0,
  "puntoInicioLng": -99.0,
  "puntoFinLat": 19.1,
  "puntoFinLng": -99.1
}
```

---

#### `GET /activo/:idUsuario`

Obtiene el entrenamiento activo del usuario (aquel con `tiempo = 0` — no finalizado).

**Response `200`:**
```json
{
  "idEntrenamiento": 5,
  "fechaInicio": "2026-06-27T15:00:00.000Z",
  "pasos": 0,
  "calorias": 0,
  "distancia": 0.0
}
```

**Response `404`:** `{ "error": "No hay entrenamiento activo" }`

---

#### `GET /usuario/:idUsuario`

Historial completo de entrenamientos del usuario, ordenado por fecha descendente.

**Response `200`:** Array de objetos con estructura de `PUT /finalizar`.

---

#### `GET /semana/:idUsuario`

Dashboard semanal: totales acumulados desde el lunes actual + desglose por día.

**Response `200`:**
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

Los 7 días (Lun–Dom) siempre se devuelven; los días sin actividad aparecen con valores en cero.

---

#### `GET /comparacion/:idUsuario`

Comparación de rendimiento entre la semana actual y la anterior.

**Response `200`:**
```json
{
  "distanciaMejora": 25.5,
  "pasosMejora": -10.0,
  "caloriasMejora": 50.0,
  "tiempoMejora": 0.0
}
```

Los valores representan el **porcentaje de cambio** (positivo = mejora, negativo = disminución). Si la semana anterior fue 0, devuelve `100.0` si la actual > 0, o `0.0` si ambas son 0.

---

### Rutas

`base: /api/rutas`

#### `POST /actualizar`

Actualiza las coordenadas de una ruta existente.

**Request:**
```json
{
  "idRuta": 3,
  "coordenadas": [{ "longitud": 19.0, "latitud": -99.0 }, ...]
}
```

**Response `200`:**
```json
{
  "idRuta": 3,
  "coordenadas": [{ "longitud": 19.0, "latitud": -99.0 }, ...]
}
```

---

#### `GET /:id`

Obtiene una ruta por su ID.

**Response `200`:**
```json
{
  "idRuta": 3,
  "coordenadas": [{ "longitud": 19.0, "latitud": -99.0 }, ...]
}
```

---

### Metas

`base: /api/metas`

#### `POST /`

Crea una nueva meta diaria para un usuario. Valida que no exista una meta activa del mismo tipo.

**Request:**
```json
{
  "idUsuario": 1,
  "tipoMeta": "DISTANCIA",
  "valorObjetivo": 5.0
}
```

**Valores válidos para `tipoMeta`:** `distancia`, `pasos`, `calorias`, `tiempo` (se convierte a minúsculas internamente).

**Validaciones:**
- Todos los campos obligatorios → 400
- Si ya existe una meta activa (no terminada) del mismo tipo → 400 `"Ya tienes una meta activa de este tipo"`

**Response `201`:**
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

Actualiza una meta existente. Todos los campos del body son opcionales (usa `COALESCE`).

**Request:**
```json
{
  "valorObjetivo": 10.0
}
```

**Response `200`:**
```json
{
  "idMetas": 10,
  "idUsuario": 1,
  "tipoMeta": "DISTANCIA",
  "valorObjetivo": 10.0,
  "valorActual": 5.0,
  "terminada": false
}
```

---

#### `DELETE /:idMetas`

Elimina una meta.

**Response:** `204` (sin cuerpo)

---

#### `GET /usuario/:idUsuario`

Obtiene todas las metas del usuario, ordenadas por ID descendente.

**Response `200`:** Array de objetos con estructura de `POST /`.

---

### Grupos

`base: /api/grupos`

#### `POST /`

Crea un nuevo grupo con un código único de 6 caracteres (letras mayúsculas + dígitos). Si el código generado ya existe (error `23505`), se reintenta hasta 5 veces.

**Request:**
```json
{
  "nombre": "Corredores Matutinos",
  "descripcion": "Grupo para correr en las mañanas"
}
```

**Response `201`:**
```json
{
  "idGrupo": 3,
  "nombre": "Corredores Matutinos",
  "codigo": "A7X3K9",
  "descripcion": "Grupo para correr en las mañanas"
}
```

---

#### `POST /unirse`

Un usuario se une a un grupo mediante su código (el código se convierte a mayúsculas).

**Request:**
```json
{
  "idUsuario": 1,
  "codigo": "A7X3K9"
}
```

Usa `ON CONFLICT DO NOTHING` para ignorar si el usuario ya pertenece al grupo.

**Response:** `200` (sin cuerpo) o `404` si el código no existe.

---

#### `GET /usuario/:idUsuario`

Grupos a los que pertenece el usuario.

**Response `200`:** Array de `{ idGrupo, nombre, codigo, descripcion }`.

---

#### `GET /:idGrupo/miembros`

Miembros del grupo con su rendimiento semanal acumulado (desde el lunes).

**Response `200`:**
```json
[
  {
    "idUsuario": 1,
    "nombre": "Juan",
    "nombreUsuario": "juan123",
    "distancia": 12.5,
    "pasos": 10000,
    "calorias": 800,
    "tiempo": 3600
  }
]
```

---

#### `GET /:idGrupo/ranking`

Miembros del grupo ordenados por distancia semanal descendente.

**Response `200`:**
```json
{
  "miembros": [
    { "idUsuario": 2, "nombre": "Ana", "distancia": 20.0, ... },
    { "idUsuario": 1, "nombre": "Juan", "distancia": 12.5, ... }
  ]
}
```

---

### Notificaciones

`base: /api/notificaciones`

#### `GET /usuario/:idUsuario`

Obtiene las notificaciones del usuario ordenadas por fecha descendente.

**Response `200`:**
```json
[
  {
    "idNotificacion": 1,
    "idUsuario": 1,
    "idMetas": 10,
    "mensaje": "¡Felicidades! Has completado tu meta diaria de Distancia (5).",
    "fechaCreacion": "2026-06-27T15:30:00.000Z",
    "leidaMovil": false,
    "leidaSmartwatch": false
  }
]
```

Las notificaciones se generan automáticamente durante el `PUT /entrenamientos/finalizar` cuando se completa una meta.

---

#### `PUT /:id/leer-movil`

Marca una notificación como leída en el dispositivo móvil.

**Response:** `200` (sin cuerpo)

---

#### `PUT /:id/leer-wear`

Marca una notificación como leída en el smartwatch.

**Response:** `200` (sin cuerpo)

---

## Flujo de registro de un entrenamiento

### Desde el smartwatch (Wear OS)

```
Usuario                    WearEntrenamientoViewModel          Backend
  │                              │                              │
  │  [INICIAR]                   │                              │
  │ ───────────────────────────► │                              │
  │                              │  POST /entrenamientos/iniciar│
  │                              │ ────────────────────────────►│
  │                              │  INSERT entrenamiento        │
  │                              │  (pasos=0, calorias=0, ...)  │
  │                              │ ◄─── { idEntrenamiento: 5 }  │
  │                              │                              │
  │  (corre, Health Services)    │                              │
  │                              │                              │
  │  [FINALIZAR]                 │                              │
  │ ───────────────────────────► │                              │
  │                              │  PUT /entrenamientos/finalizar│
  │                              │ ────────────────────────────►│
  │                              │  BEGIN                        │
  │                              │  1. UPDATE entrenamiento     │
  │                              │     SET pasos, calorias, ... │
  │                              │  2. Para cada meta activa:   │
  │                              │     UPDATE metas SET         │
  │                              │     valor_actual += incremento│
  │                              │  3. Si meta completada:      │
  │                              │     INSERT notificacion     │
  │                              │  COMMIT                      │
  │                              │ ◄─── { idEntrenamiento: 5 } │
  │                              │                              │
  │  ◄─── navega a Inicio ───── │                              │
```

### Puntos clave

1. **`POST /iniciar`** crea el registro con valores en cero y devuelve el `idEntrenamiento`.
2. Durante la actividad, las métricas solo existen en memoria del ViewModel (no se envían incrementales al backend).
3. **`PUT /finalizar`** envía los valores finales acumulados en una sola petición.
4. El smartwatch envía `coordenadas = []` y puntos de inicio/fin en (0,0) porque el seguimiento GPS se delega al teléfono.
5. Las metas se actualizan **del lado del backend** dentro de la misma transacción de finalización, sumando los valores del entrenamiento al `valor_actual` de cada meta.
6. Si al actualizar una meta se alcanza o supera el objetivo, se genera automáticamente una notificación de logro.

---

## Script de prueba

**Archivo:** `src/api_test.js`

Ejecuta el flujo completo de integración:
1. Health check (`GET /api/status`)
2. Registro de usuario (`POST /api/auth/register`)
3. Inicio de sesión (`POST /api/auth/login`)
4. Consulta de perfil (`GET /api/usuarios/:id`)
5. Creación de meta (`POST /api/metas`)
6. Listado de metas (`GET /api/metas/usuario/:id`)
7. Inicio de entrenamiento (`POST /api/entrenamientos/iniciar`)
8. Finalización con datos que disparan meta completada (`PUT /api/entrenamientos/finalizar`)
9. Verificación de meta actualizada (`GET /api/metas/usuario/:id`)
10. Verificación de notificación creada (`GET /api/notificaciones/usuario/:id`)
11. Dashboard semanal (`GET /api/entrenamientos/semana/:id`)

Ejecutar con: `node src/api_test.js`

---

## Manejo de errores

### Formato de error

Todos los errores siguen el mismo formato:

```json
{
  "error": "Mensaje descriptivo del error"
}
```

### Códigos de estado

| Código | Significado | Causas comunes |
|---|---|---|
| `201` | Creado | Inicio de entrenamiento, creación de meta/grupo |
| `200` | Éxito | Finalización, consultas, actualizaciones |
| `204` | Sin contenido | Eliminación de meta |
| `400` | Bad Request | Faltan campos obligatorios, ID inválido, meta duplicada |
| `401` | Unauthorized | Credenciales incorrectas |
| `404` | Not Found | Recurso no encontrado (usuario, grupo, entrenamiento, código) |
| `500` | Internal Server Error | Error en base de datos o excepción no manejada |

### Errores de base de datos

- Los errores de clave duplicada (código PostgreSQL `23505`) se manejan explícitamente en la creación de grupos (reintento con nuevo código hasta 5 intentos).
- Las transacciones en `PUT /finalizar` usan `BEGIN`/`COMMIT`/`ROLLBACK` para garantizar atomicidad.
- Todos los errores se registran con `console.error(...)` antes de responder.

### Error global no capturado

```javascript
app.use((err, req, res, next) => {
  console.error('Error global:', err.stack);
  res.status(500).json({ error: 'Algo salio mal en el servidor' });
});
```

### Autenticación

- Las contraseñas se almacenan hasheadas con **bcryptjs** (salt rounds = 10).
- El "token" devuelto en login es un string con formato `token_simulado_{idUsuario}_{timestamp}`.
- **No existe middleware de verificación de tokens** — ninguna ruta valida el token en requests subsecuentes.
