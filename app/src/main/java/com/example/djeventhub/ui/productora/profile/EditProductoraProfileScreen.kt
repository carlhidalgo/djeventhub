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
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductoraProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductoraProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is ProductoraProfileUiState.Success) {
            val user = (uiState as ProductoraProfileUiState.Success).user
            companyName = user.companyName ?: ""
            description = user.description ?: ""
            phone = user.phone ?: ""
            location = user.location ?: ""
        }
    }

    var lastState by remember { mutableStateOf<ProductoraProfileUiState?>(null) }
    var saveRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (lastState is ProductoraProfileUiState.Loading && uiState is ProductoraProfileUiState.Success && saveRequested) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            onNavigateBack()
        }
        lastState = uiState
    }

    // Validations
    val companyNameError = remember(companyName) {
        when {
            companyName.isBlank() -> "Requerido"
            companyName.length < 2 -> "Mínimo 2 caracteres"
            companyName.length > 60 -> "Máximo 60"
            else -> null
        }
    }
    val descriptionError = remember(description) {
        if (description.length > 500) "Máximo 500 caracteres" else null
    }
    val phoneError = remember(phone) {
        if (phone.isNotBlank() && !phone.matches("^[+0-9]{7,15}$".toRegex())) "Teléfono inválido" else null
    }
    val locationError = remember(location) {
        if (location.length > 60) "Máximo 60" else null
    }
    val isFormValid = companyNameError == null && descriptionError == null && phoneError == null && locationError == null

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
                actions = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                saveRequested = true
                                viewModel.updateProfile(
                                    companyName = companyName,
                                    description = description,
                                    phone = phone.ifBlank { null },
                                    location = location.ifBlank { null }
                                )
                            }
                        },
                        enabled = uiState !is ProductoraProfileUiState.Loading && isFormValid
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = if (isFormValid) NeonPink else TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
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
            val user = (uiState as? ProductoraProfileUiState.Success)?.user
            if (user != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto de Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para cambiar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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
                                            colors = listOf(NeonPurple, ElectricBlue)
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

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Nombre de la Productora") },
                supportingText = { if (companyNameError != null) Text(companyNameError, color = ErrorRed) },
                isError = companyNameError != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    focusedLabelColor = NeonPurple,
                    cursorColor = NeonPurple
                ),
                enabled = uiState !is ProductoraProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                supportingText = { if (descriptionError != null) Text(descriptionError, color = ErrorRed) else Text("${description.length}/500") },
                isError = descriptionError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    focusedLabelColor = NeonPurple,
                    cursorColor = NeonPurple
                ),
                enabled = uiState !is ProductoraProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                enabled = uiState !is ProductoraProfileUiState.Loading
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
                enabled = uiState !is ProductoraProfileUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is ProductoraProfileUiState.Error) {
                Text(
                    text = (uiState as ProductoraProfileUiState.Error).message,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState is ProductoraProfileUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonPurple
                )
            }
        }
    }
}
