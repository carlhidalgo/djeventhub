package com.example.djeventhub.ui.dj.profile

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
import androidx.compose.material.icons.filled.Check
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
import com.example.djeventhub.models.MusicGenres
import com.example.djeventhub.models.WeekDays
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditDJProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: DJProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsState()

    // Form state
    var artistName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedDays by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    // Initialize with current user data
    LaunchedEffect(uiState) {
        if (uiState is DJProfileUiState.Success) {
            val user = (uiState as DJProfileUiState.Success).user
            artistName = user.artistName ?: ""
            bio = user.bio ?: ""
            phone = user.phone ?: ""
            location = user.location ?: ""
            selectedGenres = user.musicGenres.toSet()
            selectedDays = user.availableDays.toSet()
        }
    }

    // Navigate back on success after save
    var lastState by remember { mutableStateOf<DJProfileUiState?>(null) }
    var saveRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (lastState is DJProfileUiState.Loading && uiState is DJProfileUiState.Success && saveRequested) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            onNavigateBack()
        }
        lastState = uiState
    }

    // Validations
    val artistNameError = remember(artistName) {
        when {
            artistName.isBlank() -> "Requerido"
            artistName.length < 2 -> "Mínimo 2 caracteres"
            artistName.length > 40 -> "Máximo 40"
            else -> null
        }
    }
    val bioError = remember(bio) {
        if (bio.length > 300) "Máximo 300 caracteres" else null
    }
    val phoneError = remember(phone) {
        if (phone.isNotBlank() && !phone.matches("^[+0-9]{7,15}$".toRegex())) "Teléfono inválido" else null
    }
    val locationError = remember(location) {
        if (location.length > 60) "Máximo 60" else null
    }
    val isFormValid = artistNameError == null && bioError == null && phoneError == null && locationError == null

    // Show snackbars for messages
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                saveRequested = true
                                viewModel.updateProfile(
                                    artistName = artistName,
                                    bio = bio,
                                    musicGenres = selectedGenres.toList(),
                                    availableDays = selectedDays.toList(),
                                    phone = phone.ifBlank { null },
                                    location = location.ifBlank { null }
                                )
                            }
                        },
                        enabled = uiState !is DJProfileUiState.Loading && isFormValid
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = if (isFormValid) NeonPink else TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile photo section
            val user = (uiState as? DJProfileUiState.Success)?.user
            if (user != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto de Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonPink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para cambiar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar with camera icon overlay
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
                            .clickable {
                                launcher.launch("image/*")
                            }
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
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Subir foto",
                                    tint = DeepBlack,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Artist Name
            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                label = { Text("Nombre Artístico") },
                supportingText = { if (artistNameError != null) Text(artistNameError, color = ErrorRed) },
                isError = artistNameError != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    focusedLabelColor = NeonPink,
                    cursorColor = NeonPink
                ),
                enabled = uiState !is DJProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Biografía") },
                supportingText = { if (bioError != null) Text(bioError, color = ErrorRed) else Text("${bio.length}/300") },
                isError = bioError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    focusedLabelColor = NeonPink,
                    cursorColor = NeonPink
                ),
                enabled = uiState !is DJProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Music Genres Section
            Text(
                text = "Géneros Musicales",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona los géneros que mezclas",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MusicGenres.ALL_GENRES.forEach { genre ->
                    SelectableChip(
                        text = genre,
                        selected = selectedGenres.contains(genre),
                        onClick = {
                            selectedGenres = if (selectedGenres.contains(genre)) {
                                selectedGenres - genre
                            } else {
                                selectedGenres + genre
                            }
                        },
                        enabled = uiState !is DJProfileUiState.Loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Availability Section
            Text(
                text = "Disponibilidad",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿Qué días estás disponible?",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeekDays.ALL_DAYS.forEach { day ->
                    SelectableChip(
                        text = day,
                        selected = selectedDays.contains(day),
                        onClick = {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        },
                        color = ElectricBlue,
                        enabled = uiState !is DJProfileUiState.Loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Section
            Text(
                text = "Información de Contacto",
                style = MaterialTheme.typography.titleMedium,
                color = NeonOrange,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono (opcional)") },
                supportingText = { if (phoneError != null) Text(phoneError, color = ErrorRed) },
                isError = phoneError != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonOrange,
                    focusedLabelColor = NeonOrange,
                    cursorColor = NeonOrange
                ),
                enabled = uiState !is DJProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación (opcional)") },
                supportingText = { if (locationError != null) Text(locationError, color = ErrorRed) },
                isError = locationError != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonOrange,
                    focusedLabelColor = NeonOrange,
                    cursorColor = NeonOrange
                ),
                enabled = uiState !is DJProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (uiState is DJProfileUiState.Error) {
                Text(
                    text = (uiState as DJProfileUiState.Error).message,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading indicator
            if (uiState is DJProfileUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonPink
                )
            }
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = NeonPink,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) color.copy(alpha = 0.3f) else color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}