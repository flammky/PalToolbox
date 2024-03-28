package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.noMinConstraints
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleNumberTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleUUIdTextField
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding


@Composable
fun AttributeEditPanel(
    modifier: Modifier,
    state: SaveGamePlayerEditorState
) = AttributeEditPanel(modifier, rememberAttributeEditPanelState(state))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttributeEditPanel(
    modifier: Modifier,
    state: AttributeEditPanelState
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
                    text = "Attributes",
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
                WidthSpacer((14 - ((6 + (4*2)) / 2f)).dp)
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
                WidthSpacer((14 - ((6 + (4*2)) / 2f)).dp)
                WidthSpacer(8.dp)
                Column(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.copy(
                            minWidth = 0,
                            minHeight = 0
                        ))

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    }
                ) {
                    HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
                    Column {
                        RevertibleTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutName ?: "", state.mutNameCursor)
                            } }.value,
                            onValueChange = state::nickNameFieldChange,
                            onRevert = state::revertNickName,
                            labelText = "Nickname"
                        )

                        HeightSpacer(8.dp)

                        RevertibleUUIdTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutUid ?: "", state.mutUidCursor)
                            } }.value,
                            onValueChange = state::uidTextFieldChange,
                            onRevert = state::revertUid,
                            labelText = "UID"
                        )

                        HeightSpacer(8.dp)
                        HeightSpacer(4.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutLevel ?: "", state.mutLevelCursor)
                            } }.value,
                            onValueChange = state::levelTextFieldChange,
                            onRevert = state::revertLevel,
                            labelText = "Level"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutExp ?: "", state.mutExpCursor)
                            } }.value,
                            onValueChange = state::expTextFieldChange,
                            onRevert = state::revertExp,
                            labelText = "Exp"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutHp ?: "", state.mutHpCursor)
                            } }.value,
                            onValueChange = state::hpTextFieldChange,
                            onRevert = state::revertHp,
                            labelText = "HP"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutMaxHp ?: "", state.mutMaxHpCursor)
                            } }.value,
                            onValueChange = state::maxHpTextFieldChange,
                            onRevert = state::revertMaxHp,
                            labelText = "MaxHP"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutFullStomach ?: "", state.mutFullStomachCursor)
                            } }.value,
                            onValueChange = state::fullStomachTextFieldChange,
                            onRevert = state::revertFullStomach,
                            labelText = "FullStomach"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutSupport ?: "", state.mutSupportCursor)
                            } }.value,
                            onValueChange = state::supportTextFieldChange,
                            onRevert = state::revertSupport,
                            labelText = "Support"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutCraftSpeed ?: "", state.mutCraftSpeedCursor)
                            } }.value,
                            onValueChange = state::craftSpeedTextFieldChange,
                            onRevert = state::revertCraftSpeed,
                            labelText = "CraftSpeed"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutMaxSp ?: "", state.mutMaxSpCursor)
                            } }.value,
                            onValueChange = state::maxSpTextFieldChange,
                            onRevert = state::revertMaxSp,
                            labelText = "MaxSP"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutSanityValue ?: "", state.mutSanityValueCursor)
                            } }.value,
                            onValueChange = state::sanityValueTextFieldChange,
                            onRevert = state::revertSanityValue,
                            labelText = "SanityValue"
                        )

                        HeightSpacer(8.dp)

                        RevertibleNumberTextField(
                            modifier = Modifier.padding(top = 2.dp),
                            value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                                TextFieldValue(state.mutUnusedStatusPoint?: "", state.mutUnusedStatusPointCursor)
                            } }.value,
                            onValueChange = state::unusedStatusPointTextFieldChange,
                            onRevert = state::revertUnusedStatusPoint,
                            labelText = "UnusedStatusPoint"
                        )
                    }
                }
            }
        }
    }
}

