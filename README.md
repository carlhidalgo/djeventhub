# DJ Event Hub

DJ Event Hub es una aplicación Android escrita en Kotlin y Jetpack Compose para la gestión y visualización de eventos, con autenticación mediante Firebase.

## Resumen

- Plataforma: Android (Kotlin, Jetpack Compose)
- Arquitectura sugerida: MVVM + Repository
- Librerías principales: Jetpack Compose, Kotlin Coroutines, Retrofit/OkHttp, Moshi/Gson, Hilt, CameraX, Google Maps, Firebase (Auth, Cloud Messaging)

---

## Contenido

- [Requisitos](#requisitos)
- [Compatibilidad AGP / Android Studio](#compatibilidad-agp--android-studio)
- [Instalación y configuración rápida](#instalaci%C3%B3n-y-configuraci%C3%B3n-r%C3%A1pida)
- [Configuración de Firebase (Auth)](#configuraci%C3%B3n-de-firebase-auth)
- [Integración de dependencias importantes](#integraci%C3%B3n-de-dependencias-importantes)
- [Funcionamiento del flujo de autenticación](#funcionamiento-del-flujo-de-autenticaci%C3%B3n)
- [Comandos Git (Windows CMD)](#comandos-git-windows-cmd)
- [Resolución de problemas comunes](#resoluci%C3%B3n-de-problemas-comunes)
- [Licencia](#licencia)

---

## Requisitos

- Android Studio compatible con la versión de AGP que uses.
- JDK 11 o superior.
- Cuenta de Firebase.
- Conexión a internet para descargar dependencias y sincronizar Gradle.
- Clave SSH configurada en tu máquina y añadida a GitHub si usarás `git@github.com`.

---

## Compatibilidad AGP / Android Studio

Aviso importante: actualmente en este proyecto se detectó una incompatibilidad entre la versión instalada del Android Gradle Plugin (AGP) y la versión soportada por tu entorno.

- Mensaje observado: "El proyecto está usando una versión incompatible (AGP 8.9.1). La versión más reciente soportada es AGP 8.6.0".

Opciones para resolverlo (elige una):

1. Actualizar Android Studio a una versión que soporte AGP 8.9.1.
2. Bajar la versión de AGP del proyecto a 8.6.0 (recomendado si no deseas actualizar Android Studio ahora).

Snippet para forzar AGP 8.6.0 (ejemplo en Gradle Kotlin DSL - archivo `build.gradle.kts` en el proyecto raíz o en el bloque `buildscript`):

```kotlin
// En el build.gradle.kts del proyecto raíz o en buildSrc/versions
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
    }
}
```

También asegúrate de que la versión de Gradle wrapper sea compatible con AGP 8.6.0; en `gradle/wrapper/gradle-wrapper.properties` usa una distribución recomendada (por ejemplo Gradle 8.0+ o la que sugiera la guía de AGP 8.6).

Si optas por actualizar Android Studio, primero actualiza el IDE y luego sincroniza el proyecto.

---

## Instalación y configuración rápida

1. Clona o copia el proyecto en tu máquina.
2. Abre el proyecto en Android Studio.
3. Coloca `google-services.json` obtenido del panel de Firebase en la carpeta `app/`.
4. Sincroniza Gradle (File → Sync Project with Gradle Files).
5. Compila y ejecuta en un emulador o dispositivo.

---

## Configuración de Firebase (Auth)

Pasos para configurar autenticación por Email/Password:

1. Ve a https://console.firebase.google.com/ y crea (o abre) un proyecto.
2. Añade una nueva app Android y registra el `applicationId` que aparece en `app/build.gradle.kts` (por ejemplo `com.example.djeventhub`).
3. Descarga `google-services.json` y colócalo en la carpeta `app/` (ruta: `app/google-services.json`).
4. En la consola de Firebase → Authentication → Sign-in method activa "Email/Password".
5. Añade las dependencias de Firebase y el plugin de Google Services (ver sección de dependencias abajo).
6. Implementa la UI de autenticación (ya hay un ejemplo `AuthScreen.kt`) y el ViewModel que usa `FirebaseAuth`.

Notas: si usas otros proveedores (Google Sign-In, Phone), deberás configurarlos en la consola de Firebase y añadir las dependencias y configuración adicional.

---

## Integración de dependencias importantes (ejemplos)

En `app/build.gradle.kts` (Kotlin DSL) añade o confirma las siguientes dependencias mínimas. Usa Firebase BoM para gestionar versiones:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit + OkHttp + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.47")

    // Jetpack Compose (placeholder - adapta versiones según tu proyecto)
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")

    // Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
}
```

Ajusta versiones si ya gestionas versiones desde `libs.versions.toml` o `buildSrc`.

---

## Funcionamiento del flujo de autenticación

- `AuthViewModel` (o similar) debe usar `FirebaseAuth.getInstance()`.
- Métodos principales:
  - `createUserWithEmailAndPassword(email, password)` → registro.
  - `signInWithEmailAndPassword(email, password)` → login.
- En Compose muestra una pantalla de login/registro (`AuthScreen`) y al completar la autenticación navega a la pantalla principal.

Ejemplo de manejo de éxito/fallo: el ViewModel expone estados (loading, success, error) que la UI debe observar y presentar mensajes.

---

## Comandos Git (Windows CMD) — crear repo remota y subir por SSH

Asegúrate de tener tu clave SSH añadida a GitHub (https://github.com/settings/ssh/new).

Ejemplos de comandos (abre una ventana `cmd.exe` en la raíz del proyecto `DJEventHub`):

```cmd
cd C:\Users\carlos\AndroidStudioProjects\DJEventHub
git init
git add .
git commit -m "Inicializa proyecto"
git remote add origin git@github.com:carlhidalgo/djeventhub.git
git branch -M main
git push -u origin main
```

Si el repositorio remoto existía y fue eliminado, recrea el repositorio en GitHub con el mismo nombre `djeventhub` y luego ejecuta los comandos anteriores.

---

## Resolución de problemas comunes

- Error: "AGP incompatible (8.9.1)" → ver sección Compatibilidad AGP.
- Error: `google-services.json missing` → colocar el archivo en `app/`.
- Error al pushear por SSH → verificar que tu clave pública esté en GitHub y que el agente SSH esté corriendo.
- Problemas con versiones de Compose o librerías → centraliza versiones en `libs.versions.toml` o actualiza las versiones para que sean compatibles.

---

## Buenas prácticas y recomendaciones

- Usa Hilt para inyectar `FirebaseAuth`, `ApiService`, y `EventRepository`.
- Centraliza las constantes de red y rutas de la API.
- Maneja estados de UI con `StateFlow`/`LiveData` desde los ViewModels.
- Añade pruebas unitarias para el `EventRepository` y tests de UI básicos para pantallas Compose.

---

## Licencia

Proyecto bajo licencia MIT. Libera el uso y modificación mientras conserves la nota de copyright.

---

## Contacto y ayuda



