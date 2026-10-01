package com.missingverses.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection

private val Gold = Color(0xFFB8860B)
private val Ink = Color(0xFF1F2A44)

private val Light = lightColorScheme(primary = Ink, secondary = Gold, background = Color(0xFFFBF6EA), surface = Color(0xFFFFFDF7))
private val Dark = darkColorScheme(primary = Color(0xFFE6C36A), secondary = Gold, background = Color(0xFF12182A), surface = Color(0xFF1B2238))

/** Forces a right-to-left layout direction for the whole game, regardless of device locale. */
@Composable
fun MissingVersesTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
    }
}
