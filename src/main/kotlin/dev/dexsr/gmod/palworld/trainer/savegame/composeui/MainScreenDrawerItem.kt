package dev.dexsr.gmod.palworld.trainer.savegame.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun saveGameMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { SaveGameFeaturesScreen() }
    val painter = painterResource("drawable/savegame_save2.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "savegame",
            icon = painter,
            text = "Save Games",
            content = content
        )
    }
}