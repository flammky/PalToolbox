package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme

@Composable
fun PlayerInventoryEssentialSlotEdit(
    modifier: Modifier,
    inventoryState: InventoryEditPanelState
) {

    val state = rememberPlayerInventoryEssentialSlotEditState(inventoryState)

    Box(modifier.background(remember { Color(29, 24, 34) })) {
        state.uid?.let { uid ->
            Text(
                text = uid,
                style = Material3Theme.typography.labelMedium,
                color = Color(252, 252, 252)
            )
        }
    }
}