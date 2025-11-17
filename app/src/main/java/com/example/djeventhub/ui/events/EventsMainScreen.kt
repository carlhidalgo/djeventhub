package com.example.djeventhub.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.djeventhub.EventListScreen
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.ui.map.MapScreen

enum class EventsTab {
    LIST, MAP
}

@Composable
fun EventsMainScreen(
    viewModel: EventListViewModel,
    onLogout: () -> Unit,
    onAddEvent: () -> Unit,
    onProfile: (() -> Unit)? = null,
    showOnlyList: Boolean = false,
    showOnlyMap: Boolean = false,
    onEventClick: (String) -> Unit = {}
) {
    var selectedTab by remember {
        mutableStateOf(
            when {
                showOnlyList -> EventsTab.LIST
                showOnlyMap -> EventsTab.MAP
                else -> EventsTab.LIST
            }
        )
    }

    // Si showOnlyList o showOnlyMap están activos, no mostramos el bottom bar interno
    val showBottomBar = !showOnlyList && !showOnlyMap

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Lista"
                        )
                    },
                    label = { Text("Lista") },
                    selected = selectedTab == EventsTab.LIST,
                    onClick = { selectedTab = EventsTab.LIST }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = "Mapa"
                        )
                    },
                    label = { Text("Mapa") },
                    selected = selectedTab == EventsTab.MAP,
                    onClick = { selectedTab = EventsTab.MAP }
                )
            }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                EventsTab.LIST -> {
                    EventListScreen(
                        viewModel = viewModel,
                        onLogout = onLogout,
                        onAddEvent = onAddEvent,
                        onProfile = onProfile,
                        onEventClick = onEventClick
                    )
                }
                EventsTab.MAP -> {
                    MapScreen(
                        viewModel = viewModel,
                        onLogout = onLogout,
                        onAddEvent = onAddEvent,
                        onProfile = onProfile
                    )
                }
            }
        }
    }
}
