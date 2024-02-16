package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

@Immutable
class MainDrawerDestination(
    val id: String,
    val icon: Painter,
    val text: String,
    val content: @Composable () -> Unit
) {
}