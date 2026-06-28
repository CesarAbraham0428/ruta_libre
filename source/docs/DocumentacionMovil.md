# Módulo Móvil — Ruta Libre

## Arquitectura

El módulo móvil (`:app`) sigue el patrón **MVVM** (Model-View-ViewModel) con **Jetpack Compose** para la UI y **Retrofit** para la comunicación con el backend a través del módulo compartido `:core`.

```
Screen (Composable) → ViewModel → Repository → ApiService (Retrofit) → Backend REST
```

Cada `ViewModel` expone un `StateFlow<UiState>` que la `Screen` observa con `collectAsState()` para renderizar la UI reactivamente.

El módulo `:core` contiene los DTOs de request/response, el `ApiService` con todos los endpoints, los repositorios y las entidades de modelo.

---

## Navegación

### Routes.kt

Define las rutas de navegación como constantes en el objeto `Routes`:

| Ruta | Constante | Ruta con parámetro |
|---|---|---|
| Inicio de sesión | `LOGIN` | `"login"` |
| Registro | `REGISTER` | `"register"` |
| Menú principal | `HOME` | `"home"` |
| Entrenamiento | `ENTRENAMIENTO` | `"entrenamiento/{idEntrenamiento}"` |
| Resumen | `RESUMEN` | `"resumen/{idEntrenamiento}"` |
| Metas | `METAS` | `"metas"` |
| Crear meta | `CREAR_META` | `"crear_meta"` |
| Editar meta | `EDITAR_META` | `"editar_meta/{idMeta}"` |
| Grupos | `GRUPOS` | `"grupos"` |
| Perfil | `PERFIL` | `"perfil"` |

**Funciones auxiliares de navegación:**
```kotlin
Routes.entrenamiento(id: Int)  → "entrenamiento/$id"
Routes.resumen(id: Int)        → "resumen/$id"
Routes.editarMeta(id: Int)     → "editar_meta/$id"
```

### NavGraph.kt

Un único `NavHost` con `startDestination = Routes.LOGIN`. Se crean instancias de `AuthViewModel` y `MetasViewModel` a nivel del NavHost para que las pantallas compartan el estado.

```kotlin
composable(Routes.LOGIN)         -> LoginScreen(navController, authViewModel)
composable(Routes.REGISTER)      -> RegisterScreen(navController, authViewModel)
composable(Routes.HOME)          -> HomeScreen(navController)
composable(Routes.ENTRENAMIENTO)  -> EntrenamientoScreen(navController)
composable(Routes.RESUMEN)       -> ResumenScreen(navController)
composable(Routes.METAS)          -> MetasScreen(navController, metasViewModel, authViewModel)
composable(Routes.CREAR_META)    -> CrearMetaScreen(navController, metasViewModel, authViewModel)
composable(Routes.EDITAR_META)   -> EditarMetaScreen(navController, metasViewModel, authViewModel, idMeta)
composable(Routes.GRUPOS)        -> GruposScreen(navController)
composable(Routes.PERFIL)        -> PerfilScreen(navController)
```

---

## Pantallas

### LoginScreen

**Archivo:** `ui/screens/auth/LoginScreen.kt`
**ViewModel:** `AuthViewModel`

Pantalla de inicio de sesión con usuario y contraseña.

- **Campos:**
  - `OutlinedTextField` para nombre de usuario con icono `Person`, label "Nombre de usuario", placeholder "Ingresa tu nombre de usuario". IME action `Next` que mueve el foco al campo de contraseña.
  - `OutlinedTextField` para contraseña con icono `Lock`, toggle de visibilidad (`Visibility`/`VisibilityOff`), `PasswordVisualTransformation`. IME action `Done` que ejecuta el login automáticamente si ambos campos no están vacíos.
- **Validación:** El botón se habilita solo si `!isLoading && usuario.isNotBlank() && password.isNotBlank()`
- **Conexión backend:** `authViewModel.login(usuario, password)` → `POST /api/auth/login` con `LoginRequest(nombreUsuario, password)`
- **Éxito:** Recibe `LoginResponse(idUsuario, nombre, nombreUsuario, token)`, establece `isLoggedIn = true` y `LaunchedEffect` navega a `HomeScreen` con `popUpTo(LOGIN) { inclusive = true }`
- **Error:** Muestra el mensaje de error del servidor en `MaterialTheme.colorScheme.error`
- **Navegación:** Al presionar "Regístrate" navega a `RegisterScreen`

### RegisterScreen

**Archivo:** `ui/screens/auth/RegisterScreen.kt`
**ViewModel:** `AuthViewModel`

Creación de cuenta nueva.

- **Campos:** `OutlinedTextField` para nombre de usuario, nombre completo y contraseña (con toggle de visibilidad)
- **Validación en tiempo real:** Checklist visual con 3 `PasswordRequirement` composables:
  - Mínimo 8 caracteres (`password.length >= 8`)
  - Al menos una letra (`password.any { it.isLetter() }`)
  - Al menos un número (`password.any { it.isDigit() }`)
  - Cada requisito muestra un `CheckCircle` icono verde o gris según se cumpla
- **Conexión backend:** `authViewModel.register(nombre, usuario, password)` → `POST /api/auth/register` con `RegisterRequest(nombre, nombreUsuario, password)`
- **Éxito:** Establece `registrationSuccess = true`, `LaunchedEffect` resetea el estado y navega a `LoginScreen` con `popUpTo(REGISTER) { inclusive = true }`
- **Error:** Muestra el mensaje de error del servidor
- **Navegación:** Al presionar "Inicia sesión" vuelve a `LoginScreen`

### HomeScreen

**Archivo:** `ui/screens/home/HomeScreen.kt`
**ViewModel:** Ninguno

Menú principal con acceso a funcionalidades mediante tarjetas `Card` clickables:

| Opción | Navegación |
|---|---|
| Iniciar entrenamiento | `EntrenamientoScreen` |
| Metas | `MetasScreen` |
| Grupos | `GruposScreen` |
| Perfil | `PerfilScreen` |

### EntrenamientoScreen

**Archivo:** `ui/screens/entrenamiento/EntrenamientoScreen.kt`
**ViewModel:** Ninguno

Pantalla de entrenamiento con valores estáticos. Muestra distancia (0.0 km), pasos (0), calorías (0), tiempo (00:00). Botón "Finalizar" que retrocede a la pantalla anterior.

### ResumenScreen

**Archivo:** `ui/screens/resumen/ResumenScreen.kt`
**ViewModel:** Ninguno

Resumen estático con datos de ejemplo: Distancia 5.2 km, Pasos 6,500, Calorías 320, Tiempo 42 min. Botón "Volver" que retrocede.

### MetasScreen

**Archivo:** `ui/screens/metas/MetasScreen.kt`
**ViewModel:** `MetasViewModel`

Pantalla principal de metas personales de actividad física.

- **TopBar:** `TopAppBar` con título "Metas" centrado, flecha de retroceso a la izquierda y botón `+` (icono `Add`) a la derecha que navega a `CrearMetaScreen`
- **Lista de metas:** `LazyColumn` con tarjetas `MetaCard` que muestran por cada meta:
  - Icono a la izquierda coloreado según el tipo (zapato azul para pasos, fuego naranja para calorías, ubicación verde para distancia, reloj morado para tiempo)
  - Nombre del tipo y valor con unidad (ej. "Pasos / 8000 pasos")
  - Botones de editar (lápiz) y eliminar (basura) a la derecha
  - `LinearProgressIndicator` coloreada según el tipo de meta con `valorActual / valorObjetivo`
  - Texto "Actual: X unidad" y "Objetivo: Y unidad" debajo de la barra
- **Acciones:**
  - **Editar** — Navega a `EditarMetaScreen` con el ID de la meta
  - **Eliminar** — Muestra un `AlertDialog` de confirmación con icono `Delete` rojo, título "Eliminar meta", mensaje "¿Estás seguro de que deseas eliminar la meta de {nombre}?", y botones CANCELAR / ELIMINAR
- **Carga:** `LaunchedEffect` llama a `metasViewModel.cargarMetas(idUsuario)` usando `authState.idUsuario`
- **Estados:** Muestra `CircularProgressIndicator` mientras carga, o texto "Aún no tienes metas. ¡Crea una!" si está vacía
- **Colores:** Fondo `Background`, tarjetas `SurfaceVariant` con `RoundedCornerShape(12dp)`

### CrearMetaScreen

**Archivo:** `ui/screens/metas/CrearMetaScreen.kt`
**ViewModel:** `MetasViewModel`

Pantalla para crear una nueva meta personal.

- **TopBar:** Título "Crear meta" centrado con flecha de retroceso
- **Card central** con fondo `SurfaceVariant` y `RoundedCornerShape(16dp)` que contiene:
  - **"Tipo de meta"** — `ExposedDropdownMenuBox` con:
    - `OutlinedTextField` de solo lectura con icono del tipo seleccionado y flecha desplegable
    - Menú desplegable que lista solo los **tipos disponibles** (excluye aquellos con una meta activa no terminada)
    - Si todos los tipos tienen metas activas, muestra texto "Completa tus metas actuales..."
  - **"Meta"** — `OutlinedTextField` para ingresar el valor numérico con:
    - Placeholder "0"
    - Sufijo con la unidad correspondiente al tipo seleccionado (pasos, kcal, km, min)
    - Validación de solo dígitos y punto decimal; si se ingresa otro carácter, muestra error "Solo se permiten números"
- **Botones inferiores:**
  - **CANCELAR** (`OutlinedButton`) — navega de regreso
  - **GUARDAR** (`Button` verde `Primary`) — llama a `metasViewModel.crearMeta(idUsuario, selectedTipo, valor)` convirtiendo el valor a `Double`
- **Comportamiento:** Al crearse exitosamente (`isMetaCreated = true`), limpia el estado y navega de regreso automáticamente
- **Validación:** Botón GUARDAR deshabilitado si el campo está vacío, hay error de entrada, hay carga en curso, o no hay tipos disponibles

### EditarMetaScreen

**Archivo:** `ui/screens/metas/EditarMetaScreen.kt`
**ViewModel:** `MetasViewModel`

Pantalla para editar el valor objetivo de una meta existente.

- **Parámetro de ruta:** `idMeta` (ID de la meta a editar)
- **Obtención de meta:** Busca la meta en `metasState.metas` filtrando por `idMetas == idMeta`
- **TopBar:** Título "Editar meta" centrado con flecha de retroceso
- **Card central** con fondo `SurfaceVariant` y bordes redondeados que contiene:
  - Icono y nombre del tipo de meta (ej. "Distancia" con icono de ubicación verde)
  - **"Meta actual"** — Valor objetivo actual con unidad (ej. "15 km")
  - **"Nueva meta"** — `OutlinedTextField` para ingresar el nuevo valor numérico con:
    - Valor actual precargado en el campo
    - Sufijo con la unidad correspondiente al tipo de meta
    - Validación de solo dígitos y punto decimal
- **Botones inferiores:**
  - **CANCELAR** (`OutlinedButton`) — navega de regreso
  - **GUARDAR** (`Button` verde `Primary`) — llama a `metasViewModel.editarMeta(idUsuario, idMetas, nuevoValor)`
- **Comportamiento:** Al actualizarse exitosamente (`isMetaUpdated = true`), limpia el estado y navega de regreso automáticamente
- **Validación:** Botón GUARDAR deshabilitado si el campo está vacío, hay error de entrada, o hay carga en curso

### GruposScreen

**Archivo:** `ui/screens/grupos/GruposScreen.kt`
**ViewModel:** Ninguno (`GrupoViewModel` existe pero no está conectado en NavGraph ni en la screen)

Gestión de grupos con UI placeholder.

- **Botones:** "Crear grupo" y "Unirse a grupo" con `onClick` vacío
- **Texto informativo:** "Tus grupos aparecerán aquí"

### PerfilScreen

**Archivo:** `ui/screens/perfil/PerfilScreen.kt`
**ViewModel:** Ninguno

Información del perfil con datos estáticos.

- **Datos:** Muestra "Nombre de usuario" y "Correo: usuario@ejemplo.com" como texto fijo
- **Acciones:** Botón "Cerrar sesión" que retrocede a la pantalla anterior

---

## ViewModels

### AuthViewModel

**Repositorio:** `AuthRepository` (del módulo `:core`)

**Estado (`AuthUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `isLoggedIn` | Boolean | `true` después de un login exitoso |
| `idUsuario` | Int? | ID del usuario autenticado |
| `nombre` | String? | Nombre completo del usuario |
| `registrationSuccess` | Boolean | `true` después de un registro exitoso |
| `error` | String? | Mensaje de error a mostrar |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `login(usuario, password)` | `POST /api/auth/login` | Inicia sesión, establece `idUsuario` y `nombre` |
| `register(nombre, usuario, password)` | `POST /api/auth/register` | Registra un nuevo usuario |
| `clearError()` | — | Limpia el mensaje de error |
| `resetRegistrationState()` | — | Resetea el flag de registro exitoso |

### EntrenamientoViewModel

**Repositorios:** `EntrenamientoRepository`, `RutaRepository` (del módulo `:core`)

**Estado (`EntrenamientoUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `idEntrenamiento` | Int? | ID del entrenamiento activo |
| `distancia` | Double | Distancia recorrida (km) |
| `pasos` | Int | Pasos realizados |
| `calorias` | Int | Calorías quemadas |
| `tiempo` | Int | Tiempo transcurrido (segundos) |
| `estaActivo` | Boolean | Indica si hay un entrenamiento activo |
| `error` | String? | Mensaje de error |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `iniciar(idUsuario)` | `POST /api/entrenamientos/iniciar` | Inicia un nuevo entrenamiento |
| `finalizar(idEntrenamiento, pasos, calorias, distancia, tiempo, coordenadas, inicio, fin)` | `PUT /api/entrenamientos/finalizar` | Finaliza el entrenamiento activo |

### GrupoViewModel

**Repositorio:** `GrupoRepository` (del módulo `:core`)

**Estado (`GrupoUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `grupos` | List\<GrupoResponse\> | Grupos del usuario |
| `miembros` | List\<MiembroGrupoResponse\> | Miembros de un grupo |
| `error` | String? | Mensaje de error |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `cargarGrupos(idUsuario)` | `GET /api/grupos/usuario/{idUsuario}` | Obtiene los grupos del usuario |
| `crearGrupo(nombre, descripcion)` | `POST /api/grupos` | Crea un nuevo grupo |
| `unirseGrupo(idUsuario, codigo)` | `POST /api/grupos/unirse` | Se une a un grupo con código |
| `cargarMiembros(idGrupo)` | `GET /api/grupos/{idGrupo}/miembros` | Obtiene los miembros de un grupo |

### MetasViewModel

**Repositorio:** `MetaRepository` (del módulo `:core`)

**Estado (`MetasUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `metas` | List\<MetaResponse\> | Metas del usuario |
| `isMetaCreated` | Boolean | `true` después de crear una meta exitosamente |
| `isMetaUpdated` | Boolean | `true` después de actualizar una meta exitosamente |
| `isMetaDeleted` | Boolean | `true` después de eliminar una meta exitosamente |
| `error` | String? | Mensaje de error |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `cargarMetas(idUsuario)` | `GET /api/metas/usuario/{idUsuario}` | Obtiene las metas del usuario |
| `crearMeta(idUsuario, tipo, valor)` | `POST /api/metas` | Crea una nueva meta y recarga la lista |
| `editarMeta(idUsuario, idMetas, nuevoValor)` | `PUT /api/metas/{idMetas}` | Actualiza el valor objetivo y recarga la lista |
| `eliminarMeta(idUsuario, idMetas)` | `DELETE /api/metas/{idMetas}` | Elimina una meta y recarga la lista |
| `clearError()` | — | Limpia el mensaje de error |
| `resetMetaCreatedState()` | — | Resetea el flag de meta creada |
| `resetMetaUpdatedState()` | — | Resetea el flag de meta actualizada |
| `resetMetaDeletedState()` | — | Resetea el flag de meta eliminada |

---

## Módulo core (`:core`)

El módulo compartido `:core` contiene toda la capa de datos:

### ApiService

Retrofit interface con `BASE_URL = "http://10.0.2.2:3000/api/"`.

| Método HTTP | Endpoint | Request | Response |
|---|---|---|---|
| POST | `auth/login` | `LoginRequest` | `LoginResponse` |
| POST | `auth/register` | `RegisterRequest` | `Unit` |
| GET | `usuarios/{id}` | path | `UsuarioResponse` |
| POST | `entrenamientos/iniciar` | `IniciarEntrenamientoRequest` | `EntrenamientoResponse` |
| PUT | `entrenamientos/finalizar` | `FinalizarEntrenamientoRequest` | `EntrenamientoResponse` |
| GET | `entrenamientos/activo/{idUsuario}` | path | `EntrenamientoActivoResponse` |
| GET | `entrenamientos/usuario/{idUsuario}` | path | `List<EntrenamientoResponse>` |
| GET | `entrenamientos/semana/{idUsuario}` | path | `DashboardSemanalResponse` |
| GET | `entrenamientos/comparacion/{idUsuario}` | path | `ComparacionRendimientoResponse` |
| POST | `rutas/actualizar` | `ActualizarRutaRequest` | `RutaResponse` |
| GET | `rutas/{id}` | path | `RutaResponse` |
| POST | `metas` | `CrearMetaRequest` | `MetaResponse` |
| GET | `metas/usuario/{idUsuario}` | path | `List<MetaResponse>` |
| PUT | `metas/{idMetas}` | path + `ActualizarMetaRequest` | `MetaResponse` |
| DELETE | `metas/{idMetas}` | path | `Unit` |
| POST | `grupos` | `CrearGrupoRequest` | `GrupoResponse` |
| POST | `grupos/unirse` | `UnirseGrupoRequest` | `Unit` |
| GET | `grupos/usuario/{idUsuario}` | path | `List<GrupoResponse>` |
| GET | `grupos/{idGrupo}/miembros` | path | `List<MiembroGrupoResponse>` |
| GET | `grupos/{idGrupo}/ranking` | path | `RankingResponse` |
| GET | `notificaciones/usuario/{idUsuario}` | path | `List<NotificacionResponse>` |
| PUT | `notificaciones/{id}/leer-movil` | path | `Unit` |
| PUT | `notificaciones/{id}/leer-wear` | path | `Unit` |

### Repositorios

| Repositorio | Métodos |
|---|---|
| `AuthRepository` | `login()`, `register()` |
| `EntrenamientoRepository` | `iniciar()`, `finalizar()`, `getActivo()`, `getHistorial()`, `getDashboardSemanal()`, `getComparacion()` |
| `MetaRepository` | `crearMeta()`, `getMetas()`, `actualizarMeta()`, `eliminarMeta()` |
| `GrupoRepository` | `crearGrupo()`, `unirseGrupo()`, `getGrupos()`, `getMiembros()`, `getRanking()` |
| `RutaRepository` | `actualizar()`, `getRuta()` |
| `NotificacionRepository` | `getNotificaciones()`, `marcarLeidaMovil()`, `marcarLeidaSmartwatch()` |

### Modelos principales

| Tipo | Clases |
|---|---|
| Request DTOs | `LoginRequest`, `RegisterRequest`, `IniciarEntrenamientoRequest`, `FinalizarEntrenamientoRequest`, `CrearMetaRequest`, `ActualizarMetaRequest`, `CrearGrupoRequest`, `UnirseGrupoRequest`, `ActualizarRutaRequest` |
| Response DTOs | `LoginResponse`, `UsuarioResponse`, `EntrenamientoResponse`, `EntrenamientoActivoResponse`, `MetaResponse`, `GrupoResponse`, `MiembroGrupoResponse`, `RankingResponse`, `RutaResponse`, `CoordenadaResponse`, `NotificacionResponse`, `DashboardSemanalResponse`, `RendimientoDiarioResponse`, `ComparacionRendimientoResponse` |
| Entidades | `Usuario`, `Entrenamiento`, `Punto`, `Meta`, `TipoMeta` (enum), `Grupo`, `UsuarioGrupo`, `Ruta`, `Coordenada`, `Notificacion` |

---

## Tema visual

El módulo móvil utiliza un tema oscuro con los siguientes colores:

| Token | Hex | Uso |
|---|---|---|
| `Primary` | `#7ED957` | Verde — color de marca, botones, enlaces |
| `PrimaryContainer` | `#1B5E20` | Contenedor primario |
| `Secondary` | `#4DA3FF` | Azul secundario |
| `Tertiary` | `#7C4DFF` | Púrpura terciario |
| `Background` | `#050B17` | Fondo principal oscuro |
| `Surface` | `#0B1424` | Fondo de campos de texto |
| `SurfaceVariant` | `#111D31` | Fondo de tarjetas y contenedores |
| `SurfaceContainer` | `#16233A` | Contenedor elevado |
| `OnBackground` | `#FFFFFF` | Texto sobre fondo |
| `OnSurface` | `#F5F5F5` | Color del texto principal |
| `OnSurfaceVariant` | `#B0B8C5` | Color del texto secundario |
| `Outline` | `#2A3B55` | Bordes de campos de texto |
| `Error` | `#FF5252` | Mensajes de error |
| `Success` | `#7ED957` | Indicadores de éxito |
| `Warning` | `#FFB020` | Advertencias |
| `Info` | `#42A5FF` | Información |

**Colores de métricas deportivas:**

| Métrica | Hex |
|---|---|
| Distancia | `#63E66C` |
| Pasos | `#42A5FF` |
| Calorías | `#FF8A1F` |
| Tiempo | `#7A5CFF` |

**Tema:** Exclusivamente oscuro (`DarkColorScheme` siempre activo sin modo claro).

---

## Estado actual de implementación

| Pantalla | UI | ViewModel | Backend |
|---|---|---|---|
| Login | ✅ Completa | ✅ Conectado | ✅ Funcional |
| Register | ✅ Completa | ✅ Conectado | ✅ Funcional |
| Home | ✅ Completa | — | — |
| Entrenamiento | ⚠️ Placeholder (estático) | ❌ No conectado | ❌ No usado |
| Resumen | ⚠️ Placeholder (estático) | ❌ No conectado | ❌ No usado |
| Metas | ✅ Completa | ✅ Conectado | ✅ Funcional |
| Crear Meta | ✅ Completa | ✅ Conectado | ✅ Funcional |
| Editar Meta | ✅ Completa | ✅ Conectado | ✅ Funcional |
| Grupos | ⚠️ Placeholder | ❌ No conectado | ❌ No usado |
| Perfil | ⚠️ Placeholder (estático) | ❌ No conectado | ❌ No usado |
