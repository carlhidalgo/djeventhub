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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.ui.events.EventListViewModel
import com.example.djeventhub.ui.events.EventsMainScreen
import com.example.djeventhub.ui.navigation.BottomNavItem
import com.example.djeventhub.ui.navigation.InstagramBottomBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class DJScreen(val route: String) {
    HOME("dj_home"),
    MY_EVENTS("dj_my_events"),
    SEARCH("dj_search"),
    CHAT("dj_chat"),
    PROFILE("dj_profile")
}

@Composable
fun DJMainScreen(
    viewModel: EventListViewModel,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onAddEvent: () -> Unit,
    onEventClick: (String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(DJScreen.HOME) }
    // Flag to show edit profile inside PROFILE tab
    var showEditProfile by remember { mutableStateOf(false) }
    var openedChatId by remember { mutableStateOf<String?>(null) }
    var openedChatUserName by remember { mutableStateOf<String?>(null) }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = DJScreen.HOME.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = "Inicio"
        ),
        BottomNavItem(
            route = DJScreen.MY_EVENTS.route,
            selectedIcon = Icons.Filled.Star,
            unselectedIcon = Icons.Outlined.Star,
            label = "Mis Eventos"
        ),
        BottomNavItem(
            route = DJScreen.SEARCH.route,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            label = "Buscar",
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
                        DJScreen.MY_EVENTS.route -> { currentScreen = DJScreen.MY_EVENTS; showEditProfile = false }
                        DJScreen.SEARCH.route -> { currentScreen = DJScreen.SEARCH; showEditProfile = false }
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
                            onAddEvent = onAddEvent,
                            onProfile = null, // Profile via bottom bar
                            showOnlyList = true,
                            onEventClick = onEventClick
                        )
                    }
                    DJScreen.MY_EVENTS -> {
                        // Show events where the DJ has applied
                        MyDJEventsScreen(
                            viewModel = viewModel,
                            onEventClick = onEventClick
                        )
                    }
                    DJScreen.SEARCH -> {
                        // Search events screen
                        SearchEventsScreen(
                            viewModel = viewModel,
                            onEventClick = onEventClick
                        )
                    }
                    DJScreen.CHAT -> {
                        if (openedChatId != null && openedChatUserName != null) {
                            // Show chat detail
                            com.example.djeventhub.ui.chat.ChatScreen(
                                chatId = openedChatId!!,
                                otherUserName = openedChatUserName!!,
                                onNavigateBack = {
                                    openedChatId = null
                                    openedChatUserName = null
                                }
                            )
                        } else {
                            // Show chat list
                            com.example.djeventhub.ui.chat.ChatListScreen(
                                onNavigateBack = null,
                                onChatClick = { chatId ->
                                    // Get chat details to extract other user name
                                    viewModel.viewModelScope.launch {
                                        try {
                                            val chat = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("chats")
                                                .document(chatId)
                                                .get()
                                                .await()
                                                .toObject(com.example.djeventhub.models.Chat::class.java)

                                            if (chat != null) {
                                                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                                openedChatId = chatId
                                                openedChatUserName = chat.getOtherParticipantName(currentUserId)
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("DJMainScreen", "Error loading chat", e)
                                        }
                                    }
                                }
                            )
                        }
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
                                onLogout = onLogout,
                                showTopBar = true
                            )
                        }
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
                title = { Text("Mensajes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = com.example.djeventhub.ui.theme.TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.djeventhub.ui.theme.DeepBlack),
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
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