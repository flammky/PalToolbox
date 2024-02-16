package dev.dexsr.gmod.palworld.trainer.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import java.awt.Window

// LocalWindow
val LocalWindow = staticCompositionLocalOf<Window> {
    compositionLocalNotProvidedError("LocalWindow not provided")
}