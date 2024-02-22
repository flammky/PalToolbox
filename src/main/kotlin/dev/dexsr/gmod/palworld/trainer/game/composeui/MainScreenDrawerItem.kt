package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun trainerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { TrainerMainScreen() }
    // TODO: icon
    val painter = painterResource("drawable/palworld_icon_256px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "trainer",
            icon = painter,
            text = "Game",
            content = content
        )
    }
}