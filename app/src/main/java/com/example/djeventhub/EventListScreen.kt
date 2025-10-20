package com.example.djeventhub

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column

// Composable screen to show a list of events
@Composable
fun EventListScreen(viewModel: EventListViewModel) {
    val events by viewModel.events.collectAsState()

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(events) { event ->
            EventListItem(event)
        }
    }
}

@Composable
fun EventListItem(event: Event) {
    Card(modifier = Modifier.padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

