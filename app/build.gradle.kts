import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

// Load API keys from local.properties (not versioned) or environment vars
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { input ->
            this.load(input)
        }
    }
}
fun prop(name: String, default: String = ""): String =
    (localProps.getProperty(name) ?: System.getenv(name) ?: default)

android {
    namespace = "com.example.djeventhub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.djeventhub"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Expose API keys / configs
        val mapsKey = prop("MAPS_API_KEY", "")
        val apiBaseUrl = prop("API_BASE_URL", "https://api.example.com")
        val thirdPartyToken = prop("THIRD_PARTY_TOKEN", "")

        // For Manifest placeholders (e.g., Google Maps)
        manifestPlaceholders += mapOf(
            "MAPS_API_KEY" to mapsKey
        )

        // For runtime use in code
        resValue("string", "api_base_url", apiBaseUrl)
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "THIRD_PARTY_TOKEN", "\"$thirdPartyToken\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    /*
    GitHub Copilot:
    Please add the necessary dependencies for a modern Android app using Jetpack Compose.
    I need libraries for:
    - Jetpack Compose (UI, Material3, ViewModel, LiveData, Navigation)
    - Kotlin Coroutines for asynchronous operations
    - Retrofit and OkHttp for networking (to call a REST API)
    - Gson or Moshi for JSON serialization
    - Firebase Bill of Materials (BoM) and dependencies for Firebase Cloud Messaging and Authentication
    - Google Maps SDK for Android
    - CameraX for camera functionality
    - Google Play Services for location (FusedLocationProviderClient)
    - Hilt for dependency injection
    */

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")

    // Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps.android:maps-compose-utils:4.3.0")

    // Google Places SDK for autocomplete
    implementation("com.google.android.libraries.places:places:3.3.0")

    // Navigation Compose and ViewModel Compose
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    // Firebase (BoM) and Auth
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Add Firebase Storage for profile images
    implementation("com.google.firebase:firebase-storage-ktx")
    // Firebase Cloud Messaging for push notifications
    implementation("com.google.firebase:firebase-messaging-ktx")

    // WorkManager for background tasks (event reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Accompanist permissions
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    // Accompanist swipe-to-refresh (used by EventListScreen)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    // Image loading for Compose (profile pictures)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Apply the Google services plugin to enable Firebase features (google-services.json must be in app/)
apply(plugin = "com.google.gms.google-services")