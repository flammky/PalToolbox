package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer

@Composable
fun InventoryEditPanel(
    modifier: Modifier,
    pState: SaveGamePlayerEditorState
) {
    val state = rememberInventoryEditPanelState(pState)

    Column(
        modifier
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = state::userToggleExpand)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier.size(12.dp),
                painter = if (state.expanded) {
                    painterResource("drawable/simple_arrow_head_down_32px.png")
                } else {
                    painterResource("drawable/simple_arrow_head_right_32px.png")
                },
                contentDescription = null,
                tint = Color.White
            )

            WidthSpacer(8.dp)

            Column(
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    modifier = Modifier.offset(y = -1.dp),
                    text = "Inventory",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        SideEffect {  }

        if (state.expanded) {
            HeightSpacer(8.dp)

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .background(remember { Color(36, 30, 42) }, RoundedCornerShape(24.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.slots().fastForEach { slot ->
                    PlayerInventorySlotTab(
                        Modifier,
                        panelState = state,
                        slot = slot
                    )
                }
            }

            HeightSpacer(8.dp)

            state.slots().fastForEach { slot ->
                key(slot) {
                    val zIndex = state.slotZIndex(slot)
                    PlayerInventorySlotEditPanel(modifier = Modifier.zIndex(zIndex), slot)
                }
            }
        }
    }

}

@Composable
private fun PlayerInventorySlotTab(
    modifier: Modifier,
    panelState: InventoryEditPanelState,
    slot: InventoryEditPanelState.Slot
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 100.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { panelState.selectSlot(slot)  }
            .then(
                if (panelState.isSlotSelected(slot)) {
                    Modifier.background(remember { Color(48, 40, 56) })
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = panelState.slotDisplayName(slot),
            style = Material3Theme.typography.labelLarge,
            maxLines = 1,
            color = Color(252, 252, 252)
        )
    }
}

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.Slot
) {
    when(slot) {
        is InventoryEditPanelState.CommonSlot -> PlayerInventoryCommonSlotEdit(modifier)
        is InventoryEditPanelState.DropSlot -> PlayerInventorySlotEditPanel(modifier, slot)
        is InventoryEditPanelState.EquipArmorSlot -> PlayerInventorySlotEditPanel(modifier, slot)
        is InventoryEditPanelState.EssentialSlot -> PlayerInventorySlotEditPanel(modifier, slot)
        is InventoryEditPanelState.FoodEquipSlot -> PlayerInventorySlotEditPanel(modifier, slot)
        is InventoryEditPanelState.WeaponLoadOutSlot -> PlayerInventorySlotEditPanel(modifier, slot)
    }
}

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.CommonSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.DropSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.EquipArmorSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.EssentialSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.FoodEquipSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventorySlotEditPanel(
    modifier: Modifier,
    slot: InventoryEditPanelState.WeaponLoadOutSlot
) = PlayerInventoryCommonSlot()

@Composable
private fun PlayerInventoryCommonSlot(

) {

}