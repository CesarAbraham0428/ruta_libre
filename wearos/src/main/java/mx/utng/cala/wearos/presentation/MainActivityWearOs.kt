package mx.utng.cala.wearos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.AppScaffold
import mx.utng.cala.wearos.presentation.navigation.WearNavGraph
import mx.utng.cala.wearos.presentation.theme.RutaLibreTheme

class MainActivityWearOs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RutaLibreTheme {
                AppScaffold {
                    val navController = rememberNavController()
                    WearNavGraph(navController = navController)
                }
            }
        }
    }
}
