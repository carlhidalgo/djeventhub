# Diagnóstico y Corrección del Sistema de Chat

## Fecha: 2025-11-21

## Problemas Identificados

### 1. **Query de Firestore con Índice Faltante**
**Problema**: La query `whereArrayContains + orderBy` en `ChatRepository.observeUserChats()` requiere un índice compuesto en Firestore que no existe.

**Código Original**:
```kotlin
val subscription = chatsCollection
    .whereArrayContains("participantIds", currentUserId)
    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING) // ❌ Requiere índice
```

**Solución**: Eliminar el `orderBy` de la query y ordenar localmente en memoria.

**Código Corregido**:
```kotlin
val subscription = chatsCollection
    .whereArrayContains("participantIds", currentUserId)
    .addSnapshotListener { snapshot, error ->
        // ...
        val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
        // Ordenar localmente
        val sortedChats = chats.sortedWith(
            compareByDescending<Chat> { it.lastMessageTimestamp?.time ?: it.createdAt?.time ?: 0L }
        )
        trySend(sortedChats)
    }
```

### 2. **Chats Nuevos Sin `lastMessageTimestamp`**
**Problema**: Cuando se crea un chat nuevo (sin mensajes), no tiene el campo `lastMessageTimestamp`, causando problemas con el ordenamiento y la query.

**Solución**: 
- Inicializar `createdAt` con `FieldValue.serverTimestamp()` al crear el chat
- Ordenar por `lastMessageTimestamp` primero, luego por `createdAt` como fallback

### 3. **Errores Silenciosos en Listeners**
**Problema**: Los errores en los listeners de Firestore se capturaban pero no se reportaban, dificultando la depuración.

**Solución**: Agregar logging detallado con `android.util.Log.e()`:
```kotlin
.addSnapshotListener { snapshot, error ->
    if (error != null) {
        android.util.Log.e("ChatRepository", "Error observing chats: ${error.message}", error)
        trySend(emptyList())
        return@addSnapshotListener
    }
    // ...
}
```

### 4. **Falta de Logging para Debugging**
**Problema**: No había manera de rastrear el flujo de datos desde Firestore hasta la UI.

**Solución**: Agregar logging en múltiples capas:
- `ChatRepository`: Log de errores y datos recibidos
- `ChatListViewModel`: Log del número de chats y sus IDs
- `ChatListScreen`: Log del estado de la UI

### 5. **Inicialización Incompleta de Chats**
**Problema**: Al crear un chat nuevo, algunos campos opcionales no se inicializaban correctamente.

**Solución**: Usar un `HashMap` en lugar del data class para asegurar que todos los campos se escriban a Firestore:
```kotlin
val chatData = hashMapOf(
    "chatId" to chatId,
    "participantIds" to listOf(currentUserId, otherUserId),
    "participantNames" to mapOf(...),
    "participantImages" to mapOf(...),
    "unreadCount" to mapOf(...),
    "lastMessage" to "",
    "lastMessageSenderId" to "",
    "createdAt" to FieldValue.serverTimestamp()
)
chatsCollection.document(chatId).set(chatData).await()
```

### 6. **Falta de Botón Manual de Actualización**
**Problema**: Si los chats no aparecen, el usuario no tiene forma de forzar una recarga.

**Solución**: Agregar botón de refresh en la TopAppBar:
```kotlin
IconButton(onClick = { viewModel.refresh() }) {
    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = NeonPink)
}
```

## Cambios Implementados

### Archivos Modificados:

1. **ChatRepository.kt**
   - ✅ Eliminar `orderBy` de la query
   - ✅ Ordenar localmente por `lastMessageTimestamp` o `createdAt`
   - ✅ Agregar logging de errores
   - ✅ Mejorar inicialización de chats con `HashMap`

2. **ChatListViewModel.kt**
   - ✅ Agregar logging detallado del flujo de datos
   - ✅ Log del número de chats recibidos y sus IDs

3. **ChatListScreen.kt**
   - ✅ Agregar logging del estado Success
   - ✅ Agregar botón de refresh manual en TopAppBar
   - ✅ Importar `Icons.Default.Refresh`

4. **DJMainScreen.kt**
   - ✅ Cambiar `onNavigateBack` a `null` cuando es una pestaña
   - ✅ Agregar log temporal en `onChatClick`

## Cómo Verificar la Solución

### 1. Verificar en Logcat
Después de ejecutar la app, buscar estos logs:

```
ChatRepository: Error observing chats: [mensaje de error]
ChatListViewModel: Starting to observe chats
ChatListViewModel: Received X chats from repository
ChatListViewModel: Chat 0: id=..., participants=[...], lastMessage=...
ChatListViewModel: UI State updated with X chats
ChatListScreen: Success state: X chats, totalUnread=Y
ChatListScreen: Filtered: X chats
```

### 2. Verificar en Firebase Console
1. Ir a Firestore Database
2. Verificar colección `chats`
3. Cada documento debe tener:
   - `chatId`: String con formato "userId1_userId2"
   - `participantIds`: Array con 2 UIDs
   - `participantNames`: Map con nombres
   - `participantImages`: Map con URLs de imágenes
   - `unreadCount`: Map con contadores
   - `createdAt`: Timestamp
   - `lastMessage`: String (vacío inicialmente)
   - `lastMessageSenderId`: String (vacío inicialmente)

### 3. Probar Funcionalidad
1. Como productora: Aceptar una postulación
2. Ir a la pestaña "Chat" (ambos usuarios)
3. Verificar que el chat aparece en la lista
4. Si no aparece, pulsar el botón de refresh (↻)
5. Revisar Logcat para ver los logs

## Posibles Problemas Restantes

### Si los chats aún no aparecen:

1. **Reglas de Firestore**: Verificar que las reglas permitan leer/escribir en `chats`:
```javascript
match /chats/{chatId} {
  allow read, write: if request.auth != null && 
    request.auth.uid in resource.data.participantIds;
}
```

2. **Usuario no autenticado**: Verificar que `FirebaseAuth.getInstance().currentUser?.uid` no sea null

3. **Datos corruptos**: Eliminar documentos de prueba en Firestore y crear uno nuevo

4. **Caché de Firestore**: Limpiar caché de la app en configuración del dispositivo

## Próximos Pasos Recomendados

1. ✅ Implementar navegación al chat detallado desde ChatListScreen
2. ✅ Agregar indicador de "escribiendo..." en tiempo real
3. ✅ Implementar notificaciones push para mensajes nuevos
4. ✅ Agregar soporte para imágenes en mensajes
5. ✅ Implementar eliminación de chats
6. ✅ Agregar búsqueda avanzada de chats

## Notas Técnicas

- El ordenamiento local es más eficiente para listas pequeñas (<1000 chats)
- Si en el futuro hay muchos chats, considerar paginación
- Los índices de Firestore se pueden crear automáticamente desde el error log
- ServerTimestamp asegura consistencia en diferentes zonas horarias
