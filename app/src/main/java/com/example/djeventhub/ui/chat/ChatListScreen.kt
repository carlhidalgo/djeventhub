package com.example.djeventhub.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.djeventhub.models.Chat
import com.example.djeventhub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit,
    onChatClick: (String) -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack
                )
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        when (val state = uiState) {
            is ChatListUiState.Loading -> {
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
                if (state.chats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes conversaciones",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(state.chats) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClick(chat.chatId) }
                            )
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
