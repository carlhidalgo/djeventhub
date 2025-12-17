package com.example.djeventhub

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object CameraHandler {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun takePicture(
        context: Context,
        imageCapture: ImageCapture,
        onImageSaved: (Uri) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val photoFile = createFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageSaved(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun bindCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onImageCaptureReady: (ImageCapture) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                onImageCaptureReady(imageCapture)
            } catch (e: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(context))
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

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}

// Composable to request camera permission and capture images with CameraX
@Composable
fun CameraPermissionHandler(
    onImageCaptured: (Uri) -> Unit,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current
    val hasPermission = remember { mutableStateOf(false) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission.value = granted
        if (granted) {
            CameraHandler.bindCameraUseCases(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onImageCaptureReady = { capture ->
                    imageCapture.value = capture
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission.value) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            Button(onClick = {
                imageCapture.value?.let { capture ->
                    CameraHandler.takePicture(
                        context = context,
                        imageCapture = capture,
                        onImageSaved = onImageCaptured,
                        onError = { /* Handle error */ }
                    )
                }
            }) {
                Text(text = "Capturar Foto")
            }
        } else {
            Text(text = "Se requiere permiso de cámara")
        }
    }
}
