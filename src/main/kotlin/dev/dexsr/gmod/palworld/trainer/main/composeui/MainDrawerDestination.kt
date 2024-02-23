package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

@Immutable
class MainDrawerDestination(
    val id: String,
    val icon: Painter,
    val name: String,
    val iconTint: Color? = null,
    val content: @Composable () -> Unit
) {
}