package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun OwnershipEditPanel(
    modifier: Modifier = Modifier,
    state: OwnershipEditPanelState
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
                    text = "Ownership",
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
                    OwnershipEditPanelContent(state)
                }
            }
        }
    }
}

@Composable
private fun OwnershipEditPanelContent(
    state: OwnershipEditPanelState,
    modifier: Modifier = Modifier
) {
    val mutOwnership = state.mutOwnership
        ?: return
    Column(
        modifier.padding(vertical = 8.dp)
    ) {
        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutOwnership.mutOwnedTime,
            labelText = "Owned Time",
            onValueChange = mutOwnership::ownedTimeChange,
            onRevert = mutOwnership::ownedTimeRevert,
        )

        HeightSpacer(4.dp)

        // TODO: selection
        RevertibleUUIdTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutOwnership.mutOwnerPlayerUid,
            labelText = "Owner Player UID",
            onValueChange = mutOwnership::ownerPlayerUidChange,
            onRevert = mutOwnership::ownerPlayerUidRevert,
        )

        HeightSpacer(8.dp)

        OldOwnerUIDsEditField(
            mutOwnership.mutOldOwnerUIds
        )
    }
}

@Composable
private fun OldOwnerUIDsEditField(
    state: OwnershipEditPanelState.MutOwnership.MutOldOwnerUIDs,
    modifier: Modifier = Modifier
) {
    val mutEntries = state.mutEntries

    Column(modifier) {

        Row(
            modifier = Modifier
                .clickable(onClick = state::userToggleExpand)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
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
                    text = "Old Owner UIDs",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

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

                run {
                    val scrollState = rememberLazyListState()
                    Row(
                        modifier = Modifier.layout { measurable, constraints ->

                            val measure = measurable.measure(constraints.noMinConstraints())

                            heightBarHeightState.value = measure.height.toDp()

                            layout(measure.width, measure.height) {
                                measure.place(0, 0)
                            }
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 1000.dp),
                            state = scrollState
                        ) {
                            items(
                                count = mutEntries.size,
                                key = { i -> mutEntries[i] },
                            ) { i ->
                                val mutEntry = mutEntries[i]
                                val uid = mutEntry
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Text(
                                        modifier = Modifier.width(24.dp),
                                        text = i.toString(),
                                        color = Color(252, 252, 252),
                                        style = Material3Theme.typography.labelMedium
                                    )

                                    WidthSpacer(4.dp)

                                    Text(
                                        modifier = Modifier.width(240.dp),
                                        text = uid,
                                        style = Material3Theme.typography.labelMedium,
                                        maxLines = 1,
                                        softWrap = false,
                                        color = Color(252, 252, 252)
                                    )

                                }
                                if (i < mutEntries.lastIndex) {
                                    HeightSpacer(4.dp)
                                }
                            }
                        }
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
        }
    }
}