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
   - [WearIdentityStore](#wearidentitystore)
   - [WearNavGraph](#wearnavgraph)
   - [InicioScreen](#inicioscreen)
   - [MetricasScreen](#metricasscreen)
   - [MetaCompletadaAlerta y MetaCompletadaScreen](#metacompletadaalerta-y-metacompletadascreen)
   - [WearEntrenamientoViewModel](#wearentrenamientoviewmodel)
   - [HealthServicesManager](#healthservicesmanager)
   - [Tema (Color / Theme)](#tema-color--theme)
7. [Flujo de navegación](#flujo-de-navegación)
8. [Sincronización y comunicación de credenciales](#sincronización-y-comunicación-de-credenciales)
   - [Google Play Services Data Layer](#google-play-services-data-layer)
   - [Eventos en tiempo real vía MQTT](#eventos-en-tiempo-real-vía-mqtt)
9. [Comunicación con el módulo core](#comunicación-con-el-módulo-core)
10. [AndroidManifest y permisos](#androidmanifest-y-permisos)
11. [Recursos](#recursos)

---

## Descripción general

El módulo `:wearos` es la aplicación companion para smartwatch con Wear OS del proyecto **Ruta Libre**. Permite al usuario iniciar y finalizar sesiones de entrenamiento/running, visualizar métricas corporales y deportivas en tiempo real (distancia, pasos, calorías, tiempo transcurrido) y recibir alertas de metas cumplidas directamente en la muñeca.

### Características clave:
- **Vinculación dinámica mediante Data Layer**: Se vincula automáticamente a la cuenta del usuario autenticado en la aplicación celular mediante la API de Google Play Services Data Client (`/ruta-libre/identity`). Si no hay sesión iniciada, muestra una pantalla de espera.
- **Control de sesión en tiempo real vía MQTT**: Mantiene una conexión a un broker MQTT (`MqttSubscriber`) para recibir eventos de desvinculación o cierre global de sesión (`/sesion/cerrada` y `/dispositivos/{idDispositivo}/desvinculado`).
- **Desvinculación manual**: Permite cerrar la sesión directamente desde el reloj con un botón "DESVINCULAR", notificando al backend y borrando credenciales locales.
- **Medición de sensores mediante Health Services**: Utiliza la API moderna `androidx.health.services.client` (Google Play Services Health) para capturar con precisión los pasos (`STEPS_TOTAL`), calorías (`CALORIES_TOTAL`) y distancia recorrida (`DISTANCE_TOTAL`).
- **UI en Jetpack Compose para Wear OS**: Interfaces responsivas construidas con `androidx.wear.compose.material3`, siguiendo el estándar Material Design para pantallas circulares u ovaladas.

---

## Arquitectura

```
WearOs App
├── MainActivityWearOs         (ComponentActivity, listener DataClient & suscripción MQTT)
├── Data Layer                 (WearIdentityStore — SharedPreferences / JWT parser)
├── NavGraph                   (WearNavGraph — manejo de estado autenticado / no autenticado)
├── Screens & Components       (InicioScreen, MetricasScreen, MetaCompletadaAlerta)
├── ViewModel                  (WearEntrenamientoViewModel — Estado UI y corrutinas)
├── HealthServicesManager      (Cliente de sensores de salud Wear OS)
└── Theme                      (Paleta de colores y tema oscuro Wear)
       │
       ├─────────────────────────┐
       ▼                         ▼ (dependencia)
┌────────────────────────┐  ┌──────────────────────────────────┐
│ Google Play Services   │  │   :core (data layer)             │
│ - Wearable DataClient  │  │   ─ EntrenamientoRepository      │
│ - Health Services      │  │   ─ MetaRepository               │
└────────────────────────┘  │   ─ DispositivoRepository        │
                            │   ─ MqttSubscriber & MqttConfig  │
                            │   ─ Modelos: MetaResponse,       │
                            │     TipoMeta, Punto              │
                            └──────────────────────────────────┘
```

---

## Configuración de Gradle

**Archivo:** [build.gradle.kts](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/build.gradle.kts)

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val mqttProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "mx.utng.cala.wearos"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "mx.utng.cala.rutalibre"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "MQTT_HOST", mqttProperties.getProperty("MQTT_HOST", "").asBuildConfigString())
        buildConfigField("int", "MQTT_PORT", mqttProperties.getProperty("MQTT_PORT", "8883"))
        buildConfigField("String", "MQTT_USERNAME", mqttProperties.getProperty("MQTT_USERNAME", "").asBuildConfigString())
        buildConfigField("String", "MQTT_PASSWORD", mqttProperties.getProperty("MQTT_PASSWORD", "").asBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += listOf("META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties")
        }
    }
}
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `applicationId` | `mx.utng.cala.rutalibre` | Mismo applicationId que el celular para compartir firma de empaquetado DataLayer |
| `compileSdk` | 36 (minorApiLevel 1) | SDK de compilación Android 15+ |
| `minSdk` | 30 | Wear OS 3+ (Android 11) |
| `targetSdk` | 36 | Android 15 |
| `buildConfig` | Habilitado | Genera constantes para configuración del broker MQTT |

---

## Versiones y dependencias

| Librería | Versión | Uso |
|---|---|---|
| Android Gradle Plugin | 9.2.1 | Plugin de construcción Android |
| Kotlin | 2.2.10 | Lenguaje de programación |
| Compose BOM | 2026.02.01 | Control unificado de dependencias de Compose |
| `androidx.activity.compose` | 1.13.0 | Integración de Activity con Compose |
| `androidx.compose.material3` (wear) | 1.6.2 | Componentes de diseño Material 3 para Wear OS |
| `androidx.compose.foundation` (wear) | 1.6.2 | Modificadores y layouts de Wear |
| `androidx.navigation.compose` | 2.9.8 | Navegación entre pantallas en Compose |
| `play-services-wearable` | 20.0.1 | Client de comunicación DataLayer entre celular y Wear OS |
| `androidx.health.services.client` | 1.1.0-alpha05 | API de sensores de ejercicio y salud |
| `androidx.concurrent.futures.ktx` | 1.2.0 | Extensión Suspend/Await para ListenableFuture de Health Services |
| `guava.android` | 33.3.1-android | Concurrencia de Google |
| `androidx.core.splashscreen` | 1.2.0 | Pantalla de carga/splash nativa |
| `:core` | (Módulo local) | Repositorios, clientes HTTP Retrofit, MQTTSubscriber y Data Transfer Objects |

---

## Estructura del código

```
wearos/src/main/java/mx/utng/cala/wearos/
├── data/
│   └── WearIdentityStore.kt          # Almacenamiento local de credenciales (JWT, idUsuario, idDispositivo)
└── presentation/
    ├── MainActivityWearOs.kt          # Entry point de la app, listener DataClient y suscripción MQTT
    ├── navigation/
    │   └── WearNavGraph.kt            # Grafo de navegación y verificación de sesión
    ├── components/
    │   └── MetaCompletadaAlerta.kt    # Diálogo modal de meta cumplida durante el entrenamiento
    ├── screens/
    │   ├── InicioScreen.kt            # Pantalla inicial con botón INICIAR y DESVINCULAR
    │   ├── MetricasScreen.kt          # Pantalla de métricas activas en tiempo real + botón FINALIZAR
    │   └── MetaCompletadaScreen.kt    # Pantalla independiente para presentación de meta alcanzada
    ├── viewmodel/
    │   ├── WearEntrenamientoViewModel.kt  # Gestión del estado de la actividad y sincronización REST
    │   └── HealthServicesManager.kt       # Gestión del cliente Health Services (ExerciseType.RUNNING)
    └── theme/
        ├── Color.kt                   # Paleta de colores e identificadores de métricas
        └── Theme.kt                   # Configuración del tema Material3 para Wear OS
```

---

## Componentes

### MainActivityWearOs

**Archivo:** [MainActivityWearOs.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/MainActivityWearOs.kt)

Punto de entrada principal. Extiende de `ComponentActivity` e implementa `DataClient.OnDataChangedListener`.

**Responsabilidades:**
1. **Inicializar `WearIdentityStore`**: Recupera el `idUsuario` guardado previamente.
2. **Suscripción MQTT**: En `onCreate`, inicia un recolector de corrutina para los eventos de `MqttSubscriber`. Si recibe un tópico `/sesion/cerrada` o `/dispositivos/$it/desvinculado`, ejecuta `limpiarSesionLocal()`.
3. **Solicitar permisos dinámicos**: Pide `BODY_SENSORS`, `ACTIVITY_RECOGNITION` y `ACCESS_FINE_LOCATION`.
4. **Listener DataClient de Google Play Services**:
   - En `onStart()`, registra el listener de `DataClient` y procesa los `DataItem` existentes.
   - Si el path es `/ruta-libre/identity`, extrae `idUsuario`, `idDispositivo` y `token`, guardándolos en `WearIdentityStore` y conectando el cliente MQTT.
   - En `onStop()`, desregistra el listener de `DataClient` y desconecta el cliente MQTT.
5. **Validación remota de sesión**: En `onStart()`, valida la vigencia del token guardado vía `DispositivoRepository.validarSesionDispositivo(token)`. Si la respuesta indica fallo/revocación, limpia la sesión local.
6. **Cierre de sesión manual (`cerrarSesionWear`)**: Llama a `DispositivoRepository.cerrarSesionDispositivo(token)` en el backend y limpia las credenciales locales.

### WearIdentityStore

**Archivo:** [WearIdentityStore.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/data/WearIdentityStore.kt)

Gestiona la persistencia local de credenciales del dispositivo usando `SharedPreferences` con la clave `"ruta_libre_wear_identity"`.

| Propiedad / Método | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `Int?` | Retorna el ID de usuario si es mayor a 0, de lo contrario `null` |
| `idDispositivo` | `String?` | Retorna el identificador del dispositivo guardado o lo decodifica del JWT como fallback |
| `token` | `String?` | Token JWT de autenticación enviado por la app móvil |
| `save(idUsuario, idDispositivo, token)` | `Unit` | Persiste en SharedPreferences las credenciales |
| `clear()` | `Unit` | Limpia todas las credenciales locales |
| `tokenDeviceId()` | `String?` | Método privado que decodifica el payload en Base64 del JWT para obtener `idDispositivo` |

### WearNavGraph

**Archivo:** [WearNavGraph.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/navigation/WearNavGraph.kt)

Define el enrutamiento de la aplicación y la validación del estado de sesión:

- **Estado No Autenticado (`idUsuario == null`)**: Renderiza un contenedor centrado con la instrucción `"Inicia sesión en Ruta Libre desde tu celular"`.
- **Estado Autenticado**: Muestra el `NavHost` con las dos pantallas principales y el diálogo de meta superpuesto.

| Ruta | Screen | Descripción |
|---|---|---|
| `inicio` | [InicioScreen](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/screens/InicioScreen.kt) | Resumen inicial, botón INICIAR y botón DESVINCULAR |
| `metricas` | [MetricasScreen](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/screens/MetricasScreen.kt) | Visualización de sensores en vivo durante el entrenamiento |

Cuando `uiState.mostrarMetaCompletada` es `true` y `uiState.metaActual != null`, dibuja en primer plano el composable [MetaCompletadaAlerta](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/components/MetaCompletadaAlerta.kt).

### InicioScreen

**Archivo:** [InicioScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/screens/InicioScreen.kt)

Pantalla principal construida con `ScreenScaffold` y `ScalingLazyColumn`. Contiene:
- Encabezado con el icono `DirectionsRun` (color `Primary`), título "Ruta Libre" y temporizador estático `00:00:00`.
- 3 filas `MetricRow` con valores iniciales en cero: Distancia (0.00 km), Pasos (0), Calorías (0 kcal).
- Botón **INICIAR** (color `Primary`, texto negro) que llama a `onIniciar()` y navega a `WearRoutes.METRICAS`.
- Botón **DESVINCULAR** (color `Color(0xFF5C2025)`, texto blanco) que invoca `onCerrarSesion()`.

### MetricasScreen

**Archivo:** [MetricasScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/screens/MetricasScreen.kt)

Pantalla activa durante el entrenamiento. Recibe como parámetros: `distancia`, `pasos`, `calorias`, `tiempoSegundos`, `estaActivo`, `onFinalizar`.
- Mantiene un cronómetro local fluido mediante `LaunchedEffect(estaActivo)` que calcula la diferencia con `System.currentTimeMillis()` cada 500ms, formateando `HH:MM:SS`.
- Despliega en vivo la distancia recorrida en km (`%.2f`), pasos formateados (`%,d`) y calorías (`kcal`).
- Botón **FINALIZAR** que dispara `onFinalizar()`.

### MetaCompletadaAlerta y MetaCompletadaScreen

**Archivos:** 
- [MetaCompletadaAlerta.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/components/MetaCompletadaAlerta.kt)
- [MetaCompletadaScreen.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/screens/MetaCompletadaScreen.kt)

`MetaCompletadaAlerta` es un componente flotante (`Dialog`) que felicita al usuario al alcanzar un objetivo durante la rutina.
- Muestra el icono del trofeo (`EmojiEvents`), la etiqueta de la meta y el valor objetivo.
- Adapta dinámicamente los iconos según el tipo de meta: `DISTANCIA` (`LocationOn`), `PASOS` (`DirectionsWalk`), `CALORIAS` (`LocalFireDepartment`), `TIEMPO` (`Timer`).
- Botón **ACEPTAR** para avanzar a la siguiente meta en cola o cerrar la alerta.

### WearEntrenamientoViewModel

**Archivo:** [WearEntrenamientoViewModel.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/viewmodel/WearEntrenamientoViewModel.kt)

ViewModel principal que coordina el ciclo de vida del entrenamiento y la comunicación con el backend.

**Estado (`WearEntrenamientoUiState`):**

| Campo | Tipo | Descripción |
|---|---|---|
| `estaActivo` | `Boolean` | Indica si el entrenamiento está en marcha |
| `idEntrenamiento` | `Int?` | ID del entrenamiento asignado por el servidor |
| `distancia` | `Double` | Distancia acumulada (km) |
| `pasos` | `Int` | Pasos totales de la sesión |
| `calorias` | `Int` | Calorías quemadas |
| `tiempo` | `Int` | Tiempo transcurrido en segundos |
| `metasCompletadas` | `List<MetaCompletada>` | Lista de metas alcanzadas |
| `mostrarMetaCompletada` | `Boolean` | Indicador para mostrar el diálogo modal |
| `metaActual` | `MetaCompletada?` | Meta actual en presentación |

**Principales Métodos:**
- `iniciar(idUsuario)`: Limpia el estado anterior, descarga las metas activas del usuario desde `MetaRepository.getMetas(idUsuario)`, inicia el recolector del `HealthServicesManager` y envía la petición `POST /entrenamientos/iniciar`.
- `actualizarMetricas(pasos, calorias, distancia)`: Actualiza las métricas en el estado UI y llama a `verificarMetasUsuario(...)` en tiempo real.
- `finalizar(idUsuario, onResult)`: Cancela el monitoreo de Health Services, toma el `idEntrenamiento` asignado y envía la petición `PUT /entrenamientos/finalizar` con las métricas finales (coordenadas vacías y puntos en 0.0, delegando el trazado GPS a la app móvil).
- `aceptarMetaCompletada()`: Descola la meta vista y pasa a la siguiente o cierra la alerta.

### HealthServicesManager

**Archivo:** [HealthServicesManager.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/viewmodel/HealthServicesManager.kt)

Encapsula la interacción con la API de `HealthServices` para Wear OS.
- `hasExerciseCapability()`: Valida la presencia del tipo de ejercicio `ExerciseType.RUNNING`.
- `exerciseStatus()`: Retorna un `Flow<ExerciseUpdate>` mediante `callbackFlow`. Inicia `startExerciseAsync()` configurando el monitoreo de `STEPS_TOTAL`, `CALORIES_TOTAL` y `DISTANCE_TOTAL`.
- `stopExercise()`: Detiene la medición activa del reloj invocando `endExerciseAsync().await()`.

### Tema (Color / Theme)

**Archivos:** 
- [Color.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/theme/Color.kt)
- [Theme.kt](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/java/mx/utng/cala/wearos/presentation/theme/Theme.kt)

Define los colores distintivos de la aplicación Wear:
- `Primary` (`#7ED957` - Verde neón deportivo)
- `Background` (`#050B17` - Negro/azul oscuro profundo)
- `Surface` (`#0B1424`)
- Colores específicos de métricas: `MetricDistancia` (`#63E66C`), `MetricPasos` (`#42A5FF`), `MetricCalorias` (`#FF8A1F`), `MetricTiempo` (`#7A5CFF`).

`RutaLibreTheme` configura un `ColorScheme` completo de Material3 para Wear con `background = Color.Black` explícito para maximizar el ahorro de batería en pantallas OLED/AMOLED.

---

## Flujo de navegación

```
                  ┌──────────────────────────────┐
                  │ ¿Usuario vinculado? (DataLayer)
                  └──────────────┬───────────────┘
                                 │
                  ┌──────────────┴──────────────┐
              No  │                             │ Si
                  ▼                             ▼
       ┌────────────────────┐          ┌────────────────────┐
       │ Mensaje de Espera  │          │   INICIO           │
       │ ("Inicia sesión...") │          │  (InicioScreen)    │
       └────────────────────┘          └─────────┬──────────┘
                                                 │
                                           [INICIAR]
                                                 │
                                                 ▼
                                       ┌────────────────────┐
                                       │   MÉTRICAS         │
                                       │  (MetricasScreen)  │
                                       └─────────┬──────────┘
                                                 │
                                           [FINALIZAR]
                                                 │
                                                 ▼
                                       ┌────────────────────┐
                                       │ Meta alcanzada?    │
                                       └─────────┬──────────┘
                                                 │
                                    ┌────────────┴────────────┐
                                 Si │                         │ No
                                    ▼                         ▼
                         ┌────────────────────┐       ┌───────────────┐
                         │ META COMPLETADA    │       │ Volver a      │
                         │ (AlertDialog)      │──────▶│ INICIO        │
                         └────────────────────┘       └───────────────┘
```

---

## Sincronización y comunicación de credenciales

### Google Play Services Data Layer

La aplicación Wear OS funciona en conjunto con la app móvil a través de la API `Wearable.getDataClient`.

1. Cuando el usuario inicia sesión en el celular, la app móvil envía un objeto `DataMap` al path `/ruta-libre/identity` conteniendo:
   - `idUsuario`: ID numérico del usuario en la base de datos.
   - `idDispositivo`: UUID único del dispositivo móvil o wearable.
   - `token`: JWT de autenticación.
2. `MainActivityWearOs` escucha eventos mediante `onDataChanged`. Al recibir los datos los guarda localmente a través de `WearIdentityStore.save(...)` y actualiza la UI de forma reactiva.

### Eventos en tiempo real vía MQTT

Para garantizar la seguridad y respuesta inmediata ante revocaciones de acceso:
1. `MainActivityWearOs` conecta a un cliente MQTT (`MqttSubscriber`) utilizando las credenciales configuradas en `buildConfigField`.
2. Escucha los tópicos:
   - `/sesion/cerrada`: Cierra todas las sesiones locales.
   - `/dispositivos/{idDispositivo}/desvinculado`: Cierra la sesión si el ID del dispositivo coincide con `WearIdentityStore.idDispositivo`.
3. Al recibir una coincidencia, ejecuta `limpiarSesionLocal()`, borrando `SharedPreferences` y forzando a la UI a retornar al estado no autenticado.

---

## Comunicación con el módulo core

El módulo `:wearos` reutiliza la capa de datos compartida del proyecto mediante la dependencia `implementation(project(":core"))`:

- **[EntrenamientoRepository](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/core/src/main/java/mx/utng/cala/core/data/repository/EntrenamientoRepository.kt)**: Ejecuta las peticiones HTTP `POST /entrenamientos/iniciar` y `PUT /entrenamientos/finalizar`.
- **[MetaRepository](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/core/src/main/java/mx/utng/cala/core/data/repository/MetaRepository.kt)**: Consulta metas activas (`GET /metas/usuario/:id`) para la evaluación en tiempo real.
- **[DispositivoRepository](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/core/src/main/java/mx/utng/cala/core/data/repository/DispositivoRepository.kt)**: Valida la sesión remota (`GET /dispositivos/validar`) y cierra la sesión (`POST /dispositivos/cerrar`).
- **[MqttSubscriber](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/core/src/main/java/mx/utng/cala/core/data/mqtt/MqttSubscriber.kt)**: Cliente Paho/Netty encapsulado en `:core` que emite eventos de tópicos suscritos.

---

## AndroidManifest y permisos

**Archivo:** [AndroidManifest.xml](file:///d:/02%20-%20Universidad/09%20-%20Noveno%20Cuatrimestre/Desarrollo%20para%20dispositivos%20Inteligentes/unidad%201/aplicaciones/rutaLibre/wearos/src/main/AndroidManifest.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
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
        android:networkSecurityConfig="@xml/network_security_config"
        android:supportsRtl="true"
        android:usesCleartextTraffic="false"
        android:theme="@style/Theme.WearOs.Dark">
        <uses-library
            android:name="com.google.android.wearable"
            android:required="true" />
        <uses-library
            android:name="wear-sdk"
            android:required="false" />

        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="false" />

        <activity
            android:name=".presentation.MainActivityWearOs"
            android:exported="true"
            android:taskAffinity=""
            android:theme="@style/MainActivityWearOsTheme.Starting">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

| Elemento | Propósito |
|---|---|
| `INTERNET` | Peticiones HTTP a la API REST y conexión MQTT |
| `ACCESS_NETWORK_STATE` | Monitoreo del estado de conectividad a la red |
| `WAKE_LOCK` | Mantener activa la CPU durante la captura continua de sensores |
| `BODY_SENSORS` | Lectura de frecuencia cardíaca y sensores de salud de Wear OS |
| `ACTIVITY_RECOGNITION` | Detección de actividad física (running/pasos) |
| `ACCESS_FINE_LOCATION` / `COARSE_LOCATION` | Permisos de GPS |
| `standalone = false` | Indica que la app Wear opera vinculada y requiere sincronización DataLayer de la app móvil |

---

## Recursos

### Values y Estilos
- `strings.xml`: Define `app_name = "WearOs"`.
- `styles.xml`: 
  - `Theme.WearOs.Dark`: Tema principal con fondo negro puro (`android:windowBackground = @android:color/black`).
  - `MainActivityWearOsTheme.Starting`: Tema de pantalla de carga inicial (`Theme.SplashScreen`) que pasa automáticamente a `Theme.WearOs.Dark`.
