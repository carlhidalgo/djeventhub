package com.example.djeventhub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for DJ Event Hub
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 */
@HiltAndroidApp
class DJEventHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide configurations here if needed
    }
}
