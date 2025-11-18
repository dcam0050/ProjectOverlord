package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overlord",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Text(
            text = "Transit Alarm App",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Core alarm engine is ready!",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Next: Implement routing and UI screens",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

