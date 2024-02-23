package dev.dexsr.gmod.palworld.toolbox.server.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination

@Composable
fun serverMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { ServerMainScreen() }
    // TODO: icon
    val painter =
        painterResource("drawable/simple_server_32px.png")
    return remember(painter) {
        MainDrawerDestination(
            id = "server",
            icon = painter,
            name = "Server",
            content = content,
            iconTint = Color(168, 140, 196)
        )
    }
}