package mx.utng.cala.rutalibre.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes

@Composable
fun HomeScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Ruta Libre", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.ENTRENAMIENTO) }) {
            Column(Modifier.padding(16.dp)) { Text("Iniciar entrenamiento", style = MaterialTheme.typography.titleMedium) }
        }
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.METAS) }) {
            Column(Modifier.padding(16.dp)) { Text("Metas", style = MaterialTheme.typography.titleMedium) }
        }
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.GRUPOS) }) {
            Column(Modifier.padding(16.dp)) { Text("Grupos", style = MaterialTheme.typography.titleMedium) }
        }
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.PERFIL) }) {
            Column(Modifier.padding(16.dp)) { Text("Perfil", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
