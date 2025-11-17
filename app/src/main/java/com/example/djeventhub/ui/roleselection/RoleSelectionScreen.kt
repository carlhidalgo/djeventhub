package com.example.djeventhub.ui.roleselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.djeventhub.R
import com.example.djeventhub.models.UserType

@Composable
fun RoleSelectionScreen(
    onRoleSelected: () -> Unit,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRole by remember { mutableStateOf<UserType?>(null) }

    // Navigate when profile is created
    LaunchedEffect(uiState) {
        if (uiState is RoleSelectionUiState.Success) {
            onRoleSelected()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Bienvenido a DJ Event Hub!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona tu tipo de cuenta",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // DJ Card
        RoleCard(
            title = "DJ",
            description = "Gestiona tus eventos, aumenta tu visibilidad y conecta con productoras",
            isSelected = selectedRole == UserType.DJ,
            onClick = { selectedRole = UserType.DJ },
            enabled = uiState !is RoleSelectionUiState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Productora Card
        RoleCard(
            title = "Productora",
            description = "Encuentra y contrata DJs, gestiona eventos y califica servicios",
            isSelected = selectedRole == UserType.PRODUCTORA,
            onClick = { selectedRole = UserType.PRODUCTORA },
            enabled = uiState !is RoleSelectionUiState.Loading
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (uiState is RoleSelectionUiState.Error) {
            Text(
                text = (uiState as RoleSelectionUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Loading indicator
        if (uiState is RoleSelectionUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }

        // Continue button
        Button(
            onClick = {
                selectedRole?.let { role ->
                    viewModel.createUserProfile(role)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = selectedRole != null && uiState !is RoleSelectionUiState.Loading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else null,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
