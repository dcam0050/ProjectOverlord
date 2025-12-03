package com.productions666.overlord.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.productions666.overlord.data.model.Place

/**
 * WhereCard — Trainline-style location input with destination FIRST
 * 
 * Visual layout:
 * ┌─────────────────────────────────────────┐
 * │ Where                                   │
 * │                                         │
 * │ ● London Kings Cross             [...] │  ← DESTINATION (filled circle)
 * │   London, UK                            │
 * │ ┊                                       │  ← Dotted connector
 * │ ┊                                       │
 * │ ○ Home                             ↕    │  ← ORIGIN (hollow circle)
 * │   23 High Street, Sheffield             │
 * └─────────────────────────────────────────┘
 */
@Composable
fun WhereCard(
    destination: Place?,
    origin: Place?,
    onDestinationClick: () -> Unit,
    onOriginClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectorColor = MaterialTheme.colorScheme.outline
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section title
            Text(
                text = "Where",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left column: circles and dotted line
                Column(
                    modifier = Modifier.width(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Filled circle for destination
                    FilledCircle(
                        color = MaterialTheme.colorScheme.primary,
                        size = 12.dp
                    )
                    
                    // Dotted connector line
                    DottedConnector(
                        modifier = Modifier
                            .height(60.dp)
                            .width(2.dp),
                        color = connectorColor
                    )
                    
                    // Hollow circle for origin
                    HollowCircle(
                        color = MaterialTheme.colorScheme.primary,
                        size = 12.dp,
                        strokeWidth = 2.dp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Right column: location rows
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Destination row (FIRST)
                    LocationRow(
                        place = destination,
                        placeholder = "Where are you going?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = onDestinationClick)
                            .padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Origin row (SECOND)
                    LocationRow(
                        place = origin,
                        placeholder = "Home",
                        isDefault = origin?.name == "Home",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = onOriginClick)
                            .padding(vertical = 8.dp)
                    )
                }
                
                // Swap button
                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap origin and destination",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    place: Place?,
    placeholder: String,
    modifier: Modifier = Modifier,
    isDefault: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = place?.name ?: placeholder,
            style = MaterialTheme.typography.bodyLarge,
            color = if (place != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (place?.address != null && place.address != place.name) {
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else if (isDefault) {
            Text(
                text = "Default starting location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FilledCircle(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun HollowCircle(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        drawCircle(
            color = color,
            radius = (size.toPx() - strokeWidth.toPx()) / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx())
        )
    }
}

@Composable
private fun DottedConnector(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(6f, 6f),
            phase = 0f
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
            pathEffect = pathEffect
        )
    }
}

