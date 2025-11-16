package com.example.djeventhub.ui.productora

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
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.ui.events.EventsMainScreen
import com.example.djeventhub.ui.navigation.BottomNavItem
import com.example.djeventhub.ui.navigation.InstagramBottomBar

enum class ProductoraScreen(val route: String) {
    HOME("productora_home"),
    SEARCH("productora_search"),
    ADD("productora_add"),
    CHAT("productora_chat"),
    PROFILE("productora_profile")
}

@Composable
fun ProductoraMainScreen(
    viewModel: EventListViewModel,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onSearchDJs: () -> Unit,
    onAddEvent: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(ProductoraScreen.HOME) }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = ProductoraScreen.HOME.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Inicio"
        ),
        BottomNavItem(
            route = ProductoraScreen.SEARCH.route,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            label = "Buscar DJs"
        ),
        BottomNavItem(
            route = ProductoraScreen.ADD.route,
            selectedIcon = Icons.Filled.Add,
            unselectedIcon = Icons.Filled.Add,
            label = "Crear Evento",
            isCenterItem = true
        ),
        BottomNavItem(
            route = ProductoraScreen.CHAT.route,
            selectedIcon = Icons.Filled.Chat,
            unselectedIcon = Icons.Outlined.Chat,
            label = "Chat"
        ),
        BottomNavItem(
            route = ProductoraScreen.PROFILE.route,
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
                        ProductoraScreen.HOME.route -> currentScreen = ProductoraScreen.HOME
                        ProductoraScreen.SEARCH.route -> currentScreen = ProductoraScreen.SEARCH
                        ProductoraScreen.ADD.route -> onAddEvent()
                        ProductoraScreen.CHAT.route -> currentScreen = ProductoraScreen.CHAT
                        ProductoraScreen.PROFILE.route -> onProfile()
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
                    ProductoraScreen.HOME -> {
                        EventsMainScreen(
                            viewModel = viewModel,
                            onLogout = onLogout,
                            onAddEvent = onAddEvent,
                            onProfile = onProfile,
                            showOnlyList = true
                        )
                    }
                    ProductoraScreen.SEARCH -> {
                        SearchDJsPlaceholderScreen(onSearch = onSearchDJs)
                    }
                    ProductoraScreen.CHAT -> {
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
fun SearchDJsPlaceholderScreen(onSearch: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar DJs") },
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
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = com.example.djeventhub.ui.theme.NeonPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Encuentra DJs profesionales",
                    style = MaterialTheme.typography.titleLarge,
                    color = com.example.djeventhub.ui.theme.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Busca por género musical, ubicación y disponibilidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.djeventhub.ui.theme.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSearch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.djeventhub.ui.theme.NeonPurple
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar búsqueda")
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
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    text = "Conecta con DJs y coordina tus eventos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.djeventhub.ui.theme.TextTertiary
                )
            }
        }
    }
}
