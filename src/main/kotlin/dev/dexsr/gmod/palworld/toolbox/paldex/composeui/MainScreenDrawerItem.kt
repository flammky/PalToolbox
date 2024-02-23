package dev.dexsr.gmod.palworld.toolbox.paldex.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun palDexMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { PalDexMainScreen() }
    // TODO: icon
    val painter =
        painterResource("drawable/flash_card_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "paldex",
            icon = painter,
            name = "PalDex",
            content = content,
            iconTint = Color(168, 140, 196)
        )
    }
}