package dev.dexsr.gmod.palworld.toolbox.game.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun gameMainScreenDrawItem(): MainDrawerDestination {
    val content = @Composable { GameMainScreen() }
    // TODO: icon
    val painter =
        painterResource("drawable/simple_file_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "game",
            icon = painter,
            name = "Game",
            content = content,
            iconTint = Color(168, 140, 196)
        )
    }
}