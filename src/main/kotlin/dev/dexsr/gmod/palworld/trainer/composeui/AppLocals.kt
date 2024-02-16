package dev.dexsr.gmod.palworld.trainer.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import dev.dexsr.gmod.palworld.trainer.PalTrainerApplication

val LocalApplication = staticCompositionLocalOf<PalTrainerApplication> {
    compositionLocalNotProvidedError("LocalApplication")
}

val LocalComposeApplicationScope = staticCompositionLocalOf<ApplicationScope> {
    compositionLocalNotProvidedError("LocalComposeApplicationScope")
}