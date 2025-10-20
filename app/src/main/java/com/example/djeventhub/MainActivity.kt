package com.example.djeventhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.djeventhub.ui.theme.DJEventHubTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            DJEventHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val showGreeting = remember { mutableStateOf<String?>(null) }
                    if (showGreeting.value == null) {
                        // Show authentication screen
                        val vm = AuthViewModel()
                        AuthScreen(viewModel = vm) { uid ->
                            showGreeting.value = uid
                        }
                    } else {
                        Greeting(name = showGreeting.value ?: "User", modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DJEventHubTheme {
        Greeting("Android")
    }
}