# Configuración de Firebase Authentication

## Estado actual del proyecto

✅ **Completado:**
- Firebase Auth SDK integrado en build.gradle.kts
- google-services.json configurado
- AuthViewModel implementado con Email/Password y Google Sign-In
- LoginScreen con diseño profesional
- Navigation entre Login y Events
- LocationManager para GPS
- Ordenamiento de eventos por cercanía
- Integración con Google Maps
- Permisos de ubicación con UX pulido

## Paso pendiente: Activar proveedores en Firebase Console

Para que la autenticación funcione, debes activar los proveedores en la consola de Firebase:

### 1. Acceder a Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto "DJEventHub" (o como lo hayas nombrado)

### 2. Activar proveedor Email/Password

1. En el menú lateral, ve a **Build > Authentication**
2. Haz clic en la pestaña **Sign-in method**
3. Busca **Email/Password** en la lista de proveedores
4. Haz clic en el lápiz (editar)
5. Activa el toggle **Enable**
6. Haz clic en **Save**

### 3. Activar proveedor Google Sign-In

1. En la misma pantalla de **Sign-in method**
2. Busca **Google** en la lista de proveedores
3. Haz clic en el lápiz (editar)
4. Activa el toggle **Enable**
5. Selecciona un email de soporte del proyecto (puede ser tu email)
6. Haz clic en **Save**

### 4. Configurar SHA-1 para Google Sign-In (Android)

Para que Google Sign-In funcione en Android, necesitas agregar el SHA-1 de tu keystore:

#### Obtener SHA-1/256 del keystore de debug

Ejecuta:

```powershell
cd C:\Users\carlos\AndroidStudioProjects\DJEventHub
./gradlew signingReport
```

Resumen (ya obtenido en tu entorno):

- Variant: debug
  - Store: `C:\Users\carlos\.android\debug.keystore`
  - Alias: `AndroidDebugKey`
  - SHA‑1: `AF:1F:79:B4:6D:55:E9:FE:05:0C:6C:4C:BC:86:D4:5A:4F:89:23:4F`
  - SHA‑256: `09:4D:34:48:97:6F:AC:5E:44:CE:BF:D2:E6:42:A7:E4:A0:6A:4C:48:A8:97:8A:21:CD:5D:73:12:C1:7A:95:4B`

> Nota: para release verás `Config: null` hasta que configures una firma release (`signingConfigs { release { ... } }`).

#### Agregar SHA-1 a Firebase

1. En Firebase Console, ve a **Project Settings** (ícono de engranaje)
2. En **Your apps**, selecciona tu app Android (`applicationId`: `com.example.djeventhub`)
3. Haz clic en **Add fingerprint**
4. Pega el SHA-1 de debug anterior
5. Guarda los cambios
6. Descarga el nuevo **google-services.json** y reemplázalo en `app/google-services.json`

> Si luego configuras una firma de release, repite el proceso agregando el SHA-1 de release.

## Funcionalidades implementadas

### 1. Autenticación Firebase
- ✅ Login con Email/Password
- ✅ Registro con Email/Password
- ✅ Google Sign-In
- ✅ Logout
- ✅ Persistencia de sesión (auto-login)

### 2. Ubicación y GPS
- ✅ Permisos de ubicación solicitados en runtime
- ✅ Diálogo explicativo cuando se deniegan permisos
- ✅ FusedLocationProviderClient para obtener ubicación actual
- ✅ Cálculo de distancia usando Haversine
- ✅ Ordenamiento de eventos por cercanía

### 3. Lista de Eventos
- ✅ Eventos de ejemplo con coordenadas (Madrid)
- ✅ Mostrar distancia en cada card ("2.3 km")
- ✅ Ordenamiento: eventos más cercanos primero
- ✅ Botón para abrir en Google Maps
- ✅ Fallback si Google Maps no está instalado
- ✅ Botón de refresh para actualizar ubicación

### 4. Diseño y UX
- ✅ LoginScreen con logo profesional
- ✅ Título "DJ Event Hub" y subtítulo
- ✅ Validación de email y password
- ✅ Mensajes de error claros
- ✅ Loading states
- ✅ Navegación fluida entre pantallas
- ✅ TopAppBar con logout y refresh

## Datos de prueba

El `EventRepository` tiene 4 eventos de ejemplo en el área de Madrid:

1. **Fiesta Electrónica en Kapital** - Teatro Kapital (40.4168, -3.7038)
2. **Noche de Reggaeton en Fabrik** - Fabrik (40.3167, -3.8897)
3. **Sesión de Deep House** - Sala But (40.4239, -3.6926)
4. **Festival Indie Rock** - La Riviera (40.3910, -3.6974)

## Próximos pasos sugeridos

1. **Activar proveedores en Firebase Console** (según instrucciones arriba)
2. **Probar la app en emulador o dispositivo:**
   - Crear cuenta con email/password
   - Login con Google
   - Ver lista de eventos ordenados por distancia
   - Hacer clic en ubicación para abrir Google Maps
   - Probar logout
3. **Personalizar:**
   - Cambiar colores en theme
   - Agregar más eventos de prueba
   - Conectar con API real cuando esté lista

## Comandos útiles

### Compilar el proyecto:
```powershell
cd C:\Users\carlos\AndroidStudioProjects\DJEventHub
./gradlew build
```

### Ejecutar en emulador:
```powershell
./gradlew installDebug
```

### Ver logs de Firebase Auth:
```bash
adb logcat | grep -i "firebase"
```

## Troubleshooting

### Error: "The API key is invalid"
- Asegúrate de que `google-services.json` está en `app/`
- Verifica que el applicationId en `build.gradle.kts` coincide con el de Firebase Console

### Google Sign-In no funciona:
- Verifica que agregaste el SHA-1 en Firebase Console
- Asegúrate de que descargaste el nuevo `google-services.json` después de agregar el SHA-1
- Revisa que el `default_web_client_id` en `strings.xml` sea correcto

### Ubicación no se obtiene:
- Verifica que aceptaste los permisos de ubicación
- En emulador, configura una ubicación mock en Extended Controls
- Verifica que el GPS esté activado en el dispositivo

## Estructura de archivos creados/modificados

```
app/src/main/java/com/example/djeventhub/
├── MainActivity.kt (actualizado con permisos y navegación)
├── Event.kt (ya tenía lat/lng)
├── EventRepository.kt (actualizado con datos mock)
├── EventListViewModel.kt (actualizado con sorting por distancia)
├── EventListScreen.kt (actualizado con UI mejorada)
├── location/
│   └── LocationManager.kt (nuevo)
├── navigation/
│   └── AppNavigation.kt (nuevo)
└── ui/auth/
    ├── AuthViewModel.kt (ya existía)
    └── LoginScreen.kt (mejorado diseño)

app/src/main/
├── AndroidManifest.xml (permisos de ubicación y meta-data de Maps agregados)
└── res/values/strings.xml (ya tenía default_web_client_id)
```

---

**Listo para probar!** 🎉

Una vez actives los proveedores en Firebase Console y agregues la huella SHA‑1, la app estará completamente funcional.