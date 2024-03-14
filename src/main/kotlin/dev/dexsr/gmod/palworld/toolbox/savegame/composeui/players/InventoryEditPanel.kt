package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayerInventoryEdit
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonFontScaled

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



        if (state.expanded) run {
            if (state.noContent)
                return@run
            Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                WidthSpacer((14 - (12 / 2f)).dp)
                Box(
                    modifier = Modifier
                        .clickable(onClick = state::userToggleExpand)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp)
                        .width(8.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - (12 / 2f)).dp)
                WidthSpacer(8.dp)
                Column {
                    HeightSpacer(8.dp)
                    if (state.sourceNotFoundErr != null) {
                        PlayerInventorySourceNotFound(Modifier, state)
                        return@Row
                    }
                    if (state.showEditor) {
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

                        Box(modifier = Modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Max)) {
                            state.slots().fastForEach { slot ->
                                key(slot) {
                                    val zIndex = state.slotZIndex(slot)
                                    PlayerInventorySlotEditPanel(modifier = Modifier.zIndex(zIndex).fillMaxSize(), slot, state)
                                }
                            }
                        }
                    }
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
    slot: InventoryEditPanelState.Slot,
    inventoryEditPanelState: InventoryEditPanelState
) {
    when(slot) {
        is InventoryEditPanelState.CommonSlot -> PlayerInventoryCommonSlotEdit(modifier, inventoryEditPanelState)
        is InventoryEditPanelState.DropSlot -> PlayerInventoryDropSlotEdit(modifier, inventoryEditPanelState)
        is InventoryEditPanelState.EquipArmorSlot -> PlayerInventoryEquipArmorSlotEdit(modifier, inventoryEditPanelState)
        is InventoryEditPanelState.EssentialSlot -> PlayerInventoryEssentialSlotEdit(modifier, inventoryEditPanelState)
        is InventoryEditPanelState.FoodEquipSlot -> PlayerInventoryFoodEquipSlotEdit(modifier, inventoryEditPanelState)
        is InventoryEditPanelState.WeaponLoadOutSlot -> PlayerInventoryWeaponLoadOutSlotEdit(modifier, inventoryEditPanelState)
    }
}

@Composable
private fun PlayerInventorySourceNotFound(
    modifier: Modifier,
    state: InventoryEditPanelState
) {
    val err = state.sourceNotFoundErr ?: return

    val labelMedium = Material3Theme.typography.labelMedium.nonFontScaled()
    val color = Color(0xFF690005)
    val msg = when(err) {
        is SaveGamePlayerInventoryEdit.Error.PlayerFileFNF -> buildAnnotatedString {
            withStyle(labelMedium.copy(color.copy(alpha = 0.80f)).toSpanStyle()) {
                append("Player save file was not found: \n")
                withStyle(
                    SpanStyle(fontWeight = FontWeight.SemiBold, color = color)
                ) {
                    append(err.file.name)
                }
            }
        }
        is SaveGamePlayerInventoryEdit.Error.PlayersFolderFNF -> buildAnnotatedString {
            withStyle(labelMedium.copy(color.copy(alpha = 0.80f)).toSpanStyle()) {
                withStyle(
                    SpanStyle(fontWeight = FontWeight.SemiBold, color = color)
                ) {
                    append(err.file.name)
                    append(" ")
                }
                append("directory was not found")
            }
        }
        else -> buildAnnotatedString {  }
    }

    PlayerInventorySaveFileNotFound(modifier, msg, state::refresh)
}

@Composable
private fun PlayerInventorySaveFileNotFound(
    modifier: Modifier,
    msg: AnnotatedString,
    refresh: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp / 2))
            .background(Color(0xFFffb4ab))
            .padding(16.dp)
    ) {
        Text(text = msg)

        HeightSpacer(8.dp)

        Button(
            onClick = refresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF93000a)
            )
        ) {
            Text(
                text = "Refresh",
                style = Material3Theme.typography.labelLarge,
                color = Color(0xFFffdad6),
                maxLines = 1
            )
        }
    }
}