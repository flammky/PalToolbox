package dev.dexsr.gmod.palworld.toolbox.trainer.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun WorldTrainer() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
    )
}