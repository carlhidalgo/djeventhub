package com.example.djeventhub.ui.dj.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.djeventhub.models.User
import com.example.djeventhub.ui.animations.*
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DJProfileScreen(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DJProfileViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    // Image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    val themeController = LocalThemeController.current

    Scaffold(
        topBar = {
            if (showTopBar) {
                SmallTopAppBar(
                    title = { Text("Mi Perfil DJ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
                    actions = {
                        IconButton(onClick = { themeController.toggle() }) {
                            Icon(imageVector = if (themeController.isDark) Icons.Default.Star else Icons.Default.Edit, contentDescription = "Alternar tema")
                        }
                        if (uiState is DJProfileUiState.Success) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NeonPink)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                )
            }
        },
        containerColor = DeepBlack
    ) { padding ->
        when (val state = uiState) {
            is DJProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPink)
                }
            }

            is DJProfileUiState.Error -> {
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

            is DJProfileUiState.Success -> {
                DJProfileContent(
                    user = state.user,
                    onPickImage = { launcher.launch("image/*") },
                    onLogout = onLogout,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DJProfileContent(
    user: User,
    onPickImage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DJProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header with avatar - animated entrance
        Box(modifier = Modifier.fadeInWithDelay(0)) {
            ProfileHeader(user, onPickImage)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Availability toggle - prominent position
        Box(modifier = Modifier.fadeInWithDelay(50)) {
            AvailabilityToggleCard(
                isAvailable = user.isAvailable,
                onToggle = { viewModel.toggleAvailability(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rating section - always show even if 0
        Box(modifier = Modifier.fadeInWithDelay(100)) {
            RatingSection(rating = user.rating, totalRatings = user.totalRatings)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bio section - show placeholder if empty
        if (!user.bio.isNullOrBlank()) {
            InfoCard(title = "Biografía", content = user.bio)
        } else {
            PlaceholderCard(title = "Biografía", message = "Añade una biografía desde Editar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Music genres - show placeholder if empty
        if (user.musicGenres.isNotEmpty()) {
            MusicGenresCard(genres = user.musicGenres)
        } else {
            PlaceholderCard(title = "Géneros Musicales", message = "Selecciona tus géneros desde Editar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Availability - show placeholder if empty
        if (user.availableDays.isNotEmpty()) {
            AvailabilityCard(days = user.availableDays)
        } else {
            PlaceholderCard(title = "Disponibilidad", message = "Indica tus días disponibles desde Editar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Stats - always show
        StatsCard(eventsCompleted = user.eventsCompleted)

        Spacer(modifier = Modifier.height(16.dp))

        // Contact info - show placeholder if empty
        if (!user.phone.isNullOrBlank() || !user.location.isNullOrBlank()) {
            ContactCard(phone = user.phone, location = user.location)
        } else {
            PlaceholderCard(title = "Contacto", message = "Añade tu teléfono y ubicación desde Editar")
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
fun ProfileHeader(user: User, onPickImage: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with neon glow - click to change photo
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.4f),
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
                                colors = listOf(NeonPink, NeonPurple)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user.artistName ?: user.displayName).firstOrNull()?.uppercase() ?: "DJ",
                        style = MaterialTheme.typography.displayLarge,
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.artistName ?: user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (user.artistName != null && user.displayName != user.artistName) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RatingSection(rating: Double, totalRatings: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star rating with staggered animation
            repeat(5) { index ->
                val scale = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 50L)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating) NeonPink else TextTertiary,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(scale.value)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%.1f", rating),
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$totalRatings calificaciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NeonPink,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicGenresCard(genres: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Géneros Musicales",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { genre ->
                    GenreChip(genre)
                }
            }
        }
    }
}

@Composable
fun GenreChip(genre: String) {
    Box(
        modifier = Modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        NeonPink.copy(alpha = 0.2f),
                        NeonPurple.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = genre,
            style = MaterialTheme.typography.labelMedium,
            color = NeonPink,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvailabilityCard(days: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Disponibilidad",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    DayChip(day)
                }
            }
        }
    }
}

@Composable
fun DayChip(day: String) {
    Box(
        modifier = Modifier
            .background(
                color = ElectricBlue.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelMedium,
            color = ElectricBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatsCard(eventsCompleted: Int) {
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
            StatItem(label = "Eventos Completados", value = eventsCompleted.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = NeonCyan,
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
fun ContactCard(phone: String?, location: String?) {
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
fun PlaceholderCard(title: String, message: String) {
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

@Composable
fun AvailabilityToggleCard(isAvailable: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) 
                NeonPink.copy(alpha = 0.15f) 
            else 
                DarkSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isAvailable) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Disponibilidad",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isAvailable) NeonPink else TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isAvailable) 
                        "Visible para productoras" 
                    else 
                        "No visible para productoras",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Switch(
                checked = isAvailable,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonPink,
                    checkedTrackColor = NeonPink.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}