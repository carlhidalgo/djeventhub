package com.example.djeventhub.ui.dj

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.ui.events.EventsMainScreen
import com.example.djeventhub.ui.navigation.BottomNavItem
import com.example.djeventhub.ui.navigation.InstagramBottomBar

enum class DJScreen(val route: String) {
    HOME("dj_home"),
    MAP("dj_map"),
    ADD("dj_add"),
    CHAT("dj_chat"),
    PROFILE("dj_profile")
}

@Composable
fun DJMainScreen(
    viewModel: EventListViewModel,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onAddEvent: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(DJScreen.HOME) }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = DJScreen.HOME.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Inicio"
        ),
        BottomNavItem(
            route = DJScreen.MAP.route,
            selectedIcon = Icons.Filled.Place,
            unselectedIcon = Icons.Outlined.Place,
            label = "Mapa"
        ),
        BottomNavItem(
            route = DJScreen.ADD.route,
            selectedIcon = Icons.Filled.Add,
            unselectedIcon = Icons.Filled.Add,
            label = "Agregar",
            isCenterItem = true
        ),
        BottomNavItem(
            route = DJScreen.CHAT.route,
            selectedIcon = Icons.Filled.ChatBubble,
            unselectedIcon = Icons.Outlined.ChatBubbleOutline,
            label = "Chat"
        ),
        BottomNavItem(
            route = DJScreen.PROFILE.route,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            label = "Perfil"
        )
    )

    Scaffold(
        bottomBar = {
            InstagramBottomBar(
                items = bottomNavItems,
                currentRoute = currentScreen.route,
                onItemClick = { route ->
                    when (route) {
                        DJScreen.HOME.route -> currentScreen = DJScreen.HOME
                        DJScreen.MAP.route -> currentScreen = DJScreen.MAP
                        DJScreen.ADD.route -> onAddEvent()
                        DJScreen.CHAT.route -> currentScreen = DJScreen.CHAT
                        DJScreen.PROFILE.route -> onProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Animated content switching
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { if (targetState.ordinal > initialState.ordinal) 300 else -300 }
                            ) togetherWith
                            fadeOut(animationSpec = tween(300)) +
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -300 else 300 }
                            )
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    DJScreen.HOME -> {
                        EventsMainScreen(
                            viewModel = viewModel,
                            onLogout = onLogout,
                            onAddEvent = onAddEvent,
                            onProfile = onProfile,
                            showOnlyList = true
                        )
                    }
                    DJScreen.MAP -> {
                        EventsMainScreen(
                            viewModel = viewModel,
                            onLogout = onLogout,
                            onAddEvent = onAddEvent,
                            onProfile = onProfile,
                            showOnlyMap = true
                        )
                    }
                    DJScreen.CHAT -> {
                        ChatPlaceholderScreen()
                    }
                    else -> {
                        // Profile se maneja con navegación externa
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPlaceholderScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.djeventhub.ui.theme.DeepBlack
                )
            )
        },
        containerColor = com.example.djeventhub.ui.theme.DeepBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = com.example.djeventhub.ui.theme.TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chat próximamente",
                    style = MaterialTheme.typography.titleLarge,
                    color = com.example.djeventhub.ui.theme.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conecta con productoras y organiza tus eventos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.djeventhub.ui.theme.TextTertiary
                )
            }
        }
    }
}
