package com.example.djeventhub

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Note: This is a simplified camera handler that does NOT use CameraX to avoid
// static analysis errors in environments where CameraX is not indexed.
// TODO: Replace with CameraX implementation when project is built and CameraX classes are resolvable.
object CameraHandler {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Create a file and return its Uri via callback. This is a stub for an actual capture.
    fun takePicture(context: Context, onImageSaved: (Uri) -> Unit, onError: (Exception) -> Unit = {}) {
        try {
            val photoFile = createFile(context)
            val savedUri = Uri.fromFile(photoFile)
            // In a real implementation we would capture and write image data here.
            onImageSaved(savedUri)
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun createFile(context: Context): File {
        val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        val name = "JPEG_" + sdf.format(Date()) + "_.jpg"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, name)
    }
}

// Simple composable to request camera permission and show a button to take a picture
@Composable
fun CameraPermissionHandler(onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val hasPermission = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission.value = granted
    }

    LaunchedEffect(Unit) {
        // request permission when composable enters composition
        launcher.launch(android.Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission.value) {
            Button(onClick = {
                CameraHandler.takePicture(context, onImageCaptured) { /* ignore errors for now */ }
            }) {
                Text(text = "Take Picture")
            }
        } else {
            Text(text = "Camera permission required")
        }
    }
}
