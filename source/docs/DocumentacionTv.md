# Documentación Técnica — Módulo Smart TV (Ruta Libre)

Este documento detalla la arquitectura, el diseño de la interfaz de usuario y el funcionamiento técnico del módulo de **Smart TV (Android TV)** en la aplicación Ruta Libre.

---

## Índice

1. [Descripción General](#descripción-general)
2. [Tecnologías](#tecnologías)
3. [Estructura del Módulo TV](#estructura-del-módulo-tv)
4. [Componentes Clave](#componentes-clave)
   - [Navegación Lateral (BarraLateralTv)](#navegación-lateral-barralateraltv)
   - [Pantalla de Estadísticas (DashboardScreen)](#pantalla-de-estadísticas-dashboardscreen)
   - [Gráfica de Rendimiento Canvas](#gráfica-de-rendimiento-canvas)
   - [Pantalla de Videos y Reproductor de YouTube (VideosScreen)](#pantalla-de-videos-y-reproductor-de-youtube-videosscreen)
5. [Flujo de Datos y Conexión con el Backend](#flujo-de-datos-y-conexión-con-el-backend)
6. [Foco e Interacción con D-pad](#foco-e-interacción-con-d-pad)

---

## Descripción General

El módulo de Smart TV funciona como un centro de análisis de rendimiento físico e histórico para el usuario. Ofrece visualizaciones detalladas y comparativas a nivel semanal y mensual del entrenamiento de running, implementando una interfaz de usuario premium diseñada específicamente para pantallas grandes y control remoto.

---

## Tecnologías

| Componente | Tecnología | Versión |
|---|---|---|
| Runtime / UI | Jetpack Compose para TV | `1.0.0-alpha07` |
| UI Components | Leanback / TV Material 3 | `1.0.0-alpha07` |
| Arquitectura | MVVM (Model-View-ViewModel) | Modern Android Architecture |
| Consumo API | Retrofit + Coroutines | 2.11.x |
| Inyección local | Gradle Project Dependency | `:core` (módulo compartido) |

---

## Estructura del Módulo TV

El código fuente del módulo de TV está estructurado bajo la siguiente jerarquía de paquetes:

```
tv/src/main/java/mx/utng/cala/tv/
├── MainActivityTv.kt            # Punto de entrada de la app en TV, configura el NavGraph
├── data/
│   ├── model/
│   │   └── ModelosYouTube.kt    # Modelos para mapear las respuestas de la API de YouTube y de UI
│   ├── remote/
│   │   ├── ConfiguracionYouTube.kt # Contiene la Clave API y URL base para Retrofit
│   │   └── ServicioApiYouTube.kt   # Interfaz de endpoints HTTP de YouTube con Retrofit
│   └── repository/
│       └── RepositorioYouTube.kt # Repositorio de consulta con fallback automático a datos mock
└── ui/
    ├── components/
    │   └── BarraLateralTv.kt    # Menú de navegación lateral adaptado a TV
    ├── navigation/
    │   └── TvNavGraph.kt        # Definición de rutas y destinos (Dashboard, Grupos, Videos)
    ├── screens/
    │   ├── dashboard/
    │   │   └── DashboardScreen.kt # Pantalla principal con estadísticas y gráficas
    │   ├── grupos/
    │   │   └── GruposTvScreen.kt  # Pantalla de comparación grupal y rankings
    │   └── videos/
    │       └── VideosScreen.kt    # Pantalla multimedia de videos recomendados y reproductor
    ├── theme/
    │   ├── Color.kt             # Colores de la marca y de las métricas de salud
    │   ├── Theme.kt             # Configuración del tema oscuro de Ruta Libre
    │   └── Type.kt              # Tipografías oficiales
    └── viewmodel/
        ├── DashboardViewModel.kt # Lógica de carga y alternancia de periodos
        ├── GrupoTvViewModel.kt  # Lógica de carga de rankings grupales
        └── ViewModelVideos.kt    # Lógica de carga y filtros de la sección de videos
```

---

## Componentes Clave

### Navegación Lateral (BarraLateralTv)

Ubicada en la parte izquierda de la interfaz, emula el menú del mockup. Contiene:
- Logo "Ruta Libre" con el icono verde del corredor.
- Tres opciones: **Dashboard**, **Grupos** y **Contenido**.
- Soporte para cambios dinámicos de foco con estados visuales claros (cambio de escala a `1.04f` y tintes verdes en foco).

---

### Pantalla de Estadísticas (DashboardScreen)

La pantalla principal se divide en dos áreas principales:
1. **Fila de Métricas Acumuladoras (Superior):**
   - Muestra tarjetas para **Distancia total**, **Pasos totales**, **Calorías totales** y **Tiempo total**.
   - Cada tarjeta contiene el icono representativo coloreado, la sumatoria calculada y un indicador de porcentaje de cambio en verde (mejora) o rojo (disminución) respecto al periodo anterior.
2. **Sección Inferior Dividida:**
   - **Gráfico de Rendimiento (Izquierda):** Gráfica de barras y líneas multi-métrica personalizada.
   - **Comparación con Periodo Anterior (Derecha Superior):** Muestra barras de progreso horizontales que ilustran proporcionalmente el avance o retroceso de cada métrica.
   - **Tendencia General (Derecha Inferior):** Muestra una síntesis motivacional dinámica y el sentido general de la tendencia física del usuario.

---

### Gráfica de Rendimiento Canvas

Debido a que no se utilizan librerías pesadas de terceros, la gráfica de barras y líneas está implementada de forma nativa a través del componente `Canvas` de Jetpack Compose:
- **Barras simultáneas:** Por cada día (o semana), dibuja tres barras delgadas contiguas: verde (`Distancia`), azul (`Pasos`) y naranja (`Calorías`), escaladas automáticamente sobre el valor máximo de la colección de datos.
- **Línea de tendencia:** Traza una línea de trayectoria continua (`Path`) de color azul que une los puntos superiores de pasos de cada día, rematada con círculos concéntricos en cada nodo de la gráfica.
- **Cuadrícula y Ejes:** Genera líneas horizontales de rejilla y las etiquetas del eje X con el día de la semana o número de semana actual.

---

### Pantalla de Videos y Reproductor de YouTube (VideosScreen)

Esta pantalla ofrece al usuario acceso a contenido multimedia sobre running desde su Smart TV utilizando la API de YouTube. Está estructurada bajo tres estados visuales o vistas internas:
- **Vista Principal (`VistaVideosTv.PRINCIPAL`):** Presenta una cabecera con el buscador de texto (`OutlinedTextField`) diseñado para control remoto y una barra de filtros de categorías con iconos representativos (Videos/Todos, Consejos, Carreras, Tips). Debajo se visualiza el listado vertical de videos recomendados (`LazyColumn`) mediante tarjetas adaptadas a TV (`TarjetaVideoListado`).
- **Vista de Filtros Detallados (`VistaVideosTv.FILTROS`):** Muestra el listado de videos bajo una categoría filtrada, un botón de retroceso superior y una barra horizontal de subfiltros por nivel físico de entrenamiento (Todos, Principiantes, Intermedios, Avanzados). En esta vista, el usuario puede marcar/guardar videos mediante un indicador en forma de marcador (`Bookmark`).
- **Vista de Detalle (`VistaVideosTv.DETALLE`):** Despliega el reproductor de video en pantalla completa con un botón para regresar.

#### Reproductor Integrado (`ReproductorYouTube`)
La reproducción de videos se realiza mediante un componente `WebView` incrustado en Compose con `AndroidView` que ejecuta de forma asíncrona la **IFrame Player API** de YouTube:
- Habilita la ejecución de JavaScript, almacenamiento DOM y escala para abarcar la pantalla completa.
- Carga código HTML dinámico inyectando el ID del video y parámetros de reproducción (`autoplay=1`, `controls=1`, `playsinline=1`, `modestbranding=1`, etc.).
- Comienza la reproducción de forma automática cuando el reproductor se inicializa (`onPlayerReady`).
- Cuenta con soporte de foco de control remoto (`D-pad`) en el contenedor del reproductor.

---

## Flujo de Datos y Conexión con el Backend

El flujo de datos para cargar las estadísticas sigue la arquitectura recomendada de Android:

```
[Backend REST API]
       ▲  (Endpoints /semana, /mes, /comparacion, /comparacion-mes)
       │
[ApiService.kt (Retrofit)]
       ▲
[EntrenamientoRepository.kt]  <-- Módulo :core compartido
       ▲
[DashboardViewModel.kt]
       │  (Expone EstadoUiDashboard como StateFlow)
       ▼
[DashboardScreen.kt (UI Compose)]
```

### Alternancia de Periodos (Semanal / Mensual)
El usuario puede cambiar el periodo del Dashboard usando los botones "Semanal" y "Mensual" de la cabecera. Al cambiar:
1. `DashboardViewModel` actualiza `periodoSeleccionado`.
2. Ejecuta en paralelo las peticiones del repositorio correspondientes al nuevo periodo (ej. `obtenerDashboardMensual` y `obtenerComparacionMensual`).
3. El `EstadoUiDashboard` se actualiza de forma reactiva y el `DashboardScreen` re-dibuja las métricas y la gráfica con transiciones fluidas.

---

### Integración y Flujo de Videos de YouTube (Consumo Directo)

A diferencia de los históricos y entrenamientos grupales, el contenido de video se obtiene de manera externa. El flujo de datos está diseñado bajo los siguientes lineamientos:

```
[YouTube v3 API] (API de Google)
       ▲
       │  (Petición HTTP HTTP GET /search)
       │
[ServicioApiYouTube.kt (Retrofit)]
       ▲
       │  (Inyección dinámica de API Key y Query)
       │
[RepositorioYouTube.kt] <─── Fallback automático si falla la API o falta API Key ───> [Datos Mock Locales]
       ▲
       │  (Retorna List<VideoRutaLibre>)
       │
[ViewModelVideos.kt]
       │  (Expone EstadoUiVideos como StateFlow)
       ▼
[VideosScreen.kt]
```

1. **Retrofit y GSON:** El `RepositorioYouTube` inicializa de manera perezosa (`lazy`) un cliente Retrofit apuntando a la URL base de YouTube v3, agregando el convertidor `GsonConverterFactory`. La interfaz `ServicioApiYouTube` realiza la consulta `search` pasando parámetros en español como `consulta`, `maxResultados`, `tipo` y `clave`.
2. **Estrategia de Fallback (Resiliencia):** Si no se define una clave de API válida en `ConfiguracionYouTube.CLAVE_API`, o si ocurre un fallo de red o un código de error HTTP (como cuota de API excedida), el repositorio captura la excepción automáticamente y consume `obtenerVideosMock`. Este método retorna una lista enriquecida de videos con IDs reales de YouTube referentes a "running" (ej. técnicas de carrera, calentamiento, entrenamientos para 5K, HIIT, etc.) clasificados por categorías, asegurando una experiencia de usuario ininterrumpida.
3. **Mapeo de Datos:** Los campos nativos de la API de YouTube (`videoId`, `title`, `description`, `channelTitle`, `publishedAt`, y resoluciones de `thumbnails`) se transforman al modelo simplificado `VideoRutaLibre`, formateando la fecha ISO original a `"dd MMM yyyy"` con configuración regional en español ("es", "ES").
4. **Filtros Combinados:** El `ViewModelVideos` gestiona la lógica de búsqueda. Construye una cadena de consulta que unifica el buscador de texto libre (`terminoBusqueda`), el filtro principal de categoría (`filtroActivo`) y el subfiltro de nivel de deportista (`subfiltroActivo`). La lista resultante actualiza el `EstadoUiVideos` expuesto de forma reactiva a la UI.

---

## Foco e Interacción con D-pad

En Android TV, todos los elementos interactivos se seleccionan a través del control remoto (D-pad). Se implementaron las siguientes directivas de diseño:
- Uso de componentes `Surface` interactivos de TV Material 3 que manejan automáticamente los estados de foco, clic y presión del control remoto.
- Modificadores `.onFocusChanged` para registrar en tiempo real qué tarjeta o botón tiene el foco y cambiar dinámicamente sus bordes, elevación y contrastes.
- Animación de escala suave al enfocar un componente para dar un efecto premium de profundidad.
