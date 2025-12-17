package com.example.djeventhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.djeventhub.ui.theme.DeepBlack

/**
 * Example screen demonstrating the usage of reusable components
 * This is a guide for developers on how to use the new component system
 */
@Composable
fun ComponentsExampleScreen(
    onNavigateBack: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Components Example",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DeepBlack)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Text Fields
            SectionHeader(
                text = "Text Fields",
                icon = Icons.Default.Edit
            )

            DJTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = "Standard Text Field",
                placeholder = "Enter text here",
                supportingText = "This is a supporting text"
            )

            DJTextField(
                value = "",
                onValueChange = {},
                label = "Error Text Field",
                isError = true,
                errorMessage = "This field has an error"
            )

            DJPasswordField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = "Password Field"
            )

            GradientDivider()

            // Section: Buttons
            SectionHeader(
                text = "Buttons",
                icon = Icons.Default.Info
            )

            DJPrimaryButton(
                text = "Primary Button",
                onClick = { },
                icon = Icons.Default.Check
            )

            DJPrimaryButton(
                text = "Loading Button",
                onClick = { },
                isLoading = true
            )

            DJSecondaryButton(
                text = "Secondary Button",
                onClick = { },
                icon = Icons.Default.Star
            )

            DJSecondaryButton(
                text = "Disabled Button",
                onClick = { },
                enabled = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DJTertiaryButton(
                    text = "Tertiary Button",
                    onClick = { }
                )
            }

            GradientDivider()

            // Section: Badges
            SectionHeader(
                text = "Badges & Labels",
                icon = Icons.Default.Info
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(text = "Badge 1")
                Badge(
                    text = "Custom Badge",
                    backgroundColor = com.example.djeventhub.ui.theme.ElectricBlue.copy(alpha = 0.2f),
                    textColor = com.example.djeventhub.ui.theme.ElectricBlue
                )
                PulsingBadge(text = "Pulsing!")
            }

            GradientDivider()

            // Section: Info Cards
            SectionHeader(
                text = "Info Cards",
                icon = Icons.Default.Info
            )

            InfoCard(
                icon = Icons.Default.LocationOn,
                title = "Location",
                content = "Madrid, Spain"
            )

            InfoCard(
                icon = Icons.Default.Warning,
                content = "This is a warning message",
                backgroundColor = com.example.djeventhub.ui.theme.WarningOrange.copy(alpha = 0.1f),
                iconTint = com.example.djeventhub.ui.theme.WarningOrange
            )

            InfoCard(
                icon = Icons.Default.CheckCircle,
                title = "Success",
                content = "Operation completed successfully",
                hasGradientBorder = true,
                backgroundColor = com.example.djeventhub.ui.theme.SuccessGreen.copy(alpha = 0.1f),
                iconTint = com.example.djeventhub.ui.theme.SuccessGreen
            )

            GradientDivider()

            // Section: Empty State
            SectionHeader(
                text = "Empty State",
                icon = Icons.Default.Info
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.djeventhub.ui.theme.DarkSurface
                )
            ) {
                EmptyState(
                    icon = Icons.Default.Info,
                    title = "No Events",
                    description = "You don't have any events yet. Create your first event to get started!",
                    actionText = "Create Event",
                    onActionClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * USAGE EXAMPLE: Refactored Login Screen
 *
 * This shows how to use the new components in a real screen
 */
@Composable
fun ExampleLoginScreenRefactored(
    onAuthenticated: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val emailError = if (email.isNotEmpty() && !email.contains("@")) {
        "Formato inválido"
    } else null

    val passwordError = if (password.isNotEmpty() && password.length < 6) {
        "Mínimo 6 caracteres"
    } else null

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo and title (keep existing design)
                // ...

                Spacer(modifier = Modifier.height(32.dp))

                // Using new components
                DJTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    isError = emailError != null,
                    errorMessage = emailError,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                DJPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                DJPrimaryButton(
                    text = "Iniciar Sesión",
                    onClick = {
                        isLoading = true
                        // Handle login
                    },
                    isLoading = isLoading,
                    enabled = email.isNotEmpty() && password.isNotEmpty() &&
                              emailError == null && passwordError == null
                )

                Spacer(modifier = Modifier.height(12.dp))

                DJSecondaryButton(
                    text = "Continuar con Google",
                    onClick = { /* Handle Google sign-in */ },
                    icon = Icons.Default.Email,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                DJTertiaryButton(
                    text = "¿Nuevo? Regístrate",
                    onClick = { /* Navigate to register */ },
                    enabled = !isLoading
                )
            }

            // Loading overlay
            LoadingOverlay(isLoading = isLoading)
        }
    }
}

/**
 * USAGE EXAMPLE: Event List with new EventCard
 */
@Composable
fun ExampleEventListWithNewCards() {
    // This shows how to use the EventCard component
    // Replace the old EventListItem with EventCard:
    /*
    LazyColumn {
        itemsIndexed(events) { index, eventWithDistance ->
            EventCard(
                eventWithDistance = eventWithDistance,
                onEventClick = { eventId ->
                    // Navigate to event detail
                },
                onMapClick = { event ->
                    // Open map
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateItemPlacement(index)
            )
        }
    }
    */
}

/**
 * USAGE EXAMPLE: Add Event Screen with new components
 */
@Composable
fun ExampleAddEventWithNewComponents() {
    // Replace OutlinedTextField with DJTextField
    // Replace Button with DJPrimaryButton
    // Replace OutlinedButton with DJSecondaryButton

    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DJTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = "Nombre del evento",
            placeholder = "Ej: Fiesta de Verano"
        )

        DJTextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = "Descripción",
            placeholder = "Describe tu evento...",
            singleLine = false,
            maxLines = 5,
            modifier = Modifier.height(140.dp)
        )

        InfoCard(
            icon = Icons.Default.Info,
            content = "Asegúrate de proporcionar una descripción clara del evento"
        )

        DJPrimaryButton(
            text = "Crear Evento",
            onClick = { /* Handle create */ },
            icon = Icons.Default.Add
        )
    }
}
