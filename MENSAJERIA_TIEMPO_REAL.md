# Sistema de Mensajería en Tiempo Real

## ✅ Implementación Completada

He implementado un sistema completo de mensajería en tiempo real para DJ Event Hub usando **Firestore** con listeners en tiempo real. El sistema permite que DJs y Productoras se comuniquen instantáneamente.

---

## 🏗️ Arquitectura

### Modelos de Datos

#### 1. **Chat** (`models/Chat.kt`)
Representa una conversación entre dos usuarios:
```kotlin
data class Chat(
    val chatId: String,                           // ID único: "userId1_userId2" (ordenado)
    val participantIds: List<String>,             // [userId1, userId2]
    val participantNames: Map<String, String>,    // userId -> displayName
    val participantImages: Map<String, String?>,  // userId -> profileImageUrl
    val lastMessage: String,                      // Último mensaje enviado
    val lastMessageSenderId: String,              // Quién envió el último mensaje
    val lastMessageTimestamp: Date?,              // Cuándo se envió
    val unreadCount: Map<String, Int>,            // userId -> cantidad de no leídos
    val createdAt: Date?
)
```

**Métodos útiles:**
- `getOtherParticipantId(currentUserId)`: Obtiene el ID del otro usuario
- `getOtherParticipantName(currentUserId)`: Obtiene el nombre del otro usuario
- `getOtherParticipantImage(currentUserId)`: Obtiene la foto del otro usuario
- `getUnreadCount(currentUserId)`: Obtiene mensajes no leídos para el usuario actual

#### 2. **Message** (`models/Message.kt`)
Representa un mensaje individual:
```kotlin
data class Message(
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Date?,
    val read: Boolean
)
```

---

## 📁 Estructura en Firestore

```
chats/
  ├─ {chatId}/                          # e.g., "userId1_userId2"
  │   ├─ participantIds: ["id1", "id2"]
  │   ├─ participantNames: {...}
  │   ├─ lastMessage: "Hola!"
  │   ├─ lastMessageTimestamp: Timestamp
  │   ├─ unreadCount: {"id1": 0, "id2": 3}
  │   └─ messages/                      # Subcolección
  │       ├─ {messageId1}/
  │       │   ├─ senderId: "id1"
  │       │   ├─ text: "Hola!"
  │       │   ├─ timestamp: Timestamp
  │       │   └─ read: false
  │       └─ {messageId2}/
  │           └─ ...
  └─ {anotherChatId}/
      └─ ...
```

---

## 🔧 Componentes Implementados

### 1. **ChatRepository** (`data/ChatRepository.kt`)

#### Métodos principales:

- **`getOrCreateChat(otherUserId, otherUserName, otherUserImage): String`**
  - Obtiene o crea un chat entre el usuario actual y otro usuario
  - Retorna el `chatId`
  - Crea el documento si no existe

- **`observeUserChats(): Flow<List<Chat>>`**
  - Stream en tiempo real de todos los chats del usuario
  - Ordenados por `lastMessageTimestamp` descendente
  - Se actualiza automáticamente cuando hay cambios

- **`observeChatMessages(chatId): Flow<List<Message>>`**
  - Stream en tiempo real de mensajes de un chat específico
  - Ordenados por `timestamp` ascendente
  - Se actualiza instantáneamente cuando llega un mensaje nuevo

- **`sendMessage(chatId, text)`**
  - Envía un mensaje en un chat
  - Actualiza el `lastMessage` y `lastMessageTimestamp` del chat
  - Incrementa el contador de no leídos del destinatario

- **`markMessagesAsRead(chatId)`**
  - Marca todos los mensajes del otro usuario como leídos
  - Resetea el contador de no leídos del usuario actual
  - Se llama automáticamente al abrir un chat

- **`getTotalUnreadCount(): Int`**
  - Obtiene el total de mensajes no leídos en todos los chats
  - Útil para mostrar un badge

---

### 2. **ViewModels**

#### **ChatListViewModel** (`ui/chat/ChatListViewModel.kt`)
```kotlin
sealed class ChatListUiState {
    object Loading
    data class Success(val chats: List<Chat>, val totalUnreadCount: Int)
    data class Error(val message: String)
}
```

- Observa todos los chats del usuario en tiempo real
- Proporciona el total de mensajes no leídos
- Método `refresh()` para forzar actualización

#### **ChatViewModel** (`ui/chat/ChatViewModel.kt`)
```kotlin
sealed class ChatUiState {
    object Loading
    data class Success(val messages: List<Message>, val currentUserId: String)
    data class Error(val message: String)
}
```

- Observa mensajes de un chat específico en tiempo real
- Gestiona el estado del texto del mensaje
- Método `sendMessage()` para enviar
- Método `updateMessageText(text)` para actualizar el input
- Marca mensajes como leídos automáticamente al abrir

---

### 3. **Pantallas**

#### **ChatListScreen** (`ui/chat/ChatListScreen.kt`)
**Lista de conversaciones** con diseño neon-noir:

**Características:**
- ✅ Lista de chats ordenada por mensaje más reciente
- ✅ Avatar del otro participante (foto o inicial con gradiente)
- ✅ Nombre del otro participante
- ✅ Último mensaje (con "Tú:" si lo enviaste tú)
- ✅ Timestamp formateado (HH:mm hoy, día de semana esta semana, dd/MM/yy para antiguo)
- ✅ Badge con contador de mensajes no leídos (rosa brillante)
- ✅ Estado vacío con mensaje "No tienes conversaciones"
- ✅ Manejo de errores con botón "Reintentar"
- ✅ Loading spinner mientras carga

#### **ChatScreen** (`ui/chat/ChatScreen.kt`)
**Pantalla de conversación individual**:

**Características:**
- ✅ Mensajes en burbujas (rosa para enviados, gris para recibidos)
- ✅ Auto-scroll al último mensaje al abrir y al enviar
- ✅ Input bar fijo en la parte inferior
- ✅ Botón de envío (rosa cuando hay texto, gris cuando está vacío)
- ✅ Indicador de carga mientras envía
- ✅ Timestamp en cada mensaje (HH:mm)
- ✅ Diseño responsive (burbujas max 280.dp de ancho)
- ✅ Bordes redondeados asimétricos según quién envió
- ✅ Estado vacío: "No hay mensajes. ¡Envía el primero!"

---

### 4. **ChatHelper** (`ui/chat/ChatHelper.kt`)

Utilidad para iniciar un chat desde cualquier parte de la app:

```kotlin
ChatHelper.startChatWith(
    navController = navController,
    scope = coroutineScope,
    otherUserId = "userId",
    otherUserName = "Nombre",
    otherUserImage = "url"
)
```

- Crea o obtiene el chat
- Navega automáticamente a la pantalla de chat
- Úsalo desde perfiles de usuarios, lista de DJs, etc.

---

## 🎨 Diseño

### Paleta de Colores Utilizada

- **NeonPink** (`#FF006E`): Burbujas de mensajes enviados, badges de no leídos, botón enviar
- **NeonPurple** (`#8338EC`): Gradientes de avatares
- **DeepBlack** (`#0A0A0A`): Fondo principal
- **DarkSurface** (`#1A1A1A`): Cards de chats, burbujas de mensajes recibidos
- **TextPrimary** (`#F5F5F5`): Texto principal
- **TextSecondary** (`#B3B3B3`): Timestamps, placeholders
- **ErrorRed** (`#FF1744`): Mensajes de error

### Componentes Clave

- **Cards con bordes redondeados** (16.dp)
- **Burbujas de mensaje asimétricas** (4.dp en esquina cerca del emisor, 16.dp en las demás)
- **Avatares circulares con glow** (gradiente radial rosa)
- **Badge circular** para mensajes no leídos (rosa brillante, texto negro)
- **Input bar elevado** con `OutlinedTextField` y botón circular

---

## 🚀 Cómo Usar

### 1. **Acceder a Mensajes**

El usuario puede acceder a sus mensajes desde:
- **Bottom Navigation Bar**: Icono de Email (cuarta opción)
- Desde ahí ve la lista de todos sus chats

### 2. **Ver Conversaciones**

En `ChatListScreen`:
- Toca cualquier chat para abrirlo
- El badge rosa muestra cuántos mensajes no leídos hay
- Los chats se ordenan por el mensaje más reciente arriba

### 3. **Enviar Mensajes**

En `ChatScreen`:
- Escribe en el input de la parte inferior
- Toca el botón de enviar (flecha)
- El mensaje aparece instantáneamente
- El otro usuario lo ve en tiempo real

### 4. **Iniciar un Chat Nuevo**

Para iniciar un chat con otro usuario (por ejemplo, desde su perfil):

```kotlin
// En cualquier composable
val scope = rememberCoroutineScope()
val navController = rememberNavController()

Button(onClick = {
    ChatHelper.startChatWith(
        navController = navController,
        scope = scope,
        otherUserId = user.uid,
        otherUserName = user.artistName ?: user.displayName,
        otherUserImage = user.profileImageUrl
    )
}) {
    Text("Enviar mensaje")
}
```

---

## ⚡ Características en Tiempo Real

### Actualizaciones Instantáneas

El sistema usa **Firestore Listeners** para actualizaciones en tiempo real:

1. **Lista de Chats**: Se actualiza cuando:
   - Llega un nuevo mensaje en cualquier chat
   - Se marca un mensaje como leído
   - Se crea un chat nuevo

2. **Mensajes**: Se actualizan cuando:
   - El otro usuario envía un mensaje (aparece instantáneamente)
   - Tu envías un mensaje (aparece sin delay)
   - Se marca como leído

3. **Contador de No Leídos**:
   - Se actualiza en tiempo real
   - Se resetea automáticamente al abrir un chat

---

## 📊 Reglas de Firestore Recomendadas

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Chats: Solo participantes pueden leer/escribir
    match /chats/{chatId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participantIds;
      
      // Mensajes dentro del chat
      match /messages/{messageId} {
        allow read: if request.auth != null && 
          request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participantIds;
        allow create: if request.auth != null && 
          request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participantIds &&
          request.resource.data.senderId == request.auth.uid;
      }
    }
  }
}
```

---

## 🧪 Cómo Probar

### Escenario 1: Ver Lista de Chats
1. Abre la app y autentícate
2. Ve al tab de "Chat" (icono de email)
3. Verás la lista de conversaciones (o mensaje vacío si no hay)

### Escenario 2: Enviar Mensaje
1. En la lista de chats, toca una conversación
2. Escribe un mensaje en el input inferior
3. Toca el botón de enviar (flecha)
4. El mensaje aparece instantáneamente en tu pantalla

### Escenario 3: Recibir Mensaje (Tiempo Real)
1. Abre la app en dos dispositivos con usuarios diferentes
2. En el dispositivo A, envía un mensaje al usuario B
3. En el dispositivo B, el mensaje aparece instantáneamente sin refrescar
4. El badge de no leídos se actualiza automáticamente

### Escenario 4: Iniciar Chat Nuevo
1. Ve al perfil de otro usuario
2. (Futuro) Toca botón "Enviar mensaje"
3. Se crea el chat automáticamente
4. Navegas a la pantalla de chat

---

## 🔧 Integración con Navegación

### Rutas Añadidas

```kotlin
sealed class Screen(val route: String) {
    // ...existing routes...
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{chatId}/{otherUserName}") {
        fun createRoute(chatId: String, otherUserName: String) = 
            "chat/$chatId/$otherUserName"
    }
}
```

### Navegación desde DJMainScreen

El tab de "Chat" en el bottom bar ahora muestra `ChatListScreen` en lugar del placeholder.

---

## ✅ Checklist de Implementación

```
- [x] Modelos de datos (Chat, Message)
- [x] ChatRepository con métodos CRUD
- [x] Listeners en tiempo real (Flows)
- [x] ChatListViewModel con estado de UI
- [x] ChatViewModel con envío y recepción
- [x] ChatListScreen con diseño neon-noir
- [x] ChatScreen con burbujas de mensajes
- [x] Auto-scroll al último mensaje
- [x] Marcado de mensajes como leídos
- [x] Contador de mensajes no leídos
- [x] Badge visual en lista de chats
- [x] Timestamps formateados
- [x] Avatares con fotos o iniciales
- [x] ChatHelper para iniciar chats
- [x] Integración con navegación
- [x] Estados de loading y error
- [x] Estados vacíos
- [x] Diseño responsive
- [x] Compilación exitosa (BUILD SUCCESSFUL)
- [x] Commit realizado
```

---

## 🚀 Próximos Pasos Sugeridos

- [ ] **Notificaciones push** cuando llega un mensaje (Firebase Cloud Messaging)
- [ ] **Indicador de "escribiendo..."** en tiempo real
- [ ] **Envío de imágenes** en mensajes
- [ ] **Confirmación de lectura** con doble check
- [ ] **Timestamp agrupado** (mostrar fecha solo una vez por día)
- [ ] **Búsqueda** en mensajes
- [ ] **Eliminar conversaciones**
- [ ] **Silenciar conversaciones**
- [ ] **Bloquear usuarios**
- [ ] **Mensajes de voz** (audio)
- [ ] **Compartir eventos** directamente en el chat
- [ ] **Paginación** de mensajes antiguos (cargar más al hacer scroll arriba)
- [ ] **Cache local** con Room para offline support

---

## ⚠️ Notas Importantes

1. **ID de Chat**: Se genera concatenando los UIDs ordenados alfabéticamente (`userId1_userId2`), asegurando que siempre sea el mismo sin importar quién inicia el chat.

2. **Seguridad**: Los mensajes y chats solo son accesibles para los participantes (implementar reglas de Firestore).

3. **Performance**: Los listeners se cancelan automáticamente cuando el composable se destruye (usando `awaitClose`).

4. **Timestamps**: Usamos `@ServerTimestamp` para evitar inconsistencias por diferencias de reloj entre dispositivos.

5. **Escalabilidad**: Para chats con muchos mensajes, considera implementar paginación (cargar 50 mensajes iniciales, luego más al hacer scroll).

---

## 📱 Screenshots Conceptuales

### ChatListScreen
```
┌─────────────────────────┐
│ ← Mensajes              │
├─────────────────────────┤
│  ⭕ DJ Carlitos     14:23│
│  Tú: Perfecto!       [3]│ <- Badge rosa
├─────────────────────────┤
│  ⭕ Productora X    Ayer│
│  Hola, disponible?      │
├─────────────────────────┤
│  ⭕ María DJ        Mar │
│  Gracias por todo       │
└─────────────────────────┘
```

### ChatScreen
```
┌─────────────────────────┐
│ ← DJ Carlitos           │
├─────────────────────────┤
│                         │
│  ┌───────────┐          │
│  │ Hola!     │ 14:20    │ <- Gris (recibido)
│  └───────────┘          │
│                         │
│          ┌───────────┐  │
│  14:21   │ Hola! ¿Qué│  │ <- Rosa (enviado)
│          │ tal?      │  │
│          └───────────┘  │
│                         │
├─────────────────────────┤
│ ┌─────────────────┐  ⭕ │
│ │ Escribe mensaje │  ➤ │ <- Input + botón
│ └─────────────────┘     │
└─────────────────────────┘
```

---

**Build Status**: ✅ BUILD SUCCESSFUL  
**Última Compilación**: 2025-01-16  
**Commit**: "Implementar sistema de mensajeria en tiempo real con Firestore"  
**APK**: `app/build/outputs/apk/debug/app-debug.apk`
