package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.rememberMutableStateOf
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerTrainer() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .padding(horizontal = 12.dp)
    ) {
        PlayerTrainerToggleUI("Infinite Player HP")
        HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
        PlayerTrainerToggleUI("Infinite Player STAMINA")
    }
}

// TODO: complete this
@Composable
private fun PlayerTrainerToggleUI(
    name: String
) {
    var checked by rememberMutableStateOf { false }
    var loading by rememberMutableStateOf { false }
    val j = remember {
        mutableStateOf<Job?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

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
            // so we use [key] which will allocate new composition on key change hence fresh start
            val keyS = remember {
                mutableStateOf(0)
            }
            key(keyS.value) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { enable ->
                        if (enable) {
                            if (!checked && !loading) {
                                loading = true
                                j.value = coroutineScope.launch {
                                    delay(200)
                                    checked = true
                                    loading = false
                                }
                            }
                        } else {
                            if (checked && !loading) {
                                loading = true
                                j.value = coroutineScope.launch {
                                    delay(200)
                                    checked = false
                                    loading = false
                                    keyS.value++
                                }
                            }
                        }
                    },
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