package com.productions666.overlord.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productions666.overlord.presentation.theme.*

/**
 * Project Overlord Navigation Drawer
 * 
 * All navigation in one place (no bottom nav)
 * - Profile section with plan indicator
 * - Main tools: Home, Journeys, Tasks, Notes
 * - App sections: Settings, Account, Support
 */
@Composable
fun OverlordDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = Surface,
        drawerContentColor = OnSurface
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // App title header
            DrawerHeader()
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Main Tools Section
            DrawerSectionLabel("Tools")
            
            DrawerMenuItem.entries
                .filter { it.isMainSection }
                .forEach { item ->
                    DrawerItem(
                        item = item,
                        isSelected = isRouteSelected(currentRoute, item.route),
                        onClick = {
                            onNavigate(item.route)
                            onCloseDrawer()
                        }
                    )
                }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Divider
            Divider(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                color = Outline.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // App Sections
            DrawerMenuItem.entries
                .filter { !it.isMainSection }
                .forEach { item ->
                    DrawerItem(
                        item = item,
                        isSelected = isRouteSelected(currentRoute, item.route),
                        onClick = {
                            onNavigate(item.route)
                            onCloseDrawer()
                        }
                    )
                }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer
            DrawerFooter()
        }
    }
}

/**
 * Check if route is selected (handles journey_planner -> journeys mapping)
 */
private fun isRouteSelected(currentRoute: String?, itemRoute: String): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == itemRoute) return true
    
    // Handle route aliases
    return when (itemRoute) {
        "journeys" -> currentRoute in listOf("journey_planner", "route_list", "alarm_setup")
        "home" -> currentRoute == "home"
        else -> false
    }
}

@Composable
private fun DrawerSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Gold,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            start = Spacing.lg + Spacing.sm,
            bottom = Spacing.xs
        )
    )
}

@Composable
private fun DrawerHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepRed
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = "Overlord",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnPrimary
            )
            Text(
                text = "Your productivity suite",
                style = MaterialTheme.typography.bodyMedium,
                color = OnPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DrawerItem(
    item: DrawerMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) DeepRed.copy(alpha = 0.2f) else Surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                tint = if (isSelected) Gold else OnSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) Gold else OnSurface
            )
        }
    }
}

@Composable
private fun DrawerFooter() {
    Column(
        modifier = Modifier.padding(Spacing.lg)
    ) {
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface.copy(alpha = 0.5f)
        )
    }
}
