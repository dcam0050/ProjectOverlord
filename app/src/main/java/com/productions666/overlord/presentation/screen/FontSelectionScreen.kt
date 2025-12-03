package com.productions666.overlord.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.productions666.overlord.data.preferences.AppFont
import com.productions666.overlord.presentation.navigation.FeatureHeader
import com.productions666.overlord.presentation.navigation.SecondaryButton
import com.productions666.overlord.presentation.theme.DeepRed
import com.productions666.overlord.presentation.theme.Gold
import com.productions666.overlord.presentation.theme.OnSurface
import com.productions666.overlord.presentation.theme.Spacing
import com.productions666.overlord.presentation.theme.Surface as CardSurface
import com.productions666.overlord.presentation.theme.TextSecondary
import com.productions666.overlord.presentation.theme.TextTertiary
import com.productions666.overlord.presentation.theme.getFontFamily

@Composable
fun FontSelectionScreen(
    currentFont: AppFont,
    onFontSelected: (AppFont) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontOptions = remember { AppFont.entries }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xl)
    ) {
        item {
            FeatureHeader(
                title = "Fonts & Legibility",
                description = "Preview each typeface and pick the one that helps you read fastest."
            )
        }

        item {
            Text(
                text = "Each option below is rendered using its own font so you can compare spacing, letter shapes, and contrast before applying it globally.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
            )
        }

        items(fontOptions) { font ->
            FontOptionCard(
                font = font,
                isSelected = font == currentFont,
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.md),
                onClick = { onFontSelected(font) }
            )
        }

        item {
            SecondaryButton(
                text = "Back to Settings",
                onClick = onBack,
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .padding(top = Spacing.sm)
            )
        }
    }
}

@Composable
private fun FontOptionCard(
    font: AppFont,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val previewFamily = remember(font) { getFontFamily(font) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.md))
            .clickable { onClick() },
        color = if (isSelected) DeepRed.copy(alpha = 0.3f) else CardSurface,
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Gold,
                        unselectedColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = font.displayName,
                        style = MaterialTheme.typography.titleMedium.withPreviewFont(previewFamily),
                        color = if (isSelected) Gold else OnSurface
                    )
                    Text(
                        text = font.description,
                        style = MaterialTheme.typography.bodySmall.withPreviewFont(previewFamily),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Sphinx of black quartz, judge my vow. 1234567890",
                style = MaterialTheme.typography.bodyMedium.withPreviewFont(previewFamily),
                color = TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun androidx.compose.ui.text.TextStyle.withPreviewFont(fontFamily: FontFamily): androidx.compose.ui.text.TextStyle {
    return this.copy(fontFamily = fontFamily)
}

