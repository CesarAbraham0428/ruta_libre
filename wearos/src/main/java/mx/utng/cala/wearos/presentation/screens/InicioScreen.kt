package mx.utng.cala.wearos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import mx.utng.cala.wearos.presentation.navigation.WearRoutes

@Composable
fun InicioScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ruta Libre")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate(WearRoutes.METRICAS) }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar")
        }
    }
}
