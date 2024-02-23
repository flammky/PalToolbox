package dev.dexsr.gmod.palworld.toolbox.gametools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun gameToolsMainScreenDrawItem(): MainDrawerDestination {
    val content = @Composable { GameToolsMainScreen() }
    // TODO: icon
    val painter =
        painterResource("drawable/game_tools_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "gametools",
            icon = painter,
            name = "Game Tools",
            content = content,
            iconTint = Color(168, 140, 196)
        )
    }
}