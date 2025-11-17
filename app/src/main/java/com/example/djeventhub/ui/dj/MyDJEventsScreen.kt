package com.example.djeventhub.ui.dj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun MyDJEventsScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit
) {
    val events by viewModel.events.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    // Filter events where the current user has applied
    val myAppliedEvents = remember(events, currentUserId) {
        events.filter { it.event.applicants.contains(currentUserId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Postulaciones",
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
        if (myAppliedEvents.isEmpty()) {
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
                        text = "No te has postulado a ningún evento",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explora eventos en Inicio y postúlate",
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
                items(myAppliedEvents) { eventWithDistance ->
                    EventApplicationCard(
                        eventWithDistance = eventWithDistance,
                        onClick = { onEventClick(eventWithDistance.event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventApplicationCard(
    eventWithDistance: EventWithDistance,
    onClick: () -> Unit
) {
    val event = eventWithDistance.event
    val isSelected = event.selectedDJ == FirebaseAuth.getInstance().currentUser?.uid
    
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
                
                if (isSelected) {
                    Surface(
                        color = NeonPink.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "¡Seleccionado!",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = ElectricBlue.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Postulado",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue,
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
        }
    }
}