package mx.utng.cala.tv.ui.screens.videos

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import mx.utng.cala.tv.data.model.VideoRutaLibre
import mx.utng.cala.tv.ui.components.BarraLateralTv
import mx.utng.cala.tv.ui.navigation.TvRoutes
import mx.utng.cala.tv.ui.theme.*
import mx.utng.cala.tv.ui.viewmodel.EstadoUiVideos
import mx.utng.cala.tv.ui.viewmodel.ViewModelVideos

enum class VistaVideosTv {
    PRINCIPAL,
    FILTROS,
    DETALLE
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideosScreen(
    navController: NavController,
    viewModel: ViewModelVideos = viewModel()
) {
    val estadoUi by viewModel.estadoUi.collectAsState()
    val contexto = LocalContext.current
    val preferencias = remember(contexto) {
        contexto.getSharedPreferences("videos_ruta_libre", Context.MODE_PRIVATE)
    }
    var vistaActual by remember { mutableStateOf(VistaVideosTv.PRINCIPAL) }
    var listaMarcados by remember {
        mutableStateOf(preferencias.getStringSet("marcados", emptySet()).orEmpty().toSet())
    }
    var textoBusqueda by remember { mutableStateOf("") }

    BackHandler(enabled = estadoUi.videoSeleccionado != null) {
        viewModel.seleccionarVideo(null)
    }

    // Control de navegación interna al cambiar el estado del ViewModel
    LaunchedEffect(estadoUi.videoSeleccionado) {
        vistaActual = if (estadoUi.videoSeleccionado != null) {
            VistaVideosTv.DETALLE
        } else if (estadoUi.filtroActivo != "Videos") {
            VistaVideosTv.FILTROS
        } else {
            VistaVideosTv.PRINCIPAL
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Barra Lateral de Navegación Fija a la izquierda (Oculta en modo reproductor para inmersión opcional,
        // pero visible en mockups. Mostrémosla tal como en los mockups).
        BarraLateralTv(
            navController = navController,
            rutaSeleccionada = TvRoutes.VIDEOS
        )

        // Contenido Dinámico a la derecha
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                .animateContentSize()
        ) {
            when (vistaActual) {
                VistaVideosTv.PRINCIPAL -> {
                    PantallaListaPrincipalVideos(
                        estadoUi = estadoUi,
                        textoBusqueda = textoBusqueda,
                        alCambiarBusqueda = {
                            textoBusqueda = it
                            viewModel.buscarPorTexto(it)
                        },
                        alSeleccionarFiltro = { filtro ->
                            viewModel.cambiarFiltro(filtro)
                        },
                        alSeleccionarVideo = { video ->
                            viewModel.seleccionarVideo(video)
                        }
                    )
                }
                VistaVideosTv.FILTROS -> {
                    PantallaFiltrosDetalladosVideos(
                        estadoUi = estadoUi,
                        listaMarcados = listaMarcados,
                        alVolver = {
                            viewModel.cambiarFiltro("Videos")
                        },
                        alSeleccionarSubfiltro = { subfiltro ->
                            viewModel.cambiarSubfiltro(subfiltro)
                        },
                        alAlternarMarcado = { idVideo ->
                            listaMarcados = if (listaMarcados.contains(idVideo)) {
                                listaMarcados - idVideo
                            } else {
                                listaMarcados + idVideo
                            }
                            preferencias.edit().putStringSet("marcados", listaMarcados).apply()
                        },
                        alSeleccionarVideo = { video ->
                            viewModel.seleccionarVideo(video)
                        }
                    )
                }
                VistaVideosTv.DETALLE -> {
                    estadoUi.videoSeleccionado?.let { video ->
                        PantallaDetalleReproductorVideo(
                            video = video,
                            alVolver = {
                                viewModel.seleccionarVideo(null)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pantalla Principal con listado de videos generales, filtros con iconos y buscador de texto.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaListaPrincipalVideos(
    estadoUi: EstadoUiVideos,
    textoBusqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alSeleccionarFiltro: (String) -> Unit,
    alSeleccionarVideo: (VideoRutaLibre) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera de Contenido
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONTENIDO",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Descubre tips, consejos, carreras y más sobre running.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Buscador estilizado para Smart TV
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = alCambiarBusqueda,
                placeholder = { Text("Buscar videos...", color = OnSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                modifier = Modifier
                    .width(280.dp)
                    .height(56.dp),
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = OnSurfaceVariant,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Outline,
                    cursorColor = Primary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fila Horizontal de Filtros con Iconos
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                TarjetaFiltroIcono(
                    texto = "Videos",
                    icono = Icons.Default.PlayArrow,
                    seleccionado = estadoUi.filtroActivo == "Videos",
                    alHacerClick = { alSeleccionarFiltro("Videos") }
                )
            }
            item {
                TarjetaFiltroIcono(
                    texto = "Consejos",
                    icono = Icons.Default.Edit,
                    seleccionado = estadoUi.filtroActivo == "Consejos",
                    alHacerClick = { alSeleccionarFiltro("Consejos") }
                )
            }
            item {
                TarjetaFiltroIcono(
                    texto = "Carreras",
                    icono = Icons.Default.CalendarToday,
                    seleccionado = estadoUi.filtroActivo == "Carreras",
                    alHacerClick = { alSeleccionarFiltro("Carreras") }
                )
            }
            item {
                TarjetaFiltroIcono(
                    texto = "Tips",
                    icono = Icons.Default.EmojiEvents,
                    seleccionado = estadoUi.filtroActivo == "Tips",
                    alHacerClick = { alSeleccionarFiltro("Tips") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Videos destacados",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Selector de Orden
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Más recientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Videos
        if (estadoUi.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Buscando videos en YouTube...", color = OnSurfaceVariant)
            }
        } else if (estadoUi.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(estadoUi.error, color = Color(0xFFFF8A80))
            }
        } else if (estadoUi.listaVideos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron videos disponibles.", color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estadoUi.listaVideos) { video ->
                    TarjetaVideoListado(
                        video = video,
                        alHacerClick = { alSeleccionarVideo(video) }
                    )
                }
            }
        }
    }
}

/**
 * Pantalla de subcategoría filtrada con barra de retroceso y subfiltros (Principiantes, Intermedios, Avanzados).
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaFiltrosDetalladosVideos(
    estadoUi: EstadoUiVideos,
    listaMarcados: Set<String>,
    alVolver: () -> Unit,
    alSeleccionarSubfiltro: (String) -> Unit,
    alAlternarMarcado: (String) -> Unit,
    alSeleccionarVideo: (VideoRutaLibre) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera con Botón de Retroceso
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var botonVolverEnfocado by remember { mutableStateOf(false) }
            Surface(
                onClick = alVolver,
                modifier = Modifier
                    .size(40.dp)
                    .onFocusChanged { botonVolverEnfocado = it.isFocused },
                shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = SurfaceVariant,
                    focusedContainerColor = Primary,
                    pressedContainerColor = PrimaryContainer
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = if (botonVolverEnfocado) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Categoría: ${estadoUi.filtroActivo}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fila de Subfiltros Horizontales (Todos, Principiantes, Intermedios, Avanzados)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val subfiltros = listOf("Todos", "Principiantes", "Intermedios", "Avanzados")
            items(subfiltros) { sub ->
                var tieneFoco by remember { mutableStateOf(false) }
                val seleccionado = estadoUi.subfiltroActivo == sub

                Surface(
                    onClick = { alSeleccionarSubfiltro(sub) },
                    modifier = Modifier.onFocusChanged { tieneFoco = it.isFocused },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (seleccionado) Primary else SurfaceVariant,
                        focusedContainerColor = if (seleccionado) Primary.copy(alpha = 0.8f) else SurfaceVariant.copy(alpha = 0.8f),
                        pressedContainerColor = PrimaryContainer
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (seleccionado) Color.Black else Color.White,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de videos de la categoría
        if (estadoUi.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando videos de la categoría...", color = OnSurfaceVariant)
            }
        } else if (estadoUi.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(estadoUi.error, color = Color(0xFFFF8A80))
            }
        } else if (estadoUi.listaVideos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay videos en esta categoría.", color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estadoUi.listaVideos) { video ->
                    TarjetaVideoListado(
                        video = video,
                        alHacerClick = { alSeleccionarVideo(video) },
                        mostrarMarcador = true,
                        esMarcado = listaMarcados.contains(video.id),
                        alAlternarMarcado = { alAlternarMarcado(video.id) }
                    )
                }
            }
        }
    }
}

/**
 * Pantalla de Detalle de Video con Reproductor de YouTube integrado usando un WebView.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaDetalleReproductorVideo(
    video: VideoRutaLibre,
    alVolver: () -> Unit
) {
    val enfoqueReproductor = remember { FocusRequester() }

    LaunchedEffect(video.id) {
        enfoqueReproductor.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera superior para regresar al listado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var botonVolverEnfocado by remember { mutableStateOf(false) }
            Surface(
                onClick = alVolver,
                modifier = Modifier
                    .height(40.dp)
                    .width(180.dp)
                    .onFocusChanged { botonVolverEnfocado = it.isFocused },
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(20.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = SurfaceVariant,
                    focusedContainerColor = Primary,
                    pressedContainerColor = PrimaryContainer
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = if (botonVolverEnfocado) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver a videos",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (botonVolverEnfocado) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = video.titulo,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor principal del reproductor de video
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Outline), shape = RoundedCornerShape(16.dp))
        ) {
            ReproductorYouTube(
                idVideo = video.id,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(enfoqueReproductor)
            )
        }
    }
}

/**
 * Componente del Reproductor de YouTube mediante WebView con IFrame API.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReproductorYouTube(
    idVideo: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { contexto ->
            WebView(contexto).apply {
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                isFocusable = true
                isFocusableInTouchMode = true
                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                
                val codigoHtml = """
                    <!DOCTYPE html>
                    <html>
                    <body style="margin:0;padding:0;background-color:black;">
                        <div id="player" style="width:100%;height:100vh;"></div>
                        <script>
                            var tag = document.createElement('script');
                            tag.src = "https://www.youtube.com/iframe_api";
                            var firstScriptTag = document.getElementsByTagName('script')[0];
                            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                            var player;
                            function onYouTubeIframeAPIReady() {
                                player = new YT.Player('player', {
                                    height: '100%',
                                    width: '100%',
                                    videoId: '$idVideo',
                                    playerVars: {
                                        'playsinline': 1,
                                        'autoplay': 1,
                                        'controls': 1,
                                        'rel': 0,
                                        'showinfo': 0,
                                        'iv_load_policy': 3,
                                        'modestbranding': 1,
                                        'enablejsapi': 1,
                                        'origin': 'https://www.youtube.com'
                                    },
                                    events: {
                                        'onReady': onPlayerReady
                                    }
                                });
                            }
                            function onPlayerReady(event) {
                                event.target.playVideo();
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()
                
                loadDataWithBaseURL("https://www.youtube.com", codigoHtml, "text/html", "utf-8", null)
                requestFocus()
            }
        },
        modifier = modifier.focusable(),
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    )
}

/**
 * Tarjeta de Filtro Horizontal con Icono.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaFiltroIcono(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    alHacerClick: () -> Unit
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = alHacerClick,
        modifier = Modifier
            .width(160.dp)
            .height(50.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (seleccionado) PrimaryContainer else Surface,
            focusedContainerColor = if (seleccionado) PrimaryContainer else SurfaceVariant,
            pressedContainerColor = PrimaryContainer
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Primary),
                shape = RoundedCornerShape(12.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (seleccionado || tieneFoco) Primary else OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = if (seleccionado || tieneFoco) Color.White else OnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tarjeta de video optimizada para TV con detalles, miniatura y soporte de D-pad.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaVideoListado(
    video: VideoRutaLibre,
    alHacerClick: () -> Unit,
    mostrarMarcador: Boolean = false,
    esMarcado: Boolean = false,
    alAlternarMarcado: (() -> Unit)? = null
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = alHacerClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = SurfaceVariant,
            pressedContainerColor = PrimaryContainer
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Primary),
                shape = RoundedCornerShape(16.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniatura izquierda premium con Canvas gradiente e icono de reproducción
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .background(SurfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = video.urlMiniatura,
                    contentDescription = "Miniatura de ${video.titulo}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                )
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = if (tieneFoco) Primary else OnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                )

                // Superposición de Duración
                video.duracion?.let { duracion ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = duracion,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información central
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = video.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = video.autor,
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = OnSurfaceVariant
                    )
                    video.vistas?.let { vistas ->
                        Text(
                            text = vistas,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Lado derecho (Categoría, Fecha y Botón de Opciones)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Etiqueta de Categoría coloreada
                val colorEtiqueta = when (video.categoria.lowercase()) {
                    "tips" -> Primary
                    "consejos" -> Secondary
                    "carreras" -> Tiempo
                    else -> OnSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .background(colorEtiqueta.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                        .border(BorderStroke(1.dp, colorEtiqueta.copy(alpha = 0.5f)), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorEtiqueta,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = video.fechaPublicacion,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                // Botón de Marcador/Guardar o de opciones
                if (mostrarMarcador) {
                    IconButton(
                        onClick = { alAlternarMarcado?.invoke() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (esMarcado) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar",
                            tint = if (esMarcado) Primary else OnSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
