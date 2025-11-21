# Firebase Security Rules

Este proyecto incluye reglas de seguridad para Firebase Firestore y Storage.

## Archivos de Reglas

- `firestore.rules` - Reglas de seguridad para Firestore Database
- `storage.rules` - Reglas de seguridad para Firebase Storage

## Desplegar Reglas

### Opción 1: Firebase Console (Recomendado para primera vez)

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Para **Firestore**:
   - Ve a `Firestore Database` > `Rules`
   - Copia y pega el contenido de `firestore.rules`
   - Click en `Publish`
4. Para **Storage**:
   - Ve a `Storage` > `Rules`
   - Copia y pega el contenido de `storage.rules`
   - Click en `Publish`

### Opción 2: Firebase CLI (Recomendado para desarrollo continuo)

1. Instala Firebase CLI (si no lo tienes):
   ```bash
   npm install -g firebase-tools
   ```

2. Inicia sesión en Firebase:
   ```bash
   firebase login
   ```

3. Inicializa Firebase en tu proyecto (solo primera vez):
   ```bash
   firebase init
   ```
   - Selecciona `Firestore` y `Storage`
   - Usa los archivos existentes cuando pregunte

4. Despliega las reglas:
   ```bash
   firebase deploy --only firestore:rules
   firebase deploy --only storage:rules
   ```

   O despliega ambas a la vez:
   ```bash
   firebase deploy --only firestore:rules,storage:rules
   ```

## Reglas Implementadas

### Firestore Rules

#### Users Collection
- ✅ Cualquier usuario autenticado puede **leer** perfiles
- ✅ Solo puedes **crear** tu propio perfil
- ✅ Solo puedes **actualizar** tu propio perfil
- ❌ **No se permite eliminar** perfiles

#### Events Collection
- ✅ Cualquier usuario autenticado puede **leer** eventos
- ✅ Solo Productoras pueden **crear** eventos
- ✅ Solo el creador puede **actualizar** información básica del evento
- ✅ DJs pueden **agregarse/eliminarse** de la lista de aplicantes
- ✅ Productoras pueden **seleccionar un DJ** de la lista de aplicantes
- ✅ Solo el creador puede **eliminar** eventos

#### Chats Collection
- ✅ Solo puedes **leer** chats en los que participas
- ✅ Puedes **crear** chats si eres uno de los participantes
- ✅ Puedes **actualizar** chats en los que participas (enviar mensajes)
- ❌ **No se permite eliminar** chats

#### Messages Subcollection
- ✅ Solo puedes **leer** mensajes de chats en los que participas
- ✅ Solo puedes **crear** mensajes en chats en los que participas
- ✅ Puedes **actualizar** mensajes para marcarlos como leídos
- ❌ **No se permite eliminar** mensajes

### Storage Rules

#### User Profile Images (`/users/{userId}/profile.jpg`)
- ✅ Cualquier usuario autenticado puede **leer**
- ✅ Solo el dueño puede **escribir** (máx 5MB, solo imágenes)
- ✅ Solo el dueño puede **eliminar**

#### Event Images (`/events/{userId}/{imageFile}`)
- ✅ Cualquier usuario autenticado puede **leer**
- ✅ Solo el dueño (Productora) puede **escribir** (máx 5MB, solo imágenes)
- ✅ Solo el dueño puede **eliminar**

## Validaciones de Seguridad

Las reglas implementan las siguientes validaciones:

1. **Autenticación obligatoria**: Todas las operaciones requieren usuario autenticado
2. **Validación de tipos de usuario**: Se verifica el tipo de usuario (DJ/PRODUCTORA) para operaciones específicas
3. **Protección de campos**: Campos como `createdBy`, `email`, `uid` no pueden ser modificados
4. **Validación de permisos**: Solo los dueños pueden modificar sus propios datos
5. **Validación de archivos**: Solo imágenes de máximo 5MB
6. **Prevención de escalada de privilegios**: Los usuarios no pueden cambiar su rol o datos de otros

## Testing de Reglas

Puedes probar las reglas en Firebase Console:
1. Ve a `Firestore Database` o `Storage` > `Rules`
2. Click en la pestaña `Rules Playground`
3. Simula operaciones para verificar el comportamiento

## Notas Importantes

⚠️ **IMPORTANTE**: Despliega estas reglas ANTES de lanzar a producción.

⚠️ Las reglas actuales son para el modelo actual de la app. Si modificas la estructura de datos, actualiza las reglas correspondientes.

⚠️ Mantén sincronizados los archivos locales con Firebase para tener versionamiento de reglas.

## Monitoreo

Revisa regularmente los logs de seguridad en Firebase Console:
- `Firestore` > `Usage` > `Security Rules`
- `Storage` > `Usage` > `Security Rules`

Esto te ayudará a detectar intentos de acceso no autorizado.
