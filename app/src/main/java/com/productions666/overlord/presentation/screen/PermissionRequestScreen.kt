package com.productions666.overlord.presentation.screen

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRequestScreen(
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    hasFullScreenIntentPermission: Boolean,
    hasOverlayPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onRequestFullScreenIntentPermission: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onContinueAnyway: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "The app needs these permissions to function properly:",
            style = MaterialTheme.typography.bodyLarge
        )
        
        // Status indicator
        val grantedCount = listOf(hasNotificationPermission, hasExactAlarmPermission, hasFullScreenIntentPermission, hasOverlayPermission).count { it }
        val totalCount = 4
        Text(
            text = "Progress: $grantedCount of $totalCount permissions granted",
            style = MaterialTheme.typography.bodyMedium,
            color = if (grantedCount == totalCount) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notification Permission Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (hasNotificationPermission) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Permission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Required for alarm notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasNotificationPermission) {
                    Text(
                        text = "✓ Granted",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Button(onClick = onRequestNotificationPermission) {
                        Text("Grant")
                    }
                }
            }
        }
        
        // Exact Alarm Permission Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (hasExactAlarmPermission) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exact Alarm Permission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Required for precise alarm scheduling",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasExactAlarmPermission) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "✓ Granted",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Text(
                                text = "(Default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Button(onClick = onRequestExactAlarmPermission) {
                        Text("Grant")
                    }
                }
            }
        }
        
        // Full-Screen Intent Permission Card (Android 14+)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (hasFullScreenIntentPermission) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Full-Screen Intent Permission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Required for alarms to appear over other apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasFullScreenIntentPermission) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "✓ Granted",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Text(
                                text = "(Default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (Build.VERSION.SDK_INT < 34) {
                            Text(
                                text = "(Auto)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Button(onClick = onRequestFullScreenIntentPermission) {
                        Text("Grant")
                    }
                }
            }
        }

        // Overlay Permission Card (Draw over other apps)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (hasOverlayPermission) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Draw Over Other Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Required to show alarm on top of other apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasOverlayPermission) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "✓ Granted",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            Text(
                                text = "(Default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Button(onClick = onRequestOverlayPermission) {
                        Text("Grant")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Warning if permissions not granted
        if (!hasNotificationPermission || !hasExactAlarmPermission || !hasFullScreenIntentPermission || !hasOverlayPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠ Warning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Alarms may not work correctly without these permissions. Please grant them for full functionality.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Continue button - only enabled when all permissions are granted
        val allPermissionsGranted = hasNotificationPermission && hasExactAlarmPermission && hasFullScreenIntentPermission && hasOverlayPermission
        Button(
            onClick = onContinueAnyway,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            enabled = allPermissionsGranted
        ) {
            Text(if (allPermissionsGranted) "Continue" else "Grant all permissions to continue")
        }
        
        if (!allPermissionsGranted) {
            Text(
                text = "Please grant all permissions above to use the alarm features.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

