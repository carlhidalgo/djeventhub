package com.example.djeventhub.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.djeventhub.R
import com.example.djeventhub.ui.components.NeonLoadingIndicator

@Composable
fun LoginScreen(onAuthenticated: (String) -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    // Local state
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isRegisterMode = remember { mutableStateOf(false) }
    val showPassword = remember { mutableStateOf(false) }

    // Validation
    val emailError = remember(email.value) { validateEmail(email.value) }
    val passwordError = remember(password.value) { validatePassword(password.value) }
    val isFormValid = emailError == null && passwordError == null && email.value.isNotBlank() && password.value.isNotBlank()

    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.handleGoogleSignIn(idToken)
                } else {
                    viewModel.setError("No ID token from Google Sign-In")
                }
            } catch (e: Exception) {
                viewModel.setError(e.message ?: "Google Sign-In failed")
            }
        }
    }

    val isLoading = uiState.value is AuthUiState.Loading
    val authErrorMessage = (uiState.value as? AuthUiState.Error)?.message

    Box(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.weight(0.3f))

        // Logo with neon glow effect
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            com.example.djeventhub.ui.theme.NeonPink.copy(alpha = 0.3f),
                            androidx.compose.ui.graphics.Color.Transparent
                        ),
                        radius = 200f
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title with gradient effect
        Text(
            text = "DJ Event Hub",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        com.example.djeventhub.ui.theme.NeonPink,
                        com.example.djeventhub.ui.theme.NeonPurple
                    )
                )
            )
        )

        Text(
            text = "Organiza tus eventos sin estrés",
            fontSize = 14.sp,
            color = com.example.djeventhub.ui.theme.TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Auth mode title
        Text(
            text = if (isRegisterMode.value) "Crear cuenta" else "Iniciar sesión",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        if (emailError != null) {
            Text(emailError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword.value = !showPassword.value }) {
                    Text(if (showPassword.value) "Ocultar" else "Ver")
                }
            }
        )
        if (passwordError != null) {
            Text(passwordError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Global auth error (e.g., wrong password)
        if (authErrorMessage != null) {
            Text(authErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (isRegisterMode.value) {
                    viewModel.signUpWithEmail(email.value, password.value)
                } else {
                    viewModel.signInWithEmail(email.value, password.value)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isFormValid && !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isRegisterMode.value) "Registrar" else "Entrar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Google button
        OutlinedButton(
            onClick = {
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continuar con Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { if (!isLoading) isRegisterMode.value = !isRegisterMode.value }) {
            Text(
                text = if (isRegisterMode.value) "¿Ya tienes cuenta? Inicia sesión" else "¿Nuevo? Regístrate",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Navigate when authenticated
        if (uiState.value is AuthUiState.Authenticated) {
            (uiState.value as? AuthUiState.Authenticated)?.uid?.let { onAuthenticated(it) }
        }
        }

        // Loading overlay - doesn't affect layout
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                NeonLoadingIndicator()
            }
        }
    }
}

// Simple validators
private fun validateEmail(value: String): String? {
    if (value.isBlank()) return "Email requerido"
    val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return if (!regex.matches(value)) "Formato inválido" else null
}

private fun validatePassword(value: String): String? {
    if (value.isBlank()) return "Password requerido"
    if (value.length < 6) return "Mínimo 6 caracteres"
    return null
}