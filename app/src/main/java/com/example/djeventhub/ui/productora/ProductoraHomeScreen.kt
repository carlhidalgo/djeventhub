package com.example.djeventhub.ui.productora

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.djeventhub.EventListViewModel

@Composable
fun ProductoraHomeScreen(
    eventViewModel: EventListViewModel,
    onLogout: () -> Unit,
    onSearchDJs: () -> Unit,
    onProfile: () -> Unit,
    onAddEvent: () -> Unit,
    onEventClick: (String) -> Unit = {}
) {
    // Use the new Instagram-style navigation
    ProductoraMainScreen(
        viewModel = eventViewModel,
        onLogout = onLogout,
        onProfile = onProfile,
        onSearchDJs = onSearchDJs,
        onAddEvent = onAddEvent,
        onEventClick = onEventClick
    )
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay eventos organizados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Busca DJs para comenzar a organizar eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
