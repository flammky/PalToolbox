package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleNumberTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleTextField
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.wrapStableList

@Composable
fun PlayerInventoryEssentialSlotEdit(
    modifier: Modifier,
    inventoryState: InventoryEditPanelState
) {

    val state = rememberPlayerInventoryEssentialSlotEditState(inventoryState)

    Box(modifier.background(remember { Color(29, 24, 34) })) {
        val entries = state.mutableEntries.value ?: return@Box
        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    entries.size,
                    key = { i -> entries[i].index.toString() }
                ) { i ->
                    val e = entries[i]
                    Row {
                        PlayerInventoryDefaultSlotEntryIndexCell(
                            Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically),
                            e.index.toString()
                        )
                        PlayerInventoryDefaultSlotEntryItemIdCell(
                            Modifier.weight(1f, fill = false),
                            e.itemId,
                            e::itemIdChange,
                            e::itemIdRevert
                        )
                        PlayerInventoryDefaultSlotEntryStackCountCell(
                            Modifier,
                            e.stackCount,
                            e::stackCountChange,
                            e::stackCountRevert
                        )
                    }
                    if (i < entries.size-1) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(8.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        scrollState.layoutInfo.viewportSize.height.toDp()
                    }
                ),
                adapter = rememberScrollbarAdapter(scrollState),
                style = remember {
                    defaultScrollbarStyle().copy(
                        unhoverColor = Color.White.copy(alpha = 0.12f),
                        hoverColor = Color.White.copy(alpha = 0.50f)
                    )
                }
            )
        }
    }
}

@Composable
private fun PlayerInventoryDefaultSlotEntryIndexCell(
    modifier: Modifier,
    index: String
) {
    Box(
        modifier
            .defaultMinSize(minWidth = 24.dp)
            .padding(2.dp)
    ) {
        Text(
            text = index,
            style = Material3Theme.typography.labelMedium,
            maxLines = 1,
            color = Color(252, 252, 252)
        )
    }
}

@Composable
private fun PlayerInventoryDefaultSlotEntryItemIdCell(
    modifier: Modifier,
    itemId: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    revert: () -> Unit
) {
    Box(
        modifier.padding(2.dp)
    ) {
        RevertibleTextField(
            Modifier.width(500.dp),
            value = itemId,
            labelText = "ItemId",
            onValueChange = onValueChange,
            onRevert = revert,
            selections = ITEM_ID_SELECTIONS.wrapStableList(),
            onSelectionsSelected = {}
        )
    }
}

@Composable
private fun PlayerInventoryDefaultSlotEntryStackCountCell(
    modifier: Modifier,
    stackCount: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    revert: () -> Unit
) {
    Box(
        modifier.width(150.dp).padding(2.dp)
    ) {
        RevertibleNumberTextField(
            Modifier,
            value = stackCount,
            labelText = "StackCount",
            onValueChange = onValueChange,
            onRevert = revert
        )
    }
}