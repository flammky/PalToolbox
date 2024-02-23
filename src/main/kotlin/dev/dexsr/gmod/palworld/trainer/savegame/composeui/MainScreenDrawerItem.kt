package dev.dexsr.gmod.palworld.trainer.savegame.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun saveGameMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { SaveGameFeaturesScreen() }
    val painter = painterResource("drawable/simple_save_file_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "savegame",
            icon = painter,
            iconTint = Color(168, 140, 196),
            name = "Save Games",
            content = content
        )
    }
}