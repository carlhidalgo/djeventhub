# DJEventHub - Documentación del Proyecto

## Información General

**Nombre del Proyecto:** DJEventHub
**Plataforma:** Android
**Lenguaje:** Kotlin
**Framework UI:** Jetpack Compose
**Package:** com.example.djeventhub
**Versión:** 1.0 (versionCode 1)

---

## Descripción del Proyecto

DJEventHub es una aplicación Android moderna desarrollada con Jetpack Compose que proporciona funcionalidades de autenticación de usuarios utilizando Firebase Authentication, con soporte para inicio de sesión por correo electrónico/contraseña y Google Sign-In.

---

## Arquitectura Técnica

### Stack Tecnológico

#### SDK Android
- **compileSdk:** 35
- **minSdk:** 26 (Android 8.0 Oreo)
- **targetSdk:** 35
- **Java Version:** 1.8

#### Frameworks y Bibliotecas Principales

**Jetpack Compose**
- androidx.compose.bom
- androidx.ui
- androidx.material3
- androidx.activity.compose
- androidx.lifecycle.viewmodel.compose:2.5.1
- androidx.navigation.navigation-compose:2.5.3
- Kotlin Compiler Extension: 1.5.1

**Firebase**
- firebase-bom:32.2.0
- firebase-auth-ktx

**Google Services**
- play-services-auth:20.7.0 (Google Sign-In)
- play-services-location:21.0.1

**Networking**
- Retrofit:2.9.0
- OkHttp:4.9.3
- Gson:2.9.0

**Camera y Multimedia**
- CameraX:1.2.3
  - camera-core
  - camera-camera2
  - camera-lifecycle
  - camera-view

**Coroutines**
- kotlinx-coroutines-android:1.6.4

**Testing**
- JUnit
- Espresso
- Compose UI Test

---

## Estructura del Proyecto

### Directorios Principales

```
DJEventHub/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/djeventhub/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   │   └── LoginScreen.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Event.kt
│   │   │   │   ├── EventRepository.kt
│   │   │   │   ├── EventListScreen.kt
│   │   │   │   ├── EventListViewModel.kt
│   │   │   │   ├── ApiService.kt
│   │   │   │   ├── CameraHandler.kt
│   │   │   │   ├── LocationProvider.kt
│   │   │   │   ├── GoogleSignInHelper.kt
│   │   │   │   ├── AuthRepository.kt
│   │   │   │   └── AuthScreen.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── google-services.json
└── build.gradle.kts
```

---

## Componentes Implementados

### 1. MainActivity.kt

**Ubicación:** `app/src/main/java/com/example/djeventhub/MainActivity.kt`

**Responsabilidades:**
- Actividad principal de la aplicación
- Inicializa Firebase en `onCreate()`
- Configura el tema de Jetpack Compose
- Gestiona el estado de autenticación del usuario
- Muestra `LoginScreen` si el usuario no está autenticado
- Muestra una pantalla de bienvenida (`Greeting`) si el usuario está autenticado

**Código Principal:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            DJEventHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentUser = remember { mutableStateOf<String?>(null) }
                    if (currentUser.value == null) {
                        LoginScreen(onAuthenticated = { uid -> currentUser.value = uid })
                    } else {
                        Greeting(name = currentUser.value ?: "User", modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
```

---

### 2. LoginScreen.kt

**Ubicación:** `app/src/main/java/com/example/djeventhub/ui/auth/LoginScreen.kt`

**Responsabilidades:**
- Interfaz de usuario para autenticación
- Campos de entrada para email y contraseña
- Botones para:
  - Iniciar sesión con email/contraseña
  - Registrarse con email/contraseña
  - Iniciar sesión con Google
- Gestión del flujo de Google Sign-In con ActivityResultLauncher
- Visualización de estados: Loading, Error, Authenticated

**Características:**
- Usa `viewModel()` para obtener instancia de AuthViewModel
- Usa `collectAsState()` para observar cambios en el estado de autenticación
- Integración con Google Sign-In usando `GoogleSignInClient`
- Manejo de tokens de ID de Google

**Estados UI:**
```kotlin
when (val state = uiState.value) {
    is AuthUiState.Loading -> CircularProgressIndicator()
    is AuthUiState.Error -> Snackbar { Text(state.message) }
    is AuthUiState.Authenticated -> onAuthenticated(state.uid)
    else -> {}
}
```

---

### 3. AuthViewModel.kt

**Ubicación:** `app/src/main/java/com/example/djeventhub/ui/auth/AuthViewModel.kt`

**Responsabilidades:**
- ViewModel para gestionar la lógica de autenticación
- Interactúa con Firebase Authentication
- Gestiona estados de autenticación (Idle, Loading, Authenticated, Error)
- Proporciona métodos para:
  - Iniciar sesión con email/contraseña
  - Registrarse con email/contraseña
  - Iniciar sesión con Google (usando ID token)
  - Cerrar sesión

**Estados Disponibles:**
```kotlin
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
```

**Métodos Principales:**

1. **signInWithEmail(email: String, password: String)**
   - Inicia sesión con credenciales de email
   - Actualiza el estado a Loading → Authenticated o Error

2. **signUpWithEmail(email: String, password: String)**
   - Crea una nueva cuenta con email
   - Actualiza el estado a Loading → Authenticated o Error

3. **handleGoogleSignIn(idToken: String)**
   - Procesa el token de Google Sign-In
   - Crea credenciales de Firebase
   - Autentica al usuario
   - Actualiza el estado a Loading → Authenticated o Error

4. **signOut()**
   - Cierra sesión del usuario
   - Resetea el estado a Idle

5. **setError(message: String)**
   - Establece un mensaje de error en el estado

**Implementación de Coroutines:**
```kotlin
private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { cont ->
    task.addOnCompleteListener { t ->
        if (t.isSuccessful) {
            cont.resume(t.result as T)
        } else {
            cont.resumeWithException(t.exception ?: Exception("Unknown task exception"))
        }
    }
}
```

---

## Configuración de Firebase

### AndroidManifest.xml

**Permisos:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Configuración de la aplicación:**
- allowBackup: true
- Theme: Theme.DJEventHub
- MainActivity exportada como LAUNCHER

### Dependencias de Firebase

```kotlin
implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.android.gms:play-services-auth:20.7.0")
```

### Google Sign-In

Para usar Google Sign-In, se necesita:
1. Archivo `google-services.json` en el directorio `app/`
2. Configuración del `default_web_client_id` en `strings.xml`
3. Proyecto configurado en Firebase Console con autenticación de Google habilitada

---

## Flujo de Autenticación

### 1. Inicio de Sesión con Email/Contraseña

```
Usuario ingresa email/contraseña
    ↓
Usuario presiona "Iniciar sesión"
    ↓
LoginScreen llama viewModel.signInWithEmail()
    ↓
AuthViewModel.signInWithEmail() se ejecuta
    ↓
Estado cambia a Loading
    ↓
Firebase Auth valida credenciales
    ↓
Si éxito: Estado → Authenticated(uid)
Si error: Estado → Error(message)
    ↓
LoginScreen detecta estado Authenticated
    ↓
Llama onAuthenticated(uid)
    ↓
MainActivity actualiza currentUser
    ↓
Muestra pantalla Greeting
```

### 2. Registro con Email/Contraseña

```
Usuario ingresa email/contraseña
    ↓
Usuario presiona "Regístrate"
    ↓
LoginScreen llama viewModel.signUpWithEmail()
    ↓
AuthViewModel.signUpWithEmail() se ejecuta
    ↓
Estado cambia a Loading
    ↓
Firebase Auth crea nueva cuenta
    ↓
Si éxito: Estado → Authenticated(uid)
Si error: Estado → Error(message)
    ↓
[Mismo flujo que inicio de sesión]
```

### 3. Inicio de Sesión con Google

```
Usuario presiona "Sign in con Google"
    ↓
LoginScreen lanza GoogleSignInClient.signInIntent
    ↓
ActivityResultLauncher espera resultado
    ↓
Usuario selecciona cuenta de Google
    ↓
Se recibe resultado con ID token
    ↓
LoginScreen llama viewModel.handleGoogleSignIn(idToken)
    ↓
AuthViewModel.handleGoogleSignIn() se ejecuta
    ↓
Estado cambia a Loading
    ↓
Crea GoogleAuthProvider.credential(idToken)
    ↓
Firebase Auth autentica con credencial
    ↓
Si éxito: Estado → Authenticated(uid)
Si error: Estado → Error(message)
    ↓
[Mismo flujo que inicio de sesión]
```

---

## Características Implementadas

### Autenticación
- ✅ Login con email y contraseña
- ✅ Registro con email y contraseña
- ✅ Google Sign-In
- ✅ Gestión de estados (Loading, Error, Authenticated)
- ✅ Manejo de errores

### UI/UX
- ✅ Interfaz moderna con Material3
- ✅ Edge-to-Edge display
- ✅ Indicadores de carga (CircularProgressIndicator)
- ✅ Mensajes de error (Snackbar)
- ✅ Campos de texto con OutlinedTextField

### Arquitectura
- ✅ Patrón MVVM (Model-View-ViewModel)
- ✅ StateFlow para gestión de estados reactivos
- ✅ Coroutines para operaciones asíncronas
- ✅ Separación de concerns (UI, ViewModel, Repository)

---

## Archivos de Configuración

### build.gradle.kts (app)

**Plugins:**
- android.application
- kotlin.android
- com.google.gms.google-services

**Configuración de Compose:**
```kotlin
buildFeatures {
    compose = true
}
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"
}
```

**Dependencias Clave:**
- Jetpack Compose BOM
- Firebase BOM
- Retrofit + Gson
- CameraX
- Play Services (Auth, Location)
- Coroutines

---

## Otros Componentes en el Proyecto

Aunque no están completamente implementados en la autenticación actual, el proyecto contiene archivos para:

### Event Management
- `Event.kt` - Modelo de datos para eventos
- `EventRepository.kt` - Repositorio para gestionar eventos
- `EventListScreen.kt` - UI para listar eventos
- `EventListViewModel.kt` - ViewModel para la lista de eventos

### Servicios
- `ApiService.kt` - Servicio para llamadas API REST
- `CameraHandler.kt` - Manejo de funcionalidad de cámara
- `LocationProvider.kt` - Proveedor de servicios de ubicación
- `GoogleSignInHelper.kt` - Helper para Google Sign-In

### Repositorios Adicionales
- `AuthRepository.kt` - Repositorio alternativo de autenticación
- `AuthViewModel.kt` (raíz) - ViewModel alternativo de autenticación
- `AuthScreen.kt` - Pantalla alternativa de autenticación

---

## Estado Actual del Proyecto

### Git Status

**Branch actual:** master
**Archivos modificados:**
- `MainActivity.kt` (modificado)
- `ui/auth/AuthViewModel.kt` (agregado)
- `ui/auth/LoginScreen.kt` (agregado)

**Commits recientes:**
```
82df6b4 - fix
757c3d3 - Local initial commit
```

---

## Próximos Pasos Sugeridos

### 1. Funcionalidades de Autenticación
- [ ] Implementar recuperación de contraseña
- [ ] Implementar verificación de email
- [ ] Agregar autenticación biométrica
- [ ] Implementar persistencia de sesión

### 2. Gestión de Eventos
- [ ] Completar implementación de EventListScreen
- [ ] Integrar con API backend
- [ ] Implementar creación de eventos
- [ ] Implementar detalles de eventos

### 3. Funcionalidades de Cámara
- [ ] Implementar captura de fotos para eventos
- [ ] Integrar con galería
- [ ] Implementar subida de imágenes

### 4. Ubicación
- [ ] Implementar servicios de ubicación
- [ ] Integrar Google Maps
- [ ] Mostrar eventos cercanos

### 5. UI/UX
- [ ] Implementar navegación completa con Navigation Compose
- [ ] Mejorar diseño visual
- [ ] Agregar animaciones
- [ ] Implementar tema oscuro

### 6. Testing
- [ ] Escribir tests unitarios para ViewModels
- [ ] Escribir tests de UI con Compose Test
- [ ] Implementar tests de integración

### 7. Optimización
- [ ] Implementar inyección de dependencias con Hilt
- [ ] Optimizar rendimiento
- [ ] Implementar caché de datos
- [ ] Agregar manejo offline

---

## Notas Técnicas Importantes

### Manejo de Tareas Asíncronas

El proyecto usa una función de extensión personalizada para convertir Tasks de Firebase en suspending functions:

```kotlin
private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { cont ->
    task.addOnCompleteListener { t ->
        if (t.isSuccessful) {
            cont.resume(t.result as T)
        } else {
            cont.resumeWithException(t.exception ?: Exception("Unknown task exception"))
        }
    }
}
```

Esto permite usar `await` con Firebase Auth de manera más elegante dentro de coroutines.

### Google Sign-In Configuration

Para que Google Sign-In funcione correctamente:

1. El archivo `google-services.json` debe estar en `app/`
2. Se debe obtener el `default_web_client_id` desde los recursos generados por Google Services
3. El proyecto debe estar configurado en Firebase Console
4. Las huellas SHA-1 deben estar registradas en Firebase

### StateFlow vs LiveData

El proyecto usa StateFlow en lugar de LiveData porque:
- Mejor integración con Compose
- Más fácil de testear
- Soporte nativo de coroutines
- Mejor rendimiento en algunos casos

---

## Recursos de Referencia

### Documentación Oficial
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### Dependencias Clave
- [Retrofit](https://square.github.io/retrofit/)
- [CameraX](https://developer.android.com/training/camerax)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

---

## Contacto y Soporte

**Desarrollador:** Carlos
**Ubicación del Proyecto:** C:\Users\carlos\AndroidStudioProjects\DJEventHub
**Última Actualización:** Noviembre 2025

---

## Licencia

[Agregar información de licencia según sea necesario]

---

## Changelog

### Version 1.0 (Actual)
- Implementación inicial de autenticación con Firebase
- Login con email/contraseña
- Registro de usuarios
- Google Sign-In
- Interfaz básica con Jetpack Compose
- Estructura del proyecto establecida

---

**Fin del Documento**
