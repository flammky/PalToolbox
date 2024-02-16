package dev.dexsr.gmod.palworld.trainer.composeui.main

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.dexsr.gmod.palworld.trainer.composeui.compositionLocalNotProvidedError

val LocalTitleBarBehavior = staticCompositionLocalOf<TitleBarBehavior> {
    compositionLocalNotProvidedError(
        "TitleBarBehavior "
    )
}

@Stable
interface TitleBarBehavior {
    val showRestoreWindow: Boolean
    val titleBarHeightPx: Int

    fun minimizeClicked()
    fun restoreClicked()
    fun maximizeClicked()
    fun closeClicked()
}