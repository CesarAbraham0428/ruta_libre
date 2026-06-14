package mx.utng.cala.rutalibre.ui.screens.entrenamiento

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun EntrenamientoScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Entrenamiento en curso", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Text("Distancia: 0.0 km", style = MaterialTheme.typography.titleLarge)
        Text("Pasos: 0", style = MaterialTheme.typography.titleLarge)
        Text("Calorías: 0", style = MaterialTheme.typography.titleLarge)
        Text("Tiempo: 00:00", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.popBackStack() }) { Text("Finalizar") }
    }
}
