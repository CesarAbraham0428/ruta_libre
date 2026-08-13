package mx.utng.cala.rutalibre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import mx.utng.cala.rutalibre.ui.navigation.NavGraph
import mx.utng.cala.rutalibre.ui.theme.RutaLibreTheme

/** Punto de entrada de la app móvil y contenedor de la navegación principal. */
class MainActivity : ComponentActivity() {
    /** Configura el tema Compose y muestra el grafo de navegación de Ruta Libre. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RutaLibreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
