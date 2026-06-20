package mx.utng.cala.wearos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.AppScaffold
import mx.utng.cala.wearos.presentation.navigation.WearNavGraph
import mx.utng.cala.wearos.presentation.theme.RutaLibreTheme

class MainActivityWearOs : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 100)
        }

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
