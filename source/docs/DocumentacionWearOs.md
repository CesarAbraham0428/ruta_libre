# Módulo Wear OS — Ruta Libre

Documentación técnica del módulo `:wearos`, la aplicación companion para smartwatch con Wear OS.

---

## Índice

1. [Descripción general](#descripción-general)
2. [Arquitectura](#arquitectura)
3. [Configuración de Gradle](#configuración-de-gradle)
4. [Versiones y dependencias](#versiones-y-dependencias)
5. [Estructura del código](#estructura-del-código)
6. [Componentes](#componentes)
   - [MainActivityWearOs](#mainactivitywearos)
   - [WearNavGraph](#wearnavgraph)
   - [InicioScreen](#inicioscreen)
   - [MetricasScreen](#metricasscreen)
   - [MetaCompletadaAlerta](#metacompletadaalerta)
   - [WearEntrenamientoViewModel](#wearentrenamientoviewmodel)
   - [HealthServicesManager](#healthservicesmanager)
   - [Tema (Color / Theme)](#tema-color--theme)
7. [Flujo de navegación](#flujo-de-navegación)
8. [Comunicación con el módulo core](#comunicación-con-el-módulo-core)
9. [Sincronización con otros dispositivos](#sincronización-con-otros-dispositivos)
10. [AndroidManifest y permisos](#androidmanifest-y-permisos)
11. [Recursos](#recursos)

---

## Descripción general

El módulo `:wearos` es la aplicación para smartwatch con Wear OS del proyecto Ruta Libre. Permite al usuario iniciar y finalizar sesiones de running, visualizar métricas en tiempo real (distancia, pasos, calorías, tiempo) y recibir notificaciones de metas cumplidas directamente desde la muñeca.

Todas las pantallas están construidas con **Jetpack Compose para Wear OS** (`androidx.wear.compose.material3`) y siguen el patrón **MVVM** con un único `WearEntrenamientoViewModel` que gestiona el estado de la actividad. Los datos de sensores se obtienen mediante **Health Services** de Google Play Services.

---

## Arquitectura

```
WearOs App
├── MainActivityWearOs      (ComponentActivity)
├── NavGraph                 (NavHost con 2 rutas + diálogo superpuesto)
├── Screens                  (Composables: Inicio, Métricas)
├── Components               (MetaCompletadaAlerta — diálogo, no screen)
├── ViewModel                (WearEntrenamientoViewModel)
├── HealthServicesManager    (Sensores Health Services)
└── Theme                    (Colores + Tema oscuro Wear)
       │
       ▼ (dependencia)
┌──────────────────────────────┐
│   :core (data layer)         │
│   ─ EntrenamientoRepository  │
│   ─ MetaRepository           │
│   ─ Modelos: MetaResponse,   │
│     TipoMeta, Punto          │
└──────────────────────────────┘
```

El módulo es **standalone** (no requiere la app móvil para funcionar) y se comunica directamente con la API REST a través del módulo compartido `:core`.

---

## Configuración de Gradle

**Archivo:** `wearos/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.cala.wearos"
    compileSdk = 36
    minSdk = 30
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.play.services.wearable)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

| Propiedad | Valor |
|---|---|
| `applicationId` | `mx.utng.cala.wearos` |
| `compileSdk` | 36 |
| `minSdk` | 30 (Android 11 / Wear OS 3) |
| `targetSdk` | 36 |
| `JavaVersion` | 11 |

---

## Versiones y dependencias

| Librería | Versión | Uso |
|---|---|---|
| Android Gradle Plugin | 9.2.1 | Compilación |
| Kotlin | 2.2.10 | Lenguaje |
| Compose BOM | 2026.02.01 | Gestión de versiones Compose |
| `androidx.activity.compose` | 1.13.0 | Activity + Compose |
| `androidx.compose.material3` (wear) | 1.6.2 | UI Material Design para Wear |
| `androidx.compose.foundation` (wear) | 1.6.2 | Fundamentos de layout Wear |
| `androidx.compose.ui` | (via BOM) | UI toolkit |
| `androidx.compose.material-icons-extended` | (via BOM) | Iconos Material |
| `androidx.navigation.compose` | 2.9.8 | Navegación entre pantallas |
| `play-services-wearable` | 20.0.1 | Google Play Services para Wear |
| `androidx.core.splashscreen` | 1.2.0 | Pantalla de splash |
| `androidx.wear.tooling.preview` | 1.0.0 | Preview en Android Studio |
| `androidx.lifecycle.viewmodel.compose` | 2.10.0 | ViewModel en Compose |
| Retrofit | 2.11.0 | (vía `:core`) Cliente HTTP |
| Gson | 2.11.0 | (vía `:core`) Serialización JSON |

---

## Estructura del código

```
wearos/src/main/java/mx/utng/cala/wearos/presentation/
├── MainActivityWearOs.kt              # Entry point de la app
├── navigation/
│   └── WearNavGraph.kt                # NavHost con 2 rutas + diálogo de meta
├── components/
│   └── MetaCompletadaAlerta.kt        # Diálogo de meta alcanzada (único componente)
├── screens/
│   ├── InicioScreen.kt                # Pantalla de inicio con botón INICIAR
│   ├── MetricasScreen.kt              # Métricas en tiempo real + botón FINALIZAR
│   └── MetaCompletadaScreen.kt        # (No usado — reemplazado por MetaCompletadaAlerta)
├── viewmodel/
│   ├── WearEntrenamientoViewModel.kt  # Lógica de negocio y estado
│   └── HealthServicesManager.kt       # Gestión de sensores Health Services
└── theme/
    ├── Color.kt                       # Paleta de colores
    └── Theme.kt                       # Tema Material3 para Wear OS
```

Además:

```
wearos/src/main/
├── AndroidManifest.xml
├── res/
│   ├── drawable/                      # splash_icon, ic_launcher_foreground/background
│   ├── mipmap-{dpi}/                  # Iconos del launcher
│   └── values/
│       ├── strings.xml                # app_name = "WearOs"
│       └── styles.xml                 # Temas oscuro y splash screen
├── lint.xml
├── proguard-rules.pro
└── build.gradle.kts
```

---

## Componentes

### MainActivityWearOs

**Archivo:** `MainActivityWearOs.kt`

Entry point de la aplicación. Solicita permisos de sensores en `onCreate` y luego infla el NavGraph dentro del tema `RutaLibreTheme`.

```kotlin
class MainActivityWearOs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solicita permisos: BODY_SENSORS, ACTIVITY_RECOGNITION, ACCESS_FINE_LOCATION
        setContent {
            RutaLibreTheme {
                AppScaffold {
                    val navController = rememberNavController()
                    WearNavGraph(navController = navController)
                }
            }
        }
    }
}
```

### WearNavGraph

**Archivo:** `WearNavGraph.kt`

Define las rutas de navegación y el `NavHost` con 2 destinos:

| Ruta | Screen | Descripción |
|---|---|---|
| `inicio` | `InicioScreen` | Pantalla principal con resumen y botón INICIAR |
| `metricas` | `MetricasScreen` | Métricas en vivo durante la actividad |

La meta completada se muestra como un **diálogo** (`MetaCompletadaAlerta`) superpuesto fuera del NavHost, activado cuando `uiState.mostrarMetaCompletada && uiState.metaActual != null`.

El ViewModel se instancia a nivel de NavGraph y se comparte entre todas las pantallas. El `idUsuario` está hardcodeado como `1` en las llamadas del NavGraph.

```kotlin
object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
}
```

**Flujo de navegación:**
1. `INICIO` → (usuario presiona INICIAR) → `viewModel.iniciar(1)` + navega a `METRICAS`
2. `METRICAS` → (usuario presiona FINALIZAR) → `viewModel.finalizar(1) { navController.popBackStack() }` → `INICIO`
3. Si hay metas completadas, se muestra `MetaCompletadaAlerta` superpuesto sobre el NavGraph
4. (ACEPTAR) → avanza a la siguiente meta o cierra el diálogo

### InicioScreen

**Archivo:** `InicioScreen.kt`

Pantalla inicial del smartwatch que muestra:

- `ScreenScaffold` + `ScalingLazyColumn` con:
  - Icono `DirectionsRun` color `Primary` (24dp)
  - Texto "Ruta Libre" (12sp)
  - Temporizador en `00:00:00` (28sp Bold)
  - Texto "Tiempo en actividad" (9sp Gray)
  - 3 filas `MetricRow` con valores en cero: Distancia 0.00 km, Pasos 0, Calorías 0 kcal
  - Botón **INICIAR** (verde `Primary`, 40dp, texto negro) que llama a `onIniciar()` y navega a `METRICAS`

Cada `MetricRow` consiste en un `Row` con fondo `Surface` (60% alpha), bordes redondeados de 20dp, padding interno, icono (14dp), label (10sp), valor (12sp Bold) y unidad opcional (9sp Gray).

### MetricasScreen

**Archivo:** `MetricasScreen.kt`

Pantalla principal durante la actividad. Parámetros: `distancia`, `pasos`, `calorias`, `tiempoSegundos`, `estaActivo`, `onFinalizar`.

- Misma estructura que InicioScreen con:
  - Temporizador **independiente** que se incrementa con `LaunchedEffect(estaActivo)` usando `System.currentTimeMillis()` con delay de 500ms, formateado como `HH:MM:SS`
  - Métricas dinámicas: Distancia (km), Pasos (formateado con separadores de miles), Calorías (kcal)
  - Botón **FINALIZAR** (verde `Primary`, texto negro) que ejecuta `onFinalizar()`

El tiempo local se calcula independientemente del ViewModel usando `System.currentTimeMillis() - tiempoBase` para mantener la fluidez sin depender de actualizaciones del estado.

### MetaCompletadaAlerta

**Archivo:** `components/MetaCompletadaAlerta.kt`

Diálogo (`AlertDialog`) que se superpone sobre el NavGraph cuando el usuario completa una o más metas durante el entrenamiento. Muestra:

- Icono de trofeo `EmojiEvents` (36dp, Primary)
- Texto "¡Meta completada!" (14sp Bold)
- Tarjeta con el tipo de meta y valor objetivo
- Botón **ACEPTAR** (verde, texto negro, 40dp)

Soporta los 4 tipos de meta definidos en `TipoMeta` del módulo `:core`:

| TipoMeta | Icono | Unidad |
|---|---|---|
| `DISTANCIA` | `LocationOn` | km |
| `PASOS` | `DirectionsWalk` | — |
| `CALORIAS` | `LocalFireDepartment` | kcal |
| `TIEMPO` | `Timer` | min |

Si hay múltiples metas completadas, se muestran una por una mediante llamadas sucesivas a `aceptarMetaCompletada()`.

### WearEntrenamientoViewModel

**Archivo:** `WearEntrenamientoViewModel.kt`

Único ViewModel del módulo. Gestiona todo el ciclo de vida del entrenamiento.

**Estado (`WearEntrenamientoUiState`):**

| Propiedad | Tipo | Descripción |
|---|---|---|
| `estaActivo` | Boolean | Indica si hay un entrenamiento en curso |
| `idEntrenamiento` | Int? | ID del entrenamiento activo en backend (`null` mientras no se confirme) |
| `distancia` | Double | Distancia recorrida (km) |
| `pasos` | Int | Número de pasos |
| `calorias` | Int | Calorías quemadas |
| `tiempo` | Int | Tiempo transcurrido (segundos) |
| `metasCompletadas` | List\<MetaCompletada\> | Lista de metas alcanzadas |
| `mostrarMetaCompletada` | Boolean | Flag para mostrar el diálogo de meta |
| `metaActual` | MetaCompletada? | Meta actual a mostrar |

**Métodos públicos:**

| Método | Descripción |
|---|---|
| `iniciar(idUsuario)` | Inicializa estado, carga metas del usuario, inicia Health Services, llama a `POST /entrenamientos/iniciar` y guarda el `idEntrenamiento` |
| `actualizarMetricas(pasos, calorias, distancia)` | Actualiza las métricas en el estado y verifica metas en tiempo real |
| `finalizar(idUsuario, onResult)` | Detiene Health Services, lee `idEntrenamiento` del estado actual, llama a `PUT /entrenamientos/finalizar`, luego verifica metas completadas |
| `aceptarMetaCompletada()` | Avanza a la siguiente meta o cierra el diálogo |

**Métodos privados:**

| Método | Descripción |
|---|---|
| `verificarMetasUsuario(distancia, pasos, calorias, tiempoSegundos)` | Verifica en tiempo real si alguna meta del usuario se alcanzó usando los valores actuales acumulados |
| `checkMetasCompletadas(idUsuario)` | Consulta metas del usuario vía `MetaRepository` después de finalizar, filtra las no terminadas cuyo `valorActual >= valorObjetivo` |

**Detalle del flujo `iniciar(idUsuario)`:**
1. Guarda `fechaInicioMillis` y limpia estado de metas
2. Establece `_uiState` con `estaActivo = true` e `idEntrenamiento = null`
3. Lanza una coroutine que:
   - Carga metas activas del usuario desde el backend
   - Inicia Health Services (`exerciseStatus()`) para recibir métricas de sensores y las convierte: pasos (`STEPS_TOTAL`), calorías (`CALORIES_TOTAL`), distancia (`DISTANCE_TOTAL` en metros convertida a km)
   - Llama a `POST /entrenamientos/iniciar` y actualiza `idEntrenamiento` en el estado

**Detalle del flujo `finalizar(idUsuario, onResult)`:**
1. Marca `estaActivo = false` en el estado
2. Lanza una coroutine que:
   - Detiene Health Services
   - Lee `idEntrenamiento` del **estado actual** (evitando condiciones de carrera con snapshots previos)
   - Si `idEntrenamiento` es `null`, registra el error y retorna sin llamar al backend
   - Calcula el tiempo transcurrido
   - Llama a `PUT /entrenamientos/finalizar` con las métricas acumuladas y `coordenadas = emptyList()`, `puntoInicio = Punto(0,0)`, `puntoFin = Punto(0,0)`
   - En éxito, si hay metas completadas, activa `mostrarMetaCompletada` con la primera meta
   - Consulta metas actualizadas del backend y ejecuta `onResult()`

**Manejo de errores:** Todos los fallos de red o API se registran con `Log.e("WearVM", ...)` para facilitar la depuración desde Logcat.

### HealthServicesManager

**Archivo:** `HealthServicesManager.kt`

Gestiona la interacción con **Health Services** de Android para Wear OS mediante `HealthServices.getClient()`.

| Método | Descripción |
|---|---|
| `hasExerciseCapability()` | Verifica si el dispositivo soporta running (`ExerciseType.RUNNING`) |
| `exerciseStatus()` | Retorna un `Flow<ExerciseUpdate>` usando `callbackFlow` que inicia una sesión de ejercicio y emite actualizaciones de sensores |
| `stopExercise()` | Detiene la sesión de ejercicio mediante `endExerciseAsync().await()` |

**Sensores monitoreados:**
- `DataType.STEPS_TOTAL` → pasos acumulados
- `DataType.CALORIES_TOTAL` → calorías acumuladas
- `DataType.DISTANCE_TOTAL` → distancia en metros (convertida a km en el ViewModel)

**Configuración de ejercicio:** Usa `ExerciseConfig.builder(ExerciseType.RUNNING)` con los tres data types configurados.

**Callback implementado:** `ExerciseUpdateCallback` con `onExerciseUpdateReceived` (actualizaciones de sensores), los demás métodos son no-op.

### Tema (Color / Theme)

**Archivo:** `Color.kt`

| Color | Hex | Uso |
|---|---|---|
| `Primary` | `#7ED957` | Verde principal, botones, iconos |
| `Secondary` | `#4DA3FF` | Azul secundario |
| `Tertiary` | `#7C4DFF` | Púrpura terciario |
| `Background` | `#050B17` | Fondo general |
| `Surface` | `#0B1424` | Superficies y tarjetas |
| `OnBackground` | `#FFFFFF` | Texto sobre fondo |
| `OnSurface` | `#F5F5F5` | Texto sobre superficie |
| `Error` | `#FF5252` | Errores |

Colores de métricas deportivas:

| Métrica | Hex |
|---|---|
| `MetricDistancia` | `#63E66C` |
| `MetricPasos` | `#42A5FF` |
| `MetricCalorias` | `#FF8A1F` |
| `MetricTiempo` | `#7A5CFF` |

**Archivo:** `Theme.kt`

Define `RutaLibreTheme` que envuelve la app con un `MaterialTheme` de Wear OS usando un `ColorScheme` oscuro personalizado con `background = Color.Black` explícito para evitar fondo blanco. Incluye propiedades completas: `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `tertiaryContainer`, `surfaceVariant`, `onSurfaceVariant`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerLow`, `onError`, `outline`, `outlineVariant`.

---

## Flujo de navegación

```
                    ┌──────────────┐
                    │   INICIO     │
                    │ (InicioScreen)│
                    └──────┬───────┘
                           │
                     [INICIAR]
                     viewModel.iniciar(1)
                           │
                           ▼
                    ┌──────────────┐
                    │  MÉTRICAS    │
                    │ (MetricasScr)│
                    └──────┬───────┘
                           │
                     [FINALIZAR]
                     viewModel.finalizar(1) {
                       navController.popBackStack()
                     }
                           │
                           ▼
                    ┌──────────────┐
                    │   INICIO     │
                    └──────┬───────┘
                           │
                   ┌───────┴────────┐
                   │                │
              ¿Meta           Sin meta
            completada?      completada
                   │                │
                   ▼                ▼
            ┌──────────────┐   (FIN)
            │ META COMPL.  │
            │ (AlertDialog)│
            └──────┬───────┘
                   │
             [ACEPTAR]
             (siguiente meta
              o cerrar)
                   │
                   ▼
            ┌──────────────┐
            │   INICIO     │
            └──────────────┘
```

---

## Comunicación con el módulo core

El módulo `:wearos` depende de `:core` (declarado como `implementation(project(":core"))` en Gradle). Usa directamente:

- **`EntrenamientoRepository`** — para iniciar/finalizar entrenamientos vía API REST
- **`MetaRepository`** — para consultar metas del usuario y detectar metas completadas
- **Modelos del core** — `TipoMeta` (enum), `MetaResponse`, `Punto` (data classes)

No existe comunicación directa con el módulo `:app` (móvil) ni con `:tv`. Toda la sincronización se realiza a través del backend.

---

## Sincronización con otros dispositivos

1. El smartwatch **inicia** el entrenamiento → `POST /entrenamientos/iniciar` → backend
2. El smartwatch **finaliza** el entrenamiento → `PUT /entrenamientos/finalizar` → backend (con `coordenadas = []` y puntos de inicio/fin en cero)
3. El móvil puede detectar el entrenamiento activo y mostrar el mapa con la ruta
4. Al finalizar, el backend actualiza las metas y genera notificaciones automáticamente

El smartwatch **no** envía coordenadas GPS, ya que el seguimiento de ruta se delega al teléfono. El reloj solo aporta las métricas resumidas (pasos, calorías, distancia, tiempo) medidas por Health Services.

---

## AndroidManifest y permisos

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<uses-feature android:name="android.hardware.type.watch" />

<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:supportsRtl="true"
    android:theme="@style/Theme.WearOs.Dark">

<meta-data
    android:name="com.google.android.wearable.standalone"
    android:value="true" />
```

| Permiso | Propósito |
|---|---|
| `INTERNET` | Comunicación con la API REST |
| `WAKE_LOCK` | Mantener pantalla encendida durante el entrenamiento |
| `BODY_SENSORS` | Acceso a sensores corporales |
| `ACTIVITY_RECOGNITION` | Reconocimiento de actividad física (running) |
| `ACCESS_FINE_LOCATION` | Ubicación precisa (GPS) |
| `ACCESS_COARSE_LOCATION` | Ubicación aproximada (red) |

La app se declara **standalone** (`com.google.android.wearable.standalone = true`), funciona independientemente sin la app móvil instalada.

| Elemento | Valor | Descripción |
|---|---|---|
| `android:theme` del `<application>` | `@style/Theme.WearOs.Dark` | Tema oscuro con `windowBackground = black` |
| `android:theme` del `<activity>` | `@style/MainActivityWearOsTheme.Starting` | Tema de splash screen |

---

## Recursos

### Drawables

| Archivo | Propósito |
|---|---|
| `ic_launcher_foreground.xml` | Foreground del icono del launcher |
| `ic_launcher_background.xml` | Background del icono del launcher |
| `splash_icon.xml` | Icono de la pantalla de splash |

### Strings

```xml
<string name="app_name">WearOs</string>
```

### Styles

| Style | Propósito |
|---|---|
| `Theme.WearOs.Dark` | Tema base: fondo y barras del sistema en negro |
| `MainActivityWearOsTheme.Starting` | Tema de splash screen (`Theme.SplashScreen`), transiciona a `Theme.WearOs.Dark` |
