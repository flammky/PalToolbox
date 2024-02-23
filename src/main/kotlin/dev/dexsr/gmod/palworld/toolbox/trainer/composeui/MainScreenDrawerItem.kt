package dev.dexsr.gmod.palworld.toolbox.trainer.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun trainerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { TrainerMainScreen() }
    // TODO: icon
    val painter =
        painterResource("drawable/system_processes_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "traier",
            icon = painter,
            name = "Trainer",
            content = content,
            iconTint = Color(168, 140, 196)
        )
    }
}