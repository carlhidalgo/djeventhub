package com.example.djeventhub.ui.productora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.djeventhub.models.User
import com.example.djeventhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsListScreen(
    eventId: String,
    eventName: String,
    onNavigateBack: () -> Unit,
    onViewDJProfile: (String) -> Unit,
    viewModel: ApplicationsViewModel = hiltViewModel()
) {
    val applicants by viewModel.applicants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(eventId) {
        viewModel.loadApplicants(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Postulantes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            eventName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack
                )
            )
        },
        containerColor = DeepBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NeonPink
                    )
                }
                applicants.isEmpty() -> {
                    EmptyApplicationsState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "${applicants.size} DJ${if (applicants.size != 1) "s" else ""} postulado${if (applicants.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonPink,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(applicants) { dj ->
                            DJApplicantCard(
                                dj = dj,
                                onViewProfile = { onViewDJProfile(dj.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DJApplicantCard(
    dj: User,
    onViewProfile: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(64.dp)
                ) {
                    if (!dj.profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = dj.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(NeonPink, NeonPurple)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (dj.artistName ?: dj.displayName).firstOrNull()?.uppercase() ?: "DJ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = DeepBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dj.artistName ?: dj.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", dj.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${dj.eventsCompleted} eventos",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    
                    // Music genres preview
                    if (dj.musicGenres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dj.musicGenres.take(3).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonPurple,
                            maxLines = 1
                        )
                    }
                }
                
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            
            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = TextTertiary.copy(alpha = 0.2f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bio
                    if (!dj.bio.isNullOrBlank()) {
                        Text(
                            text = "Biografía",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonPink,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dj.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // All genres
                    if (dj.musicGenres.isNotEmpty()) {
                        Text(
                            text = "Géneros",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dj.musicGenres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonPurple.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // View profile button
                    Button(
                        onClick = onViewProfile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver perfil completo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyApplicationsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = TextTertiary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin postulantes aún",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cuando los DJs se postulen a este evento, aparecerán aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
