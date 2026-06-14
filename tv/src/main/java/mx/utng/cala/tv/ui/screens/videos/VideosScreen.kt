package mx.utng.cala.tv.ui.screens.videos

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.Card
import androidx.tv.material3.Text

@Composable
fun VideosScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("Videos de ejercicios", style = androidx.tv.material3.MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))
        Text("Tus favoritos aparecerán aquí")
    }
}
