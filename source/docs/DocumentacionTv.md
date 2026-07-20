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
    │       └── VideosScreen.kt    # Pantalla multimedia de videos recomendados
    ├── theme/
    │   ├── Color.kt             # Colores de la marca y de las métricas de salud
    │   ├── Theme.kt             # Configuración del tema oscuro de Ruta Libre
    │   └── Type.kt              # Tipografías oficiales
    └── viewmodel/
        ├── DashboardViewModel.kt # Lógica de carga y alternancia de periodos
        └── GrupoTvViewModel.kt  # Lógica de carga de rankings grupales
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

## Foco e Interacción con D-pad

En Android TV, todos los elementos interactivos se seleccionan a través del control remoto (D-pad). Se implementaron las siguientes directivas de diseño:
- Uso de componentes `Surface` interactivos de TV Material 3 que manejan automáticamente los estados de foco, clic y presión del control remoto.
- Modificadores `.onFocusChanged` para registrar en tiempo real qué tarjeta o botón tiene el foco y cambiar dinámicamente sus bordes, elevación y contrastes.
- Animación de escala suave al enfocar un componente para dar un efecto premium de profundidad.
