package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.productions666.overlord.data.preferences.AppFont
import com.productions666.overlord.presentation.navigation.FeatureHeader
import com.productions666.overlord.presentation.navigation.NeutralCard
import com.productions666.overlord.presentation.theme.*

/**
 * Settings Screen
 * 
 * Allows users to customize app behavior including:
 * - Font selection (Lexend, OpenDyslexic, System Default)
 * - Home/Work address (future)
 * - Notification settings (future)
 */
@Composable
fun SettingsScreen(
    currentFont: AppFont,
    onFontSelectionClick: () -> Unit,
    onAlarmTestClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xl)
    ) {
        // Header
        item {
            FeatureHeader(
                title = "Settings",
                description = "Customize your experience"
            )
        }
        
        // Accessibility Section
        item {
            SettingsSectionHeader(
                title = "Accessibility",
                icon = Icons.Default.Accessibility
            )
        }
        
        // Font Selection
        item {
            FontSelectionCard(
                currentFont = currentFont,
                onClick = onFontSelectionClick,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
        }
        
        // Locations Section (Future)
        item {
            SettingsSectionHeader(
                title = "Locations",
                icon = Icons.Default.LocationOn
            )
        }
        
        item {
            NeutralCard(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                onClick = { /* TODO: Set home address */ }
            ) {
                SettingsRow(
                    icon = Icons.Default.Home,
                    title = "Home Address",
                    subtitle = "Not set",
                    onClick = { /* TODO */ }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
        
        item {
            NeutralCard(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                onClick = { /* TODO: Set work address */ }
            ) {
                SettingsRow(
                    icon = Icons.Default.Work,
                    title = "Work Address",
                    subtitle = "Not set",
                    onClick = { /* TODO */ }
                )
            }
        }
        
        // Notifications Section (Future)
        item {
            SettingsSectionHeader(
                title = "Notifications",
                icon = Icons.Default.Notifications
            )
        }
        
        item {
            NeutralCard(
                modifier = Modifier.padding(horizontal = Spacing.lg)
            ) {
                SettingsRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Default Alarm Sound",
                    subtitle = "System default",
                    onClick = { /* TODO */ }
                )
            }
        }
        
        // Developer Section
        item {
            SettingsSectionHeader(
                title = "Developer",
                icon = Icons.Default.Code
            )
        }
        
        item {
            NeutralCard(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                onClick = onAlarmTestClick
            ) {
                SettingsRow(
                    icon = Icons.Default.Alarm,
                    title = "Alarm Test",
                    subtitle = "Test alarm scheduling and sounds",
                    onClick = onAlarmTestClick
                )
            }
        }
        
        // About Section
        item {
            SettingsSectionHeader(
                title = "About",
                icon = Icons.Default.Info
            )
        }
        
        item {
            NeutralCard(
                modifier = Modifier.padding(horizontal = Spacing.lg)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        showChevron = false,
                        onClick = { }
                    )
                    
                    Divider(color = Outline.copy(alpha = 0.3f))
                    
                    SettingsRow(
                        icon = Icons.Default.Description,
                        title = "Privacy Policy",
                        onClick = { /* TODO */ }
                    )
                    
                    Divider(color = Outline.copy(alpha = 0.3f))
                    
                    SettingsRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.lg,
                bottom = Spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Gold
        )
    }
}

@Composable
private fun FontSelectionCard(
    currentFont: AppFont,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeutralCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TextFormat,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Font",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = currentFont.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = currentFont.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select font",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}