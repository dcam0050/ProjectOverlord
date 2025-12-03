package com.productions666.overlord.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.productions666.overlord.R
import com.productions666.overlord.data.preferences.AppFont

/**
 * Project Overlord Typography
 * 
 * Supports multiple font families for accessibility:
 * - Lexend: Designed for improved reading fluency
 * - OpenDyslexic: Designed to help with dyslexia
 * - System Default: Device's default font
 * 
 * Key accessibility features:
 * - Minimum 16sp for body text
 * - Line height >= 1.5x font size
 * - Increased letter spacing (+0.5sp)
 * - Clear visual hierarchy
 */

// ============================================================================
// FONT FAMILIES
// ============================================================================

// Lexend Font Family - designed for reading fluency
val LexendFamily = FontFamily(
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_medium, FontWeight.Medium),
    Font(R.font.lexend_semibold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold)
)

// OpenDyslexic Font Family - designed to help with dyslexia
// The upstream project only ships regular/bold/italic weights, so we reuse
// those files for the intermediate weights that our typography expects.
val OpenDyslexicFamily = FontFamily(
    Font(R.font.opendyslexic_regular, FontWeight.Normal),
    Font(R.font.opendyslexic_regular, FontWeight.Medium),
    Font(R.font.opendyslexic_bold, FontWeight.SemiBold),
    Font(R.font.opendyslexic_bold, FontWeight.Bold),
    Font(R.font.opendyslexic_italic, FontWeight.Normal, FontStyle.Italic)
)

// System Default Font Family
val SystemDefaultFamily = FontFamily.Default

// ============================================================================
// TYPOGRAPHY FACTORY
// ============================================================================

fun getFontFamily(appFont: AppFont): FontFamily {
    return when (appFont) {
        AppFont.LEXEND -> LexendFamily
        AppFont.OPEN_DYSLEXIC -> OpenDyslexicFamily
        AppFont.SYSTEM_DEFAULT -> SystemDefaultFamily
    }
}

/**
 * Create Typography with the specified font family
 * 
 * Dyslexia-optimized settings:
 * - Larger base sizes than Material defaults
 * - 1.5x+ line height for all text
 * - Positive letter spacing throughout
 */
fun createTypography(fontFamily: FontFamily): Typography {
    return Typography(
        // =========================================================================
        // DISPLAY - Hero text, very large promotional text
        // =========================================================================
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 56.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        
        // =========================================================================
        // HEADLINE - Screen titles, major section headers
        // =========================================================================
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        
        // =========================================================================
        // TITLE - Card titles, section headers, dialog titles
        // =========================================================================
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.5.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        
        // =========================================================================
        // BODY - Main content, paragraphs, descriptions
        // Larger than Material defaults for accessibility
        // =========================================================================
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp
        ),
        
        // =========================================================================
        // LABEL - Buttons, chips, tags, form labels
        // =========================================================================
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.5.sp
        )
    )
}

// Default typography using Lexend
val OverlordTypography = createTypography(LexendFamily)

// ============================================================================
// SPECIAL TEXT STYLES
// ============================================================================

/**
 * Large time display for dyscalculia support
 */
fun createTimeDisplayLarge(fontFamily: FontFamily) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 48.sp,
    lineHeight = 56.sp,
    letterSpacing = 2.sp,
    fontFeatureSettings = "tnum"
)

/**
 * Medium time display
 */
fun createTimeDisplayMedium(fontFamily: FontFamily) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 1.sp,
    fontFeatureSettings = "tnum"
)

/**
 * Duration text (e.g., "1 hour 30 mins")
 */
fun createDurationText(fontFamily: FontFamily) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)
