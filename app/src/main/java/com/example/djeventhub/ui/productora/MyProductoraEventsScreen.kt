package com.example.djeventhub.ui.productora

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductoraEventsScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit
) {
    val events by viewModel.events.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    // Filter events created by this productora
    val myCreatedEvents = remember(events, currentUserId) {
        events.filter { it.event.createdBy == currentUserId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Eventos Creados",
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack
                )
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        if (myCreatedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "No has creado ningún evento",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca el botón + para crear tu primer evento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp)
            ) {
                items(myCreatedEvents) { eventWithDistance ->
                    ProductoraEventCard(
                        eventWithDistance = eventWithDistance,
                        onClick = { onEventClick(eventWithDistance.event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoraEventCard(
    eventWithDistance: EventWithDistance,
    onClick: () -> Unit
) {
    val event = eventWithDistance.event
    val applicantsCount = event.applicants.size
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                if (event.selectedDJ != null) {
                    Surface(
                        color = NeonPink.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DJ Asignado",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = event.locationName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = java.text.SimpleDateFormat("dd MMM yyyy 'a las' HH:mm", java.util.Locale.getDefault()).format(java.util.Date(event.date)),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            
            if (event.musicGenre != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NeonPurple.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.musicGenre,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Show applicants count
            if (applicantsCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$applicantsCount ${if (applicantsCount == 1) "postulante" else "postulantes"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}