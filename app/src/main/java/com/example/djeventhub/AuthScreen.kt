package com.example.djeventhub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: (String) -> Unit) {
    val authState by viewModel.authState.collectAsState()

    // Local UI state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // React to success state once
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                message = null
                onAuthSuccess((authState as AuthState.Success).uid)
            }
            is AuthState.Error -> {
                message = (authState as AuthState.Error).message
            }
            is AuthState.Loading -> {
                message = null
            }
            AuthState.Idle -> {
                // no-op
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }

                Button(onClick = { viewModel.register(email.trim(), password) }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Registrar")
                }

                Button(onClick = { viewModel.login(email.trim(), password) }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Iniciar sesión")
                }

                message?.let {
                    Text(text = it, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

