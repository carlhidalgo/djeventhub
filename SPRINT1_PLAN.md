# 🎯 Sprint 1: Notificaciones + Reviews + Badge

## Fecha: 2025-01-21
## Duración Estimada: 2 semanas

---

## 📋 Tareas del Sprint

### Fase 1: Configuración de Firebase Cloud Messaging (FCM)
- [ ] 1.1 Agregar dependencias de FCM en build.gradle.kts
- [ ] 1.2 Crear servicio MyFirebaseMessagingService
- [ ] 1.3 Configurar AndroidManifest.xml para notificaciones
- [ ] 1.4 Solicitar permiso de notificaciones (Android 13+)
- [ ] 1.5 Guardar FCM token en Firestore (colección users)

### Fase 2: Sistema de Notificaciones Locales
- [ ] 2.1 Crear NotificationHelper para crear canales
- [ ] 2.2 Implementar NotificationManager centralizado
- [ ] 2.3 Diseñar notificaciones con estilos Material 3
- [ ] 2.4 Configurar deep links para navegación desde notificación

### Fase 3: Triggers de Notificaciones
- [ ] 3.1 Notificación cuando DJ postula a evento (→ Productora)
- [ ] 3.2 Notificación cuando Productora acepta (→ DJ)
- [ ] 3.3 Notificación de mensaje nuevo en chat (→ Ambos)
- [ ] 3.4 Notificación de evento próximo 24h antes (→ Ambos)
- [ ] 3.5 Implementar Cloud Functions para enviar notificaciones

### Fase 4: Badge de Mensajes Sin Leer
- [ ] 4.1 Agregar contador en BottomNavigationBar (DJ y Productora)
- [ ] 4.2 Actualizar en tiempo real desde ChatRepository
- [ ] 4.3 Limpiar contador al abrir ChatListScreen
- [ ] 4.4 Persistir contador en caso de cierre de app

### Fase 5: Sistema de Reviews
- [ ] 5.1 Crear modelo Review en Firestore
- [ ] 5.2 Crear ReviewRepository para CRUD
- [ ] 5.3 Implementar ReviewDialog con rating + texto + foto
- [ ] 5.4 Agregar vista de reviews en perfil público del DJ
- [ ] 5.5 Actualizar rating del usuario al agregar review
- [ ] 5.6 Validación: solo puede reviewear si trabajaron juntos

### Fase 6: Testing y Refinamiento
- [ ] 6.1 Probar notificaciones en dispositivo físico
- [ ] 6.2 Probar notificaciones en background/foreground
- [ ] 6.3 Verificar que badge se actualiza correctamente
- [ ] 6.4 Probar sistema de reviews end-to-end
- [ ] 6.5 Optimizar queries de Firestore
- [ ] 6.6 Code review y refactoring

---

## 🗂️ Estructura de Archivos a Crear

```
app/src/main/java/com/example/djeventhub/
├── notifications/
│   ├── MyFirebaseMessagingService.kt
│   ├── NotificationHelper.kt
│   ├── NotificationChannels.kt
│   └── DeepLinkHandler.kt
├── data/
│   └── ReviewRepository.kt
├── models/
│   └── Review.kt
├── ui/reviews/
│   ├── ReviewDialog.kt
│   ├── ReviewsListScreen.kt
│   └── ReviewViewModel.kt
└── workers/
    └── EventReminderWorker.kt (WorkManager para recordatorios)
```

---

## 📊 Modelos de Datos

### Review Model
```kotlin
data class Review(
    val reviewId: String = "",
    val eventId: String = "",
    val reviewerId: String = "", // quien escribe el review
    val reviewedUserId: String = "", // a quien se le escribe
    val rating: Int = 5, // 1-5 estrellas
    val comment: String = "",
    val photoUrls: List<String> = emptyList(),
    val createdAt: Date? = null,
    val verified: Boolean = false // true si trabajaron juntos
)
```

### User Model (agregar campo)
```kotlin
data class User(
    // ...existing fields...
    val fcmToken: String? = null, // ← NUEVO
    val reviews: List<String> = emptyList() // IDs de reviews recibidos
)
```

---

## 🔔 Tipos de Notificaciones

### 1. Nueva Postulación (→ Productora)
```
Título: "Nueva postulación 🎵"
Mensaje: "[DJ Name] se postuló a tu evento [Event Name]"
Acción: Abrir ApplicationsListScreen
```

### 2. Postulación Aceptada (→ DJ)
```
Título: "¡Te aceptaron! 🎉"
Mensaje: "Tu postulación a [Event Name] fue aceptada"
Acción: Abrir EventDetailScreen
```

### 3. Mensaje Nuevo (→ Ambos)
```
Título: "[Sender Name]"
Mensaje: "[Texto del mensaje]"
Acción: Abrir ChatScreen con chatId
```

### 4. Evento Próximo (→ Ambos)
```
Título: "Evento mañana 📅"
Mensaje: "[Event Name] es en 24 horas"
Acción: Abrir EventDetailScreen
```

---

## 🎨 Diseño del Badge

```
┌─────────────────────────┐
│   [Icon: Home]          │
│      Inicio             │ ← Sin badge
└─────────────────────────┘

┌─────────────────────────┐
│   [Icon: Chat] (3)      │ ← Badge con número
│      Chat               │
└─────────────────────────┘
```

---

## 🚀 Orden de Implementación

**DÍA 1-2: FCM + NotificationHelper**
- Configurar FCM
- Crear servicio y helper
- Solicitar permisos

**DÍA 3-4: Triggers de Notificaciones**
- Implementar en EventRepository cuando hay nueva postulación
- Implementar en ApplicationsViewModel cuando se acepta
- Implementar en ChatRepository cuando hay nuevo mensaje

**DÍA 5-6: Badge + Deep Links**
- Agregar badge en BottomNav
- Configurar deep links
- Testar navegación

**DÍA 7-9: Sistema de Reviews**
- Crear modelos y repository
- Implementar UI del dialog
- Integrar con perfiles

**DÍA 10-11: Testing Completo**
- Probar todos los flujos
- Fix bugs
- Optimizar

**DÍA 12-14: Buffer y Refinamiento**
- Ajustes finales
- Documentación
- Deploy

---

## ✅ Criterios de Aceptación

### Notificaciones
- [x] Usuario recibe notificación cuando corresponde
- [x] Notificación funciona en background y foreground
- [x] Deep link navega a la pantalla correcta
- [x] Notificación tiene el estilo correcto (Material 3)

### Badge
- [x] Badge muestra número correcto de mensajes sin leer
- [x] Badge se actualiza en tiempo real
- [x] Badge se limpia al abrir el chat
- [x] Badge persiste entre sesiones

### Reviews
- [x] Solo puede reviewear si trabajaron juntos
- [x] Review incluye rating + texto + opcional foto
- [x] Reviews aparecen en perfil público del DJ
- [x] Rating promedio se actualiza correctamente
- [x] No se puede reviewear dos veces al mismo evento

---

## 🎯 Métricas de Éxito

- **Tasa de apertura de notificaciones**: >40%
- **Engagement en chat**: +200% (con badge)
- **Reviews completados**: >60% de eventos finalizados
- **Crashes**: 0 relacionados con notificaciones

---

## 🔗 Referencias

- [Firebase Cloud Messaging Android](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Android Notification Channels](https://developer.android.com/develop/ui/views/notifications)
- [Material 3 Badge](https://m3.material.io/components/badges)
- [WorkManager for Reminders](https://developer.android.com/topic/libraries/architecture/workmanager)

---

## 📝 Notas Importantes

- **Permisos Android 13+**: Necesitas solicitar `POST_NOTIFICATIONS` en runtime
- **FCM Token**: Se debe actualizar cuando cambia (raro pero posible)
- **Background Restrictions**: Notificaciones pueden fallar si la app está en Doze mode
- **Rate Limiting**: Firebase tiene límites de envío (500 notificaciones/segundo)
- **Testing**: Usar dispositivo físico, notificaciones no funcionan bien en emulador

---

## 🐛 Posibles Issues y Soluciones

### Issue: Notificación no aparece en background
**Solución**: Verificar que `MyFirebaseMessagingService` está en el Manifest con prioridad alta

### Issue: Badge no se actualiza
**Solución**: Usar `StateFlow` y `collectAsState` correctamente, verificar que el Flow no se cancela

### Issue: Deep link no navega
**Solución**: Verificar que `android:launchMode="singleTop"` está en MainActivity

### Issue: Review duplicado
**Solución**: Agregar constraint único en Firestore: `{eventId}_{reviewerId}`

---

## 🎉 Entregables

Al final del Sprint 1 deberás tener:

1. ✅ Notificaciones funcionando en 4 escenarios
2. ✅ Badge de mensajes sin leer funcionando
3. ✅ Sistema de reviews completo
4. ✅ Tests básicos escritos
5. ✅ Documentación actualizada
6. ✅ Commit en Git con mensaje descriptivo
7. ✅ Video demo de 2 minutos mostrando las features

---

## 🚀 Siguiente: Sprint 2 - Calendario

Después de completar este sprint, continuaremos con:
- Vista de calendario mensual/semanal
- Detección de conflictos de horario
- Integración con Google Calendar
- Export a .ics

---

**IMPORTANTE**: Este plan está vivo y puede ajustarse según avancemos. Cualquier blocker debe reportarse inmediatamente.
