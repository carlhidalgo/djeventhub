# Guía de Componentes Reutilizables - DJ Event Hub

## 📋 Índice
1. [Introducción](#introducción)
2. [Componentes Básicos](#componentes-básicos)
3. [Componentes de Tarjetas](#componentes-de-tarjetas)
4. [Patrones de Uso](#patrones-de-uso)
5. [Migración](#migración)

---

## Introducción

Este sistema de componentes reutilizables fue creado para mejorar la consistencia visual y reducir la duplicación de código en la aplicación DJ Event Hub.

### Archivos Principales
- `CommonComponents.kt` - Componentes básicos (botones, campos de texto, badges, etc.)
- `Cards.kt` - Tarjetas especializadas (eventos, perfiles, aplicaciones, chat)
- `ComponentsExample.kt` - Ejemplos de uso y referencia
- `CompactTopBar.kt` - TopBar existente (ahora complementado con StandardTopBar)

---

## Componentes Básicos

### 1. StandardTopBar

**Uso:** TopBar consistente para todas las pantallas

```kotlin
StandardTopBar(
    title = "Mi Pantalla",
    onNavigationClick = { navController.popBackStack() },
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription = "Volver",
    actions = {
        IconButton(onClick = { /* acción */ }) {
            Icon(Icons.Default.Settings, "Configuración")
        }
    }
)
```

**Características:**
- ✅ Maneja padding de status bar automáticamente
- ✅ Tipografía consistente
- ✅ Soporte para acciones personalizadas
- ✅ Iconos de navegación opcionales

---

### 2. DJTextField

**Uso:** Campo de texto estándar con validación

```kotlin
DJTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    placeholder = "ejemplo@email.com",
    isError = emailError != null,
    errorMessage = emailError,
    supportingText = "Ingresa tu correo electrónico",
    leadingIcon = {
        Icon(Icons.Default.Email, null)
    }
)
```

**Características:**
- ✅ Estilo neon-noir consistente
- ✅ Validación integrada con mensajes de error
- ✅ Soporte para iconos leading/trailing
- ✅ Texto de soporte opcional

---

### 3. DJPasswordField

**Uso:** Campo de contraseña con toggle de visibilidad

```kotlin
DJPasswordField(
    value = password,
    onValueChange = { password = it },
    label = "Contraseña",
    isError = passwordError != null,
    errorMessage = passwordError
)
```

**Características:**
- ✅ Toggle "Ver/Ocultar" integrado
- ✅ Mismas características que DJTextField

---

### 4. Botones

#### DJPrimaryButton
```kotlin
DJPrimaryButton(
    text = "Guardar Cambios",
    onClick = { /* acción */ },
    icon = Icons.Default.Save,
    isLoading = isSaving,
    enabled = formIsValid
)
```

#### DJSecondaryButton
```kotlin
DJSecondaryButton(
    text = "Cancelar",
    onClick = { navController.popBackStack() },
    icon = Icons.Default.Close,
    borderColor = ErrorRed
)
```

#### DJTertiaryButton
```kotlin
DJTertiaryButton(
    text = "¿Olvidaste tu contraseña?",
    onClick = { /* acción */ },
    color = ElectricBlue
)
```

**Características:**
- ✅ Estados de loading automáticos
- ✅ Iconos opcionales
- ✅ Colores personalizables
- ✅ Alto consistente (50.dp)

---

### 5. Badges

#### Badge Estándar
```kotlin
Badge(
    text = "Nuevo",
    backgroundColor = NeonPink.copy(alpha = 0.2f),
    textColor = NeonPink
)
```

#### Badge con Animación
```kotlin
PulsingBadge(
    text = "¡En vivo!",
    backgroundColor = ErrorRed.copy(alpha = 0.2f),
    textColor = ErrorRed
)
```

---

### 6. InfoCard

**Uso:** Tarjetas informativas con iconos

```kotlin
InfoCard(
    icon = Icons.Default.LocationOn,
    iconTint = ElectricBlue,
    title = "Ubicación",
    content = "Madrid, España",
    hasGradientBorder = true
)
```

**Casos de uso:**
- Mostrar información de ubicación
- Mensajes de advertencia
- Confirmaciones
- Información contextual

---

### 7. SectionHeader

**Uso:** Headers de sección para agrupar contenido

```kotlin
SectionHeader(
    text = "Información Personal",
    icon = Icons.Default.Person
)
```

---

### 8. EmptyState

**Uso:** Estado vacío con CTA opcional

```kotlin
EmptyState(
    icon = Icons.Default.EventAvailable,
    title = "No hay eventos",
    description = "Aún no tienes eventos. ¡Crea tu primer evento!",
    actionText = "Crear Evento",
    onActionClick = { navController.navigate("add_event") }
)
```

---

### 9. LoadingOverlay

**Uso:** Overlay de carga full-screen

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Contenido de la pantalla

    LoadingOverlay(isLoading = isLoading)
}
```

---

### 10. GradientDivider

**Uso:** Divisor con gradiente

```kotlin
GradientDivider(
    thickness = 2.dp,
    startColor = NeonPink.copy(alpha = 0.5f),
    endColor = NeonPurple.copy(alpha = 0.5f)
)
```

---

## Componentes de Tarjetas

### 1. EventCard

**Uso:** Tarjeta de evento completa con distancia

```kotlin
EventCard(
    eventWithDistance = eventWithDistance,
    onEventClick = { eventId ->
        navController.navigate("event_detail/$eventId")
    },
    onMapClick = { event ->
        openInMaps(context, event)
    },
    showDistance = true,
    modifier = Modifier.padding(8.dp)
)
```

**Características:**
- ✅ Animación de pulso para eventos próximos
- ✅ Borde gradiente neon para destacar
- ✅ Badge de distancia
- ✅ Botón de mapa integrado
- ✅ Badge de fecha

---

### 2. CompactEventCard

**Uso:** Tarjeta compacta para listas densas

```kotlin
CompactEventCard(
    event = event,
    onClick = { /* navegar */ },
    modifier = Modifier.padding(4.dp)
)
```

---

### 3. DJProfileCard

**Uso:** Tarjeta de perfil de DJ

```kotlin
DJProfileCard(
    djId = dj.id,
    djName = dj.name,
    djImageUrl = dj.imageUrl,
    genres = listOf("Techno", "House", "EDM"),
    rating = 4.5,
    onClick = {
        navController.navigate("dj_profile/${dj.id}")
    }
)
```

**Características:**
- ✅ Imagen de perfil con placeholder
- ✅ Rating con estrellas
- ✅ Badges de géneros musicales
- ✅ Límite de 2 géneros visibles + contador

---

### 4. ApplicationCard

**Uso:** Tarjeta de solicitud de DJ a evento

```kotlin
ApplicationCard(
    djName = application.djName,
    djImageUrl = application.djImageUrl,
    applicationDate = application.timestamp,
    status = "Pendiente",
    onViewProfile = {
        navController.navigate("dj_profile/${application.djId}")
    },
    onAccept = {
        viewModel.acceptApplication(application.id)
    },
    onReject = {
        viewModel.rejectApplication(application.id)
    }
)
```

**Características:**
- ✅ Estados: Pendiente, Aceptado, Rechazado
- ✅ Botones de acción para estado pendiente
- ✅ Badge de estado con colores dinámicos
- ✅ Click en imagen para ver perfil

---

### 5. ChatPreviewCard

**Uso:** Tarjeta de preview de chat

```kotlin
ChatPreviewCard(
    chatName = chat.otherUserName,
    lastMessage = chat.lastMessage,
    timestamp = chat.lastMessageTime,
    unreadCount = chat.unreadCount,
    imageUrl = chat.otherUserImage,
    onClick = {
        navController.navigate("chat/${chat.id}/${chat.otherUserName}")
    }
)
```

**Características:**
- ✅ Contador de no leídos
- ✅ Timestamp formateado
- ✅ Imagen de perfil
- ✅ Resaltado visual para mensajes no leídos

---

## Patrones de Uso

### Patrón 1: Formularios

```kotlin
@Composable
fun MyFormScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Registro",
                onNavigationClick = { /* volver */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(text = "Información Personal")

            DJTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre"
            )

            DJTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            DJPasswordField(
                value = password,
                onValueChange = { password = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            DJPrimaryButton(
                text = "Registrarse",
                onClick = { /* submit */ },
                isLoading = isLoading
            )
        }
    }
}
```

---

### Patrón 2: Listas con Estado Vacío

```kotlin
@Composable
fun MyListScreen(
    items: List<Item>,
    isLoading: Boolean
) {
    Scaffold(
        topBar = {
            StandardTopBar(title = "Mis Items")
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                // Loading state
                LoadingOverlay(isLoading = true)
            } else if (items.isEmpty()) {
                // Empty state
                EmptyState(
                    icon = Icons.Default.Inbox,
                    title = "No hay items",
                    description = "Agrega tu primer item",
                    actionText = "Agregar",
                    onActionClick = { /* acción */ }
                )
            } else {
                // List
                LazyColumn {
                    items(items) { item ->
                        // Item card
                    }
                }
            }
        }
    }
}
```

---

### Patrón 3: Pantalla con Información

```kotlin
@Composable
fun MyInfoScreen() {
    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Información",
                onNavigationClick = { /* volver */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                icon = Icons.Default.Info,
                title = "Aviso",
                content = "Información importante aquí"
            )

            InfoCard(
                icon = Icons.Default.Warning,
                title = "Advertencia",
                content = "Ten cuidado con esto",
                backgroundColor = WarningOrange.copy(alpha = 0.1f),
                iconTint = WarningOrange
            )

            GradientDivider()

            SectionHeader(text = "Detalles")

            // Más contenido...
        }
    }
}
```

---

## Migración

### Cómo Migrar Pantallas Existentes

#### 1. TopBar
**Antes:**
```kotlin
SmallTopAppBar(
    title = {
        Text(
            "Mi Título",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
        }
    },
    colors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
)
```

**Después:**
```kotlin
StandardTopBar(
    title = "Mi Título",
    onNavigationClick = onBack
)
```

---

#### 2. TextField
**Antes:**
```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Email") },
    isError = emailError != null,
    modifier = Modifier.fillMaxWidth(),
    singleLine = true
)
if (emailError != null) {
    Text(
        emailError,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp
    )
}
```

**Después:**
```kotlin
DJTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    isError = emailError != null,
    errorMessage = emailError
)
```

---

#### 3. Botones
**Antes:**
```kotlin
Button(
    onClick = { /* acción */ },
    modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
    shape = RoundedCornerShape(12.dp)
) {
    Text(
        text = "Guardar",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}
```

**Después:**
```kotlin
DJPrimaryButton(
    text = "Guardar",
    onClick = { /* acción */ }
)
```

---

#### 4. Event Card
**Antes:**
```kotlin
EventListItem(
    eventWithDistance = eventWithDistance,
    onMapClick = { event -> openInMaps(context, event) },
    onEventClick = { eventId -> /* navegar */ },
    modifier = Modifier.animateItemPlacement(index)
)
```

**Después:**
```kotlin
EventCard(
    eventWithDistance = eventWithDistance,
    onEventClick = { eventId -> /* navegar */ },
    onMapClick = { event -> openInMaps(context, event) },
    modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .animateItemPlacement(index)
)
```

---

## Beneficios del Sistema

### ✅ Consistencia Visual
- Todos los componentes usan el mismo sistema de colores
- Tipografía uniforme en toda la app
- Espaciado y padding estandarizado

### ✅ Menos Código
- Reducción de ~40% de código repetitivo
- Componentes de una línea en lugar de 10-20 líneas

### ✅ Mantenibilidad
- Cambios centralizados en un solo lugar
- Fácil de actualizar el diseño globalmente
- Menos bugs por inconsistencias

### ✅ Desarrollo Más Rápido
- No reinventar la rueda en cada pantalla
- Copiar y pegar ejemplos de la guía
- Focus en la lógica, no en el UI

---

## Colores del Theme

Referencia rápida de colores disponibles:

```kotlin
// Neon Accents
NeonPink      // #FF006E - Acciones primarias
NeonOrange    // #FF6B35 - Acentos cálidos
ElectricBlue  // #3A86FF - Links, secundarios
NeonPurple    // #8338EC - Highlights especiales
NeonCyan      // #00F5FF - Estados de éxito

// Dark Surfaces
DeepBlack          // #0A0A0A - Fondo principal
DarkSurface        // #1A1A1A - Cards
DarkSurfaceVariant // #2A2A2A - Superficies elevadas

// Text
TextPrimary    // #F5F5F5 - Texto principal
TextSecondary  // #B3B3B3 - Texto secundario
TextTertiary   // #808080 - Texto terciario

// Status
SuccessGreen   // #00E676
ErrorRed       // #FF1744
WarningOrange  // #FFAB00
```

---

## Próximos Pasos

1. ✅ Crear componentes básicos
2. ✅ Crear componentes de tarjetas
3. ✅ Documentar con ejemplos
4. 🔄 Migrar pantallas existentes progresivamente
5. 📝 Agregar más componentes según necesidad

---

## Soporte

Para preguntas o sugerencias sobre los componentes, revisa:
- `ComponentsExample.kt` - Ejemplos interactivos
- Este documento - Guía completa
- Código fuente de los componentes para casos avanzados
