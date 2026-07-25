package mx.utng.cala.tv.ui.screens.grupos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.*
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.tv.ui.components.BarraLateralTv
import mx.utng.cala.tv.ui.navigation.TvRoutes
import mx.utng.cala.tv.ui.theme.*
import mx.utng.cala.tv.ui.viewmodel.EstadoUiGrupoTv
import mx.utng.cala.tv.ui.viewmodel.GrupoTvViewModel

enum class VistaGruposTv {
    PRINCIPAL,
    CREAR,
    UNIRSE,
    DETALLE
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GruposTvScreen(
    navController: NavController,
    idUsuario: Int,
    onCerrarSesion: () -> Unit,
    grupoViewModel: GrupoTvViewModel = viewModel()
) {
    val estadoUi by grupoViewModel.estadoUi.collectAsState()

    var vistaActual by remember { mutableStateOf(VistaGruposTv.PRINCIPAL) }
    var grupoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var grupoSeleccionadoNombre by remember { mutableStateOf("") }
    var grupoSeleccionadoCodigo by remember { mutableStateOf("") }
    var grupoSeleccionadoDescripcion by remember { mutableStateOf<String?>(null) }
    var grupoSeleccionadoIdCreador by remember { mutableStateOf<Int?>(null) }

    val solicitadorEnfoquePrincipal = remember { FocusRequester() }
    val solicitadorEnfoqueCrear = remember { FocusRequester() }
    val solicitadorEnfoqueUnirse = remember { FocusRequester() }
    val solicitadorEnfoqueDetalle = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        grupoViewModel.cargarGruposDeUsuario(idUsuario)
    }

    LaunchedEffect(vistaActual) {
        delay(100)
        try {
            when (vistaActual) {
                VistaGruposTv.PRINCIPAL -> solicitadorEnfoquePrincipal.requestFocus()
                VistaGruposTv.CREAR -> solicitadorEnfoqueCrear.requestFocus()
                VistaGruposTv.UNIRSE -> solicitadorEnfoqueUnirse.requestFocus()
                VistaGruposTv.DETALLE -> solicitadorEnfoqueDetalle.requestFocus()
            }
        } catch (e: Exception) {
            // Ignorar si el elemento enfocado aún no se adjunta
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Barra Lateral de Navegación
        BarraLateralTv(
            navController = navController,
            rutaSeleccionada = TvRoutes.GRUPOS,
            onCerrarSesion = onCerrarSesion
        )

        // Contenido Principal Dinámico
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            when (vistaActual) {
                VistaGruposTv.PRINCIPAL -> {
                    PantallaPrincipalGruposTv(
                        estadoUi = estadoUi,
                        solicitadorEnfoque = solicitadorEnfoquePrincipal,
                        alSeleccionarCrear = { vistaActual = VistaGruposTv.CREAR },
                        alSeleccionarUnirse = { vistaActual = VistaGruposTv.UNIRSE },
                        alSeleccionarGrupo = { id, nombre, codigo, descripcion, idCreador ->
                            grupoSeleccionadoId = id
                            grupoSeleccionadoNombre = nombre
                            grupoSeleccionadoCodigo = codigo
                            grupoSeleccionadoDescripcion = descripcion
                            grupoSeleccionadoIdCreador = idCreador
                            grupoViewModel.cargarDetalleGrupo(id)
                            vistaActual = VistaGruposTv.DETALLE
                        }
                    )
                }
                VistaGruposTv.CREAR -> {
                    PantallaCrearGrupoTv(
                        estaCargando = estadoUi.estaCargando,
                        solicitadorEnfoque = solicitadorEnfoqueCrear,
                        alGuardar = { nombre, descripcion ->
                            grupoViewModel.crearNuevoGrupo(nombre, descripcion, idUsuario)
                            vistaActual = VistaGruposTv.PRINCIPAL
                        },
                        alVolver = {
                            vistaActual = VistaGruposTv.PRINCIPAL
                        }
                    )
                }
                VistaGruposTv.UNIRSE -> {
                    PantallaUnirseGrupoTv(
                        estaCargando = estadoUi.estaCargando,
                        solicitadorEnfoque = solicitadorEnfoqueUnirse,
                        alUnirse = { codigo ->
                            grupoViewModel.unirseAGrupoConCodigo(idUsuario, codigo)
                            vistaActual = VistaGruposTv.PRINCIPAL
                        },
                        alVolver = {
                            vistaActual = VistaGruposTv.PRINCIPAL
                        }
                    )
                }
                VistaGruposTv.DETALLE -> {
                    PantallaDetalleGrupoTv(
                        idGrupo = grupoSeleccionadoId ?: 0,
                        idUsuarioActual = idUsuario,
                        idCreadorGrupo = grupoSeleccionadoIdCreador,
                        nombreGrupo = grupoSeleccionadoNombre,
                        codigoGrupo = grupoSeleccionadoCodigo,
                        descripcionGrupo = grupoSeleccionadoDescripcion,
                        estadoUi = estadoUi,
                        solicitadorEnfoque = solicitadorEnfoqueDetalle,
                        alCargarEstadisticasMiembro = { idMiembro ->
                            grupoViewModel.cargarEstadisticasMiembro(idMiembro)
                        },
                        alSalirGrupo = {
                            grupoSeleccionadoId?.let { idGrupo ->
                                grupoViewModel.salirDeGrupo(idUsuario, idGrupo) {
                                    vistaActual = VistaGruposTv.PRINCIPAL
                                }
                            }
                        },
                        alEliminarGrupo = {
                            grupoSeleccionadoId?.let { idGrupo ->
                                grupoViewModel.eliminarGrupo(idUsuario, idGrupo) {
                                    vistaActual = VistaGruposTv.PRINCIPAL
                                }
                            }
                        },
                        alVolver = {
                            vistaActual = VistaGruposTv.PRINCIPAL
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaPrincipalGruposTv(
    estadoUi: EstadoUiGrupoTv,
    solicitadorEnfoque: FocusRequester,
    alSeleccionarCrear: () -> Unit,
    alSeleccionarUnirse: () -> Unit,
    alSeleccionarGrupo: (Int, String, String, String?, Int?) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Grupos",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Columna de Acciones
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaAccionGrupoTv(
                    titulo = "Unirse a un grupo",
                    descripcion = "Unete a un grupo mediante un código de grupo",
                    vectorIcono = Icons.Default.Groups,
                    alHacerClick = alSeleccionarUnirse,
                    modifier = Modifier.focusRequester(solicitadorEnfoque)
                )

                TarjetaAccionGrupoTv(
                    titulo = "Crear un grupo",
                    descripcion = "Crea tu propio grupo e invita a tus amigos.",
                    vectorIcono = Icons.Default.Add,
                    alHacerClick = alSeleccionarCrear
                )
            }

            // Columna de Grupos del Usuario
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Tus Comunidades",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (estadoUi.estaCargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cargando grupos...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                } else if (estadoUi.listaGrupos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceVariant, shape = RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no perteneces a ningún grupo.\n¡Crea uno o únete con un código!",
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(estadoUi.listaGrupos.size) { indice ->
                            val grupo = estadoUi.listaGrupos[indice]
                            TarjetaGrupoTv(
                                nombre = grupo.nombre,
                                descripcion = grupo.descripcion,
                                codigo = grupo.codigo,
                                alHacerClick = {
                                    alSeleccionarGrupo(grupo.idGrupo, grupo.nombre, grupo.codigo, grupo.descripcion, grupo.idCreador)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaAccionGrupoTv(
    titulo: String,
    descripcion: String,
    vectorIcono: ImageVector,
    alHacerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = alHacerClick,
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceVariant,
            focusedContainerColor = SurfaceVariant.copy(alpha = 0.8f),
            pressedContainerColor = PrimaryContainer
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape = RoundedCornerShape(16.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (tieneFoco) Primary else PrimaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorIcono,
                    contentDescription = null,
                    tint = if (tieneFoco) Color.Black else Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaGrupoTv(
    nombre: String,
    descripcion: String?,
    codigo: String,
    alHacerClick: () -> Unit
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = alHacerClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceVariant,
            focusedContainerColor = SurfaceVariant.copy(alpha = 0.8f),
            pressedContainerColor = PrimaryContainer
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Secondary),
                shape = RoundedCornerShape(12.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SecondaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (!descripcion.isNullOrBlank()) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Código",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = codigo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaCrearGrupoTv(
    estaCargando: Boolean,
    solicitadorEnfoque: FocusRequester,
    alGuardar: (String, String?) -> Unit,
    alVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Crear un grupo",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Outline, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Crea tu propio grupo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "e invita a tus amigos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Column {
                    Text(
                        text = "Nombre del grupo",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = { Text("Ej. Corredores del Parque", color = OnSurfaceVariant) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(solicitadorEnfoque)
                    )
                }

                Column {
                    Text(
                        text = "Descripción (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Agrega una descripción para tu grupo", color = OnSurfaceVariant) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && !estaCargando) {
                            alGuardar(nombre.trim(), descripcion.trim().takeIf { it.isNotBlank() })
                        }
                    },
                    enabled = nombre.isNotBlank() && !estaCargando,
                    colors = ButtonDefaults.colors(
                        containerColor = PrimaryContainer,
                        contentColor = Color.White,
                        focusedContainerColor = Primary,
                        focusedContentColor = Color.Black
                    ),
                    scale = ButtonDefaults.scale(focusedScale = 1.0f),
                    modifier = Modifier.fillMaxWidth(),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Crear grupo",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = alVolver,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = SurfaceVariant,
                focusedContainerColor = SurfaceVariant.copy(alpha = 0.8f)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Volver a Grupos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaUnirseGrupoTv(
    estaCargando: Boolean,
    solicitadorEnfoque: FocusRequester,
    alUnirse: (String) -> Unit,
    alVolver: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var botonVolverEnfocado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Ingresar Código",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Outline, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Unirse con Código",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Código de Grupo:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.take(6) },
                        placeholder = { Text("Ej. CORR01", color = OnSurfaceVariant) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(solicitadorEnfoque)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (codigo.isNotBlank() && !estaCargando) {
                            alUnirse(codigo.trim().uppercase())
                        }
                    },
                    enabled = codigo.isNotBlank() && !estaCargando,
                    colors = ButtonDefaults.colors(
                        containerColor = PrimaryContainer,
                        contentColor = Color.White,
                        focusedContainerColor = Primary,
                        focusedContentColor = Color.Black
                    ),
                    scale = ButtonDefaults.scale(focusedScale = 1.0f),
                    modifier = Modifier.fillMaxWidth(),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Confirmar y Unirse",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = alVolver,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = SurfaceVariant,
                focusedContainerColor = SurfaceVariant.copy(alpha = 0.8f)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Volver a Grupos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaDetalleGrupoTvLegacy(
    idUsuarioActual: Int,
    idCreadorGrupo: Int?,
    nombreGrupo: String,
    codigoGrupo: String,
    descripcionGrupo: String?,
    estadoUi: EstadoUiGrupoTv,
    solicitadorEnfoque: FocusRequester,
    alSalirGrupo: () -> Unit,
    alEliminarGrupo: () -> Unit,
    alVolver: () -> Unit
) {
    var pestanaSeleccionada by remember { mutableStateOf(0) }
    var mostrarConfirmarSalir by remember { mutableStateOf(false) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var botonVolverEnfocado by remember { mutableStateOf(false) }

    val miembros = estadoUi.listaMiembros
    val distanciaTotal = miembros.sumOf { it.distancia }
    val caloriasTotal = miembros.sumOf { it.calorias }
    val tiempoTotalSegundos = miembros.sumOf { it.tiempo }
    val tiempoTotalMinutos = tiempoTotalSegundos / 60

    val esDueño = idUsuarioActual == idCreadorGrupo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = alVolver,
                modifier = Modifier
                    .size(40.dp)
                    .onFocusChanged { botonVolverEnfocado = it.isFocused }
                    .focusRequester(solicitadorEnfoque),
                shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = SurfaceVariant,
                    focusedContainerColor = Primary,
                    pressedContainerColor = PrimaryContainer
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Mi grupo",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (esDueño) {
                        mostrarConfirmarEliminar = true
                    } else {
                        mostrarConfirmarSalir = true
                    }
                },
                colors = ButtonDefaults.colors(
                    containerColor = Error,
                    contentColor = Color.White
                ),
                scale = ButtonDefaults.scale(focusedScale = 1.0f),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (esDueño) Icons.Default.Delete else Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esDueño) "Eliminar grupo" else "Salir del grupo", 
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceVariant, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Outline, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreGrupo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${miembros.size} miembros | Código: $codigoGrupo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    if (!descripcionGrupo.isNullOrBlank()) {
                        Text(
                            text = descripcionGrupo,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pestañas (TabRow de TV Material 3)
        TabRow(
            selectedTabIndex = pestanaSeleccionada,
            modifier = Modifier.fillMaxWidth().background(Background),
            indicator = { tabPositions ->
                if (pestanaSeleccionada < tabPositions.size) {
                    TabRowDefaults.PillIndicator(
                        currentTabPosition = tabPositions[pestanaSeleccionada],
                        activeColor = PrimaryContainer,
                        inactiveColor = Color.Transparent
                    )
                }
            }
        ) {
            Tab(
                selected = pestanaSeleccionada == 0,
                onFocus = { pestanaSeleccionada = 0 }
            ) {
                Text(
                    text = "Actividad",
                    fontWeight = if (pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (pestanaSeleccionada == 0) Primary else OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Tab(
                selected = pestanaSeleccionada == 1,
                onFocus = { pestanaSeleccionada = 1 }
            ) {
                Text(
                    text = "Miembros",
                    fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (pestanaSeleccionada == 1) Primary else OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (pestanaSeleccionada == 0) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TarjetaMetricaDetalleTv(
                            titulo = "Distancia total",
                            valor = String.format("%.1f km", distanciaTotal),
                            icono = Icons.Default.DirectionsRun,
                            colorMetrica = Distancia,
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaMetricaDetalleTv(
                            titulo = "Calorías totales",
                            valor = String.format("%,d kcal", caloriasTotal),
                            icono = Icons.Default.LocalFireDepartment,
                            colorMetrica = Calorias,
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaMetricaDetalleTv(
                            titulo = "Tiempo total",
                            valor = "${tiempoTotalMinutos / 60}h ${tiempoTotalMinutos % 60}min",
                            icono = Icons.Default.AccessTime,
                            colorMetrica = Tiempo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    // Botón 'Unirse a otro grupo' eliminado
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Miembros del grupo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (miembros.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay miembros en este grupo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(miembros.size) { indice ->
                                    val miembro = miembros[indice]
                                    FilaMiembroGrupoTv(miembro = miembro)
                                }
                            }
                        }
                    }

                    // Botón 'Unirse a otro grupo' eliminado
                }
            }
        }
    }

    if (mostrarConfirmarSalir) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarConfirmarSalir = false },
            containerColor = SurfaceVariant,
            title = {
                Text(
                    text = "Salir del grupo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir del grupo '$nombreGrupo'? Dejarás de compartir tus estadísticas en esta comunidad.",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        mostrarConfirmarSalir = false
                        alSalirGrupo()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("SALIR DEL GRUPO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = { mostrarConfirmarSalir = false },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (mostrarConfirmarEliminar) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            containerColor = SurfaceVariant,
            title = {
                Text(
                    text = "Eliminar grupo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "¿Quieres eliminar el grupo?  (el grupo se eliminara junto con todos los miembros)",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        mostrarConfirmarEliminar = false
                        alEliminarGrupo()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("ELIMINAR GRUPO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = { mostrarConfirmarEliminar = false },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaMetricaDetalleTv(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorMetrica: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = NonInteractiveSurfaceDefaults.colors(
            containerColor = SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorMetrica,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilaMiembroGrupoTv(
    miembro: MiembroGrupoResponse
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceVariant,
            focusedContainerColor = SurfaceVariant.copy(alpha = 0.8f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape = RoundedCornerShape(12.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val inicial = miembro.nombre.firstOrNull()?.toString()?.uppercase() ?: "?"
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SecondaryContainer, shape = CircleShape)
                    .border(1.dp, Secondary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inicial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = miembro.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "@${miembro.nombreUsuario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.1f km", miembro.distancia),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Distancia
                    )
                    Text(
                        text = "Distancia",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%,d", miembro.pasos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Pasos
                    )
                    Text(
                        text = "Pasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}
