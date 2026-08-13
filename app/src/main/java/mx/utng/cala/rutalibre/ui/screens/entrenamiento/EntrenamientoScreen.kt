package mx.utng.cala.rutalibre.ui.screens.entrenamiento

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.maptiler.maptilersdk.MTConfig
import com.maptiler.maptilersdk.annotations.MTCustomAnnotationView
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.options.MTCameraOptions
import com.maptiler.maptilersdk.map.style.layer.line.MTLineCap
import com.maptiler.maptilersdk.map.style.layer.line.MTLineJoin
import com.maptiler.maptilersdk.map.style.layer.line.MTLineLayer
import com.maptiler.maptilersdk.map.style.source.MTGeoJSONSource
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.types.MTData
import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.rutalibre.BuildConfig
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.Background
import mx.utng.cala.rutalibre.ui.theme.Error
import mx.utng.cala.rutalibre.ui.theme.OnBackground
import mx.utng.cala.rutalibre.ui.theme.OnSurface
import mx.utng.cala.rutalibre.ui.theme.OnSurfaceVariant
import mx.utng.cala.rutalibre.ui.theme.Primary
import mx.utng.cala.rutalibre.ui.theme.Surface
import mx.utng.cala.rutalibre.ui.viewmodel.EntrenamientoViewModel
import java.util.Locale

private const val ROUTE_SOURCE_ID = "ruta-libre-source"
private const val ROUTE_LAYER_ID = "ruta-libre-layer"

/** Controla la sesión de entrenamiento y muestra mapa, métricas y acciones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientoScreen(
    navController: NavController,
    idUsuario: Int,
    pesoKg: Double,
    viewModel: EntrenamientoViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var iniciarTrasPermiso by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            if (iniciarTrasPermiso) viewModel.iniciar(idUsuario, pesoKg)
            else viewModel.cargarUbicacionInicial()
        }
        iniciarTrasPermiso = false
    }

    /** Solicita la ubicación necesaria y comienza el entrenamiento cuando está disponible. */
    fun iniciarConPermiso() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val activityGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

        if ((fineGranted || coarseGranted) && activityGranted) {
            viewModel.iniciar(idUsuario, pesoKg)
        } else {
            iniciarTrasPermiso = true
            locationPermissionLauncher.launch(
                buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        add(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }.toTypedArray()
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.prepararPantalla()

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.cargarUbicacionInicial()
        } else {
            iniciarTrasPermiso = false
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruta en tiempo real", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground,
                    navigationIconContentColor = OnBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MapaDeEntrenamiento(
                ruta = state.ruta,
                ubicacionActual = state.ubicacionActual,
                finalizado = state.finalizado,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Distancia",
                    value = String.format(Locale.US, "%.2f km", state.distancia),
                    icon = Icons.Default.Route,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Tiempo",
                    value = formatTime(state.tiempo),
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = if (state.metricasSimuladas) "Calorías (sim.)" else "Calorías",
                    value = "${state.calorias} kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = if (state.metricasSimuladas) "Pasos (sim.)" else "Pasos",
                    value = "${state.pasos} pasos",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                state.estaActivo -> {
                    Button(
                        onClick = viewModel::finalizar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("FINALIZAR Y GUARDAR", fontWeight = FontWeight.Bold)
                    }
                }

                state.finalizado -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Entrenamiento y ruta guardados correctamente",
                            color = Primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                state.idEntrenamiento?.let {
                                    navController.navigate(Routes.resumen(it))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("VER RESUMEN", color = Background, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                state.idEntrenamiento != null && state.ruta.isNotEmpty() -> {
                    Button(
                        onClick = viewModel::finalizar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("REINTENTAR GUARDADO", color = Background, fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    Button(
                        onClick = ::iniciarConPermiso,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Background)
                        Spacer(Modifier.size(8.dp))
                        Text("INICIAR ACTIVIDAD", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/** Renderiza el mapa, la polilínea de la ruta y la ubicación actual del usuario. */
@Composable
private fun MapaDeEntrenamiento(
    ruta: List<Coordenada>,
    ubicacionActual: Coordenada?,
    finalizado: Boolean,
    modifier: Modifier = Modifier
) {
    if (BuildConfig.MAPTILER_API_KEY.isBlank()) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Falta configurar la clave del mapa", color = OnSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Agrega MAPTILER_API_KEY en local.properties y vuelve a sincronizar Gradle.",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        return
    }

    MTConfig.apiKey = BuildConfig.MAPTILER_API_KEY
    val context = LocalContext.current
    var mapReady by remember { mutableStateOf(false) }
    val delegate = remember {
        object : MTMapViewDelegate {
            /** Configura el estilo y la cámara cuando MapTiler termina de inicializarse. */
            override fun onMapViewInitialized() {
                mapReady = true
            }

            /** Ignora eventos del mapa que no requieren una acción en la pantalla. */
            override fun onEventTriggered(event: MTEvent, data: MTData?) = Unit
        }
    }
    val controller = remember {
        MTMapViewController(context).apply { this.delegate = delegate }
    }
    val routeSourceHolder = remember { arrayOfNulls<MTGeoJSONSource>(1) }
    val routeLayerHolder = remember { arrayOfNulls<MTLineLayer>(1) }
    val defaultCenter = remember { LngLat(-100.931, 21.156) }

    LaunchedEffect(mapReady, ruta) {
        if (!mapReady) return@LaunchedEffect
        val style = controller.style ?: return@LaunchedEffect

        // removeLayerById/removeSourceById no limpian el registro interno de la
        // versión 1.3.0 del SDK. Remover los objetos evita SourceAlreadyExists.
        routeLayerHolder[0]?.let { layer ->
            runCatching { style.removeLayer(layer) }
            routeLayerHolder[0] = null
        }
        routeSourceHolder[0]?.let { source ->
            runCatching { style.removeSource(source) }
            routeSourceHolder[0] = null
        }

        if (ruta.size >= 2) {
            val source = MTGeoJSONSource(ROUTE_SOURCE_ID, ruta.toGeoJson())
            val layer = MTLineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).apply {
                color = AndroidColor.parseColor("#63E66C")
                width = 6.0
                opacity = 0.95
                cap = MTLineCap.ROUND
                join = MTLineJoin.ROUND
            }
            style.addSource(source)
            style.addLayer(layer)
            routeSourceHolder[0] = source
            routeLayerHolder[0] = layer
        }
    }

    val initialCenter = ruta.firstOrNull() ?: ubicacionActual

    // Centrar al obtener la ubicación inicial y después conservar el control manual.
    LaunchedEffect(mapReady, initialCenter) {
        if (!mapReady) return@LaunchedEffect
        initialCenter?.let {
            controller.easeTo(
                MTCameraOptions(
                    center = LngLat(it.longitud, it.latitud),
                    zoom = 16.0
                )
            )
        }
    }

    Box(modifier = modifier.background(Surface, RoundedCornerShape(20.dp))) {
        MTMapView(
            referenceStyle = MTMapReferenceStyle.STREETS,
            options = MTMapOptions(
                center = defaultCenter,
                zoom = 13.0,
                navigationControlIsVisible = true,
                scaleControlIsVisible = true
            ),
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        ruta.firstOrNull()?.let {
            RouteMarker(
                controller = controller,
                coordinate = it,
                color = Color(0xFF43A047),
                label = "Inicio"
            )
        }

        val currentMarker = if (finalizado) ruta.lastOrNull() else ubicacionActual
        currentMarker?.let {
            RouteMarker(
                controller = controller,
                coordinate = it,
                color = if (finalizado) Color(0xFFE53935) else Color(0xFF1E88E5),
                label = if (finalizado) "Final" else "Actual"
            )

            IconButton(
                onClick = {
                    controller.easeTo(
                        MTCameraOptions(
                            center = LngLat(it.longitud, it.latitud),
                            zoom = 16.0
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Surface.copy(alpha = 0.92f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar en mi ubicación",
                    tint = Primary
                )
            }
        }
    }
}

/** Dibuja el marcador de la ubicación actual sobre el mapa. */
@Composable
private fun RouteMarker(
    controller: MTMapViewController,
    coordinate: Coordenada,
    color: Color,
    label: String
) {
    key(label, coordinate.longitud, coordinate.latitud) {
        MTCustomAnnotationView(
            controller = controller,
            coordinates = LngLat(coordinate.longitud, coordinate.latitud)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        color = OnSurface,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.White, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color, RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

/** Presenta una métrica del entrenamiento con icono, valor y unidad. */
@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Primary)
            Column {
                Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(value, color = OnSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Convierte las coordenadas de la ruta al GeoJSON usado por MapTiler. */
private fun List<Coordenada>.toGeoJson(): String {
    val coordinates = joinToString(separator = ",") { "[${it.longitud},${it.latitud}]" }
    return """{"type":"Feature","geometry":{"type":"LineString","coordinates":[$coordinates]},"properties":{}}"""
}

/** Convierte los segundos transcurridos al formato HH:mm:ss. */
private fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
