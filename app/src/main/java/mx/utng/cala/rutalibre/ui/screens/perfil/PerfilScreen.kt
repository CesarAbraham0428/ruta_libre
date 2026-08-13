package mx.utng.cala.rutalibre.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.PerfilViewModel

/** Muestra y permite editar los datos personales de la cuenta autenticada. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel,
    authViewModel: AuthViewModel
) {
    val estadoAutenticacion by authViewModel.uiState.collectAsState()
    val estadoPerfil by perfilViewModel.estado.collectAsState()
    val administradorEnfoque = LocalFocusManager.current

    var nombreEditable by remember { mutableStateOf("") }
    var pesoEditable by remember { mutableStateOf("") }
    var contrasenaEditable by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }

    val tieneLongitudMinima = contrasenaEditable.length >= 8
    val tieneLetra = contrasenaEditable.any { it.isLetter() }
    val tieneNumero = contrasenaEditable.any { it.isDigit() }
    val contrasenaValida = contrasenaEditable.isEmpty() || (tieneLongitudMinima && tieneLetra && tieneNumero)

    // Cargar información del usuario
    LaunchedEffect(estadoAutenticacion.idUsuario) {
        estadoAutenticacion.idUsuario?.let { id ->
            perfilViewModel.cargarUsuario(id)
        }
    }

    // Actualizar campos locales cuando cambie el estado del perfil
    LaunchedEffect(estadoPerfil.nombre) {
        nombreEditable = estadoPerfil.nombre
    }

    LaunchedEffect(estadoPerfil.pesoKg) {
        pesoEditable = estadoPerfil.pesoKg?.let { peso ->
            if (peso % 1.0 == 0.0) peso.toInt().toString() else peso.toString()
        } ?: ""
    }

    // Manejar éxito de la actualización
    LaunchedEffect(estadoPerfil.exito) {
        if (estadoPerfil.exito) {
            authViewModel.actualizarNombreLocal(nombreEditable)
            estadoPerfil.pesoKg?.let(authViewModel::actualizarPesoLocal)
            perfilViewModel.restablecerExito()
            contrasenaEditable = "" // Limpiar el campo de contraseña tras guardar
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = OnBackground
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            administradorEnfoque.clearFocus()
                            authViewModel.cerrarSesion()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Text(
                            "Cerrar sesión",
                            color = Error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            if (estadoPerfil.cargando && estadoPerfil.nombreUsuario.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    // Avatar con Inicial del Nombre
                    val inicial = if (nombreEditable.isNotEmpty()) nombreEditable.take(1).uppercase() else "U"
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Secondary, Tertiary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inicial,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = estadoPerfil.nombreUsuario,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )

                    Spacer(Modifier.height(32.dp))

                    // Campo 2: Nombre (Editable)
                    OutlinedTextField(
                        value = nombreEditable,
                        onValueChange = {
                            nombreEditable = it
                            perfilViewModel.limpiarError()
                        },
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            cursorColor = Primary,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pesoEditable,
                        onValueChange = { nuevo ->
                            if (nuevo.length <= 6 && nuevo.all { it.isDigit() || it == '.' || it == ',' }) {
                                pesoEditable = nuevo
                                perfilViewModel.limpiarError()
                            }
                        },
                        label = { Text("Peso") },
                        suffix = { Text("kg") },
                        leadingIcon = {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = Primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            cursorColor = Primary,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(Modifier.height(16.dp))

                    // Campo 3: Contraseña (Editable)
                    OutlinedTextField(
                        value = contrasenaEditable,
                        onValueChange = {
                            contrasenaEditable = it
                            perfilViewModel.limpiarError()
                        },
                        label = { Text("Nueva contraseña") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Secondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    imageVector = if (contrasenaVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = OnSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedLabelColor = Secondary,
                            unfocusedLabelColor = OnSurfaceVariant,
                            cursorColor = Secondary,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // Requisitos de la Contraseña (Dinámicos)
                    if (contrasenaEditable.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Tu contraseña debe tener:",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            RequisitoContrasena("Mínimo 8 caracteres", tieneLongitudMinima)
                            RequisitoContrasena("Al menos una letra", tieneLetra)
                            RequisitoContrasena("Al menos un número", tieneNumero)
                        }
                    }

                    // Mensaje de Error
                    if (estadoPerfil.error != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = estadoPerfil.error ?: "",
                            color = Error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Botón: Guardar Cambios
                    val pesoKg = pesoEditable.replace(',', '.').toDoubleOrNull()
                    val pesoValido = pesoKg != null && pesoKg in 20.0..300.0
                    val datosCambiados = nombreEditable != estadoPerfil.nombre ||
                        contrasenaEditable.isNotEmpty() || pesoKg != estadoPerfil.pesoKg
                    val botonHabilitado = datosCambiados && nombreEditable.isNotBlank() &&
                        contrasenaValida && pesoValido && !estadoPerfil.cargando

                    Button(
                        onClick = {
                            administradorEnfoque.clearFocus()
                            val idUsuario = estadoAutenticacion.idUsuario
                            val pesoAGuardar = pesoKg
                            if (idUsuario != null && pesoAGuardar != null) {
                                perfilViewModel.actualizarUsuario(
                                    idUsuario = idUsuario,
                                    nuevoNombre = nombreEditable,
                                    nuevaContrasena = contrasenaEditable.ifEmpty { null },
                                    nuevoPesoKg = pesoAGuardar
                                )
                            }
                        },
                        enabled = botonHabilitado,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color(0xFF050B17),
                            disabledContainerColor = Primary.copy(alpha = 0.3f),
                            disabledContentColor = OnSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (estadoPerfil.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF050B17),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "GUARDAR CAMBIOS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/** Indica visualmente si un requisito de contraseña se encuentra cumplido. */
@Composable
private fun RequisitoContrasena(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (cumplido) Primary else Outline,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = texto,
            color = if (cumplido) OnSurface else OnSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}
