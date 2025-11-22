package com.example.djeventhub.ui.chat

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.djeventhub.models.Chat
import com.example.djeventhub.ui.theme.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: (() -> Unit)? = null,
    onChatClick: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mensajes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (uiState is ChatListUiState.Success) {
                        val total = (uiState as ChatListUiState.Success).totalUnreadCount
                        if (total > 0) {
                            AssistChip(
                                onClick = {},
                                label = { Text("$total") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = NeonPink.copy(alpha = 0.2f),
                                    labelColor = NeonPink
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepBlack
                ),
                modifier = Modifier.height(56.dp)
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        when (val state = uiState) {
            is ChatListUiState.Loading -> {
                // This state is no longer used, but keep for compatibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPink)
                }
            }
            is ChatListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPink
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is ChatListUiState.Success -> {
                // Debug logging
                android.util.Log.d("ChatListScreen", "Success state: ${state.chats.size} chats, totalUnread=${state.totalUnreadCount}")
                val filtered = if (searchQuery.isBlank()) state.chats else state.chats.filter { chat ->
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    chat.getOtherParticipantName(currentUserId).contains(searchQuery, ignoreCase = true)
                }
                android.util.Log.d("ChatListScreen", "Filtered: ${filtered.size} chats")

                val swipeState = rememberSwipeRefreshState(state.isRefreshing)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            focusedLabelColor = NeonPink,
                            cursorColor = NeonPink
                        )
                    )

                    SwipeRefresh(
                        state = swipeState,
                        onRefresh = { viewModel.refresh() },
                        indicator = { s, trigger ->
                            SwipeRefreshIndicator(
                                state = s,
                                refreshTriggerDistance = trigger,
                                contentColor = NeonPink
                            )
                        }
                    ) {
                        // Show shimmer only on first load when list is empty and refreshing
                        if (state.chats.isEmpty() && state.isRefreshing) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(6) {
                                    ShimmerChatItem()
                                }
                            }
                        } else if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No tienes conversaciones" else "Sin resultados para '$searchQuery'",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filtered, key = { it.chatId }) { chat ->
                                    ChatListItem(chat = chat, onClick = { onChatClick(chat.chatId) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val unreadCount = chat.getUnreadCount(currentUserId)
    val otherUserName = chat.getOtherParticipantName(currentUserId)
    val otherUserImage = chat.getOtherParticipantImage(currentUserId)
    val isLastMessageFromMe = chat.lastMessageSenderId == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                NeonPink.copy(alpha = 0.3f),
                                androidx.compose.ui.graphics.Color.Transparent
                            ),
                            radius = 80f
                        ),
                        shape = CircleShape
                    )
            ) {
                if (!otherUserImage.isNullOrBlank()) {
                    AsyncImage(
                        model = otherUserImage,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape),
                        error = androidx.compose.ui.res.painterResource(
                            android.R.drawable.ic_menu_camera
                        ),
                        placeholder = androidx.compose.ui.res.painterResource(
                            android.R.drawable.ic_menu_gallery
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(NeonPink, NeonPurple)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = otherUserName.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (chat.lastMessageTimestamp != null) {
                        Text(
                            text = formatTimestamp(chat.lastMessageTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unreadCount > 0) NeonPink else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastMessageFromMe) "Tú: ${chat.lastMessage}" else chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (unreadCount > 0) TextPrimary else TextSecondary,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = NeonPink,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerChatItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar shimmer
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

// Simple shimmer modifier using alpha animation
@Composable
private fun Modifier.shimmer(): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    return this.background(DarkSurface.copy(alpha = alphaAnim))
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = timestamp }

    return when {
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(timestamp)
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(timestamp)
        }
    }
}