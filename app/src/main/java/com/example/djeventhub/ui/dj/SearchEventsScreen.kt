package com.example.djeventhub.ui.dj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.EventListItem
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchEventsScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit
) {
    val events by viewModel.events.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Buscar Eventos",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack
                ),
                windowInsets = WindowInsets(0.dp),
                modifier = Modifier.height(48.dp)
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, género, ubicación...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    unfocusedBorderColor = TextTertiary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = if (searchQuery.isBlank()) 
                    "Todos los eventos disponibles" 
                else 
                    "Resultados de búsqueda",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Filtered events list
            val filteredEvents = if (searchQuery.isBlank()) {
                events
            } else {
                events.filter {
                    it.event.name.contains(searchQuery, ignoreCase = true) ||
                            it.event.description.contains(searchQuery, ignoreCase = true) ||
                            it.event.locationName.contains(searchQuery, ignoreCase = true) ||
                            (it.event.musicGenre?.contains(searchQuery, ignoreCase = true) ?: false)
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) 
                                "No hay eventos disponibles" 
                            else 
                                "No se encontraron eventos",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Intenta con otros términos",
                                color = TextTertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEvents) { eventWithDistance ->
                        EventListItem(
                            eventWithDistance = eventWithDistance,
                            onMapClick = { },
                            onEventClick = onEventClick
                        )
                    }
                }
            }
        }
    }
}
