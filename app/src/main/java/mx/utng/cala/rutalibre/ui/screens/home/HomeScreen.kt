package mx.utng.cala.rutalibre.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.data.mqtt.MqttConnectionStatus

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mqttStatus: MqttConnectionStatus
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "¿Listo para tu próxima ruta?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val synchronizationText = when (mqttStatus) {
                MqttConnectionStatus.CONNECTED -> "Sincronización en tiempo real activa"
                MqttConnectionStatus.CONNECTING -> "Conectando sincronización..."
                MqttConnectionStatus.ERROR -> "Sin conexión en tiempo real"
                MqttConnectionStatus.DISCONNECTED -> "Sincronización desconectada"
            }
            val synchronizationColor = if (mqttStatus == MqttConnectionStatus.CONNECTED) {
                Primary
            } else {
                OnSurfaceVariant
            }
            Text(
                text = synchronizationText,
                color = synchronizationColor,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))

            // Tarjeta Destacada: Iniciar Entrenamiento
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { navController.navigate(Routes.ENTRENAMIENTO) },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Primary, Secondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Iniciar Entrenamiento",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF050B17)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Registra tus pasos, calorías y recorridos en tiempo real.",
                                fontSize = 13.sp,
                                color = Color(0xFF050B17).copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF050B17).copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = "Iniciar",
                                tint = Color(0xFF050B17),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sección Opciones
            Text(
                text = "Accesos Rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TarjetaHome(
                titulo = "Historial de actividades",
                descripcion = "Consulta tus entrenamientos guardados y revisa sus métricas.",
                icono = Icons.Default.History,
                colorAcento = Primary,
                alHacerClick = { navController.navigate(Routes.HISTORIAL) }
            )

            Spacer(Modifier.height(16.dp))

            // Tarjeta 2: Metas
            TarjetaHome(
                titulo = "Metas Personales",
                descripcion = "Establece objetivos de distancia, pasos, tiempo o calorías y supera tus límites.",
                icono = Icons.Default.EmojiEvents,
                colorAcento = Tertiary,
                alHacerClick = { navController.navigate(Routes.METAS) }
            )

            Spacer(Modifier.height(16.dp))

            // Tarjeta 3: Grupos
            TarjetaHome(
                titulo = "Grupos y Comunidad",
                descripcion = "Únete a grupos de corredores, compite en los rankings y comparte tus logros.",
                icono = Icons.Default.Group,
                colorAcento = Secondary,
                alHacerClick = { navController.navigate(Routes.GRUPOS) }
            )

            Spacer(Modifier.height(16.dp))

            // Tarjeta 4: Perfil
            TarjetaHome(
                titulo = "Mi Perfil",
                descripcion = "Edita tus datos personales y administra tu cuenta.",
                icono = Icons.Default.Person,
                colorAcento = Primary,
                alHacerClick = { navController.navigate(Routes.PERFIL) }
            )

            Spacer(Modifier.height(16.dp))

            TarjetaHome(
                titulo = "Vincular TV",
                descripcion = "Conecta una pantalla de TV con tu cuenta mediante un código temporal.",
                icono = Icons.Default.Link,
                colorAcento = Secondary,
                alHacerClick = { navController.navigate(Routes.VINCULAR_DISPOSITIVO) }
            )

            Spacer(Modifier.height(16.dp))

            TarjetaHome(
                titulo = "Dispositivos vinculados",
                descripcion = "Consulta y administra las sesiones de tu TV y reloj.",
                icono = Icons.Default.Devices,
                colorAcento = Primary,
                alHacerClick = { navController.navigate(Routes.DISPOSITIVOS) }
            )

            Spacer(Modifier.height(16.dp))

            TarjetaHome(
                titulo = "Acerca de la app",
                descripcion = "Conoce el alcance de las métricas y la información mostrada.",
                icono = Icons.Default.Info,
                colorAcento = Secondary,
                alHacerClick = { navController.navigate(Routes.ACERCA_DE_LA_APP) }
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TarjetaHome(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    colorAcento: Color,
    alHacerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = alHacerClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorAcento.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorAcento,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
