    package com.example.djeventhub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.djeventhub.location.LocationManager
import com.example.djeventhub.navigation.AppNavigation
import com.example.djeventhub.ui.theme.DJEventHubTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Places SDK
        // TODO: Add your Maps API key to local.properties as MAPS_API_KEY=your_key_here
        // The key is automatically loaded from BuildConfig or use a default for development
        if (!Places.isInitialized()) {
            try {
                val apiKey = BuildConfig.MAPS_API_KEY.ifEmpty { "YOUR_API_KEY_HERE" }
                Places.initialize(applicationContext, apiKey)
            } catch (e: Exception) {
                // Places will not work without a valid API key
                android.util.Log.e("MainActivity", "Failed to initialize Places SDK: ${e.message}")
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            DJEventHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun MainContent() {
        var locationPermissionGranted by remember { mutableStateOf(checkLocationPermission()) }
        var showPermissionRationale by remember { mutableStateOf(false) }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            locationPermissionGranted = permissions.values.any { it }
            if (!locationPermissionGranted) {
                showPermissionRationale = true
            }
        }

        LaunchedEffect(Unit) {
            if (!locationPermissionGranted) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        if (showPermissionRationale) {
            PermissionRationaleDialog(
                onDismiss = { showPermissionRationale = false },
                onRequestPermission = {
                    showPermissionRationale = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
        }

        // Create LocationManager (or null if permission denied)
        val locationManager = remember(locationPermissionGranted) {
            if (locationPermissionGranted) LocationManager(this) else null
        }

        AppNavigation(
            locationManager = locationManager ?: LocationManager(this) // Pass even if null to avoid crashes
        )
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permiso de ubicación") },
        text = {
            Column {
                Text(
                    "DJ Event Hub necesita acceso a tu ubicación para:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("• Ordenar eventos por cercanía")
                Text("• Mostrarte la distancia a cada evento")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sin este permiso, los eventos se mostrarán sin ordenar por distancia.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Solicitar permiso")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continuar sin ubicación")
            }
        }
    )
}