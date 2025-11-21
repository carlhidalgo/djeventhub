package com.example.djeventhub.ui.productora.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.djeventhub.models.User
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoraProfileScreen(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProductoraProfileViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mi Perfil",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        if (uiState is ProductoraProfileUiState.Success) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = NeonPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DeepBlack
                    ),
                    modifier = Modifier.height(56.dp)
                )
            }
        },
        containerColor = DeepBlack
    ) { padding ->
        when (val state = uiState) {
            is ProductoraProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPink)
                }
            }

            is ProductoraProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is ProductoraProfileUiState.Success -> {
                ProductoraProfileContent(
                    user = state.user,
                    onPickImage = { launcher.launch("image/*") },
                    onLogout = onLogout,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun ProductoraProfileContent(
    user: User,
    onPickImage: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header with avatar
        ProductoraProfileHeader(user, onPickImage)

        Spacer(modifier = Modifier.height(24.dp))

        // Company description
        if (!user.description.isNullOrBlank()) {
            ProductoraInfoCard(title = "Acerca de", content = user.description)
        } else {
            ProductoraPlaceholderCard(title = "Acerca de", message = "Añade una descripción de tu productora")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        ProductoraStatsCard(eventsOrganized = user.eventsOrganized)

        Spacer(modifier = Modifier.height(16.dp))

        // Contact info
        if (!user.phone.isNullOrBlank() || !user.location.isNullOrBlank()) {
            ProductoraContactCard(phone = user.phone, location = user.location)
        } else {
            ProductoraPlaceholderCard(title = "Contacto", message = "Añade tu información de contacto")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ErrorRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Cerrar sesión",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar sesión",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProductoraProfileHeader(user: User, onPickImage: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.4f),
                            androidx.compose.ui.graphics.Color.Transparent
                        ),
                        radius = 200f
                    ),
                    shape = CircleShape
                )
                .clickable { onPickImage() }
        ) {
            if (!user.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
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
                        .size(120.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(NeonPurple, ElectricBlue)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = DeepBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.companyName ?: user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (user.companyName != null && user.displayName != user.companyName) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ProductoraInfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun ProductoraStatsCard(eventsOrganized: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProductoraStatItem(label = "Eventos Organizados", value = eventsOrganized.toString())
        }
    }
}

@Composable
fun ProductoraStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = NeonPurple,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ProductoraContactCard(phone: String?, location: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Contacto",
                style = MaterialTheme.typography.titleMedium,
                color = NeonOrange,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (!phone.isNullOrBlank()) {
                Text(
                    text = "📱 $phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            if (!location.isNullOrBlank()) {
                if (!phone.isNullOrBlank()) Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 $location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun ProductoraPlaceholderCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}