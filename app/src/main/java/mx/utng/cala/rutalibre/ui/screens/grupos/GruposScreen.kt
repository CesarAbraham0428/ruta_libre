package mx.utng.cala.rutalibre.ui.screens.grupos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.GrupoViewModel

/** Muestra los grupos del usuario y los diálogos para crear o unirse a uno. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposScreen(
    navController: NavController,
    grupoViewModel: GrupoViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val estadoUi by grupoViewModel.estadoUi.collectAsState()
    val idUsuarioActual = authState.idUsuario

    LaunchedEffect(idUsuarioActual) {
        idUsuarioActual?.let(grupoViewModel::cargarGruposDeUsuario)
    }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var mostrarDialogoUnirse by remember { mutableStateOf(false) }

    var nombreGrupoTexto by remember { mutableStateOf("") }
    var descripcionGrupoTexto by remember { mutableStateOf("") }
    var codigoGrupoTexto by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Grupos",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Espacio para equilibrar y centrar el título
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Botones de acción superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mostrarDialogoCrear = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = OnBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear", color = OnBackground, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { mostrarDialogoUnirse = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unirse", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tus Comunidades",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            estadoUi.mensajeError?.let { error ->
                Text(
                    text = error,
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (estadoUi.listaGrupos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(estadoUi.listaGrupos) { grupo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Routes.detalleGrupo(grupo.idGrupo, grupo.nombre)
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = Secondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    val descripcion = grupo.descripcion
                                    Text(
                                        text = grupo.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    if (!descripcion.isNullOrBlank()) {
                                        Text(
                                            text = descripcion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Código: ${grupo.codigo}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Ver Detalle",
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para Crear Grupo
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrear = false
                nombreGrupoTexto = ""
                descripcionGrupoTexto = ""
            },
            containerColor = SurfaceVariant,
            icon = {
                Icon(
                    Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    "Crear nuevo grupo",
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Crea una comunidad para compartir tus metas y competir con otros corredores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nombreGrupoTexto,
                        onValueChange = { nombreGrupoTexto = it },
                        label = { Text("Nombre del grupo") },
                        placeholder = { Text("Ej. Los Correcaminos") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descripcionGrupoTexto,
                        onValueChange = { descripcionGrupoTexto = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Ej. Entrenamiento matutino...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreGrupoTexto.isNotBlank()) {
                            idUsuarioActual?.let { idUsuario ->
                                grupoViewModel.crearNuevoGrupo(
                                    nombre = nombreGrupoTexto.trim(),
                                    descripcion = descripcionGrupoTexto.trim().takeIf { it.isNotBlank() },
                                    idCreador = idUsuario
                                )
                            }
                            mostrarDialogoCrear = false
                            nombreGrupoTexto = ""
                            descripcionGrupoTexto = ""
                        }
                    },
                    enabled = nombreGrupoTexto.isNotBlank() && idUsuarioActual != null && !estadoUi.estaCargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("GUARDAR", fontWeight = FontWeight.Bold, color = OnBackground)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarDialogoCrear = false
                        nombreGrupoTexto = ""
                        descripcionGrupoTexto = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Diálogo para Unirse a Grupo
    if (mostrarDialogoUnirse) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoUnirse = false
                codigoGrupoTexto = ""
            },
            containerColor = SurfaceVariant,
            icon = {
                Icon(
                    Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    "Unirse a un grupo",
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ingresa el código único del grupo al que deseas unirte.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    OutlinedTextField(
                        value = codigoGrupoTexto,
                        onValueChange = { codigoGrupoTexto = it.take(6) },
                        label = { Text("Código de grupo") },
                        placeholder = { Text("Ej. RL1234") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = Secondary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (codigoGrupoTexto.isNotBlank()) {
                            idUsuarioActual?.let { idUsuario ->
                                grupoViewModel.unirseAGrupoConCodigo(
                                    idUsuario = idUsuario,
                                    codigo = codigoGrupoTexto.trim().uppercase()
                                )
                            }
                            mostrarDialogoUnirse = false
                            codigoGrupoTexto = ""
                        }
                    },
                    enabled = codigoGrupoTexto.isNotBlank() && idUsuarioActual != null && !estadoUi.estaCargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("UNIRSE", fontWeight = FontWeight.Bold, color = OnBackground)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarDialogoUnirse = false
                        codigoGrupoTexto = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
