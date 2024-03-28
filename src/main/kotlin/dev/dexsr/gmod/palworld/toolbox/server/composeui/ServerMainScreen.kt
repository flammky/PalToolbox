package dev.dexsr.gmod.palworld.toolbox.server.composeui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.dexsr.gmod.palworld.toolbox.composeui.WorkInProgressScreen
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers

@Composable
fun ServerMainScreen() {

    CompositionLocalProvider(
        LocalIndication provides rememberRipple()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {
            WorkInProgressScreen(Modifier)
        }
    }
}