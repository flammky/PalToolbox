package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Composable
fun rememberPlayerInventoryCommonSlotEditState(): PlayerInventoryCommonSlotEditState {

    val state = remember {
        PlayerInventoryCommonSlotEditState()
    }


    return state
}

@Stable
class PlayerInventoryCommonSlotEditState {
}