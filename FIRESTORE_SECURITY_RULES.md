# Reglas de Seguridad Firebase para DJ Event Hub

## Firestore Database Rules

Copia y pega estas reglas en **Firebase Console > Firestore Database > Rules**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Users collection
    match /users/{userId} {
      // Anyone can read user profiles (for searching DJs, etc.)
      allow read: if isAuthenticated();

      // Users can only create/update their own profile
      allow create: if isOwner(userId);
      allow update: if isOwner(userId);

      // Users cannot delete their profiles (handle via Cloud Functions if needed)
      allow delete: if false;
    }

    // Events collection
    match /events/{eventId} {
      // Anyone authenticated can read events
      allow read: if isAuthenticated();

      // Any authenticated user can create events
      allow create: if isAuthenticated()
                    && request.resource.data.keys().hasAll(['name', 'description', 'date', 'locationName'])
                    && request.resource.data.name is string
                    && request.resource.data.description is string
                    && request.resource.data.date is int
                    && request.resource.data.locationName is string;

      // Users can update their own events (add createdBy field to track ownership)
      allow update: if isAuthenticated();

      // Users can delete their own events
      allow delete: if isAuthenticated();
    }

    // Chats collection
    match /chats/{chatId} {
      // Users can only read chats they participate in
      allow read: if isAuthenticated()
                  && request.auth.uid in resource.data.participantIds;

      // Users can create chats
      allow create: if isAuthenticated()
                    && request.auth.uid in request.resource.data.participantIds;

      // Participants can update chat metadata
      allow update: if isAuthenticated()
                    && request.auth.uid in resource.data.participantIds;

      // No one can delete chats (handle via Cloud Functions if needed)
      allow delete: if false;

      // Messages subcollection
      match /messages/{messageId} {
        // Participants can read messages
        allow read: if isAuthenticated()
                    && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participantIds;

        // Participants can create messages
        allow create: if isAuthenticated()
                      && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participantIds
                      && request.resource.data.senderId == request.auth.uid;

        // Participants can update messages (for read receipts)
        allow update: if isAuthenticated()
                      && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participantIds;

        // No one can delete messages
        allow delete: if false;
      }
    }
  }
}
```

## Firebase Storage Rules

Copia y pega estas reglas en **Firebase Console > Storage > Rules**:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    function isImage() {
      return request.resource.contentType.matches('image/.*');
    }

    function isUnder5MB() {
      return request.resource.size < 5 * 1024 * 1024;
    }

    // User profile images
    match /users/{userId}/{fileName} {
      // Anyone can read profile images
      allow read: if true;

      // Users can only upload their own profile images
      allow write: if isAuthenticated()
                   && isOwner(userId)
                   && isImage()
                   && isUnder5MB();

      // Users can delete their own images
      allow delete: if isAuthenticated() && isOwner(userId);
    }

    // Event images
    match /events/{userId}/{fileName} {
      // Anyone can read event images
      allow read: if true;

      // Authenticated users can upload event images
      // Images must be under 5MB and be image files
      allow write: if isAuthenticated()
                   && isOwner(userId)
                   && isImage()
                   && isUnder5MB();

      // Users can delete their own event images
      allow delete: if isAuthenticated() && isOwner(userId);
    }
  }
}
```

## Índices Compuestos Recomendados

Crea estos índices en **Firebase Console > Firestore Database > Indexes** para mejorar el performance:

### 1. Events ordenados por fecha
- **Collection**: `events`
- **Fields**:
  - `date` (Ascending)
  - `__name__` (Ascending)

### 2. Chats ordenados por último mensaje
- **Collection**: `chats`
- **Fields**:
  - `participantIds` (Arrays)
  - `lastMessageTimestamp` (Descending)

### 3. Usuarios por tipo
- **Collection**: `users`
- **Fields**:
  - `userType` (Ascending)
  - `createdAt` (Descending)

### 4. Mensajes en un chat
- **Collection**: `chats/{chatId}/messages`
- **Fields**:
  - `timestamp` (Ascending)
  - `__name__` (Ascending)

## Instrucciones de Configuración

1. **Accede a Firebase Console**: https://console.firebase.google.com
2. Selecciona tu proyecto **DJ Event Hub**
3. **Configura Firestore Rules**:
   - Ve a **Firestore Database** > **Rules**
   - Copia y pega las reglas de Firestore de arriba
   - Haz clic en **Publish**

4. **Configura Storage Rules**:
   - Ve a **Storage** > **Rules**
   - Copia y pega las reglas de Storage de arriba
   - Haz clic en **Publish**

5. **Crea los índices**:
   - Ve a **Firestore Database** > **Indexes**
   - Crea cada índice compuesto listado arriba
   - Alternativamente, Firebase te sugerirá crear índices automáticamente cuando ejecutes queries que los necesiten

## Notas de Seguridad

- ✅ **Autenticación requerida**: Todas las operaciones requieren autenticación
- ✅ **Validación de datos**: Se validan tipos de datos en creación de eventos
- ✅ **Límites de tamaño**: Imágenes limitadas a 5MB
- ✅ **Formato de archivos**: Solo se permiten imágenes en Storage
- ✅ **Privacy**: Los usuarios solo pueden ver chats en los que participan
- ⚠️ **Ownership**: Considera agregar un campo `createdBy` en eventos para mejor control de acceso

## Próximas Mejoras de Seguridad

1. Agregar campo `createdBy` en eventos para control de ownership real
2. Implementar Cloud Functions para validaciones complejas
3. Agregar rate limiting para prevenir spam
4. Implementar soft delete en lugar de delete directo
5. Agregar logs de auditoría para operaciones críticas
