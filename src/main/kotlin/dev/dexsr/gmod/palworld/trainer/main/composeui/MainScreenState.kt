package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Composable
fun rememberMainScreenState(): MainScreenState {
    val state = remember { MainScreenState() }

    return state
}

@Stable
class MainScreenState {}