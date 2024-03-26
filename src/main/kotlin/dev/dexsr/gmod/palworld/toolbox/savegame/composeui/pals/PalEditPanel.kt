package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.NoOpPainter
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding

@Composable
fun PalEditPanel(
    modifier: Modifier,
    palsEditPanelState: PalsEditPanelState,
    pal: String
) = PalEditPanel(
    modifier,
    rememberPalEditPanelState(palsEditPanelState, pal)
)

@Composable
fun PalEditPanel(
    modifier: Modifier,
    state: PalEditPanelState
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
            .padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {

            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 35.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = state::exit)
                        .padding(2.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("drawable/arrow_left_simple_32px.png"),
                        tint = Color(168, 140, 196),
                        contentDescription = null
                    )
                }

                WidthSpacer(12.dp)

                Row {
                    Text(
                        "Editing Pal",
                        style = Material3Theme.typography.labelMedium,
                        color = Color(252, 252, 252)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFF978e98)
            )

            HeightSpacer(8.dp)

            Column(modifier = Modifier.padding(start = 4.dp)) {

                run {
                    val nickName = state.mutAttribute?.attribute?.nickName
                    val style = Material3Theme.typography.labelMedium
                    Text(
                        buildAnnotatedString {
                            append("Nickname: ")
                            withStyle(style.toSpanStyle().copy(
                                color = Color(
                                    252, 252, 252,
                                    alpha = if (nickName != null) 0xFF else (0.38f * 255).toInt()
                                )
                            )) {
                                append("${nickName ?: state.mutAttribute?.attributeDisplayData?.displayName}")
                            }
                        },
                        style = style,
                        color = Color.White
                    )
                }

                HeightSpacer(4.dp)

                run {
                    val breedName = state.mutAttribute?.attributeDisplayData?.breed
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFAED285))
                            .padding(vertical = 2.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            text = "$breedName",
                            color = Color(0xFF221728),
                            fontWeight = FontWeight.SemiBold,
                            style = Material3Theme.typography.labelMedium,
                            maxLines = 1
                        )
                        val gender = state.mutAttribute?.attribute?.gender
                        if (gender != null && gender !is PalGender.Named) {
                            WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = when(gender) {
                                    PalGender.Female -> painterResource("drawable/gender_female_16px.png")
                                    PalGender.Male -> painterResource("drawable/gender_male_16px.png")
                                    else -> NoOpPainter
                                },
                                contentDescription = null,
                                tint = Color(10,10,10)
                            )
                        }
                        if (state.mutAttribute?.attributeDisplayData?.isLucky == true) {
                            WidthSpacer(4.dp)
                            Icon(
                                modifier = Modifier.size(14.dp),
                                painter = painterResource("drawable/sparkle1_filled_16px.png"),
                                contentDescription = null,
                                tint = Color(0xFFd1831b)
                            )
                        } else if (state.mutAttribute?.attributeDisplayData?.isAlpha == true) {
                            WidthSpacer(4.dp)
                            Icon(
                                modifier = Modifier.size(14.dp),
                                painter = painterResource("drawable/devil_simple_16px.png"),
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    }
                }

                HeightSpacer(4.dp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFA7C8FD))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "Lv. ${state.mutAttribute?.attribute?.let { it.level ?: 0 }}",
                        color = Color(0xFF221728),
                        fontWeight = FontWeight.SemiBold,
                        style = Material3Theme.typography.labelMedium,
                        maxLines = 1
                    )
                }

                HeightSpacer(4.dp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFf5d9ff))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "UID: ${state.mutAttribute?.attributeDisplayData?.dashSeparatedUid}",
                        color = Color(0xFF221728),
                        fontWeight = FontWeight.SemiBold,
                        style = Material3Theme.typography.labelMedium
                    )
                }
            }

            HeightSpacer(12.dp)

            AttributeEditPanel(
                Modifier,
                rememberAttributeEditPanelState(state)
            )

            HeightSpacer(12.dp)

            InventoryEditPanel(
                Modifier,
                rememberInventoryEditPanelState(state)
            )

            HeightSpacer(12.dp)

            OwnershipEditPanel(
                Modifier,
                rememberOwnershipEditPanelState(state)
            )

            HeightSpacer(12.dp)

            SkillsEditPanel(
                Modifier,
                rememberSkillsEditPanelState(state)
            )
        }
    }
}