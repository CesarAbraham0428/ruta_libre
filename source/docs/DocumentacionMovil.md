# Módulo Móvil — Ruta Libre

## Arquitectura

El módulo móvil (`:app`) sigue el patrón **MVVM** (Model-View-ViewModel) con **Jetpack Compose** para la UI y **Retrofit** para la comunicación con el backend.

```
Screen (Composable) → ViewModel → Repository → Retrofit (ApiService) → Backend REST
```

Cada `ViewModel` expone un `StateFlow<UiState>` que la `Screen` observa con `collectAsState()` para renderizar la UI reactivamente.

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
| Grupos | `GRUPOS` | `"grupos"` |
| Perfil | `PERFIL` | `"perfil"` |

### NavGraph.kt

Un único `NavHost` con `startDestination = Routes.LOGIN`. Se crea una instancia compartida de `AuthViewModel` a nivel del NavHost para que `LoginScreen` y `RegisterScreen` compartan el estado de autenticación.

```kotlin
composable(Routes.LOGIN)        -> LoginScreen(navController, authViewModel)
composable(Routes.REGISTER)     -> RegisterScreen(navController, authViewModel)
composable(Routes.HOME)         -> HomeScreen(navController)
composable(Routes.ENTRENAMIENTO) -> EntrenamientoScreen(navController)
composable(Routes.RESUMEN)      -> ResumenScreen(navController)
composable(Routes.METAS)        -> MetasScreen(navController)
composable(Routes.GRUPOS)       -> GruposScreen(navController)
composable(Routes.PERFIL)       -> PerfilScreen(navController)
```

---

## Pantallas

### LoginScreen

**Archivo:** `ui/screens/auth/LoginScreen.kt`
**ViewModel:** `AuthViewModel`

Permite al usuario iniciar sesión con nombre de usuario y contraseña.

- **Campos:** `OutlinedTextField` para usuario y contraseña (con visibilidad toggle)
- **Validación:** El botón se habilita solo si ambos campos no están vacíos
- **Conexión backend:** Llama a `authViewModel.login(usuario, password)` que envía un `POST /api/auth/login` con `LoginRequest(nombreUsuario, password)`
- **Éxito:** Recibe `LoginResponse(idUsuario, nombre, nombreUsuario, token)`, establece `isLoggedIn = true`, y navega a `HomeScreen` eliminando la pila de navegación
- **Error:** Muestra el mensaje de error del servidor en texto rojo
- **Navegación:** Al presionar "Regístrate" navega a `RegisterScreen`
- **Acción por teclado:** El `Done` del campo contraseña ejecuta el login automáticamente

### RegisterScreen

**Archivo:** `ui/screens/auth/RegisterScreen.kt`
**ViewModel:** `AuthViewModel`

Permite crear una cuenta nueva.

- **Campos:** `OutlinedTextField` para nombre de usuario, nombre completo y contraseña
- **Validación en tiempo real:** Checklist visual con 3 requisitos:
  - Mínimo 8 caracteres (`password.length >= 8`)
  - Al menos una letra (`password.any { it.isLetter() }`)
  - Al menos un número (`password.any { it.isDigit() }`)
- **Conexión backend:** Llama a `authViewModel.register(nombre, usuario, password)` que envía `POST /api/auth/register` con `RegisterRequest(nombre, nombreUsuario, password)`
- **Éxito:** Establece `registrationSuccess = true`, resetea el estado y navega a `LoginScreen`
- **Error:** Muestra el mensaje de error del servidor
- **Navegación:** Al presionar "Inicia sesión" vuelve a `LoginScreen`

### HomeScreen

**Archivo:** `ui/screens/home/HomeScreen.kt`
**ViewModel:** Ninguno

Menú principal con acceso a las funcionalidades de la app mediante 4 tarjetas:

| Opción | Navegación |
|---|---|
| Iniciar entrenamiento | `EntrenamientoScreen` |
| Metas | `MetasScreen` |
| Grupos | `GruposScreen` |
| Perfil | `PerfilScreen` |

### EntrenamientoScreen

**Archivo:** `ui/screens/entrenamiento/EntrenamientoScreen.kt`
**ViewModel:** `EntrenamientoViewModel`

Pantalla de entrenamiento en curso. Muestra métricas en tiempo real durante la actividad física.

- **Métricas:** Distancia (km), Pasos, Calorías, Tiempo (mm:ss)
- **Acciones:** Botón "Finalizar" que navega al resumen de la actividad
- **Nota:** Actualmente es una pantalla estática con valores predeterminados. Está diseñada para conectarse al `EntrenamientoViewModel` que expone `idEntrenamiento`, `distancia`, `pasos`, `calorias`, `tiempo` y `estaActivo`.

### ResumenScreen

**Archivo:** `ui/screens/resumen/ResumenScreen.kt`
**ViewModel:** Ninguno

Resumen de la actividad al finalizar el entrenamiento.

- **Datos:** Muestra la distancia, pasos, calorías y tiempo de la sesión completada
- **Acciones:** Botón "Volver" para regresar al menú principal
- **Nota:** Actualmente usa datos de ejemplo estáticos. Debe cargar los datos reales desde el backend usando el `idEntrenamiento` recibido como parámetro de ruta.

### MetasScreen

**Archivo:** `ui/screens/metas/MetasScreen.kt`
**ViewModel:** `MetasViewModel`

Gestión de metas personales de actividad física.

- **Lista:** Muestra las metas creadas por el usuario
- **Acciones:** Botón "Nueva meta" para agregar una meta
- **Conexión backend (pendiente):** Debe conectarse al `MetasViewModel` que expone `metas: List<MetaResponse>` y funciones `cargarMetas(idUsuario)` y `crearMeta(idUsuario, tipo, valor)`
- **Tipos de meta:** `PASOS`, `CALORIAS`, `DISTANCIA`, `TIEMPO`

### GruposScreen

**Archivo:** `ui/screens/grupos/GruposScreen.kt`
**ViewModel:** `GrupoViewModel`

Gestión de grupos sociales para compartir resultados deportivos.

- **Lista:** Muestra los grupos a los que pertenece el usuario
- **Acciones:**
  - "Crear grupo" — permite crear un nuevo grupo con nombre y descripción
  - "Unirse a grupo" — permite unirse a un grupo existente mediante un código único
- **Conexión backend (pendiente):** Debe conectarse al `GrupoViewModel` que expone `grupos: List<GrupoResponse>`, `miembros: List<MiembroGrupoResponse>` y funciones `cargarGrupos(idUsuario)`, `crearGrupo(nombre, descripcion)`, `unirseGrupo(idUsuario, codigo)`, `cargarMiembros(idGrupo)`

### PerfilScreen

**Archivo:** `ui/screens/perfil/PerfilScreen.kt`
**ViewModel:** Ninguno

Información del perfil del usuario.

- **Datos:** Muestra el nombre de usuario y correo electrónico
- **Acciones:** Botón "Cerrar sesión" para salir de la cuenta
- **Nota:** Actualmente usa datos de ejemplo. Debe mostrar los datos reales del usuario obtenidos del `LoginResponse` (disponible en `AuthViewModel`) y al cerrar sesión debe navegar a `LoginScreen` eliminando la pila de navegación.

---

## ViewModels

### AuthViewModel

**Estado (`AuthUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `isLoggedIn` | Boolean | `true` después de un login exitoso |
| `registrationSuccess` | Boolean | `true` después de un registro exitoso |
| `error` | String? | Mensaje de error a mostrar |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `login(usuario, password)` | `POST /api/auth/login` | Inicia sesión |
| `register(nombre, usuario, password)` | `POST /api/auth/register` | Registra un nuevo usuario |
| `clearError()` | — | Limpia el mensaje de error |
| `resetRegistrationState()` | — | Resetea el flag de registro exitoso |

### EntrenamientoViewModel

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

**Estado (`GrupoUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `grupos` | List<GrupoResponse> | Grupos del usuario |
| `miembros` | List<MiembroGrupoResponse> | Miembros de un grupo |
| `error` | String? | Mensaje de error |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `cargarGrupos(idUsuario)` | `GET /api/grupos/usuario/{idUsuario}` | Obtiene los grupos del usuario |
| `crearGrupo(nombre, descripcion)` | `POST /api/grupos` | Crea un nuevo grupo |
| `unirseGrupo(idUsuario, codigo)` | `POST /api/grupos/unirse` | Se une a un grupo con código |
| `cargarMiembros(idGrupo)` | `GET /api/grupos/{idGrupo}/miembros` | Obtiene los miembros de un grupo |

### MetasViewModel

**Estado (`MetasUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `isLoading` | Boolean | Indica si hay una petición en curso |
| `metas` | List<MetaResponse> | Metas del usuario |
| `error` | String? | Mensaje de error |

**Funciones:**

| Función | Backend | Descripción |
|---|---|---|
| `cargarMetas(idUsuario)` | `GET /api/metas/usuario/{idUsuario}` | Obtiene las metas del usuario |
| `crearMeta(idUsuario, tipo, valor)` | `POST /api/metas` | Crea una nueva meta |

---

## Tema visual

El módulo móvil utiliza un tema oscuro con los siguientes colores:

| Token | Hex | Uso |
|---|---|---|
| `Primary` | `#7ED957` | Verde — color de marca, botones, enlaces |
| `Background` | `#050B17` | Fondo principal oscuro |
| `Surface` | `#0B1424` | Fondo de campos de texto y tarjetas |
| `OnSurface` | `#F5F5F5` | Color del texto |
| `Error` | `#FF5252` | Mensajes de error |

**Colores de métricas deportivas:**

| Métrica | Hex |
|---|---|
| Distancia | `#63E66C` |
| Pasos | `#42A5FF` |
| Calorías | `#FF8A1F` |
| Tiempo | `#7A5CFF` |

---

## Estado actual de implementación

| Pantalla | UI | ViewModel | Backend |
|---|---|---|---|
| Login | ✅ Completada | ✅ Conectado | ✅ Funcional |
| Register | ✅ Completada | ✅ Conectado | ✅ Funcional |
| Home | ✅ Completada | — | — |
| Entrenamiento | ⚠️ Placeholder | ❌ No conectado | ❌ No funcional |
| Resumen | ⚠️ Placeholder | ❌ No conectado | ❌ No funcional |
| Metas | ⚠️ Placeholder | ❌ No conectado | ❌ No funcional |
| Grupos | ⚠️ Placeholder | ❌ No conectado | ❌ No funcional |
| Perfil | ⚠️ Placeholder | ❌ No conectado | ❌ No funcional |
