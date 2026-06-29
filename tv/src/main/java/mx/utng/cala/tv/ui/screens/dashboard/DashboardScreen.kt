package mx.utng.cala.tv.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("Dashboard Semanal", style = androidx.tv.material3.MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(onClick = {}) { Column(Modifier.padding(16.dp)) { Text("Distancia"); Text("0 km") } }
            Card(onClick = {}) { Column(Modifier.padding(16.dp)) { Text("Pasos"); Text("0") } }
            Card(onClick = {}) { Column(Modifier.padding(16.dp)) { Text("Calorías"); Text("0") } }
            Card(onClick = {}) { Column(Modifier.padding(16.dp)) { Text("Tiempo"); Text("0 min") } }
        }

        Spacer(Modifier.height(24.dp))
        Text("Gráfica de rendimiento diario", style = androidx.tv.material3.MaterialTheme.typography.titleLarge)
    }
}
