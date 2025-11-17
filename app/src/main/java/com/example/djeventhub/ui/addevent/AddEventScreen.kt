package com.example.djeventhub.ui.addevent

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var musicGenre by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis() + 3600000) } // +1 hour by default

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var showMap by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Autocomplete inline
    var addressPredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearchingAddress by remember { mutableStateOf(false) }
    var showPredictions by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    val token = remember { AutocompleteSessionToken.newInstance() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Autocomplete search effect
    LaunchedEffect(locationName) {
        if (locationName.length >= 3) {
            showPredictions = true
            isSearchingAddress = true
            delay(500) // Debounce

            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(locationName)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    addressPredictions = response.autocompletePredictions
                    isSearchingAddress = false
                }
                .addOnFailureListener { exception ->
                    isSearchingAddress = false
                    addressPredictions = emptyList()
                }
        } else {
            addressPredictions = emptyList()
            showPredictions = false
            isSearchingAddress = false
        }
    }

    // Default map position (Madrid, Spain)
    val defaultPosition = LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: defaultPosition, 15f)
    }

    // Update camera position when location is selected
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    // Update selectedLocation from ViewModel's captured location
    LaunchedEffect(uiState) {
        if (uiState is AddEventUiState.LocationCaptured) {
            val loc = uiState as AddEventUiState.LocationCaptured
            selectedLocation = LatLng(loc.lat, loc.lng)
        }
    }

    // Handle success
    LaunchedEffect(uiState) {
        if (uiState is AddEventUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Evento",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.height(48.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del evento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AddEventUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Imagen del evento",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar imagen",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca para agregar imagen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                enabled = uiState !is AddEventUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Music genre field (optional)
            OutlinedTextField(
                value = musicGenre,
                onValueChange = { musicGenre = it },
                label = { Text("Género musical (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AddEventUiState.Loading,
                supportingText = {
                    Text("Ej: Techno, House, Reggaeton, etc.")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location field with inline autocomplete
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Nombre de la ubicación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is AddEventUiState.Loading,
                    supportingText = {
                        Text("Escribe para buscar (min. 3 caracteres)")
                    },
                    trailingIcon = {
                        if (locationName.isNotEmpty()) {
                            IconButton(onClick = {
                                locationName = ""
                                addressPredictions = emptyList()
                                showPredictions = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )

                // Autocomplete dropdown
                if (showPredictions && (addressPredictions.isNotEmpty() || isSearchingAddress)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    ) {
                        if (isSearchingAddress) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            LazyColumn {
                                items(addressPredictions) { prediction ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Fetch place details
                                                val placeFields = listOf(
                                                    Place.Field.ID,
                                                    Place.Field.NAME,
                                                    Place.Field.LAT_LNG,
                                                    Place.Field.ADDRESS
                                                )
                                                val request = FetchPlaceRequest
                                                    .builder(prediction.placeId, placeFields)
                                                    .build()

                                                placesClient
                                                    .fetchPlace(request)
                                                    .addOnSuccessListener { response ->
                                                        val place = response.place
                                                        locationName = place.name ?: ""
                                                        place.latLng?.let { latLng ->
                                                            selectedLocation = latLng
                                                        }
                                                        showPredictions = false
                                                        addressPredictions = emptyList()
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        android.util.Log.e(
                                                            "AddressSearch",
                                                            "Error fetching place",
                                                            exception
                                                        )
                                                    }
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = prediction.getPrimaryText(null).toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = prediction
                                                    .getSecondaryText(null)
                                                    .toString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (prediction != addressPredictions.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.useCurrentLocation() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is AddEventUiState.Loading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mi ubicación")
                }

                OutlinedButton(
                    onClick = { showMap = !showMap },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showMap) "Ocultar mapa" else "Seleccionar en mapa")
                }
            }

            // Show selected location
            if (selectedLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ubicación seleccionada",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.4f", selectedLocation!!.latitude)}, ${String.format("%.4f", selectedLocation!!.longitude)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Interactive map for location selection
            if (showMap) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                selectedLocation = latLng
                            }
                        ) {
                            selectedLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = locationName.ifBlank { "Ubicación del evento" }
                                )
                            }
                        }

                        // Instruction overlay
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Text(
                                text = "Toca en el mapa para seleccionar la ubicación",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Date/Time picker button
            Text(
                text = "Fecha y Hora",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddEventUiState.Loading
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inicio",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = dateFormat.format(Date(startDate)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // End Date/Time picker button
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddEventUiState.Loading
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fin",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = dateFormat.format(Date(endDate)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (uiState is AddEventUiState.Error) {
                Text(
                    text = (uiState as AddEventUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Loading indicator
            if (uiState is AddEventUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Upload progress indicator
            when (val currentState = uiState) {
                is AddEventUiState.Uploading -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = currentState.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Subiendo imagen: ${(currentState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> {}
            }

            // Save button
            Button(
                onClick = {
                    viewModel.createEvent(
                        name = name,
                        description = description,
                        locationName = locationName.ifBlank { "Sin ubicación" },
                        startDate = startDate,
                        endDate = endDate,
                        latitude = selectedLocation?.latitude,
                        longitude = selectedLocation?.longitude,
                        musicGenre = musicGenre.ifBlank { null },
                        imageUri = selectedImageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() &&
                         description.isNotBlank() &&
                         uiState !is AddEventUiState.Loading &&
                         uiState !is AddEventUiState.Uploading
            ) {
                Text("Crear Evento")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                    showStartTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Hora de inicio") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = startDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    startDate = calendar.timeInMillis
                    // Also update end date to be at least 1 hour after start
                    if (endDate <= startDate) {
                        endDate = startDate + 3600000 // +1 hour
                    }
                    showStartTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                    showEndTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Hora de fin") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = endDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    endDate = calendar.timeInMillis
                    showEndTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
