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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers

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

                Text(
                    "Nickname: ${state.mutAttribute?.attribute?.nickName}",
                    style = Material3Theme.typography.labelMedium,
                    color = Color(252, 252, 252)
                )

                HeightSpacer(4.dp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFAED285))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "${state.mutAttribute?.attribute?.characterId}",
                        color = Color(0xFF221728),
                        fontWeight = FontWeight.SemiBold,
                        style = Material3Theme.typography.labelMedium
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
                        text = "UID: ${state.mutAttribute?.attribute?.uid}",
                        color = Color(0xFF221728),
                        fontWeight = FontWeight.SemiBold,
                        style = Material3Theme.typography.labelMedium
                    )
                }
            }
        }
    }
}