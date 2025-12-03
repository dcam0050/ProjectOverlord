package com.productions666.overlord.presentation.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.productions666.overlord.data.preferences.AppFont

/**
 * Project Overlord Theme
 * 
 * Visual Identity v2.0
 * - Dark theme with deep red primary and gold accents
 * - Dynamic font selection (Lexend, OpenDyslexic, System)
 * - High contrast for accessibility
 * - WCAG AA compliant color combinations
 */

// ============================================================================
// COMPOSITION LOCALS
// ============================================================================

/**
 * Local provider for the current font family
 */
val LocalFontFamily = staticCompositionLocalOf { LexendFamily }

// ============================================================================
// COLOR SCHEME - Dark Only (per Visual Identity v2.0)
// ============================================================================
private val OverlordColorScheme = darkColorScheme(
    // Primary - Deep Red (headers, feature cards, prominent UI)
    primary = DeepRed,
    onPrimary = OnPrimary,
    primaryContainer = Crimson,
    onPrimaryContainer = OnPrimary,
    
    // Secondary - Gold (CTAs, highlights, interactive elements)
    secondary = Gold,
    onSecondary = OnSecondary,
    secondaryContainer = MutedGold,
    onSecondaryContainer = OnSecondary,
    
    // Tertiary - Success green (confirmations, completed states)
    tertiary = Success,
    onTertiary = OnPrimary,
    tertiaryContainer = Success.copy(alpha = 0.2f),
    onTertiaryContainer = Success,
    
    // Background - Near black
    background = Background,
    onBackground = OnBackground,
    
    // Surface - Dark grey for cards and elevated content
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurface.copy(alpha = 0.8f),
    
    // Surface tones for elevation
    surfaceTint = DeepRed,
    inverseSurface = OnBackground,
    inverseOnSurface = Background,
    
    // Outline - Borders and dividers
    outline = Outline,
    outlineVariant = OutlineVariant,
    
    // Error states
    error = Error,
    onError = OnPrimary,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,
    
    // Inverse primary for contrast situations
    inversePrimary = LightRed,
    
    // Scrim for modal overlays
    scrim = Background.copy(alpha = 0.6f)
)

// ============================================================================
// SHAPES - Consistent rounded corners
// ============================================================================
private val OverlordShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),     // Chips, small badges
    small = RoundedCornerShape(12.dp),          // Small cards, inputs
    medium = RoundedCornerShape(16.dp),         // Standard cards
    large = RoundedCornerShape(20.dp),          // Feature cards, dialogs
    extraLarge = RoundedCornerShape(28.dp)      // Buttons, prominent elements
)

// ============================================================================
// THEME COMPOSABLE
// ============================================================================

/**
 * Main theme for Project Overlord
 * 
 * @param selectedFont The font to use throughout the app
 * @param content The composable content
 */
@Composable
fun OverlordTheme(
    selectedFont: AppFont = AppFont.LEXEND,
    content: @Composable () -> Unit
) {
    val fontFamily = getFontFamily(selectedFont)
    val typography = createTypography(fontFamily)
    
    val colorScheme = OverlordColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to match primary color (deep red)
            window.statusBarColor = colorScheme.primary.toArgb()
            // Use light icons on dark status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            // Set navigation bar to match background
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(LocalFontFamily provides fontFamily) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = OverlordShapes,
            content = content
        )
    }
}

// ============================================================================
// SPACING CONSTANTS - For consistent layouts
// ============================================================================

/**
 * Spacing scale following 4dp grid
 * Use these for consistent padding and margins
 */
object Spacing {
    val xxs = 4.dp      // Minimal internal padding
    val xs = 8.dp       // Icon to text gap, tight spacing
    val sm = 12.dp      // Tight grouping
    val md = 16.dp      // Default padding
    val lg = 24.dp      // Section spacing
    val xl = 32.dp      // Major section breaks
    val xxl = 48.dp     // Screen-level spacing
}

/**
 * Touch target sizes for accessibility
 * All interactive elements should meet these minimums
 */
object TouchTargets {
    val minimum = 48.dp      // Absolute minimum touch target
    val recommended = 56.dp  // Recommended for important actions
    val comfortable = 64.dp  // Large touch targets for primary actions
}
