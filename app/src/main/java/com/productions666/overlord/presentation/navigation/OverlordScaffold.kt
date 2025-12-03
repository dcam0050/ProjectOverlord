package com.productions666.overlord.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.productions666.overlord.presentation.theme.*
import kotlinx.coroutines.launch

/**
 * Project Overlord Main Scaffold
 * 
 * Combines:
 * - Navigation Drawer (sidebar)
 * - Top App Bar with menu icon
 * - Bottom Navigation Bar
 * - Content area
 * 
 * This is the main structural component for all screens
 */
/**
 * Project Overlord Main Scaffold
 * 
 * Simple layout with:
 * - Navigation Drawer (sidebar) for all navigation
 * - Top App Bar with menu icon
 * - Content area
 * 
 * No bottom navigation - everything is in the sidebar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlordScaffold(
    title: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    showTopBar: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            OverlordDrawer(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                if (showTopBar) {
                    OverlordTopBar(
                        title = title,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
            },
            floatingActionButton = floatingActionButton,
            containerColor = Background,
            contentColor = OnBackground
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlordTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation menu"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DeepRed,
            titleContentColor = OnPrimary,
            navigationIconContentColor = OnPrimary,
            actionIconContentColor = OnPrimary
        ),
        modifier = modifier
    )
}

/**
 * Feature Header Section
 * 
 * Used at the top of feature screens (like the Trainline-style header)
 * Deep red background with title and description
 */
@Composable
fun FeatureHeader(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepRed
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = OnPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = OnPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Primary Action Button
 * 
 * Gold button for main CTAs
 * Follows accessibility guidelines with minimum 56dp height
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(TouchTargets.recommended),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = OnSecondary,
            disabledContainerColor = Gold.copy(alpha = 0.4f),
            disabledContentColor = OnSecondary.copy(alpha = 0.6f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = OnSecondary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Secondary Action Button
 * 
 * Outlined button for secondary actions
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(TouchTargets.recommended),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Gold)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Gold,
            disabledContentColor = Gold.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Feature Card
 * 
 * Primary red card for feature items (like Trainline's ticket cards)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = DeepRed
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnPrimary
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
            }
            
            trailingContent?.invoke()
        }
    }
}

/**
 * Neutral Card
 * 
 * Dark surface card for settings, lists, secondary content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeutralCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                content = content
            )
        }
    }
}

