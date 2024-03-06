package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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

        // TODO: we can do custom Layout placement instead of detaching
        if (state.expanded) {
            HeightSpacer(MD3Spec.padding.incrementsDp(2).dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevertibleTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutName ?: "", state.mutNameCursor)
                    } }.value,
                    onValueChange = state::nickNameFieldChange,
                    onRevert = state::revertNickName,
                    labelText = "Nickname"
                )

                RevertibleUUIdTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutUid ?: "", state.mutUidCursor)
                    } }.value,
                    onValueChange = state::uidTextFieldChange,
                    onRevert = state::revertUid,
                    labelText = "UID"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutLevel ?: "", state.mutLevelCursor)
                    } }.value,
                    onValueChange = state::levelTextFieldChange,
                    onRevert = state::revertLevel,
                    labelText = "Level"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutExp ?: "", state.mutExpCursor)
                    } }.value,
                    onValueChange = state::expTextFieldChange,
                    onRevert = state::revertExp,
                    labelText = "Exp"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutHp ?: "", state.mutHpCursor)
                    } }.value,
                    onValueChange = state::hpTextFieldChange,
                    onRevert = state::revertHp,
                    labelText = "HP"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutMaxHp ?: "", state.mutMaxHpCursor)
                    } }.value,
                    onValueChange = state::maxHpTextFieldChange,
                    onRevert = state::revertMaxHp,
                    labelText = "MaxHP"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutFullStomach ?: "", state.mutFullStomachCursor)
                    } }.value,
                    onValueChange = state::fullStomachTextFieldChange,
                    onRevert = state::revertFullStomach,
                    labelText = "FullStomach"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutSupport ?: "", state.mutSupportCursor)
                    } }.value,
                    onValueChange = state::supportTextFieldChange,
                    onRevert = state::revertSupport,
                    labelText = "Support"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutCraftSpeed ?: "", state.mutCraftSpeedCursor)
                    } }.value,
                    onValueChange = state::craftSpeedTextFieldChange,
                    onRevert = state::revertCraftSpeed,
                    labelText = "CraftSpeed"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutMaxSp ?: "", state.mutMaxSpCursor)
                    } }.value,
                    onValueChange = state::maxSpTextFieldChange,
                    onRevert = state::revertMaxSp,
                    labelText = "MaxSP"
                )

                RevertibleNumberTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember(state) { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(state.mutSanityValue ?: "", state.mutSanityValueCursor)
                    } }.value,
                    onValueChange = state::sanityValueTextFieldChange,
                    onRevert = state::revertSanityValue,
                    labelText = "SanityValue"
                )

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

