# Documentación Técnica — Módulo Smart TV (Ruta Libre)

Este documento detalla la arquitectura, el diseño de la interfaz de usuario, el consumo de servicios web/REST, la sincronización en tiempo real vía MQTT y el funcionamiento técnico del módulo de **Smart TV (Android TV)** en la aplicación Ruta Libre.

---

## Índice

1. [Descripción General](#descripción-general)
2. [Tecnologías y Dependencias](#tecnologías-y-dependencias)
3. [Estructura del Módulo TV](#estructura-del-módulo-tv)
4. [Módulos y Componentes Clave](#módulos-y-componentes-clave)
   - [Flujo de Vinculación y Autenticación de Dispositivo (VinculacionTvScreen)](#flujo-de-vinculación-y-autenticación-de-dispositivo-vinculaciontvscreen)
   - [Navegación Lateral Dinámica (BarraLateralTv)](#navegación-lateral-dinámica-barralateraltv)
   - [Panel de Control y Estadísticas (DashboardScreen)](#panel-de-control-y-estadísticas-dashboardscreen)
   - [Gráfica de Rendimiento Multi-métrica Nativa (Canvas)](#gráfica-de-rendimiento-multi-métrica-nativa-canvas)
   - [Comunidad y Rankings Grupales (GruposTvScreen)](#comunidad-y-rankings-grupales-grupostvscreen)
   - [Centro Multimedia y Reproductor de YouTube (VideosScreen & ReproductorVideoActivity)](#centro-multimedia-y-reproductor-de-youtube-videosscreen--reproductorvideoactivity)
5. [Endpoints Consultados y Servicios Consumidos](#endpoints-consultados-y-servicios-consumidos)
   - [Endpoints de Autenticación y Dispositivos (Backend REST)](#endpoints-de-autenticación-y-dispositivos-backend-rest)
   - [Endpoints de Estadísticas y Rendimiento (Backend REST)](#endpoints-de-estadísticas-y-rendimiento-backend-rest)
   - [Endpoints de Grupos y Rankings (Backend REST)](#endpoints-de-grupos-y-rankings-backend-rest)
   - [API Externa de YouTube v3](#api-externa-de-youtube-v3)
6. [Sincronización en Tiempo Real y Cierre de Sesión Remoto (MQTT)](#sincronización-en-tiempo-real-y-cierre-de-sesión-remoto-mqtt)
7. [Foco e Interacción con D-pad (Smart TV UX)](#foco-e-interacción-con-d-pad-smart-tv-ux)

---

## Descripción General

El módulo de Smart TV para Ruta Libre proporciona un centro integral de análisis de rendimiento deportivo, interacción comunitaria y consumo multimedia optimizado para pantallas de gran formato y navegación mediante control remoto (D-pad).

Permite a los usuarios:
- Vincular la Smart TV de forma rápida mediante un código único de 6 caracteres conectado con la app móvil.
- Visualizar métricas avanzadas e históricas de running (distancia, pasos, calorías, tiempo) en periodos semanales y mensuales.
- Consultar tendencias y comparativas de progreso respecto a periodos anteriores.
- Explorar sus grupos de entrenamiento, unirse mediante códigos de invitación, crear nuevos grupos y visualizar la tabla de clasificación (ranking) de miembros.
- Acceder a recomendaciones multimedia de running mediante la API de YouTube v3 con reproductor integrado.
- Recibir órdenes de desvinculación o cierre de sesión remoto de manera reactiva e instantánea mediante subscripción a tópicos MQTT.

---

## Tecnologías y Dependencias

| Componente / Capa | Tecnología | Descripción / Versión |
|---|---|---|
| Runtime / UI | Jetpack Compose para TV | `1.0.0-alpha07` |
| Componentes Material | TV Material 3 / Leanback | `1.0.0-alpha07` |
| Arquitectura | MVVM (Model-View-ViewModel) | Modern Android Architecture con Kotlin Coroutines y StateFlow |
| Navegación | Jetpack Navigation Compose | Ruta unificada mediante `TvNavGraph` |
| Consumo API REST | Retrofit + OkHttpClient | Retrofit 2.11.x, OkHttpClient con convertidor Gson |
| Mensajería en Tiempo Real | Eclipse Paho MQTT Client | Conexión WebSocket/TCP mediante módulo compartido `:core` (`MqttSubscriber`) |
| Persistencia Local | SharedPreferences + JSON Parsing | `TvIdentityStore` almacena credenciales de dispositivo y JWT |
| Gráficos Personalizados | Jetpack Compose `Canvas` | Renderizado nativo multi-métrica sin dependencias de terceros |
| Dependencia Interna | Módulo compartido `:core` | `mx.utng.cala.core` (repositorios, DTOs, MQTT) |

---

## Estructura del Módulo TV

```
tv/src/main/java/mx/utng/cala/tv/
├── MainActivityTv.kt            # Punto de entrada, inicialización de MQTT subscriber y estado de sesión
├── data/
│   ├── TvIdentityStore.kt       # Gestión local de credenciales (idUsuario, idDispositivo, token JWT)
│   ├── model/
│   │   └── ModelosYouTube.kt    # Data classes para respuesta de API YouTube v3 y estado UI
│   ├── remote/
│   │   ├── ConfiguracionYouTube.kt # Configuración de API Key y URL base de YouTube
│   │   └── ServicioApiYouTube.kt   # Interfaz Retrofit para llamadas a YouTube Data API
│   └── repository/
│       └── RepositorioYouTube.kt # Repositorio con estrategia de fallback automático a datos mock
└── ui/
    ├── components/
    │   └── BarraLateralTv.kt    # Menú lateral animado, colapsable y superpuesto sin comprimir contenido
    ├── navigation/
    │   └── TvNavGraph.kt        # Definición de rutas y navegación (vinculacion, dashboard, grupos, videos)
    ├── screens/
    │   ├── dashboard/
    │   │   └── DashboardScreen.kt # Vista principal con tarjetas acumuladoras, gráficas y comparativas
    │   ├── grupos/
    │   │   └── GruposTvScreen.kt  # Vista de grupos, creación, código de invitación y ranking de miembros
    │   ├── videos/
    │   │   ├── ReproductorVideoActivity.kt # Activity dedicada para reproducción YouTube en pantalla completa
    │   │   └── VideosScreen.kt    # Buscador, filtros por categoría/nivel y lista de videos
    │   └── vinculacion/
    │       └── VinculacionTvScreen.kt # Pantalla de emparejamiento con código de 6 caracteres
    ├── theme/
    │   ├── Color.kt             # Paleta de colores de marca y métricas de rendimiento
    │   ├── Theme.kt             # Configuración del tema oscuro de Ruta Libre
    │   └── Type.kt              # Tipografías oficiales
    └── viewmodel/
        ├── DashboardViewModel.kt # Carga reactiva de estadísticas y alternancia semanal/mensual
        ├── GrupoTvViewModel.kt  # Gestión concurrente de grupos, miembros y rankings
        ├── TvPairingViewModel.kt# Lógica de solicitud de código, polling y validación de sesión
        └── ViewModelVideos.kt    # Debounce de búsqueda, filtros por categoría y carga de videos
```

---

## Módulos y Componentes Clave

### Flujo de Vinculación y Autenticación de Dispositivo (VinculacionTvScreen)
- **Generación de Código:** Al iniciar la app sin sesión previa, `TvPairingViewModel` solicita un código temporal de 6 caracteres al backend (`solicitarTv`), mostrando una interfaz limpia en `VinculacionTvScreen`.
- **Polling de Estado:** La TV inicia una corrutina en segundo plano (`esperarAutorizacion`) que consulta periódicamente (`delay(2500ms)`) el estado de la solicitud.
- **Enlace Exitoso:** Cuando el usuario ingresa el código en su aplicación móvil, la TV recibe la respuesta `"vinculado"` junto con el `idUsuario` y el `token` JWT de sesión, guardándolos de forma persistente mediante `TvIdentityStore` e ingresando automáticamente al Dashboard.
- **Verificación Continua de Sesión:** `TvPairingViewModel` valida periódicamente la validez del token guardado contra el backend (`validarSesionDispositivo`). Si recibe un código HTTP `401` o `403`, invalida la sesión local y redirige al flujo de vinculación.

---

### Navegación Lateral Dinámica (BarraLateralTv)
- **Diseño Colapsable Animado:** Permanece compacta (72dp) mostrando únicamente los iconos principales. Al recibir el foco de navegación del D-pad (`onFocusChanged`), se expande de forma fluida a 260dp mediante `animateDpAsState`.
- **Superposición sin Desplazamiento (`zIndex`):** Se renderiza en una capa superior (`zIndex(10f)`) manteniendo un espacio reservado compacto, evitando comprimir o re-renderizar las tarjetas del Dashboard al abrirse o cerrarse.
- **Opciones de Menú:**
  1. **Dashboard** (`TvRoutes.DASHBOARD`)
  2. **Grupos** (`TvRoutes.GRUPOS`)
  3. **Contenido** (`TvRoutes.VIDEOS`)
  4. **Cerrar sesión** (Ejecuta la invalidación del token y revocación del dispositivo).

---

### Panel de Control y Estadísticas (DashboardScreen)
- **Alternancia de Periodos (Semanal / Mensual):** El usuario puede conmutar entre vista semanal y mensual en tiempo real. `DashboardViewModel` actualiza el estado y realiza peticiones paralelas al repositorio.
- **Soporte Dinámico de 4 Estados de Rendimiento:** La pantalla del Dashboard y todas sus tarjetas evalúan si el usuario tiene registros de entrenamiento en el periodo actual. En función de esto y la comparación con el periodo previo, se definen los siguientes estados:
  1. **Estado Inicial (Sin Datos):** Detectado automáticamente si el usuario es recién registrado y no tiene entrenamientos en el periodo actual. En las tarjetas acumuladoras se ocultan las flechas e incrementos y se muestra *"Sin datos anteriores"*. La tarjeta de comparación y la gráfica muestran una UI estilizada de estado vacío (Empty State) con mensajes informativos y motivadores para registrar actividad.
  2. **Estado de Mejora:** Cuando el promedio del rendimiento mejora respecto al periodo anterior, se muestran indicadores visuales llamativos en color verde (`Primary`) acompañados de flechas hacia arriba (`↑`).
  3. **Estado de Disminución (Empeora):** Cuando el promedio del rendimiento desciende respecto al periodo anterior, se muestran indicadores visuales en color rojo (`Error`) y flechas hacia abajo (`↓`).
  4. **Estado Neutral (Estable / Sin Cambios):** Cuando el rendimiento del usuario es idéntico o no presenta variaciones significativas (`0.0%`), se muestran indicadores en color amarillo/ámbar (`Neutral`) con flechas horizontales (`→`).
- **Tarjetas Acumuladoras:** Muestra distancia total (km), pasos totales, calorías (kcal) y tiempo (min/hrs) aplicando el formato de color e icono de tendencia correspondiente.
- **Tarjeta Comparativa y Tendencia:** Presenta barras de avance proporcional e indicadores visuales de tendencia general utilizando la enumeración `EstadoTendencia` para renderizar el icono de tendencia (`TrendingUp`, `TrendingDown`, `TrendingFlat`, `DirectionsRun`), el título y la descripción motivacional adecuada.

---

### Gráfica de Rendimiento Multi-métrica Nativa (Canvas)
- Renderizada de forma nativa con Jetpack Compose `Canvas` para optimizar el rendimiento en procesadores de Smart TV.
- **Barras Multi-métrica Simultáneas:** Renderiza por cada periodo tres barras contiguas (distancia en verde, pasos en azul y calorías en naranja), escaladas dinámicamente según los valores máximos del periodo.
- **Línea de Trayectoria Continua (Path):** Traza una línea suave uniendo los puntos de pasos diarios/semanales, rematada con nodos en círculos concéntricos.
- **Estado Vacío Estético:** Si el usuario no cuenta con entrenamientos registrados (`sinDatos == true`), se ocultan la leyenda de colores y el Canvas para renderizar una sección con un diseño visual limpio con el icono `Icons.Default.BarChart` y un texto de ayuda que invita al usuario a realizar su primer entrenamiento.

---

### Comunidad y Rankings Grupales (GruposTvScreen)
- **Gestión de Grupos y Roles:** Permite al usuario visualizar los grupos en los que participa, unirse a un grupo mediante su código único de invitación y crear nuevos grupos desde la TV. El sistema diferencia si el usuario actual es el creador/dueño del grupo (`idUsuarioActual == idCreadorGrupo`).
- **Carga Concurrente (Async / Await):** Al seleccionar un grupo, `GrupoTvViewModel` dispara consultas paralelas para obtener la lista de miembros (`getMiembros`) y la tabla de clasificación o ranking (`getRanking`).
- **Tabla de Posiciones (Ranking):** Muestra el medallero e icono de podio (oro, plata, bronce) con la distancia recorrida y pasos acumulados por cada integrante del grupo.
- **Acciones Diferenciadas por Rol (Eliminar vs. Salir):**
  - **Dueño / Creador del Grupo:** No tiene la opción de salir del grupo, sino de **Eliminar el grupo**. Al dar clic en "Eliminar grupo", se despliega una alerta de confirmación con el mensaje: `¿Quieres eliminar el grupo?  (el grupo se eliminara junto con todos los miembros)`. Al confirmar, el grupo se elimina por completo de la base de datos (con cascada de miembros).
  - **Miembro Común:** Tiene disponible únicamente la opción **Salir del grupo**, que le permite abandonar la comunidad compartida manteniendo el grupo activo para los demás integrantes.
- **Mejoras UX / UI de Interacción:**
  - **Color de fondo en pestañas:** Se personalizó el indicador del `TabRow` usando `TabRowDefaults.PillIndicator` con color de fondo `PrimaryContainer` (verde oscuro) e inactivo `Transparent`, eliminando el fondo blanco plano por defecto.
  - **Corrección en Hover de Unirse a Otro Grupo:** El botón de "Unirse a otro grupo" en el detalle de métricas ahora utiliza una escala enfocada moderada (`1.02f`) y un cambio dinámico de contenedor a `Primary` (verde brillante) con texto e icono en negro al enfocarse, previniendo que se agrande desproporcionadamente en pantalla completa.

---

### Centro Multimedia y Reproductor de YouTube (VideosScreen & ReproductorVideoActivity)
- **Buscador con Debounce:** Cuadro de texto `OutlinedTextField` adaptado a control remoto que aplica una pausa prudencial de 400ms antes de emitir la consulta HTTP, evitando saturar la API con cada pulsación.
- **Filtros por Categoría y Nivel:** Categorías (Videos, Consejos, Carreras, Tips) y subfiltros técnicos (Todos, Principiantes, Intermedios, Avanzados).
- **Reproducción Integrada (`ReproductorVideoActivity`):** Al presionar un video, se abre una Activity landscape en pantalla completa configurada con `WebView` y YouTube IFrame Player API. Habilita aceleración por hardware, soporte para JavaScript, inicio automático (`autoplay=1`) y control mediante D-pad.
- **Resiliencia y Fallback Locales:** Si no existe una clave de API configurada en `ConfiguracionYouTube`, o ante fallos de red/cuota, `RepositorioYouTube` intercepta el error y entrega un listado estático filtrado de videos simulados referentes a running con IDs reales de YouTube.

---

## Endpoints Consultados y Servicios Consumidos

### Endpoints de Autenticación y Dispositivos (Backend REST)

| Método | Endpoint | ViewModel / Componente | Descripción |
|---|---|---|---|
| `POST` | `/api/dispositivos/solicitar-tv` | `TvPairingViewModel` | Solicita un nuevo código de 6 dígitos para emparejar la Smart TV. |
| `GET` | `/api/dispositivos/estado` | `TvPairingViewModel` | Consulta el estado del proceso de vinculación (`pendiente`, `vinculado`, `expirado`). |
| `GET` | `/api/dispositivos/validar` | `TvPairingViewModel` | Valida la vigencia de la sesión y token guardados en el almacenamiento local de la TV. |
| `POST` | `/api/dispositivos/logout` | `TvPairingViewModel` | Cierra la sesión del dispositivo y revoca el token en el backend. |

---

### Endpoints de Estadísticas y Rendimiento (Backend REST)

| Método | Endpoint | ViewModel / Componente | Descripción |
|---|---|---|---|
| `GET` | `/api/entrenamientos/semana?idUsuario={id}` | `DashboardViewModel` | Obtiene métricas acumuladas y desglose diario de la semana actual. |
| `GET` | `/api/entrenamientos/comparacion?idUsuario={id}` | `DashboardViewModel` | Obtiene porcentaje de cambio y deltas respecto a la semana previa. |
| `GET` | `/api/entrenamientos/mes?idUsuario={id}` | `DashboardViewModel` | Obtiene métricas acumuladas y desglose por semanas del mes actual. |
| `GET` | `/api/entrenamientos/comparacion-mes?idUsuario={id}` | `DashboardViewModel` | Obtiene porcentaje de cambio y deltas respecto al mes previo. |

---

### Endpoints de Grupos y Rankings (Backend REST)

| Método | Endpoint | ViewModel / Componente | Descripción |
|---|---|---|---|
| `GET` | `/api/grupos/usuario/{idUsuario}` | `GrupoTvViewModel` | Obtiene la lista de grupos a los que pertenece el usuario (incluye propiedad `idCreador`). |
| `POST` | `/api/grupos` | `GrupoTvViewModel` | Crea un nuevo grupo de entrenamiento y registra al creador en la columna `id_creador` y la relación `usuario_grupo`. |
| `POST` | `/api/grupos/unirse` | `GrupoTvViewModel` | Une al usuario a un grupo mediante su código de invitación de 6 caracteres. |
| `GET` | `/api/grupos/{idGrupo}/miembros` | `GrupoTvViewModel` | Retorna los miembros registrados en el grupo con su rendimiento de la semana en curso. |
| `GET` | `/api/grupos/{idGrupo}/ranking` | `GrupoTvViewModel` | Retorna la tabla de posiciones semanal de los miembros del grupo por distancia recorrida. |
| `DELETE` | `/api/grupos/{idGrupo}/miembros/{idUsuario}` | `GrupoTvViewModel` | Elimina la pertenencia del usuario al grupo (salir del grupo). |
| `DELETE` | `/api/grupos/{idGrupo}?idUsuario={id}` | `GrupoTvViewModel` | Elimina el grupo y sus miembros en cascada (solo permitido al dueño/creador). |

---

### API Externa de YouTube v3

| Método | Endpoint / Servicio | Repositorio / ViewModel | Descripción |
|---|---|---|---|
| `GET` | `https://www.googleapis.com/youtube/v3/search` | `RepositorioYouTube` / `ServicioApiYouTube` | Realiza búsquedas de contenidos de running con parámetros `q`, `type=video`, `maxResults=15` y `key`. |

---

## Sincronización en Tiempo Real y Cierre de Sesión Remoto (MQTT)

La Smart TV integra el cliente MQTT a través del módulo compartido `:core` (`MqttSubscriber`), ofreciendo reactividad instantánea ante acciones del usuario en la app móvil.

```
                  [ Servidor Broker MQTT ]
                             │
     Tópicos: /sesion/cerrada | /dispositivos/{idDispositivo}/desvinculado
                             │
                             ▼
                    [ MqttSubscriber ] (MainActivityTv)
                             │
                             ▼
                    [ TvIdentityStore.clear() ]
                             │
                             ▼
               [ Redirección a VinculacionTvScreen ]
```

1. **Suscripción Automática:** Al vincular la sesión o reiniciar la app con un usuario activo (`idUsuario`), `MainActivityTv` conecta el suscriptor MQTT.
2. **Tópicos Escuchados:**
   - `ruta-libre/usuarios/{idUsuario}/sesion/cerrada`: Notifica que el usuario cerró sesión en todas sus aplicaciones o dispositivos.
   - `ruta-libre/usuarios/{idUsuario}/dispositivos/{idDispositivo}/desvinculado`: Notifica que la Smart TV específica fue desvinculada desde el teléfono.
3. **Respuesta Reactiva:** Al recibir cualquiera de estos eventos, `MainActivityTv`:
   - Elimina las credenciales locales mediante `identityStore.clear()`.
   - Desconecta el cliente MQTT.
   - Dispara `remoteLogoutSignal`, provocando que `TvNavGraph` limpie el stack de navegación y redirija inmediatamente a la pantalla de vinculación (`VinculacionTvScreen`).

---

## Foco e Interacción con D-pad (Smart TV UX)

El módulo de Smart TV cumple rigurosamente con las directrices de experiencia de usuario para Android TV:
- **Resaltado Visual Claro:** Todos los elementos interactivos utilizan componentes `Surface` de TV Material 3 con estados de foco explícitos (escalado suave de `1.04f` a `1.06f`, cambio de elevación y bordes iluminados en verde primario).
- **Control de Foco Nativo:** Manejo de navegación direccional (Arriba, Abajo, Izquierda, Derecha, OK/Seleccionar) sin requerir puntero ni pantalla táctil.
- **Teclado Virtual Optimizado:** Cuadros de texto adaptados para ingresar texto con el control remoto de la televisión de forma ágil.

