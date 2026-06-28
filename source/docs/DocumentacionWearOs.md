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
12. [Changelog](#changelog)

---

## Descripción general

El módulo `:wearos` es la aplicación para smartwatch con Wear OS del proyecto Ruta Libre. Permite al usuario iniciar y finalizar sesiones de running, visualizar métricas en tiempo real (distancia, pasos, calorías, tiempo) y recibir notificaciones de metas cumplidas directamente desde la muñeca.

Todas las pantallas están construidas con **Jetpack Compose para Wear OS** (`androidx.wear.compose.material3`) y siguen el patrón **MVVM** con un único `WearEntrenamientoViewModel` que gestiona el estado de la actividad.

---

## Arquitectura

El módulo sigue la misma arquitectura que el resto del proyecto: **MVVM + StateFlow**.

```
WearOs App
├── MainActivityWearOs      (ComponentActivity)
├── NavGraph                 (NavHost con 3 rutas)
├── Screens                  (Composables: Inicio, Métricas, Meta Completada)
├── ViewModel                (WearEntrenamientoViewModel)
└── Theme                    (Colores + Tema oscuro Wear)
       │
       ▼ (dependencia)
┌──────────────────────┐
│   :core (data layer) │
│   ─ ApiService       │
│   ─ Repository       │
│   ─ Modelos/DTOs     │
└──────────────────────┘
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
    implementation(libs.androidx.compose.foundation)        // wear compose-foundation
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.compose.material3)                  // wear compose-material3
    implementation(libs.compose.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.play.services.wearable)
    // Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

### Detalles de configuración

| Propiedad | Valor |
|-----------|-------|
| `applicationId` | `mx.utng.cala.wearos` |
| `compileSdk` | 36 |
| `minSdk` | 30 (Android 11 / Wear OS 3) |
| `targetSdk` | 36 |
| `compileSdk.minorApiLevel` | 1 |
| `JavaVersion` | 11 |
| Compose habilitado | `true` |
| `useLibrary("wear-sdk")` | `true` |

### Plugins utilizados

| Plugin | Propósito |
|--------|-----------|
| `com.android.application` | Aplicación Android |
| `org.jetbrains.kotlin.plugin.compose` | Soporte de Compose en Kotlin |

---

## Versiones y dependencias

Extraídas del catálogo `gradle/lib.versions.toml`:

| Librería | Versión | Uso en Wear OS |
|----------|---------|---------------|
| Android Gradle Plugin | 9.2.1 | Compilación |
| Kotlin | 2.2.10 | Lenguaje |
| Compose BOM | 2026.02.01 | Gestión de versions Compose |
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
| Retrofit | 2.11.0 | (vía :core) Cliente HTTP |
| Gson | 2.11.0 | (vía :core) Serialización JSON |

---

## Estructura del código

```
wearos/src/main/java/mx/utng/cala/wearos/presentation/
├── MainActivityWearOs.kt              # Entry point de la app
├── navigation/
│   └── WearNavGraph.kt                # NavHost con 2 rutas + diálogo de meta
├── components/
│   └── MetaCompletadaAlerta.kt        # Diálogo de meta alcanzada (ya no es screen)
├── screens/
│   ├── InicioScreen.kt                # Pantalla de inicio con botón INICIAR
│   └── MetricasScreen.kt              # Métricas en tiempo real + botón FINALIZAR
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
│       └── styles.xml                 # Tema de splash screen
├── lint.xml                           # Configuración de lint
├── proguard-rules.pro                 # Reglas ProGuard
└── build.gradle.kts                   # Script de build
```

---

## Componentes

### MainActivityWearOs

**Archivo:** `MainActivityWearOs.kt:10`

Entry point de la aplicación. `ComponentActivity` que infla el NavGraph dentro del tema `RutaLibreTheme`.

```kotlin
class MainActivityWearOs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RutaLibreTheme {
                val navController = rememberNavController()
                WearNavGraph(navController = navController)
            }
        }
    }
}
```

### WearNavGraph

**Archivo:** `WearNavGraph.kt:15`

Define las rutas de navegación y el `NavHost` con 2 destinos:

| Ruta | Screen | Descripción |
|------|--------|-------------|
| `inicio` | `InicioScreen` | Pantalla principal con resumen y botón INICIAR |
| `metricas` | `MetricasScreen` | Métricas en vivo durante la actividad |

La meta completada **ya no es una ruta independiente** — se muestra como un diálogo (`MetaCompletadaAlerta`) superpuesto en el NavGraph, activado cuando `uiState.mostrarMetaCompletada = true`.

El ViewModel se instancia a nivel de NavGraph y se comparte entre todas las pantallas.

```kotlin
object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
}
```

**Flujo de navegación:**
1. `INICIO` → (usuario presiona INICIAR) → `METRICAS`
2. `METRICAS` → (usuario presiona FINALIZAR) → si hay meta completada → se muestra `MetaCompletadaAlerta` → (ACEPTAR) → `INICIO`
3. `METRICAS` → (usuario presiona FINALIZAR) → sin metas → `INICIO`

### InicioScreen

**Archivo:** `InicioScreen.kt:26`

Pantalla inicial del smartwatch que muestra:

- Icono de correr (color `Primary`)
- Nombre de la app "Ruta Libre"
- Temporizador en `00:00:00`
- Cuadro con métricas en cero (Distancia 0.00 km, Pasos 0, Calorías 0 kcal)
- Botón **INICIAR** (verde) que llama a `viewModel.iniciar()` y navega a `METRICAS`

Utiliza componentes `androidx.wear.compose.material3` (`Button`, `Icon`, `Text`) y un composable privado `MetricRow` para cada métrica.

### MetricasScreen

**Archivo:** `MetricasScreen.kt:30`

Pantalla principal durante la actividad. Muestra:

- Mismo encabezado que InicioScreen
- Temporizador **incrementándose en tiempo real** con `LaunchedEffect + delay(1000)`
- Métricas: Distancia (km), Pasos, Calorías (kcal)
- Botón **FINALIZAR** (verde, color `Primary`) que llama a `onFinalizar`

> **Nota de diseño:** El botón FINALIZAR usa `Primary` (verde) con texto negro, consistente con el mockup de la aplicación. Anteriormente usaba `Error` (rojo).

Las métricas se reciben como parámetros desde el ViewModel. El tiempo local se incrementa independientemente con un contador interno para mantener la fluidez.

### MetaCompletadaAlerta

**Archivo:** `components/MetaCompletadaAlerta.kt`

Diálogo que se superpone sobre `MetricasScreen` cuando el usuario completa una o más metas durante el entrenamiento. Muestra:

- Icono de trofeo (`EmojiEvents`)
- Texto "¡Meta completada!"
- Tarjeta con el tipo de meta y valor objetivo
- Botón **ACEPTAR** (verde)

Se activa mediante `uiState.mostrarMetaCompletada` en el NavGraph, sin necesidad de una ruta de navegación independiente.

Soporta los 4 tipos de meta definidos en `TipoMeta` del módulo `:core`:

| TipoMeta | Icono | Unidad |
|----------|-------|--------|
| `DISTANCIA` | `LocationOn` | km |
| `PASOS` | `DirectionsWalk` | — |
| `CALORIAS` | `LocalFireDepartment` | kcal |
| `TIEMPO` | `Timer` | min |

Si hay múltiples metas completadas, se muestran una por una mediante llamadas sucesivas a `aceptarMetaCompletada()`.

### WearEntrenamientoViewModel

**Archivo:** `WearEntrenamientoViewModel.kt:34`

Único ViewModel del módulo. Gestiona todo el ciclo de vida del entrenamiento.

**Estado (`WearEntrenamientoUiState`):**

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `estaActivo` | `Boolean` | Indica si hay un entrenamiento en curso |
| `idEntrenamiento` | `Int?` | ID del entrenamiento activo en backend (`null` mientras no se confirme) |
| `distancia` | `Double` | Distancia recorrida (km) |
| `pasos` | `Int` | Número de pasos |
| `calorias` | `Int` | Calorías quemadas |
| `tiempo` | `Int` | Tiempo transcurrido (segundos) |
| `metasCompletadas` | `List<MetaCompletada>` | Lista de metas alcanzadas |
| `mostrarMetaCompletada` | `Boolean` | Flag para mostrar el diálogo de meta |
| `metaActual` | `MetaCompletada?` | Meta actual a mostrar |

**Métodos públicos:**

| Método | Descripción |
|--------|-------------|
| `iniciar(idUsuario)` | Inicializa estado, carga metas del usuario, inicia Health Services, llama a `POST /entrenamientos/iniciar` y guarda el `idEntrenamiento` |
| `actualizarMetricas(pasos, calorias, distancia)` | Actualiza las métricas en el estado y verifica metas en tiempo real |
| `finalizar(idUsuario, onResult)` | Detiene Health Services, lee `idEntrenamiento` del estado actual, llama a `PUT /entrenamientos/finalizar`, luego verifica metas con `checkMetasCompletadas()` |
| `aceptarMetaCompletada()` | Avanza a la siguiente meta o cierra el diálogo |

**Métodos privados:**

| Método | Descripción |
|--------|-------------|
| `verificarMetasUsuario(distancia, pasos, calorias, tiempoSegundos)` | Verifica en tiempo real si alguna meta del usuario se alcanzó usando los valores actuales acumulados |
| `checkMetasCompletadas(idUsuario)` | Consulta metas del usuario vía `MetaRepository` después de finalizar, filtra las no terminadas cuyo `valorActual >= valorObjetivo` |

**Manejo de errores:** Todos los fallos de red o API se registran con `Log.e("WearVM", ...)` para facilitar la depuración desde Logcat.

**Detalle del flujo `iniciar()`:**

1. Guarda `fechaInicioMillis` y limpia estado de metas.
2. Establece `_uiState` con `estaActivo = true` e `idEntrenamiento = null`.
3. Lanza una coroutine que:
   - Carga metas activas del usuario desde el backend.
   - Inicia Health Services para recibir métricas de sensores.
   - Llama a `POST /entrenamientos/iniciar` y actualiza `idEntrenamiento` en el estado.

**Detalle del flujo `finalizar()`:**

1. Marca `estaActivo = false` en el estado.
2. Lanza una coroutine que:
   - Detiene Health Services.
   - Lee `idEntrenamiento` del **estado actual** (no de un snapshot previo), evitando condiciones de carrera.
   - Si `idEntrenamiento` es `null`, registra el error y retorna sin llamar al backend.
   - Calcula el tiempo transcurrido.
   - Llama a `PUT /entrenamientos/finalizar` con las métricas acumuladas.
   - En éxito, consulta metas actualizadas y ejecuta `onResult()`.

### HealthServicesManager

**Archivo:** `HealthServicesManager.kt:12`

Gestiona la interacción con `Health Services` de Android para Wear OS. Proporciona:

| Método | Descripción |
|--------|-------------|
| `hasExerciseCapability()` | Verifica si el dispositivo soporta ejercicio de running |
| `exerciseStatus()` | Retorna un `Flow<ExerciseUpdate>` que emite actualizaciones de `STEPS_TOTAL`, `CALORIES_TOTAL` y `DISTANCE_TOTAL` |
| `stopExercise()` | Detiene la sesión de ejercicio |

Utiliza `callbackFlow` para convertir el callback `ExerciseUpdateCallback` en un Flow de Kotlin, permitiendo su consumo con corutinas.

**Sensores monitoreados:**
- `DataType.STEPS_TOTAL` → pasos acumulados
- `DataType.CALORIES_TOTAL` → calorías acumuladas
- `DataType.DISTANCE_TOTAL` → distancia en metros (convertida a km en el ViewModel)

### Tema (Color / Theme)

**Archivo:** `Color.kt`

Paleta de colores compartida con los otros módulos (móvil y TV):

| Color | Hex | Uso |
|-------|-----|-----|
| `Primary` | `#7ED957` | Verde principal, botones, iconos |
| `Secondary` | `#4DA3FF` | Azul secundario |
| `Tertiary` | `#7C4DFF` | Púrpura terciario |
| `Background` | `#050B17` | Fondo general |
| `Surface` | `#0B1424` | Superficies y tarjetas |
| `OnBackground` | `#FFFFFF` | Texto sobre fondo |
| `OnSurface` | `#F5F5F5` | Texto sobre superficie |
| `Error` | `#FF5252` | Botón FINALIZAR |

Colores de métricas deportivas:

| Métrica | Hex |
|---------|-----|
| `MetricDistancia` | `#63E66C` |
| `MetricPasos` | `#42A5FF` |
| `MetricCalorias` | `#FF8A1F` |
| `MetricTiempo` | `#7A5CFF` |

**Archivo:** `Theme.kt`

Define `RutaLibreTheme` que envuelve la app con un `MaterialTheme` de Wear OS usando un `ColorScheme` oscuro personalizado. El esquema incluye todas las propiedades necesarias para un tema oscuro completo:

```kotlin
private val WearColorScheme = ColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = Color.Black,
    secondaryContainer = Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = Secondary,
    tertiary = Tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = Tertiary.copy(alpha = 0.2f),
    onTertiaryContainer = Tertiary,
    background = Color.Black,           // Fondo negro explícito
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Surface.copy(alpha = 0.8f),
    surfaceContainerLow = Color.Black,
    error = Error,
    onError = Color.White,
    outline = Color.Gray,
    outlineVariant = Color.DarkGray
)
```

| Propiedad añadida | Valor | Propósito |
|-------------------|-------|-----------|
| `onPrimary` | `Color.Black` | Texto/icono sobre Primary |
| `primaryContainer` | `Primary 20%` | Fondo de contenedor primario |
| `surfaceVariant` | `Surface` | Variante de superficie |
| `surfaceContainerHigh` | `Surface 80%` | Contenedor elevado |
| `surfaceContainerLow` | `Color.Black` | Contenedor bajo |
| `onError` | `Color.White` | Texto sobre Error |
| `outline` / `outlineVariant` | `Gray` / `DarkGray` | Bordes y separadores |

---

## Flujo de navegación

```
                    ┌──────────────┐
                    │  INICIO      │
                    │ (InicioScreen)│
                    └──────┬───────┘
                           │
                     [INICIAR]
                           │
                           ▼
                    ┌──────────────┐
                    │  MÉTRICAS    │
                    │ (MetricasScr)│
                    └──────┬───────┘
                           │
                     [FINALIZAR]
                           │
                    ┌──────┴───────┐
                    │              │
               ¿Meta        No meta
             completada?   completada
                    │              │
                    ▼              ▼
            ┌──────────────┐  ┌──────────────┐
            │ META COMPL.  │  │              │
            │ (MetaCompl.) │──┤   INICIO     │
            └──────┬───────┘  └──────────────┘
                   │
             [ACEPTAR]
                   │
                   ▼
            ┌──────────────┐
            │  INICIO      │
            └──────────────┘
```

---

## Comunicación con el módulo core

El módulo `:wearos` depende de `:core` (declarado como `implementation(project(":core"))` en Gradle). Usa directamente:

- **`EntrenamientoRepository`** — para iniciar/finalizar entrenamientos vía API REST
- **`MetaRepository`** — para consultar metas del usuario y detectar metas completadas
- **Modelos del core** — `TipoMeta` (enum), `Coordenada`, `Punto` (data classes)

No existe comunicación directa con el módulo `:app` (móvil) ni con `:tv`. Toda la sincronización se realiza a través del backend.

---

## Sincronización con otros dispositivos

1. El smartwatch **inicia** el entrenamiento → `POST /entrenamientos/iniciar` → backend
2. El smartwatch **finaliza** el entrenamiento → `PUT /entrenamientos/finalizar` → backend
3. El móvil detecta el entrenamiento activo y muestra el mapa con la ruta
4. Al finalizar, el móvil envía el registro completo con coordenadas al backend
5. La TV consulta el histórico semanal desde el backend
6. Las notificaciones de metas se sincronizan marcando `leida_smartwatch`

El smartwatch **no** envía coordenadas GPS (`coordenadas = emptyList()` al finalizar), ya que el seguimiento de ruta se delega al teléfono. El reloj solo aporta las métricas resumidas (pasos, calorías, distancia, tiempo).

---

## AndroidManifest y permisos

**Archivo:** `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.BODY_SENSORS" />

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

| Elemento | Valor | Descripción |
|----------|-------|-------------|
| `android:theme` del `<application>` | `@style/Theme.WearOs.Dark` | Tema oscuro personalizado con `windowBackground = black` |
| `android:theme` del `<activity>` | `@style/MainActivityWearOsTheme.Starting` | Tema de splash screen |

> **Nota:** El tema `@style/Theme.WearOs.Dark` se creó para forzar fondo negro (`android:windowBackground = @android:color/black`) y evitar que la ventana del sistema muestre fondo blanco antes de que Compose renderice el contenido oscuro.

| Permiso | Propósito |
|---------|-----------|
| `INTERNET` | Comunicación con la API REST |
| `WAKE_LOCK` | Mantener pantalla encendida durante el entrenamiento |
| `BODY_SENSORS` | Acceso a sensores corporales (ritmo cardíaco, etc.) |

La app se declara **standalone** (`com.google.android.wearable.standalone = true`), lo que significa que funciona independientemente sin necesidad de la app móvil instalada.

---

## Recursos

### Drawables

| Archivo | Propósito |
|---------|-----------|
| `ic_launcher_foreground.xml` | Foreground del icono del launcher |
| `ic_launcher_background.xml` | Background del icono del launcher |
| `splash_icon.xml` | Icono de la pantalla de splash |

### Mipmaps

Iconos del launcher en densidades: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` y `anydpi` (adaptive icons).

### Strings

```xml
<string name="app_name">WearOs</string>
```

### Styles

Tema de splash screen basado en `Theme.SplashScreen` con fondo negro e icono personalizado. Además se define un tema oscuro global para la aplicación:

```xml
<style name="Theme.WearOs.Dark" parent="@android:style/Theme.DeviceDefault.NoActionBar">
    <item name="android:windowBackground">@android:color/black</item>
    <item name="android:colorBackground">@android:color/black</item>
    <item name="android:navigationBarColor">@android:color/black</item>
    <item name="android:statusBarColor">@android:color/black</item>
</style>
```

| Style | Propósito |
|-------|-----------|
| `Theme.WearOs.Dark` | Tema base de la app: fondo y barras del sistema en negro |
| `MainActivityWearOsTheme.Starting` | Tema de splash screen (`Theme.SplashScreen`), transiciona a `Theme.WearOs.Dark` |

### Lint

El archivo `lint.xml` ignora el warning `IconLocation` para las imágenes de preview del Tile.

---

## ProGuard

**Archivo:** `proguard-rules.pro`

Sin reglas personalizadas (archivo vacío por defecto). Las reglas necesarias para Retrofit/Gson/Coroutines se definen centralizadamente si es necesario.

---

## Changelog

### 14/06/2026 — Corrección de fondo blanco y optimización para pantalla redonda

Se corrigieron problemas de visualización en el emulador Wear OS: fondo blanco en lugar de negro y UI comprimida.

#### Problemas detectados

| Problema | Causa | Solución |
|----------|-------|----------|
| Fondo blanco | El tema XML `Theme.DeviceDefault` no forzaba fondo negro. El `ColorScheme` de Compose tenía propiedades incompletas, permitiendo valores claros por defecto. | Tema XML oscuro personalizado + `background = Color.Black` explícito en `ColorScheme` + `Box` con `Modifier.background(Color.Black)` en todas las screens |
| UI muy ajustada (espaciados y fuentes grandes sin considerar bordes redondos) | Padding insuficiente en bordes verticales, fuentes sobredimensionadas para pantalla de 450dp | Aumento de padding vertical (12→28dp), reducción de fuentes (~15%) e iconos (~25%) |
| Filas de métricas sin contraste visual | `MetricRow` usaba solo `Row` sin fondo | Se añadió `RoundedCornerShape(20dp)` con `Surface.copy(alpha = 0.6f)` a cada fila |
| Botón FINALIZAR rojo inconsistente con mockup | Usaba `Error` (rojo) en lugar de `Primary` (verde) | Cambiado a `ButtonDefaults.buttonColors(containerColor = Primary)` con texto negro |

#### Archivos modificados

| Archivo | Cambios |
|---------|---------|
| `AndroidManifest.xml:15` | `android:theme` cambiado de `@android:style/Theme.DeviceDefault` a `@style/Theme.WearOs.Dark` |
| `res/values/styles.xml` | Nuevo estilo `Theme.WearOs.Dark` con `windowBackground=black`, `colorBackground=black`, `navigationBarColor=black`, `statusBarColor=black` |
| `theme/Theme.kt` | `ColorScheme` expandido de 8 a 19 propiedades: `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `onSecondaryContainer`, `tertiaryContainer`, `onTertiaryContainer`, `surfaceVariant`, `onSurfaceVariant`, `surfaceContainerHigh`, `surfaceContainerLow`, `onError`, `outline`, `outlineVariant`. Se fijó `background = Color.Black` (antes `Background = #050B17`) |
| `screens/InicioScreen.kt` | Añadido `Box.with(Modifier.background(Color.Black))`. Icono: 32→24dp. Fuente título: 14→12sp. Tiempo: 32→28sp. Métricas: 11→10sp. Valores: 14→12sp. Botón: 14→12sp, altura 40→36dp. Padding: vertical 12→28dp, horizontal 16→20dp. `MetricRow` rediseñada con fondo `Surface.copy(0.6f)` y `RoundedCornerShape(20dp)`, iconos 18→14dp, padding interno añadido |
| `screens/MetricasScreen.kt` | Mismos cambios que InicioScreen. Botón FINALIZAR cambiado de `Error` (rojo) a `Primary` (verde) con texto `Color.Black` |
| `screens/MetaCompletadaScreen.kt` | Añadido `Box.with(Modifier.background(Color.Black))`. Trofeo: 48→36dp. Título: 16→14sp. Tarjeta meta: `Surface.copy(0.6f)` con `RoundedCornerShape(20dp)`. Icono meta: 36→28dp. Fuente valor: 16→14sp. Botón: 14→12sp, altura 40→36dp. Padding ajustado a 20dp horizontal, 28dp vertical |

#### Comparativa de métricas de espaciado

| Elemento | Antes | Después |
|----------|-------|---------|
| Padding horizontal | 16dp | 20dp |
| Padding vertical | 12dp | 28dp |
| Icono correr | 32dp | 24dp |
| Fuente "Ruta Libre" | 14sp | 12sp |
| Fuente tiempo | 32sp | 28sp |
| Fuente métrica (label) | 11sp | 10sp |
| Fuente métrica (valor) | 14sp | 12sp |
| Fuente botón | 14sp | 12sp |
| Altura botón | 40dp | 36dp |
| Icono métrica | 18dp | 14dp |
| Fondo métrica | — (transparente) | `Surface 60%` + `RoundedCornerShape(20dp)` |

### 27/06/2026 — Corrección de guardado de entrenamientos y logging

Se corrigió un bug crítico que impedía que los datos del entrenamiento se guardaran en la base de datos al finalizar desde el smartwatch.

#### Problema detectado

| Problema | Causa | Solución |
|----------|-------|----------|
| Los entrenamientos no se guardaban en BD al finalizar | `finalizar()` capturaba `idEntrenamiento` de un snapshot del estado antes de que la coroutine de `iniciar()` respondiera. Además, los errores de red eran tragados silenciosamente (`onFailure = { }`). | Se movió la lectura de `idEntrenamiento` **dentro** de la coroutine en `finalizar()`, se cambió el valor inicial de `-1` a `null`, y se agregó `Log.e(...)` en todos los `onFailure`. |

#### Archivos modificados

| Archivo | Cambios |
|---------|---------|
| `viewmodel/WearEntrenamientoViewModel.kt` | Se cambió `idEntrenamiento = -1` → `idEntrenamiento = null`. Se movió `val state = _uiState.value` dentro de la coroutine de `finalizar()`. Se agregó `import android.util.Log` y logging con tag `WearVM` en todos los `onFailure`. Se eliminaron imports no usados (`CumulativeDataPoint`, `DataPoint`). |
