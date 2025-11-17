# DJEventHub - Documentación del Proyecto

## Información General

**Nombre del Proyecto:** DJEventHub
**Plataforma:** Android
**Lenguaje:** Kotlin
**Framework UI:** Jetpack Compose
**Package:** com.example.djeventhub
**Versión:** 2.0 (versionCode 1)
**Última Actualización:** Noviembre 2025

---

## Descripción del Proyecto

DJEventHub es una aplicación Android moderna desarrollada con Jetpack Compose que conecta DJs con productoras de eventos. La aplicación proporciona:

- **Autenticación robusta** con Firebase (Email/Password y Google Sign-In)
- **Sistema de roles** diferenciados (DJ y Productora) con navegación personalizada
- **Gestión de eventos** con lista y vista de mapa interactivo
- **Perfiles de usuario** con información detallada, géneros musicales, disponibilidad y calificaciones
- **Sistema de chat** para comunicación entre DJs y productoras
- **Navegación estilo Instagram** con barra inferior profesional y animaciones fluidas
- **Integración de Google Maps** para visualizar eventos cercanos
- **Animaciones profesionales** en toda la aplicación
- **Geolocalización** para ordenar eventos por proximidad

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
- firebase-firestore-ktx
- firebase-storage-ktx

**Google Services**
- play-services-auth:20.7.0 (Google Sign-In)
- play-services-location:21.0.1
- play-services-maps:18.2.0

**Google Maps Compose**
- maps-compose:4.3.0
- maps-compose-utils:4.3.0

**Accompanist (Permisos)**
- accompanist-permissions:0.30.1

**Coil (Carga de imágenes)**
- coil-compose:2.4.0

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
│   │   │   │   │   ├── roleselection/
│   │   │   │   │   │   └── RoleSelectionScreen.kt
│   │   │   │   │   ├── dj/
│   │   │   │   │   │   ├── DJMainScreen.kt
│   │   │   │   │   │   └── profile/
│   │   │   │   │   │       ├── DJProfileScreen.kt
│   │   │   │   │   │       └── DJProfileViewModel.kt
│   │   │   │   │   ├── productora/
│   │   │   │   │   │   ├── ProductoraMainScreen.kt
│   │   │   │   │   │   └── ProductoraHomeScreen.kt
│   │   │   │   │   ├── events/
│   │   │   │   │   │   └── EventsMainScreen.kt
│   │   │   │   │   ├── map/
│   │   │   │   │   │   └── MapScreen.kt
│   │   │   │   │   ├── chat/
│   │   │   │   │   │   ├── ChatListScreen.kt
│   │   │   │   │   │   └── ChatListViewModel.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── AppNavigation.kt
│   │   │   │   │   │   └── InstagramBottomBar.kt
│   │   │   │   │   ├── animations/
│   │   │   │   │   │   └── AnimationUtils.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   └── LoadingComponents.kt
│   │   │   │   │   ├── addevent/
│   │   │   │   │   │   └── AddEventScreen.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── models/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── UserRole.kt
│   │   │   │   │   ├── Chat.kt
│   │   │   │   │   └── Message.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── UserRepository.kt
│   │   │   │   │   └── ChatRepository.kt
│   │   │   │   ├── location/
│   │   │   │   │   └── LocationManager.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Event.kt
│   │   │   │   ├── EventRepository.kt
│   │   │   │   ├── EventListScreen.kt
│   │   │   │   ├── EventListViewModel.kt
│   │   │   │   ├── ApiService.kt
│   │   │   │   ├── CameraHandler.kt
│   │   │   │   └── LocationProvider.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   └── logo.png
│   │   │   │   ├── xml/
│   │   │   │   │   └── file_paths.xml
│   │   │   │   ├── mipmap-*/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── google-services.json
├── DOCUMENTACION_PROYECTO.md
├── CONFIGURACION_FIREBASE.md
├── README.md
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
- ✅ Google Sign-In con manejo de tokens
- ✅ Persistencia de sesión (auto-login)
- ✅ Logout completo
- ✅ Gestión de estados (Loading, Error, Authenticated)
- ✅ Manejo de errores con mensajes descriptivos
- ✅ Overlay de carga sin afectar el layout

### Sistema de Roles
- ✅ Selección de rol (DJ o Productora) después del registro
- ✅ Navegación personalizada según rol
- ✅ Pantalla RoleSelectionScreen con diseño profesional
- ✅ Guardado de rol en Firestore

### Navegación
- ✅ Navegación estilo Instagram con barra inferior
- ✅ 5 pestañas para cada rol (Inicio, Mapa/Búsqueda, Agregar, Chat, Perfil)
- ✅ FAB central con gradiente y animación de pulso
- ✅ Transiciones animadas entre pantallas
- ✅ Glow effect en íconos seleccionados
- ✅ InstagramBottomBar reutilizable

### Gestión de Eventos
- ✅ Lista de eventos con scroll
- ✅ Vista de mapa interactivo con Google Maps
- ✅ Tabs para alternar entre Lista y Mapa
- ✅ Marcadores de eventos en el mapa
- ✅ Cálculo de distancia con fórmula Haversine
- ✅ Ordenamiento de eventos por proximidad
- ✅ Botón para abrir en Google Maps
- ✅ Refresh para actualizar ubicación

### Perfiles de Usuario
- ✅ Perfil de DJ con información completa
- ✅ Avatar con gradiente neon
- ✅ Carga de foto de perfil desde galería
- ✅ Sistema de calificaciones con estrellas animadas
- ✅ Géneros musicales con chips de colores
- ✅ Días de disponibilidad
- ✅ Estadísticas de eventos completados
- ✅ Información de contacto (teléfono, ubicación)
- ✅ Biografía personalizada

### Chat (En desarrollo)
- ✅ Pantalla de lista de chats
- ✅ Repositorio de chats con Firestore
- ✅ Modelo de datos para Chat y Message
- ✅ UI placeholder para próximas funcionalidades

### Animaciones
- ✅ Sistema centralizado de animaciones (AnimationUtils.kt)
- ✅ Bounce click para elementos interactivos
- ✅ Fade in con delay para entradas escalonadas
- ✅ Pulse animation para elementos destacados
- ✅ Shimmer effect para placeholders
- ✅ Slide transitions para navegación
- ✅ Spring animations para calificaciones
- ✅ NeonLoadingIndicator con glow effect
- ✅ Animaciones de aparición en listas

### Google Maps
- ✅ Integración completa de Google Maps
- ✅ Marcadores de eventos
- ✅ Bottom sheet con información del evento
- ✅ Botón de ubicación actual
- ✅ Permisos de ubicación con Accompanist
- ✅ Diálogo explicativo si se deniegan permisos
- ✅ MapScreen con cámara animada
- ✅ Clustering de marcadores cercanos

### Geolocalización
- ✅ LocationManager personalizado
- ✅ FusedLocationProviderClient
- ✅ Solicitud de permisos en runtime
- ✅ Obtención de ubicación actual
- ✅ Actualización automática de distancias
- ✅ Soporte para emuladores (ubicación mock)

### UI/UX
- ✅ Interfaz moderna con Material3
- ✅ Tema oscuro con colores neon (NeonPink, NeonPurple, ElectricBlue)
- ✅ Edge-to-Edge display
- ✅ Cards con gradientes y efectos de glow
- ✅ Loading states con indicadores profesionales
- ✅ Mensajes de error con Snackbar
- ✅ Validación de formularios
- ✅ Responsive design
- ✅ Logo personalizado de la app

### Arquitectura
- ✅ Patrón MVVM (Model-View-ViewModel)
- ✅ Repository pattern para datos
- ✅ StateFlow para gestión de estados reactivos
- ✅ Coroutines para operaciones asíncronas
- ✅ Separación de concerns (UI, ViewModel, Repository, Data)
- ✅ Navegación con Navigation Compose
- ✅ Inyección manual de dependencias

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
- [x] ~~Implementar persistencia de sesión~~ ✅ Completado
- [ ] Implementar recuperación de contraseña
- [ ] Implementar verificación de email
- [ ] Agregar autenticación biométrica

### 2. Gestión de Eventos
- [x] ~~Completar implementación de EventListScreen~~ ✅ Completado
- [x] ~~Implementar vista de mapa~~ ✅ Completado
- [x] ~~Mostrar eventos cercanos~~ ✅ Completado
- [ ] **[PRIORIDAD]** Arreglar botones de crear evento (actualmente no funcionan)
- [ ] **[PRIORIDAD]** Implementar AddEventScreen con mapa integrado
- [ ] **[PRIORIDAD]** Agregar autocomplete de direcciones con Google Places
- [ ] Implementar selector de ubicación en mapa para crear eventos
- [ ] Implementar detalles de eventos (pantalla de detalle)
- [ ] Integrar con API backend (cuando esté disponible)

### 3. Chat y Mensajería
- [x] ~~Estructura básica de chat~~ ✅ Completado
- [ ] Implementar chat en tiempo real con Firestore
- [ ] Notificaciones de mensajes nuevos
- [ ] Pantalla de conversación individual
- [ ] Envío de imágenes en chat

### 4. Funcionalidades de Productora
- [ ] Pantalla de búsqueda de DJs funcional
- [ ] Filtros de búsqueda (género, ubicación, disponibilidad)
- [ ] Sistema de contratación/invitación a eventos
- [ ] Perfil de productora completo

### 5. UI/UX
- [x] ~~Implementar navegación completa con Navigation Compose~~ ✅ Completado
- [x] ~~Mejorar diseño visual~~ ✅ Completado
- [x] ~~Agregar animaciones~~ ✅ Completado
- [x] ~~Implementar tema oscuro~~ ✅ Completado
- [ ] **[PRIORIDAD]** Reducir altura de la TopBar (muy gruesa actualmente)
- [ ] Mejorar responsive design para tablets
- [ ] Agregar splash screen animada

### 6. Testing
- [ ] Escribir tests unitarios para ViewModels
- [ ] Escribir tests de UI con Compose Test
- [ ] Implementar tests de integración
- [ ] Tests de permisos de ubicación

### 7. Optimización
- [ ] Implementar inyección de dependencias con Hilt
- [ ] Optimizar rendimiento del mapa
- [ ] Implementar caché de datos con Room
- [ ] Agregar manejo offline con sincronización
- [ ] Optimizar carga de imágenes

### 8. Funcionalidades Avanzadas
- [ ] Sistema de calificaciones y reviews
- [ ] Galería de fotos para perfiles y eventos
- [ ] Compartir eventos en redes sociales
- [ ] Notificaciones push con Firebase Cloud Messaging
- [ ] Estadísticas y analytics para productoras
- [ ] Sistema de favoritos para DJs

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

### Version 2.0 (Actual - Noviembre 2025)
- ✅ Sistema completo de roles (DJ y Productora) con navegación personalizada
- ✅ Navegación estilo Instagram con barra inferior profesional y FAB central
- ✅ Integración completa de Google Maps con marcadores y ubicación
- ✅ Sistema de animaciones profesionales (bounce, fade, pulse, shimmer)
- ✅ Perfiles de usuario completos con fotos, calificaciones y géneros
- ✅ Sistema de chat básico (estructura y UI)
- ✅ Geolocalización con cálculo de distancias y ordenamiento
- ✅ Vista de eventos en lista y mapa
- ✅ Tema oscuro con colores neon (NeonPink, NeonPurple, ElectricBlue)
- ✅ Persistencia de sesión con auto-login
- ✅ Manejo de permisos de ubicación con Accompanist
- ✅ Loading states mejorados con NeonLoadingIndicator
- ✅ Transiciones animadas entre pantallas
- ✅ Carga de fotos de perfil desde galería
- 🚧 AddEventScreen (pendiente)
- 🚧 Autocomplete de direcciones con Google Places (pendiente)
- 🚧 Reducir altura de TopBar (pendiente)

### Version 1.0 (Septiembre 2025)
- Implementación inicial de autenticación con Firebase
- Login con email/contraseña
- Registro de usuarios
- Google Sign-In
- Interfaz básica con Jetpack Compose
- Estructura del proyecto establecida
- EventListScreen básico
- LocationManager para GPS

---

## Problemas Conocidos

### Alta Prioridad
1. **Botones de crear evento no funcionan** - Los dos botones en la interfaz (uno en EventListScreen y otro en la navegación) no tienen funcionalidad implementada
2. **TopBar muy gruesa** - La barra superior tiene demasiada altura y necesita ajuste
3. **AddEventScreen no implementada** - Falta la pantalla para crear eventos con mapa y autocomplete

### Media Prioridad
1. Chat solo tiene placeholders - Funcionalidad de mensajería no implementada
2. Búsqueda de DJs no funcional - Solo muestra placeholder
3. Perfil de productora incompleto - Solo tiene pantalla placeholder

### Baja Prioridad
1. Algunas animaciones pueden optimizarse para mejor rendimiento
2. Falta manejo offline completo

---

**Fin del Documento**
