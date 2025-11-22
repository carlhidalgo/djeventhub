package com.example.djeventhub

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.djeventhub.models.Event
import com.example.djeventhub.ui.animations.animateItemPlacement
import com.example.djeventhub.ui.animations.bounceClick
import com.example.djeventhub.ui.events.EventListViewModel
import com.example.djeventhub.ui.events.EventWithDistance
import com.example.djeventhub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Composable screen to show a list of events sorted by proximity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onAddEvent: () -> Unit,
    onProfile: (() -> Unit)? = null,
    onEventClick: (String) -> Unit = {}
) {
    val events by viewModel.events.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DJ Event Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepBlack
                )
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        val swipeState = rememberSwipeRefreshState(isRefreshing)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SwipeRefresh(
                state = swipeState,
                onRefresh = { viewModel.refreshLocation() },
                indicator = { s, trigger ->
                    SwipeRefreshIndicator(state = s, refreshTriggerDistance = trigger)
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    itemsIndexed(
                        items = events,
                        key = { _, item -> item.event.id }
                    ) { index, eventWithDistance ->
                        EventListItem(
                            eventWithDistance = eventWithDistance,
                            onMapClick = { event ->
                                openInMaps(context, event)
                            },
                            onEventClick = { eventId ->
                                onEventClick(eventId)
                            },
                            modifier = Modifier.animateItemPlacement(index)
                        )
                    }
                }
            }

            // Optional: keep a manual indicator (Accompanist shows one inside SwipeRefresh)
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EventListItem(
    eventWithDistance: EventWithDistance,
    onMapClick: (Event) -> Unit,
    onEventClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val event = eventWithDistance.event
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date(event.date)

    // Check if this is the next upcoming event
    val isUpcoming = event.date > System.currentTimeMillis()

    // Pulse animation for upcoming events
    val pulseScale by animateFloatAsState(
        targetValue = if (isUpcoming) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(if (isUpcoming) pulseScale else 1f)
            .clickable { onEventClick(event.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box {
            // Neon glow border for upcoming events
            if (isUpcoming) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(1.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    NeonPink,
                                    NeonPurple
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        DarkSurface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date box (left side)
                    Column(
                        modifier = Modifier
                            .background(
                                DarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = SimpleDateFormat("dd", Locale.getDefault()).format(date),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        )
                        Text(
                            text = SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Event details (right side)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }

                    // Distance badge
                    if (eventWithDistance.distanceText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    NeonPink.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = eventWithDistance.distanceText,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonPink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time and location row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeFormat.format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Map button with bounce animation
                    Box(
                        modifier = Modifier.bounceClick { onMapClick(event) }
                    ) {
                        OutlinedButton(
                            onClick = { },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonPink
                            ),
                            border = BorderStroke(
                                1.dp,
                                NeonPink
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            enabled = false
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Abrir mapa",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MAPA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Function to open Google Maps with the event location
private fun openInMaps(context: android.content.Context, event: Event) {
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
