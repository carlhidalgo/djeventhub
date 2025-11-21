package com.example.djeventhub.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.djeventhub.EventListViewModel
import com.example.djeventhub.EventRepository
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.location.LocationManager
import com.example.djeventhub.models.UserType
import com.example.djeventhub.ui.auth.AuthViewModel
import com.example.djeventhub.ui.auth.LoginScreen
import com.example.djeventhub.ui.dj.DJHomeScreen
import com.example.djeventhub.ui.productora.ProductoraHomeScreen
import com.example.djeventhub.ui.roleselection.RoleSelectionScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object RoleSelection : Screen("role_selection")
    object DJHome : Screen("dj_home")
    object ProductoraHome : Screen("productora_home")
    object AddEvent : Screen("add_event")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object DJProfile : Screen("dj_profile")
    object EditDJProfile : Screen("edit_dj_profile")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{chatId}/{otherUserName}") {
        fun createRoute(chatId: String, otherUserName: String) = "chat/$chatId/$otherUserName"
    }
    object ApplicationsList : Screen("applications/{eventId}/{eventName}") {
        fun createRoute(eventId: String, eventName: String) = "applications/$eventId/$eventName"
    }
    object PublicDJProfile : Screen("public_dj_profile/{djId}") {
        fun createRoute(djId: String) = "public_dj_profile/$djId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    locationManager: LocationManager
) {
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Determine start destination based on auth and profile status
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val storage = FirebaseStorage.getInstance()
        val userRepository = UserRepository(firestore, auth, storage)
        val currentUser = auth.currentUser
        startDestination = if (currentUser == null) {
            Screen.Login.route
        } else {
            // Check if user has a profile with role
            val userProfile = userRepository.getCurrentUserProfile()
            when {
                userProfile == null || userProfile.userType == null -> Screen.RoleSelection.route
                userProfile.userType == UserType.DJ -> Screen.DJHome.route
                userProfile.userType == UserType.PRODUCTORA -> Screen.ProductoraHome.route
                else -> Screen.Login.route
            }
        }
    }

    if (startDestination == null) {
        // Show loading while determining start destination
        androidx.compose.material3.CircularProgressIndicator()
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = { _ ->
                    // After login, poll for the user profile to avoid racing with Firestore writes
                    scope.launch {
                        val firestore = FirebaseFirestore.getInstance()
                        val auth = FirebaseAuth.getInstance()
                        val storage = FirebaseStorage.getInstance()
                        val userRepository = UserRepository(firestore, auth, storage)
                        var userProfile = userRepository.getCurrentUserProfile()
                        var attempts = 0
                        // Retry a few times while waiting for profile to be available (e.g., after sign-up)
                        while ((userProfile == null || userProfile.userType == null) && attempts < 8) {
                            attempts++
                            kotlinx.coroutines.delay(500L)
                            userProfile = userRepository.getCurrentUserProfile()
                        }

                        val destination = when {
                            userProfile == null || userProfile.userType == null -> Screen.RoleSelection.route
                            userProfile.userType == UserType.DJ -> Screen.DJHome.route
                            userProfile.userType == UserType.PRODUCTORA -> Screen.ProductoraHome.route
                            else -> Screen.Login.route
                        }

                        // Avoid navigating if already at the desired destination (prevents double navigation)
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != destination) {
                            navController.navigate(destination) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                     }
                 },
                 viewModel = authViewModel
             )
         }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = {
                    // After selecting role, navigate to appropriate home
                    scope.launch {
                        val firestore = FirebaseFirestore.getInstance()
                        val auth = FirebaseAuth.getInstance()
                        val storage = FirebaseStorage.getInstance()
                        val userRepository = UserRepository(firestore, auth, storage)
                        val userProfile = userRepository.getCurrentUserProfile()
                        val destination = when (userProfile?.userType) {
                            UserType.DJ -> Screen.DJHome.route
                            UserType.PRODUCTORA -> Screen.ProductoraHome.route
                            else -> Screen.Login.route
                        }
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != destination) {
                            navController.navigate(destination) {
                                popUpTo(Screen.RoleSelection.route) { inclusive = true }
                            }
                        }
                     }
                 }
             )
         }

        composable(Screen.DJHome.route) {
            val eventViewModel: EventListViewModel = hiltViewModel()
            DJHomeScreen(
                viewModel = eventViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAddEvent = {
                    navController.navigate(Screen.AddEvent.route)
                },
                onProfile = {
                    navController.navigate(Screen.DJProfile.route)
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(Screen.ProductoraHome.route) {
            val eventViewModel: EventListViewModel = hiltViewModel()
            ProductoraHomeScreen(
                eventViewModel = eventViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSearchDJs = {
                    // TODO: Navigate to search DJs screen
                },
                onProfile = {
                    // TODO: Navigate to profile screen
                },
                onAddEvent = {
                    navController.navigate(Screen.AddEvent.route)
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onViewApplicants = { eventId, eventName ->
                    navController.navigate(Screen.ApplicationsList.createRoute(eventId, eventName))
                }
            )
        }

        composable(Screen.AddEvent.route) {
            com.example.djeventhub.ui.addevent.AddEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val repository = remember { EventRepository() }
            var event by remember { mutableStateOf<com.example.djeventhub.Event?>(null) }

            LaunchedEffect(eventId) {
                event = repository.getEventById(eventId)
            }

            event?.let {
                com.example.djeventhub.ui.events.EventDetailScreen(
                    event = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onApplyToEvent = { eventId ->
                        scope.launch {
                            val result = repository.applyToEvent(eventId)
                            result.fold(
                                onSuccess = {
                                    // Refresh event to show updated state
                                    event = repository.getEventById(eventId)
                                },
                                onFailure = { e ->
                                    // Show error - could use SnackBar
                                    android.util.Log.e("AppNavigation", "Error applying to event: ${e.message}")
                                }
                            )
                        }
                    }
                )
            } ?: run {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }

        composable(Screen.DJProfile.route) {
            com.example.djeventhub.ui.dj.profile.DJProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate(Screen.EditDJProfile.route)
                }
            )
        }

        composable(Screen.EditDJProfile.route) {
            com.example.djeventhub.ui.dj.profile.EditDJProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChatList.route) {
            com.example.djeventhub.ui.chat.ChatListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChatClick = { chatId ->
                    // Navigate with chatId - we'll get the name in the screen
                    navController.navigate(Screen.Chat.createRoute(chatId, "Chat"))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                androidx.navigation.navArgument("chatId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("otherUserName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: "Chat"

            com.example.djeventhub.ui.chat.ChatScreen(
                chatId = chatId,
                otherUserName = otherUserName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ApplicationsList.route,
            arguments = listOf(
                androidx.navigation.navArgument("eventId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("eventName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val eventName = backStackEntry.arguments?.getString("eventName") ?: "Evento"

            com.example.djeventhub.ui.productora.ApplicationsListScreen(
                eventId = eventId,
                eventName = eventName,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewDJProfile = { djId ->
                    navController.navigate(Screen.PublicDJProfile.createRoute(djId))
                },
                onOpenChat = { chatId, otherName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherName))
                },
                onAcceptApplicant = { djId ->
                    // accept application via repository using the existing scope
                    scope.launch {
                        val repo = EventRepository()
                        repo.acceptApplication(eventId, djId)
                    }
                }
            )
        }

        composable(
            route = Screen.PublicDJProfile.route,
            arguments = listOf(
                androidx.navigation.navArgument("djId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val djId = backStackEntry.arguments?.getString("djId") ?: ""

            com.example.djeventhub.ui.dj.profile.PublicDJProfileScreen(
                djId = djId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactDJ = {
                    // TODO: Navigate to chat with DJ
                }
            )
        }
    }
}