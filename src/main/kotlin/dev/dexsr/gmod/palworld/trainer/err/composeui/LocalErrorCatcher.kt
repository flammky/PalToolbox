package dev.dexsr.gmod.palworld.trainer.err.composeui

import androidx.compose.runtime.staticCompositionLocalOf
import dev.dexsr.gmod.palworld.trainer.composeui.compositionLocalNotProvidedError

val LocalComposeErrorCatcher = staticCompositionLocalOf<ComposeErrorCatcher> {
    compositionLocalNotProvidedError("ComposeErrorCatcher")
}