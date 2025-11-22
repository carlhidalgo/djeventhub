package com.example.djeventhub.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.djeventhub.models.Event
import com.example.djeventhub.ui.events.EventListViewModel
import com.example.djeventhub.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: EventListViewModel
) {
    val events by viewModel.events.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    // Location permission
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Map state
    val defaultLocation = LatLng(40.4168, -3.7038) // Madrid by default
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation?.let { LatLng(it.first, it.second) } ?: defaultLocation,
            12f
        )
    }

    // Selected event for bottom sheet
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Update camera when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.first, it.second),
                12f
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DJ Event Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.djeventhub.ui.theme.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.example.djeventhub.ui.theme.DeepBlack
                ),
                modifier = Modifier.height(56.dp)
            )
        },
        containerColor = com.example.djeventhub.ui.theme.DeepBlack
    ) { paddingValues ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionState.status.isGranted
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Event markers
            events.forEach { eventWithDistance ->
                val event = eventWithDistance.event
                if (event.latitude != null && event.longitude != null) {
                    Marker(
                        state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                        title = event.name,
                        snippet = event.locationName,
                        onClick = {
                            selectedEvent = event
                            true
                        }
                    )
                }
            }
        }

        // My Location button with pulse animation
        val pulseScale = remember { Animatable(1f) }

        LaunchedEffect(Unit) {
            pulseScale.animateTo(
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        FloatingActionButton(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    viewModel.refreshLocation()
                    userLocation?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.first, it.second),
                            14f
                        )
                    }
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset(y = if (selectedEvent != null) (-200).dp else 0.dp)
                .scale(pulseScale.value),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Mi ubicación"
            )
        }

        // Event details bottom sheet with slide animation
        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedEvent?.let { event ->
                EventDetailsCard(
                    event = event,
                    onDismiss = { selectedEvent = null },
                    onOpenMaps = {
                        openEventInMaps(context, it)
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    }
}

// Function to open Google Maps with the event location
private fun openEventInMaps(context: android.content.Context, event: Event) {
    val uri = if (event.latitude != null && event.longitude != null) {
        // Use coordinates for more precise location
        Uri.parse("geo:${event.latitude},${event.longitude}?q=${Uri.encode(event.locationName)}")
    } else {
        // Fallback to search by address
        Uri.parse("geo:0,0?q=${Uri.encode(event.locationName)}")
    }

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    // Check if Google Maps is installed, otherwise use browser
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback to browser
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(event.locationName)}")
        )
        context.startActivity(browserIntent)
    }
}

@Composable
fun EventDetailsCard(
    event: Event,
    onDismiss: () -> Unit,
    onOpenMaps: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Text(
                text = event.locationName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = event.description,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Open in Google Maps button
            Button(
                onClick = { onOpenMaps(event) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver en Google Maps")
            }
        }
    }
}