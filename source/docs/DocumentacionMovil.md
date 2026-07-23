# Módulo Móvil — Ruta Libre

## Arquitectura

El módulo móvil (`:app`) sigue el patrón **MVVM** (Model-View-ViewModel) con **Jetpack Compose** para la interfaz de usuario, **Kotlin Coroutines & Flow** para la reactividad, y se apoya en el módulo compartido **`:core`** para el acceso a datos remotos mediante **Retrofit**.

```
[UI Component (Composable)]
       │
       ▼
  [ViewModel] (StateFlow / SharedFlow)
       ├──► [LocationTracker / StepCounterTracker] (GPS y Sensores Hardware)
       ├──► [MqttManager] ──(TLS SSL)──► [HiveMQ MQTT Broker]
       ├──► [AuthSessionStore] ──► [SharedPreferences JWT Store]
       └──► [Repositories (:core)] ──► [ApiService (Retrofit)] ──► [Backend REST]
```

### Integraciones y Servicios Clave

- **MapTiler SDK (`com.maptiler.maptilersdk`)**: Renderizado de mapa vectorial interactivo y trazado en tiempo real de la polilínea GPS de la ruta (`MTMapView`, `MTLineLayer`, `MTGeoJSONSource`).
- **HiveMQ MQTT Client (`com.hivemq.client.mqtt`)**: Conexión TLS asíncrona y segura a HiveMQ Cloud (`MQTT_HOST`, `MQTT_PORT`, `MQTT_USERNAME`, `MQTT_PASSWORD` provistos en `BuildConfig`). Se suscribe al tópico `rutalibre/usuarios/{idUsuario}/#` y notifica eventos como `/entrenamientos/finalizado` para refrescar reactivamente el historial.
- **Seguimiento GPS (`LocationTracker`)**: Utiliza `FusedLocationProviderClient` con máxima precisión (`PRIORITY_HIGH_ACCURACY`), intervalo de actualización de 2,000 ms y distancia mínima de 3 m. Incluye filtros de precisión (descarta puntos con imprecisión > 35 m) y supresión de artefactos de teletransporte.
- **Podómetro y Métricas (`StepCounterTracker`)**: Captura pasos físicos mediante el sensor `Sensor.TYPE_STEP_COUNTER`. Detecta entornos simulados (emulador) y calcula dinámicamente pasos y calorías quemadas con base en el peso corporal (`pesoKg * distanciaKm * 0.75`).
- **Sincronización Wear OS (`Google Play Services Wearable`)**: Sincroniza automáticamente las credenciales (`idUsuario`, `idDispositivo`, `token`) hacia el reloj inteligente a través de `Wearable.getDataClient` en la ruta `/ruta-libre/identity`.
- **Persistencia de Sesión (`AuthSessionStore`)**: Almacena de forma segura la sesión del usuario en `SharedPreferences` (`ruta_libre_auth_session`). Analiza el payload JWT en Base64 para validar la expiración (`exp`) automáticamente.

---

## Navegación

### Routes.kt

Ubica las constantes de ruta y funciones auxiliares para la navegación en el objeto [Routes](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/navigation/Routes.kt):

| Constante | Ruta | Parámetros | Descripción |
|---|---|---|---|
| `LOGIN` | `"login"` | Ninguno | Pantalla de inicio de sesión |
| `REGISTER` | `"register"` | Ninguno | Pantalla de registro de usuario |
| `PESO_INICIAL` | `"peso_inicial"` | Ninguno | Registro de peso inicial tras registro/login sin peso |
| `HOME` | `"home"` | Ninguno | Menú principal y dashboard de accesos rápidos |
| `ENTRENAMIENTO` | `"entrenamiento/{idEntrenamiento}"` | `{idEntrenamiento}` | Pantalla de entrenamiento activo con GPS y mapa |
| `RESUMEN` | `"resumen/{idEntrenamiento}"` | `{idEntrenamiento}` | Resumen detallado post-entrenamiento |
| `HISTORIAL` | `"historial"` | Ninguno | Historial completo de actividades guardadas |
| `METAS` | `"metas"` | Ninguno | Listado y progreso de metas personales |
| `CREAR_META` | `"crear_meta"` | Ninguno | Formulario de creación de nueva meta |
| `EDITAR_META` | `"editar_meta/{idMeta}"` | `{idMeta}` | Edición del valor objetivo de una meta existente |
| `GRUPOS` | `"grupos"` | Ninguno | Listado de grupos del usuario, creación y unión |
| `DETALLE_GRUPO` | `"detalle_grupo/{idGrupo}/{nombreGrupo}"` | `{idGrupo}`, `{nombreGrupo}` | Estadísticas acumuladas, ranking y miembros |
| `PERFIL` | `"perfil"` | Ninguno | Edición de datos de cuenta y accesos directos |
| `VINCULAR_DISPOSITIVO` | `"vincular_dispositivo"` | Ninguno | Ingreso de código de 6 caracteres para vincular TV |
| `DISPOSITIVOS` | `"dispositivos"` | Ninguno | Gestión de dispositivos (TV/Reloj) y sesiones activas |

**Funciones auxiliares:**
```kotlin
Routes.entrenamiento(id: Int)                        -> "entrenamiento/$id"
Routes.resumen(id: Int)                              -> "resumen/$id"
Routes.editarMeta(id: Int)                           -> "editar_meta/$id"
Routes.detalleGrupo(idGrupo: Int, nombreGrupo: String) -> "detalle_grupo/$idGrupo/$nombreGrupo"
```

### NavGraph.kt

El archivo [NavGraph.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/app/src/main/java/mx/utng/cala/rutalibre/ui/navigation/NavGraph.kt) gestiona el contenedor `NavHost` con `startDestination = Routes.LOGIN`. Inicializa los `ViewModels` principales a nivel de grafo para mantener la consistencia de sesión y sincronizar eventos MQTT y Wear OS en tiempo real:

- Conecta MQTT al iniciar sesión con `mqttViewModel.connect(idUsuario)`.
- Envía credenciales al Wear OS cuando se obtiene un `token` válido.
- Escucha el flujo de eventos de MQTT para recargar el historial cuando llega la notificación `/entrenamientos/finalizado`.

---

## Pantallas

### 1. LoginScreen
- **Archivo:** [LoginScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/auth/LoginScreen.kt)
- **ViewModel:** `AuthViewModel`
- **Backend:** `POST /api/auth/login`
- **Descripción:** Permite autenticarse ingresando usuario y contraseña. Guarda la sesión y el token JWT en `AuthSessionStore`. Si el usuario no tiene peso configurado (`pesoKg == null`), redirige a `PesoInicialScreen`; de lo contrario, navega a `HomeScreen`.

### 2. RegisterScreen
- **Archivo:** [RegisterScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/auth/RegisterScreen.kt)
- **ViewModel:** `AuthViewModel`
- **Backend:** `POST /api/auth/register`
- **Descripción:** Registro de nuevos usuarios. Incluye validación de requisitos de contraseña en tiempo real mediante checklist visual: mínimo 8 caracteres, al menos una letra y al menos un número.

### 3. PesoInicialScreen
- **Archivo:** [PesoInicialScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/perfil/PesoInicialScreen.kt)
- **ViewModel:** `PesoViewModel`, `AuthViewModel`
- **Backend:** `PUT /api/usuarios/{idUsuario}/peso`
- **Descripción:** Formulario presentado tras el registro o inicio de sesión si el usuario no ha especificado su peso corporal. Requerido para el cálculo preciso del consumo calórico.

### 4. HomeScreen
- **Archivo:** [HomeScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/home/HomeScreen.kt)
- **ViewModel:** `AuthViewModel`, `MqttViewModel`
- **Descripción:** Dashboard principal. Muestra el estado actual de la sincronización MQTT en tiempo real ("Sincronización en tiempo real activa / Conectando... / Sin conexión"). Contiene tarjetas interactivas para:
  - **Iniciar Entrenamiento** (Banner destacado gradiente)
  - **Historial de actividades**
  - **Metas Personales**
  - **Grupos y Comunidad**
  - **Mi Perfil**
  - **Vincular TV**
  - **Dispositivos vinculados**

### 5. EntrenamientoScreen
- **Archivo:** [EntrenamientoScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/entrenamiento/EntrenamientoScreen.kt)
- **ViewModel:** `EntrenamientoViewModel`
- **Backend:** `POST /api/entrenamientos/iniciar` y `PUT /api/entrenamientos/finalizar`
- **Descripción:** Pantalla central de carrera. Integra un mapa vectorial interactivo mediante MapTiler SDK (`MTMapView`), dibujando la ruta trazada por el GPS en tiempo real. Gestiona la solicitud de permisos de ubicación y reconocimiento de actividad (`ACTIVITY_RECOGNITION`). Muestra métricas activas:
  - **Distancia:** Calculada en kilómetros.
  - **Pasos:** Leídos por el sensor hardware o estimados en emulador.
  - **Calorías:** Estimadas según el peso corporal y la distancia.
  - **Tiempo:** Cronómetro transcurrido (hh:mm:ss).
  Al finalizar, envía la ruta con las coordenadas geográficas al backend y navega automáticamente al resumen.

### 6. ResumenScreen
- **Archivo:** [ResumenScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/resumen/ResumenScreen.kt)
- **ViewModel:** `HistorialViewModel`
- **Backend:** Datos cargados mediante el repositorio de entrenamientos.
- **Descripción:** Presenta el reporte consolidado de un entrenamiento finalizado: distancia total recorida, tiempo empleado, pasos contabilizados, calorías quemadas y fecha de realización.

### 7. HistorialScreen
- **Archivo:** [HistorialScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/resumen/HistorialScreen.kt)
- **ViewModel:** `HistorialViewModel`
- **Backend:** `GET /api/entrenamientos/usuario/{idUsuario}`
- **Descripción:** Lista cronológica de todas las actividades deportivas realizadas por el usuario. Cada tarjeta resume distancia, tiempo, pasos y calorías, permitiendo navegar al resumen detallado. Se recarga automáticamente ante mensajes MQTT de cierre de entrenamiento.

### 8. MetasScreen
- **Archivo:** [MetasScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/metas/MetasScreen.kt)
- **ViewModel:** `MetasViewModel`, `AuthViewModel`
- **Backend:** `GET /api/metas/usuario/{idUsuario}` y `DELETE /api/metas/{idMetas}`
- **Descripción:** Muestra las metas de actividad del usuario (Pasos, Calorías, Distancia, Tiempo). Renderiza indicadores de progreso `LinearProgressIndicator` con colores específicos por tipo de métrica. Ofrece opciones para crear, editar o eliminar metas mediante diálogos de confirmación.

### 9. CrearMetaScreen
- **Archivo:** [CrearMetaScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/metas/CrearMetaScreen.kt)
- **ViewModel:** `MetasViewModel`, `AuthViewModel`
- **Backend:** `POST /api/metas`
- **Descripción:** Permite configurar una nueva meta personal. Filtra automáticamente en un menú desplegable solo los tipos de meta que el usuario aún no tenga activos. Valida numéricamente el valor objetivo con sufijos de unidad (pasos, kcal, km, min).

### 10. EditarMetaScreen
- **Archivo:** [EditarMetaScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/metas/EditarMetaScreen.kt)
- **ViewModel:** `MetasViewModel`, `AuthViewModel`
- **Backend:** `PUT /api/metas/{idMetas}`
- **Descripción:** Permite modificar el valor objetivo de una meta activa existente.

### 11. GruposScreen
- **Archivo:** [GruposScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/grupos/GruposScreen.kt)
- **ViewModel:** `GrupoViewModel`, `AuthViewModel`
- **Backend:** `GET /api/grupos/usuario/{idUsuario}`, `POST /api/grupos`, `POST /api/grupos/unirse`
- **Descripción:** Pantalla principal de comunidades. Muestra las tarjetas de los grupos a los que pertenece el usuario. Incluye dos diálogos emergentes:
  - **Crear Grupo:** Ingrese nombre y descripción opcional. Genera un grupo y su código de invitación.
  - **Unirse a Grupo:** Permite ingresar un código alfanumérico para unirse a un grupo existente.

### 12. DetalleGrupoScreen
- **Archivo:** [DetalleGrupoScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/grupos/DetalleGrupoScreen.kt)
- **ViewModel:** `GrupoViewModel`
- **Backend:** `GET /api/grupos/{idGrupo}/miembros`, `GET /api/grupos/{idGrupo}/ranking`, `DELETE /api/grupos/{idGrupo}/miembros/{idUsuario}`
- **Descripción:** Vista detallada de un grupo con navegación por pestañas (`TabRow`):
  - **Pestaña Estadísticas:** Sumatoria total de kilómetros, pasos, calorías y tiempo acumulados por todos los miembros, junto a la tabla de posiciones (Ranking) ordenada por rendimiento.
  - **Pestaña Miembros:** Listado de integrantes del grupo. Incluye opción para abandonar el grupo.

### 13. PerfilScreen
- **Archivo:** [PerfilScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/perfil/PerfilScreen.kt)
- **ViewModel:** `PerfilViewModel`, `AuthViewModel`
- **Backend:** `GET /api/usuarios/{idUsuario}` y `PUT /api/usuarios/{idUsuario}`
- **Descripción:** Permite consultar y modificar la información del perfil del usuario (nombre completo, contraseña y peso corporal). Ofrece accesos a vincular TV, gestionar dispositivos vinculados y cerrar sesión de la aplicación.

### 14. VincularDispositivoScreen
- **Archivo:** [VincularDispositivoScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/perfil/VincularDispositivoScreen.kt)
- **ViewModel:** `VincularDispositivoViewModel`
- **Backend:** `POST /api/dispositivos/vincular-codigo`
- **Descripción:** Permite vincular una pantalla de TV ingresando el código alfanumérico temporal de 6 caracteres proyectado en el módulo de TV.

### 15. DispositivosScreen
- **Archivo:** [DispositivosScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/screens/perfil/DispositivosScreen.kt)
- **ViewModel:** `DispositivosViewModel`, `AuthViewModel`
- **Backend:** `GET /api/dispositivos`, `DELETE /api/dispositivos/{idDispositivo}`, `POST /api/dispositivos/cerrar-sesiones`
- **Descripción:** Lista los dispositivos vinculados a la cuenta del usuario (Smart TV y Wear OS). Permite desvincular dispositivos de forma individual o revocar el acceso a todas las sesiones activas.

---

## ViewModels

### 1. AuthViewModel
- **Archivo:** [AuthViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/AuthViewModel.kt)
- **Estado (`AuthUiState`):** `isLoading`, `isLoggedIn`, `idUsuario`, `nombre`, `pesoKg`, `token`, `registrationSuccess`, `error`
- **Funciones principales:** `login()`, `register()`, `actualizarNombreLocal()`, `actualizarPesoLocal()`, `cerrarSesion()`. Persiste la sesión activa en `AuthSessionStore`.

### 2. EntrenamientoViewModel
- **Archivo:** [EntrenamientoViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/EntrenamientoViewModel.kt)
- **Estado (`EntrenamientoUiState`):** `idEntrenamiento`, `distancia`, `pasos`, `calorias`, `tiempo`, `estaActivo`, `ruta` (lista de `Coordenada`), `ubicacionActual`, `metricasSimuladas`, `finalizado`, `error`
- **Funciones principales:** `iniciar()`, `cargarUbicacionInicial()`, `actualizarMetricas()`, `finalizar()`. Gestiona corrutinas independientes para la recepción de ubicación GPS, conteo de pasos del sensor y cronómetro.

### 3. HistorialViewModel
- **Archivo:** [HistorialViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/HistorialViewModel.kt)
- **Estado (`HistorialUiState`):** `isLoading`, `entrenamientos` (lista de `EntrenamientoResponse`), `error`
- **Funciones principales:** `cargar(idUsuario, forzar)`. Carga la lista de entrenamientos desde el servidor.

### 4. MetasViewModel
- **Archivo:** [MetasViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/MetasViewModel.kt)
- **Estado (`MetasUiState`):** `isLoading`, `metas` (lista de `MetaResponse`), `isMetaCreated`, `isMetaUpdated`, `isMetaDeleted`, `error`
- **Funciones principales:** `cargarMetas()`, `crearMeta()`, `editarMeta()`, `eliminarMeta()`.

### 5. GrupoViewModel
- **Archivo:** [GrupoViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/GrupoViewModel.kt)
- **Estado (`EstadoUiGrupos`):** `listaGrupos`, `listaMiembros`, `listaRanking`, `estaCargando`, `mensajeError`
- **Funciones principales:** `cargarGruposDeUsuario()`, `crearNuevoGrupo()`, `unirseAGrupoConCodigo()`, `cargarDetalleGrupo()`, `salirDeGrupo()`.

### 6. PerfilViewModel
- **Archivo:** [PerfilViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/PerfilViewModel.kt)
- **Estado (`EstadoUiPerfil`):** `cargando`, `nombreUsuario`, `nombre`, `pesoKg`, `exito`, `error`
- **Funciones principales:** `cargarUsuario()`, `actualizarUsuario()`.

### 7. PesoViewModel
- **Archivo:** [PesoViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/PesoViewModel.kt)
- **Estado (`PesoUiState`):** `guardando`, `guardado`, `error`
- **Funciones principales:** `guardarPeso()`. Valida que el peso esté en el rango realista de 20.0 kg a 300.0 kg.

### 8. MqttViewModel
- **Archivo:** [MqttViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/MqttViewModel.kt)
- **Estado:** Expone `connectionState` (`StateFlow<MqttConnectionState>`) y `events` (`SharedFlow<MqttEvent>`) provistos por `MqttManager`.
- **Funciones principales:** `connect(userId)`, `disconnect()`.

### 9. VincularDispositivoViewModel
- **Archivo:** [VincularDispositivoViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/VincularDispositivoViewModel.kt)
- **Estado (`VincularDispositivoUiState`):** `cargando`, `vinculado`, `mensaje`, `error`
- **Funciones principales:** `vincular(token, codigo)`. Normaliza y valida códigos de 6 caracteres.

### 10. DispositivosViewModel
- **Archivo:** [DispositivosViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/viewmodel/DispositivosViewModel.kt)
- **Estado (`DispositivosUiState`):** `cargando`, `dispositivos`, `error`, `sesionesCerradas`
- **Funciones principales:** `cargar(token)`, `desvincular(token, idDispositivo)`, `cerrarTodas(token)`.

---

## Módulo Compartido `:core` — Endpoints Consultados

El módulo `:core` contiene la definición centralizada de Retrofit ([ApiService.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/core/src/main/java/mx/utng/cala/core/data/remote/ApiService.kt)) consumida por el módulo móvil:

| Método HTTP | Endpoint | Request Body | Response Body | Repositorio |
|---|---|---|---|---|
| `POST` | `auth/login` | `LoginRequest` | `LoginResponse` | `AuthRepository` |
| `POST` | `auth/register` | `RegisterRequest` | `Unit` | `AuthRepository` |
| `GET` | `usuarios/{id}` | Path variable | `UsuarioResponse` | `RepositorioUsuario` |
| `PUT` | `usuarios/{id}` | `ActualizarUsuarioPeticion` | `UsuarioResponse` | `RepositorioUsuario` |
| `PUT` | `usuarios/{id}/peso` | `ActualizarPesoPeticion` | `UsuarioResponse` | `RepositorioUsuario` |
| `POST` | `entrenamientos/iniciar` | `IniciarEntrenamientoRequest` | `EntrenamientoResponse` | `EntrenamientoRepository` |
| `PUT` | `entrenamientos/finalizar` | `FinalizarEntrenamientoRequest` | `EntrenamientoResponse` | `EntrenamientoRepository` |
| `GET` | `entrenamientos/usuario/{idUsuario}` | Path variable | `List<EntrenamientoResponse>` | `EntrenamientoRepository` |
| `POST` | `metas` | `CrearMetaRequest` | `MetaResponse` | `MetaRepository` |
| `GET` | `metas/usuario/{idUsuario}` | Path variable | `List<MetaResponse>` | `MetaRepository` |
| `PUT` | `metas/{idMetas}` | `ActualizarMetaRequest` | `MetaResponse` | `MetaRepository` |
| `DELETE` | `metas/{idMetas}` | Path variable | `Unit` | `MetaRepository` |
| `POST` | `grupos` | `CrearGrupoRequest` | `GrupoResponse` | `GrupoRepository` |
| `POST` | `grupos/unirse` | `UnirseGrupoRequest` | `Unit` | `GrupoRepository` |
| `GET` | `grupos/usuario/{idUsuario}` | Path variable | `List<GrupoResponse>` | `GrupoRepository` |
| `GET` | `grupos/{idGrupo}/miembros` | Path variable | `List<MiembroGrupoResponse>` | `GrupoRepository` |
| `GET` | `grupos/{idGrupo}/ranking` | Path variable | `RankingResponse` | `GrupoRepository` |
| `DELETE` | `grupos/{idGrupo}/miembros/{idUsuario}` | Path variable | `Unit` | `GrupoRepository` |
| `POST` | `dispositivos/vincular-codigo` | `VincularCodigoRequest` | `DispositivoResponse` | `DispositivoRepository` |
| `GET` | `dispositivos` | Header Token | `List<DispositivoResponse>` | `DispositivoRepository` |
| `DELETE` | `dispositivos/{idDispositivo}` | Path variable | `Unit` | `DispositivoRepository` |
| `POST` | `dispositivos/cerrar-sesiones` | Header Token | `Unit` | `DispositivoRepository` |

---

## Tema Visual y Paleta de Colores

El módulo móvil aplica un esquema visual exclusivo de **modo oscuro** (`DarkColorScheme`) definido en [Color.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/app/src/main/java/mx/utng/cala/rutalibre/ui/theme/Color.kt):

| Token | Hex | Propósito |
|---|---|---|
| `Primary` | `#7ED957` | Verde de marca, botones principales y acentos de éxito |
| `PrimaryContainer` | `#1B5E20` | Contenedores primarios oscuros |
| `Secondary` | `#4DA3FF` | Azul secundario para grupos y elementos interactivos |
| `Tertiary` | `#7C4DFF` | Púrpura terciario para metas |
| `Background` | `#050B17` | Fondo general de la aplicación |
| `Surface` | `#0B1424` | Fondo de tarjetas y campos de texto |
| `SurfaceVariant` | `#111D31` | Fondo de contenedores elevados |
| `Outline` | `#2A3B55` | Bordes de tarjetas e insumos |
| `Error` | `#FF5252` | Mensajes y estados de error |

**Paleta de Métricas Deportivas:**

| Métrica | Token Color | Hex |
|---|---|---|
| Distancia | `Distancia` | `#63E66C` |
| Pasos | `Pasos` | `#42A5FF` |
| Calorías | `Calorias` | `#FF8A1F` |
| Tiempo | `Tiempo` | `#7A5CFF` |

---

## Estado Actual de Implementación

| Pantalla | Interfaz Visual (Compose) | ViewModel / Estado | Integración Backend / MQTT | Estado |
|---|---|---|---|---|
| **Login** | ✅ Completa | ✅ `AuthViewModel` | ✅ `POST /api/auth/login` | 🟢 100% Funcional |
| **Register** | ✅ Completa | ✅ `AuthViewModel` | ✅ `POST /api/auth/register` | 🟢 100% Funcional |
| **Peso Inicial** | ✅ Completa | ✅ `PesoViewModel` | ✅ `PUT /api/usuarios/{id}/peso` | 🟢 100% Funcional |
| **Home** | ✅ Completa | ✅ `MqttViewModel` | ✅ MQTT Status en tiempo real | 🟢 100% Funcional |
| **Entrenamiento** | ✅ Completa | ✅ `EntrenamientoViewModel` | ✅ REST API & MapTiler & GPS | 🟢 100% Funcional |
| **Resumen** | ✅ Completa | ✅ `HistorialViewModel` | ✅ REST API | 🟢 100% Funcional |
| **Historial** | ✅ Completa | ✅ `HistorialViewModel` | ✅ REST API & Refresco MQTT | 🟢 100% Funcional |
| **Metas** | ✅ Completa | ✅ `MetasViewModel` | ✅ REST API (GET, DELETE) | 🟢 100% Funcional |
| **Crear Meta** | ✅ Completa | ✅ `MetasViewModel` | ✅ `POST /api/metas` | 🟢 100% Funcional |
| **Editar Meta** | ✅ Completa | ✅ `MetasViewModel` | ✅ `PUT /api/metas/{idMetas}` | 🟢 100% Funcional |
| **Grupos** | ✅ Completa | ✅ `GrupoViewModel` | ✅ REST API (GET, POST) | 🟢 100% Funcional |
| **Detalle Grupo** | ✅ Completa | ✅ `GrupoViewModel` | ✅ REST Ranking & Miembros | 🟢 100% Funcional |
| **Perfil** | ✅ Completa | ✅ `PerfilViewModel` | ✅ REST API (GET, PUT) | 🟢 100% Funcional |
| **Vincular TV** | ✅ Completa | ✅ `VincularDispositivoViewModel` | ✅ `POST /api/dispositivos/vincular-codigo` | 🟢 100% Funcional |
| **Dispositivos** | ✅ Completa | ✅ `DispositivosViewModel` | ✅ REST API (GET, DELETE, POST) | 🟢 100% Funcional |
