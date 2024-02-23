package dev.dexsr.gmod.palworld.toolbox.paldex.composeui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers

@Composable
fun PalDexMainScreen() {

    CompositionLocalProvider(
        LocalIndication provides rememberRipple()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {
            PalDexContent(Modifier)
        }
    }
}

@Composable
private fun PalDexContent(
    modifier: Modifier
) {

}