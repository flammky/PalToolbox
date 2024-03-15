package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import dev.dexsr.gmod.palworld.trainer.composeui.wrapStableList

@Preview
@Composable
fun PlayerInventoryCommonSlotEditTablePreview(
    modifier: Modifier = Modifier
) {
    val entries = remember {
        buildList {
            repeat(10) { i ->
                val entry = PlayerInventoryCommonSlotEditState.Entry(
                    index = i,
                    itemId = "Cold Resistant Refined Metal Armor Schematic 3: Blueprint_IronArmorCold_$i",
                    stackCount = 9999-i
                )
                add(PlayerInventoryCommonSlotEditState.MutEntry(entry))
            }
        }
    }

    val table = SlotTableLayout()

    Column(
        modifier.background(remember { Color(29, 24, 34) }),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        entries.fastForEach { e ->
            Row {
                PlayerInventoryCommonSlotEntryIndexCell(
                    Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically),
                    e.index.toString()
                )
                PlayerInventoryCommonSlotEntryItemIdCell(
                    Modifier.weight(1f, fill = false),
                    e.itemId,
                    e::itemIdChange,
                    e::itemIdRevert
                )
                PlayerInventoryCommonSlotEntryStackCountCell(
                    Modifier.width(150.dp),
                    e.stackCount,
                    e::stackCountChange,
                    e::stackCountRevert
                )
            }
        }
    }
}

@Composable
private fun PlayerInventoryCommonSlotEntryIndexCell(
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
private fun PlayerInventoryCommonSlotEntryItemIdCell(
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
            selections = remember {
                buildList<String> {
                    repeat(100) { add("Cold Resistant Refined Metal Armor Schematic 3: Blueprint_IronArmorCold_0") }
                }.wrapStableList()
            },
            onSelectionsSelected = {}
        )
    }
}

@Composable
private fun PlayerInventoryCommonSlotEntryStackCountCell(
    modifier: Modifier,
    stackCount: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    revert: () -> Unit
) {
    Box(
        modifier.padding(2.dp)
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