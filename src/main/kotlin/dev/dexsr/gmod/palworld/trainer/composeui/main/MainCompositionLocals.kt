package dev.dexsr.gmod.palworld.trainer.composeui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.dexsr.gmod.palworld.trainer.PalTrainerApplication
import dev.dexsr.gmod.palworld.trainer.composeui.LocalApplication

@Composable
fun ProvideMainCompositionLocals(
    app: PalTrainerApplication,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalApplication provides app
    ) {
        content()
    }
}