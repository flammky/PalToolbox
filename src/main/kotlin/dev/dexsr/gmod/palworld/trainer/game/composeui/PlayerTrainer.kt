package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding

@Composable
fun PlayerTrainer() {
    val state = rememberPlayerTrainerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .padding(horizontal = 12.dp)
    ) {
        PlayerTrainerDisclaimer()
        HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
        PlayerTrainerToggleUI(
            "Infinite Player HP",
            getLoading = state::infiniteHPLoading::get,
            getEnabled = state::infiniteHPEnabled::get,
            setEnabled = state::toggleInfiniteHP
        )
        HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
        PlayerTrainerToggleUI(
            "Infinite Player STAMINA",
            getLoading = state::infiniteStaminaLoading::get,
            getEnabled = state::infiniteStaminaEnabled::get,
            setEnabled = state::toggleInfiniteStamina
        )
    }
}

@Composable
private fun PlayerTrainerDisclaimer() {
    var expanded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(24 * 2, 20 * 2, 28 * 2))
            // TODO: make so ripple expand as container expand
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(12.dp),
                painter = if (expanded) {
                    painterResource("drawable/simple_arrow_head_down_32px.png")
                } else {
                    painterResource("drawable/simple_arrow_head_right_32px.png")
                },
                contentDescription = null,
                tint = Color.White
            )
            WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
            Text(
                modifier = Modifier,
                text = "Note",
                color = remember { Color(240, 240, 240, 255) },
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
            )
        }
        if (expanded) {
            HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
            Text(
                "These modifications are only tested and intended for Single Player, it may or may not work on Dedicated Server",
                color = remember { Color(240, 240, 240, 230) },
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



// TODO: tooltip
@Composable
private fun PlayerTrainerToggleUI(
    name: String,
    getLoading: () -> Boolean,
    getEnabled: () -> Boolean,
    setEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.width(200.dp),
            text = name,
            maxLines = 1,
            color = remember { Color(240, 240,240) },
            fontWeight = FontWeight.Medium
        )
        WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)
        Box {
            // note: we don't want the toggle back animate after showing the loading indicator,
            // so we use [key] which will allocate new composition on key change hence fresh start,
            // this is a quick workaround rather than having to copy the CheckBoxKt implementation
            val newCompositionTrigger = remember {
                var bool = false
                var v = 0
                derivedStateOf { if (bool and !getEnabled().also { bool = it }) ++v else v }
            }

            val enabled = getEnabled()
            val loading = getLoading()
            key(newCompositionTrigger.value) {

                Checkbox(
                    checked = enabled,
                    // fixme: should we force recomposition immediately ?
                    // so it will at least render the new state for a frame when clicking the cb really fast
                    onCheckedChange = { enable -> setEnabled(enable) },
                    modifier = Modifier.alpha(if (loading) 0f else 1f),
                    colors = CheckboxDefaults.colors(
                        checkedColor = remember { Color(144, 120, 168) },
                        uncheckedColor = Color.White.copy(alpha = 0.6f)
                    )
                )
            }
            if (loading) CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp
            )
        }
    }
}