package com.example.djeventhub.ui.dj

import androidx.compose.runtime.Composable
import com.example.djeventhub.EventListViewModel

@Composable
fun DJHomeScreen(
    viewModel: EventListViewModel,
    onLogout: () -> Unit,
    onAddEvent: () -> Unit,
    onProfile: () -> Unit,
    onEventClick: (String) -> Unit = {}
) {
    // Use the new Instagram-style navigation
    DJMainScreen(
        viewModel = viewModel,
        onLogout = onLogout,
        onAddEvent = onAddEvent,
        onProfile = onProfile,
        onEventClick = onEventClick
    )
}
