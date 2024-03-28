package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.noMinConstraints
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleNumberTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.RevertibleUUIdTextField
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SingleLineSimpleTooltip
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding

@Composable
fun AttributeEditPanel(
    modifier: Modifier = Modifier,
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
                AttributeEditPanelContent(
                    modifier = Modifier.layout { measurable, constraints ->

                        val measure = measurable.measure(constraints.noMinConstraints())

                        heightBarHeightState.value = measure.height.toDp()

                        layout(measure.width, measure.height) {
                            measure.place(0, 0)
                        }
                    },
                    state = state
                )
            }
        }
    }
}

@Composable
private fun AttributeEditPanelContent(
    modifier: Modifier,
    state: AttributeEditPanelState
) {
    Column(modifier) {
        val mutAttr = state.mutAttribute ?: return@Column
        HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
        RevertibleTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutNickName,
            labelText = "NickName",
            onValueChange = mutAttr::mutNickNameChange,
            onRevert = mutAttr::mutNickNameRevert,
        )

        HeightSpacer(8.dp)

        RevertibleUUIdTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutUid,
            labelText = "UID",
            onValueChange = mutAttr::uidChange,
            onRevert = mutAttr::uidRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutLevel,
            labelText = "Level",
            onValueChange = mutAttr::levelChange,
            onRevert = mutAttr::levelRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutHp,
            labelText = "Hp",
            onValueChange = mutAttr::hpChange,
            onRevert = mutAttr::hpRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutMaxHp,
            labelText = "MaxHp",
            onValueChange = mutAttr::maxHpChange,
            onRevert = mutAttr::maxHpRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutFullStomach,
            labelText = "FullStomach",
            onValueChange = mutAttr::fullStomachChange,
            onRevert = mutAttr::fullStomachRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutMaxFullStomach,
            labelText = "MaxFullStomach",
            onValueChange = mutAttr::maxFullStomachChange,
            onRevert = mutAttr::maxFullStomachRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutMp,
            labelText = "MP",
            onValueChange = mutAttr::mpChange,
            onRevert = mutAttr::mpRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.mutMaxSp,
            labelText = "MaxSP",
            onValueChange = mutAttr::maxSpChange,
            onRevert = mutAttr::maxSpRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.sanityValue,
            labelText = "SanityValue",
            onValueChange = mutAttr::sanityValueChange,
            onRevert = mutAttr::sanityValueRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.talentHp,
            labelText = "Talent HP",
            onValueChange = mutAttr::talentHpChange,
            onRevert = mutAttr::talentHpRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.talentMelee,
            labelText = "Talent Melee",
            onValueChange = mutAttr::talentMeleeChange,
            onRevert = mutAttr::talentMeleeRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.talentShot,
            labelText = "Talent Shot",
            onValueChange = mutAttr::talentShotHpChange,
            onRevert = mutAttr::talentShotRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.talentDefense,
            labelText = "Talent Defense",
            onValueChange = mutAttr::talentDefenseChange,
            onRevert = mutAttr::talentDefenseRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.craftSpeed,
            labelText = "CraftSpeed",
            onValueChange = mutAttr::craftSpeedChange,
            onRevert = mutAttr::craftSpeedRevert,
        )

        HeightSpacer(8.dp)

        RevertibleNumberTextField(
            modifier = Modifier.padding(top = 2.dp),
            value = mutAttr.rank,
            labelText = "Rank",
            onValueChange = mutAttr::rankChange,
            onRevert = mutAttr::rankRevert,
        )

        HeightSpacer(12.dp)

        PalGenderEditField(
            modifier = Modifier,
            value = mutAttr.mutGender,
            onValueChange = mutAttr::genderChange,
            onRevert = mutAttr::genderRevert
        )

        HeightSpacer(16.dp)

        WorkSuitabilitiesEditField(
            modifier = Modifier,
            mutCraftSpeeds = mutAttr.craftSpeeds
        )
    }
}

@Composable
private fun PalGenderEditField(
    modifier: Modifier,
    value: PalGender?,
    onValueChange: (PalGender?) -> Unit,
    onRevert: () -> Unit
) {
    Column(modifier) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Gender",
                color = Color(252, 252, 252),
                style = Material3Theme.typography.titleMedium,
                maxLines = 1,
            )
            WidthSpacer(4.dp)
            SingleLineSimpleTooltip(
                text = "Revert",
                modifier = Modifier.clickable(onClick = onRevert)
            ) {
                Box(modifier = Modifier.size(16.dp)) {
                    Icon(
                        painter = painterResource("drawable/undo_simplefill_32px.png"),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }
        }

        HeightSpacer(4.dp)

        Row {
            WidthSpacer(8.dp)

            Column {
                // Male
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier,
                        selected = value == PalGender.Male,
                        onClick = { onValueChange.invoke(PalGender.Male) },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = Color(0xFFCAC4D0),
                            disabledUnselectedColor = Color(0xFFE6E1E5)
                                .copy(alpha = 0.38f)
                        )
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "Male",
                        color = Color(252, 252, 252),
                        style = Material3Theme.typography.labelMedium,
                        maxLines = 1,
                    )
                }

                HeightSpacer(4.dp)

                // Female
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier,
                        selected = value == PalGender.Female,
                        onClick = { onValueChange.invoke(PalGender.Female) },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = Color(0xFFCAC4D0),
                            disabledUnselectedColor = Color(0xFFE6E1E5)
                                .copy(alpha = 0.38f)
                        )
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "Female",
                        color = Color(252, 252, 252),
                        style = Material3Theme.typography.labelMedium,
                        maxLines = 1,
                    )
                }

                HeightSpacer(4.dp)

                // Undefined
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier,
                        selected = value == null,
                        onClick = { onValueChange.invoke(null) },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = Color(0xFFCAC4D0),
                            disabledUnselectedColor = Color(0xFFE6E1E5)
                                .copy(alpha = 0.38f)
                        )
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "Undefined",
                        color = Color(252, 252, 252),
                        style = Material3Theme.typography.labelMedium,
                        maxLines = 1,
                    )
                    WidthSpacer(2.dp)
                    SingleLineSimpleTooltip(
                        text = "Undefined Gender, Black Marketeer for example has no defined gender",
                        modifier = Modifier
                    ) {
                        Box(modifier = Modifier.padding(4.dp).size(16.dp)) {
                            Icon(
                                painter = painterResource("drawable/info_simple_outline_16px.png"),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                    }
                }

                HeightSpacer(4.dp)

                // Named (NoImpl yet)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        modifier = Modifier,
                        selected = false,
                        onClick = { onValueChange.invoke(null) },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = Color(0xFFCAC4D0),
                            disabledUnselectedColor = Color(0xFFE6E1E5)
                                .copy(alpha = 0.38f)
                        ),
                        enabled = false
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "Named",
                        color = Color(252, 252, 252).copy(alpha = 0.68f),
                        style = Material3Theme.typography.labelMedium,
                        maxLines = 1,
                    )
                    WidthSpacer(2.dp)
                    SingleLineSimpleTooltip(
                        text = "(Not Implemented) placeholder",
                        modifier = Modifier
                    ) {
                        Box(modifier = Modifier.padding(4.dp).size(16.dp)) {
                            Icon(
                                painter = painterResource("drawable/info_simple_outline_16px.png"),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkSuitabilitiesEditField(
    modifier: Modifier,
    mutCraftSpeeds: AttributeEditPanelState.MutAttribute.MutCraftSpeeds
) {
    val mutEntries = mutCraftSpeeds.mutEntries

    Column(modifier) {

        Row(
            modifier = Modifier
                .clickable(onClick = mutCraftSpeeds::userToggleExpand)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(12.dp),
                painter = if (mutCraftSpeeds.expanded) {
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
                    text = "Work Suitabilities",
                    style = Material3Theme.typography.titleMedium,
                    maxLines = 1,
                    color = Color(252, 252, 252),
                )
            }
        }

        if (mutCraftSpeeds.opened) {

            val heightBarHeightState = remember {
                mutableStateOf(0.dp)
            }
            Row(
                modifier = Modifier.layout { measurable, constraints ->
                    val measure = measurable.measure(constraints.noMinConstraints())

                    if (!mutCraftSpeeds.expanded) {
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
                        .clickable(onClick = mutCraftSpeeds::userToggleExpand)
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
                                key = { i -> mutEntries[i].entry.craftSpeed.name },
                            ) { i ->
                                val mutEntry = mutEntries[i]
                                val name = mutEntry.entry.craftSpeed.name
                                val rank = mutEntry.mutRank
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
                                        text = name,
                                        style = Material3Theme.typography.labelMedium,
                                        maxLines = 1,
                                        softWrap = false,
                                        color = Color(252, 252, 252)
                                    )

                                    WidthSpacer(4.dp)

                                    RevertibleNumberTextField(
                                        modifier = Modifier.padding(top = 2.dp),
                                        value = rank,
                                        labelText = "Rank",
                                        onValueChange = mutEntry::rankChange,
                                        onRevert = mutEntry::rankRevert
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
        }
    }
}

@Composable
private fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val dotRadius = animateDpAsState(
        targetValue = if (selected) RadioButtonDotSize / 2 else 0.dp,
        animationSpec = tween(durationMillis = RadioAnimationDuration)
    )
    val radioColor = run {
        val target = with(colors) {
            when {
                enabled && selected -> selectedColor
                enabled && !selected -> unselectedColor
                !enabled && selected -> disabledSelectedColor
                else -> disabledUnselectedColor
            }
        }

        // If not enabled 'snap' to the disabled state, as there should be no animations between
        // enabled / disabled.
        if (enabled) {
            animateColorAsState(target, tween(durationMillis = RadioAnimationDuration))
        } else {
            rememberUpdatedState(target)
        }
    }
    val selectableModifier =
        if (onClick != null) {
            @Suppress("DEPRECATION_ERROR")
            Modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                    radius = /*RadioButtonTokens.StateLayerSize*/ 40.dp / 2 / 2
                )
            )
        } else {
            Modifier
        }
    Canvas(
        modifier
            .then(
                if (onClick != null) {
                    Modifier/*.minimumInteractiveComponentSize()*/
                } else {
                    Modifier
                }
            )
            .then(selectableModifier)
            .wrapContentSize(Alignment.Center)
            .padding(RadioButtonPadding)
            .requiredSize(/*RadioButtonTokens.IconSize*/ 20.dp)
    ) {
        // Draw the radio button
        val strokeWidth = RadioStrokeWidth.toPx()
        drawCircle(
            radioColor.value,
            radius = (/*RadioButtonTokens.IconSize*/ 20.dp / 2).toPx() - strokeWidth / 2,
            style = Stroke(strokeWidth)
        )
        if (dotRadius.value > 0.dp) {
            drawCircle(radioColor.value, dotRadius.value.toPx() - strokeWidth / 2, style = Fill)
        }
    }
}

private const val RadioAnimationDuration = 100

private val RadioButtonPadding = 2.dp
private val RadioButtonDotSize = 12.dp
private val RadioStrokeWidth = 2.dp