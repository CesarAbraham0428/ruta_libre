package mx.utng.cala.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import mx.utng.cala.tv.ui.navigation.TvNavGraph
import mx.utng.cala.tv.ui.theme.RutaLibreTheme

class MainActivityTv : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RutaLibreTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    val navController = rememberNavController()
                    TvNavGraph(navController = navController)
                }
            }
        }
    }
}
