package com.example.djeventhub.ui.dj

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    // Flag to show edit profile inside PROFILE tab
    var showEditProfile by remember { mutableStateOf(false) }

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
            selectedIcon = Icons.Filled.Email,
            unselectedIcon = Icons.Outlined.Email,
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
                        DJScreen.HOME.route -> { currentScreen = DJScreen.HOME; showEditProfile = false }
                        DJScreen.MAP.route -> { currentScreen = DJScreen.MAP; showEditProfile = false }
                        DJScreen.ADD.route -> onAddEvent()
                        DJScreen.CHAT.route -> { currentScreen = DJScreen.CHAT; showEditProfile = false }
                        DJScreen.PROFILE.route -> { currentScreen = DJScreen.PROFILE; /* keep showEditProfile as is */ }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Animated content switching
            AnimatedContent(
                targetState = Pair(currentScreen, showEditProfile),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { 300 }
                            ) togetherWith
                            fadeOut(animationSpec = tween(300)) +
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { -300 }
                            )
                },
                label = "screenTransition"
            ) { (screen, editing) ->
                when (screen) {
                    DJScreen.HOME -> {
                        EventsMainScreen(
                            viewModel = viewModel,
                            onLogout = onLogout,
                            onAddEvent = onAddEvent,
                            onProfile = null, // Profile via bottom bar
                            showOnlyList = true
                        )
                    }
                    DJScreen.MAP -> {
                        EventsMainScreen(
                            viewModel = viewModel,
                            onLogout = onLogout,
                            onAddEvent = onAddEvent,
                            onProfile = null, // Profile via bottom bar
                            showOnlyMap = true
                        )
                    }
                    DJScreen.CHAT -> {
                        com.example.djeventhub.ui.chat.ChatListScreen(
                            onNavigateBack = { currentScreen = DJScreen.HOME },
                            onChatClick = { /* Navigate to chat detail if needed */ }
                        )
                    }
                    DJScreen.PROFILE -> {
                        if (editing) {
                            com.example.djeventhub.ui.dj.profile.EditDJProfileScreen(
                                onNavigateBack = { showEditProfile = false }
                            )
                        } else {
                            com.example.djeventhub.ui.dj.profile.DJProfileScreen(
                                onNavigateBack = { currentScreen = DJScreen.HOME },
                                onEdit = { showEditProfile = true },
                                showTopBar = true
                            )
                        }
                    }
                    DJScreen.ADD -> {
                        // Trigger add-event then go back to HOME
                        LaunchedEffect(Unit) {
                            onAddEvent()
                            currentScreen = DJScreen.HOME
                        }
                        Spacer(modifier = Modifier.height(0.dp))
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
                    imageVector = Icons.Outlined.Email,
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