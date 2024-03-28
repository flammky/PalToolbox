package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.noMinConstraints
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleNumberTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleUUIdTextField
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers

@Composable
fun PlayerSaveEditPanel(
    modifier: Modifier,
    state: PlayerSaveEditPanelState = rememberPlayerSaveEditPanelState()
) {
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
                    text = "Player save data",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        if (state.expanded) run {
            if (state.noContent)
                return@run
            // don't use intrinsics
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row {
                WidthSpacer((14 - ((4 + (6*2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((4 + (6*2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(
                            constraints.copy(
                                minWidth = 0,
                                minHeight = 0
                            )
                        )

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    HeightSpacer(4.dp)

                    val otomoContainerId = state.mutOtomoContainerId ?: return@Column
                    HeightSpacer(4.dp)
                    OtomoContainerIdEditTextField(
                        Modifier,
                        otomoContainerId
                    )
                    val inventoryInfo = state.mutInventoryInfo ?: return@Column
                    HeightSpacer(4.dp)
                    PlayerSaveEditPanelInventoryInfo(
                        Modifier,
                        inventoryInfo
                    )
                    val technologyPoint = state.mutTechnologyPoint ?: return@Column
                    HeightSpacer(12.dp)
                    TechnologyPointEditTextField(
                        Modifier,
                        technologyPoint
                    )

                    val technologyNames = state.mutUnlockedTechnologyRecipe ?: return@Column
                    HeightSpacer(12.dp)
                    UnlockedRecipeTechnologyNamesEditField(
                        technologyNames
                    )

                    val palStorageContainerId = state.mutPalStorageContainerId ?: return@Column
                    HeightSpacer(12.dp)
                    PalStorageContainerIdEditTextField(
                        Modifier,
                        palStorageContainerId
                    )

                    val recordData = state.mutRecordData ?: return@Column
                    HeightSpacer(12.dp)
                    PalRecordDataEditField(
                        recordData
                    )
                }
            }
        }
    }
}

@Composable
private fun OtomoContainerIdEditTextField(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutOtomoContainerId
) {
    Box(modifier) {
        RevertibleUUIdTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mut.otomoContainerId,
            onValueChange = mut::otomoContainerIdChange,
            onRevert = mut::otomoContainerIdRevert,
            labelText = "OtomoContainerId"
        )
    }
}

@Composable
private fun TechnologyPointEditTextField(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutTechnologyPoint
) {
    Box(modifier) {
        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mut.technologyPoint,
            onValueChange = mut::technologyPointChange,
            onRevert = mut::technologyPointRevert,
            labelText = "TechnologyPoint"
        )
    }
}

@Composable
private fun PlayerSaveEditPanelInventoryInfo(
    modifier: Modifier,
    info: PlayerSaveEditPanelState.MutInventoryInfo
) {
    Column(modifier) {

        Text(
            "Inventory Info",
            color = Color(252, 252, 252),
            style = Material3Theme.typography.titleMedium
        )

        HeightSpacer(8.dp)

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.commonContainerId,
                onValueChange = info::commonContainerIdChange,
                onRevert = info::commonContainerIdRevert,
                labelText = "Common"
            )

            HeightSpacer(4.dp)

            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.dropSlotContainerId,
                onValueChange = info::dropSlotContainerIdChange,
                onRevert = info::dropSlotContainerIdRevert,
                labelText = "Drop"
            )

            HeightSpacer(4.dp)

            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.essentialContainerId,
                onValueChange = info::essentialContainerIdChange,
                onRevert = info::essentialContainerIdRevert,
                labelText = "Essential"
            )

            HeightSpacer(4.dp)

            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.weaponLoadOutContainerId,
                onValueChange = info::weaponLoadOutContainerIdChange,
                onRevert = info::weaponLoadOutContainerIdRevert,
                labelText = "WeaponLoadOut"
            )

            HeightSpacer(4.dp)

            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.playerEquipArmorContainerId,
                onValueChange = info::playerEquipArmorContainerIdChange,
                onRevert = info::playerEquipArmorContainerIdRevert,
                labelText = "EquipArmor"
            )

            HeightSpacer(4.dp)

            RevertibleUUIdTextField(
                modifier = Modifier.padding(top = 2.dp),
                value = info.foodEquipContainerId,
                onValueChange = info::foodEquipContainerIdChange,
                onRevert = info::foodEquipContainerIdRevert,
                labelText = "FoodEquip"
            )
        }
    }
}

@Composable
private fun UnlockedRecipeTechnologyNamesEditField(
    state: PlayerSaveEditPanelState.MutUnlockedTechnologyRecipe,
    modifier: Modifier = Modifier
) {
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
                    text = "Unlocked Technologies",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PlayerSaveEditPanelUnlockedRecipeTechnologyNames(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSaveEditPanelUnlockedRecipeTechnologyNames(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutUnlockedTechnologyRecipe
) {
    val mutEntries = mut.mutEntries

    Column(modifier = modifier) {
        Row {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/plus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Add",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }

            WidthSpacer(4.dp)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/minus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Remove",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }

        HeightSpacer(8.dp)

        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    mutEntries.size,
                    key = { i -> mutEntries[i].index }
                ) { i ->
                    val entry = mutEntries[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.width(24.dp),
                            text = entry.index.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(500.dp),
                            text = entry.value,
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                    }
                    if (i < mutEntries.lastIndex) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(4.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
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
private fun PalStorageContainerIdEditTextField(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutPalStorageContainerId
) {
    Box(modifier) {
        RevertibleUUIdTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mut.palStorageContainerId,
            onValueChange = mut::palStorageContainerIdChange,
            onRevert = mut::palStorageContainerIdRevert,
            labelText = "PalStorageContainerId"
        )
    }
}

@Composable
private fun PalRecordDataEditField(
    state: PlayerSaveEditPanelState.MutRecordData,
    modifier: Modifier = Modifier
) {
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
                    text = "RecordData",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PalRecordDataEditPanel(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PalRecordDataEditPanel(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData
) {
    Column(
        modifier = modifier
    ) {
        PalRecordDataTribeCountTextField(
            Modifier,
            mut.mutTribeCaptureCount
        )
        HeightSpacer(4.dp)
        PalRecordDataPalCaptureCountField(
            mut.mutPalCaptureCount
        )
        HeightSpacer(8.dp)
        PalRecordDataMapField(
            mut.mutPaldeckUnlockFlag
        )
        HeightSpacer(8.dp)
        PalRecordDataMapField(
            mut.mutNoteObtainForInstanceFlag
        )
        HeightSpacer(8.dp)
        PalRecordDataMapField(
            mut.mutFastTravelPointUnlockFlag
        )
    }
}

@Composable
private fun PalRecordDataTribeCountTextField(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData.MutTribeCaptureCount
) {
    Box(modifier) {
        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mut.mutValue,
            onValueChange = mut::mutValueChange,
            onRevert = mut::mutValueReset,
            labelText = "TribeCaptureCount"
        )
    }
}

@Composable
private fun PalRecordDataPalCaptureCountField(
    state: PlayerSaveEditPanelState.MutRecordData.MutPalCaptureCount,
    modifier: Modifier = Modifier
) {
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
                    text = "PalCaptureCount",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PalRecordDataPalCaptureCountColumn(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PalRecordDataPalCaptureCountColumn(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData.MutPalCaptureCount
) {
    val mutEntries = mut.mutEntries
    Column(
        modifier = modifier
    ) {
        Row {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/plus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Add",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }

            WidthSpacer(4.dp)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/minus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Remove",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }

        HeightSpacer(8.dp)

        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    mutEntries.size,
                    key = { i -> mutEntries[i].first }
                ) { i ->
                    val entry = mutEntries[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.width(24.dp),
                            text = i.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(500.dp).weight(1f, fill = false),
                            text = entry.first,
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(80.dp),
                            text = entry.second.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                    }
                    if (i < mutEntries.lastIndex) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(4.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
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
private fun PalRecordDataMapField(
    state: PlayerSaveEditPanelState.MutRecordData.MutNoteObtainForInstanceFlag,
    modifier: Modifier = Modifier
) {
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
                    text = "NoteObtainForInstanceFlag",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PalRecordDataMapColumn(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PalRecordDataMapColumn(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData.MutNoteObtainForInstanceFlag
) {
    val mutEntries = mut.mutEntries
    Column(
        modifier = modifier
    ) {
        Row {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/plus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Add",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }

            WidthSpacer(4.dp)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/minus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Remove",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }

        HeightSpacer(8.dp)

        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    mutEntries.size,
                    key = { i -> mutEntries[i].first }
                ) { i ->
                    val entry = mutEntries[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.width(24.dp),
                            text = i.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(500.dp).weight(1f, fill = false),
                            text = entry.first,
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(80.dp),
                            text = entry.second.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                    }
                    if (i < mutEntries.lastIndex) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(4.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
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
private fun PalRecordDataMapField(
    state: PlayerSaveEditPanelState.MutRecordData.MutFastTravelPointUnlockFlag,
    modifier: Modifier = Modifier
) {
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
                    text = "FastTravelPointUnlockFlag",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PalRecordDataMapColumn(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PalRecordDataMapColumn(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData.MutFastTravelPointUnlockFlag
) {
    val mutEntries = mut.mutEntries
    Column(
        modifier = modifier
    ) {
        Row {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/plus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Add",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }

            WidthSpacer(4.dp)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/minus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Remove",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }

        HeightSpacer(8.dp)

        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    mutEntries.size,
                    key = { i -> mutEntries[i].first }
                ) { i ->
                    val entry = mutEntries[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.width(24.dp),
                            text = i.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(500.dp).weight(1f, fill = false),
                            text = entry.first,
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(80.dp),
                            text = entry.second.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                    }
                    if (i < mutEntries.lastIndex) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(4.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
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
private fun PalRecordDataMapField(
    state: PlayerSaveEditPanelState.MutRecordData.MutPaldeckUnlockFlag,
    modifier: Modifier = Modifier
) {
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
                    text = "PaldeckUnlockFlag",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        // TODO: we can do custom Layout placement instead of detaching
        if (state.opened) {
            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!state.expanded) {
                        return@layout layout(0, 0) {}
                    }

                    layout(measure.width, measure.height) {
                        measure.place(0, 0)
                    }
                }
            ) {
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                Box(
                    modifier = Modifier
                        // wait for public API on scroll focus
                        .focusProperties {
                            canFocus = false
                        }
                        .height(heightBarHeightState.value)
                        .clickable(onClick = state::userToggleExpand)
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .background(Color(0x40FFFFFF))
                )
                WidthSpacer((14 - ((6 + (4 * 2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    PalRecordDataMapColumn(
                        Modifier,
                        state
                    )
                }
            }
        }
    }
}

@Composable
private fun PalRecordDataMapColumn(
    modifier: Modifier,
    mut: PlayerSaveEditPanelState.MutRecordData.MutPaldeckUnlockFlag
) {
    val mutEntries = mut.mutEntries
    Column(
        modifier = modifier
    ) {
        Row {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/plus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Add",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }

            WidthSpacer(4.dp)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {  }
                    .background(Color(0xFF694fa3))
                    .padding(start = 6.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp).size(12.dp),
                    painter = painterResource("drawable/minus_rounded_16px.png"),
                    contentDescription = null,
                    tint = Color(0xFFffffff)
                )
                WidthSpacer(8.dp)
                Text(
                    text = "Remove",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color(0xFFffffff),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }

        HeightSpacer(8.dp)

        Row {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.heightIn(max = 1000.dp),
                state = scrollState
            ) {
                items(
                    mutEntries.size,
                    key = { i -> mutEntries[i].first }
                ) { i ->
                    val entry = mutEntries[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.width(24.dp),
                            text = i.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(500.dp).weight(1f, fill = false),
                            text = entry.first,
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                        WidthSpacer(4.dp)
                        Text(
                            modifier = Modifier.width(80.dp),
                            text = entry.second.toString(),
                            color = Color(252, 252, 252),
                            style = Material3Theme.typography.labelMedium
                        )
                    }
                    if (i < mutEntries.lastIndex) {
                        HeightSpacer(4.dp)
                    }
                }
            }

            WidthSpacer(4.dp)

            VerticalScrollbar(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        remember(this) {
                            derivedStateOf { scrollState.layoutInfo.viewportSize.height.toDp() }
                        }.value
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


// ----- PREVIEW -----

@Composable
@Preview
private fun PlayerSaveEditPanelPreview() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
            .padding(top = 4.dp)
    ) {
        PlayerSaveEditPanel(
            Modifier,
            state = run {
                val state = rememberPlayerSaveEditPanelState()
                val mock = remember(state) { state.Mock() }
                mock.mockInit()
                state.userToggleExpand()
                state
            }
        )
    }
}