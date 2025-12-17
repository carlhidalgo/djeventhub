package com.example.djeventhub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.djeventhub.models.Event
import com.example.djeventhub.ui.animations.bounceClick
import com.example.djeventhub.ui.events.EventWithDistance
import com.example.djeventhub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reusable Event Card component
 * Shows event details with optional distance and animation for upcoming events
 */
@Composable
fun EventCard(
    eventWithDistance: EventWithDistance,
    onEventClick: (String) -> Unit,
    onMapClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
    showDistance: Boolean = true
) {
    val event = eventWithDistance.event
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date(event.date)
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
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonPink, NeonPurple)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date box
                    DateBadge(date = date)

                    Spacer(modifier = Modifier.width(12.dp))

                    // Event details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Distance badge
                    if (showDistance && !eventWithDistance.distanceText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            text = eventWithDistance.distanceText ?: "",
                            backgroundColor = NeonPink.copy(alpha = 0.2f),
                            textColor = NeonPink
                        )
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
                            contentDescription = "Hora",
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

                    // Map button
                    Box(modifier = Modifier.bounceClick { onMapClick(event) }) {
                        OutlinedButton(
                            onClick = { },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonPink
                            ),
                            border = BorderStroke(1.dp, NeonPink),
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

/**
 * Compact Event Card for lists with less detail
 */
@Composable
fun CompactEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image or placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonPink.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!event.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(event.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Date Badge component for event cards
 */
@Composable
fun DateBadge(
    date: Date,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
}

/**
 * DJ Profile Card component
 */
@Composable
fun DJProfileCard(
    djId: String,
    djName: String,
    djImageUrl: String,
    genres: List<String>,
    rating: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonPink.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!djImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = djImageUrl,
                        contentDescription = djName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = djName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = djName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating
                if (rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = NeonOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Genres
                if (genres.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        genres.take(2).forEach { genre ->
                            Badge(
                                text = genre,
                                backgroundColor = ElectricBlue.copy(alpha = 0.15f),
                                textColor = ElectricBlue,
                                modifier = Modifier
                            )
                        }
                        if (genres.size > 2) {
                            Badge(
                                text = "+${genres.size - 2}",
                                backgroundColor = TextTertiary.copy(alpha = 0.15f),
                                textColor = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Application Card for showing DJ applications to events
 */
@Composable
fun ApplicationCard(
    djName: String,
    djImageUrl: String,
    applicationDate: Long,
    status: String,
    onViewProfile: () -> Unit,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // DJ profile image
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    NeonPink.copy(alpha = 0.3f),
                                    NeonPurple.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .clickable(onClick = onViewProfile),
                    contentAlignment = Alignment.Center
                ) {
                    if (!djImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = djImageUrl,
                            contentDescription = djName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = djName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = djName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(applicationDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Status badge
                Badge(
                    text = status,
                    backgroundColor = when (status.lowercase()) {
                        "pendiente" -> NeonOrange.copy(alpha = 0.2f)
                        "aceptado" -> SuccessGreen.copy(alpha = 0.2f)
                        "rechazado" -> ErrorRed.copy(alpha = 0.2f)
                        else -> TextTertiary.copy(alpha = 0.2f)
                    },
                    textColor = when (status.lowercase()) {
                        "pendiente" -> NeonOrange
                        "aceptado" -> SuccessGreen
                        "rechazado" -> ErrorRed
                        else -> TextSecondary
                    }
                )
            }

            // Action buttons for pending applications
            if (status.lowercase() == "pendiente" && onAccept != null && onReject != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DJSecondaryButton(
                        text = "Rechazar",
                        onClick = onReject,
                        borderColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    DJPrimaryButton(
                        text = "Aceptar",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Chat Preview Card
 */
@Composable
fun ChatPreviewCard(
    chatName: String,
    lastMessage: String,
    timestamp: Long,
    unreadCount: Int,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unreadCount > 0) DarkSurfaceVariant else DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonPink.copy(alpha = 0.3f),
                                NeonPurple.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = chatName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = chatName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unreadCount > 0) NeonPink else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (unreadCount > 0) {
                        Badge(
                            text = unreadCount.toString(),
                            backgroundColor = NeonPink,
                            textColor = Color.White
                        )
                    }
                }
            }
        }
    }
}
