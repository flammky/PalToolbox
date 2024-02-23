package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.StableList
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonFontScaled
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonScaledFontSize
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import dev.dexsr.gmod.palworld.trainer.win32.WinHelper
import dev.dexsr.gmod.palworld.trainer.win32.WindowProcessInfo
import dev.dexsr.gmod.palworld.trainer.win32.queryProcessByExecName
import kotlinx.coroutines.delay
import java.awt.Dimension

@Composable
fun TrainerMainScreen(
    modifier: Modifier = Modifier,
    state: TrainerMainScreenState = rememberTrainerMainScreenState()
) {
    CompositionLocalProvider(
        LocalIndication provides rememberRipple()
    ) {
        Column(
            modifier
                .fillMaxSize()
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {

            TrainerProcessSelectPanel(
                modifier = Modifier
                    .padding(
                        start = MD3Spec.padding.incrementsDp(2).dp,
                        end = MD3Spec.padding.incrementsDp(2).dp,
                        top = MD3Spec.padding.incrementsDp(2).dp
                    )
                    .defaultMinSize(minWidth = 200.dp)
                    .widthIn(max = 800.dp)
                    .height(30.dp),
                state
            )

            state.selectedProcess?.let { proc ->
                TrainerContent(
                    Modifier.padding(top = MD3Spec.padding.incrementsDp(3).dp),
                    proc
                )
            }
        }
    }
}

@Composable
private fun TrainerProcessSelectPanel(
    modifier: Modifier,
    state: TrainerMainScreenState
) {
    val openSelectProcess = remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
        .clickable { openSelectProcess.value = true }
        .background(remember { Color(41, 36, 46) })
        .padding(MD3Spec.padding.incrementsDp(1).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
        ) {
            run {
                val proc = state.selectedProcess
                val text = remember(proc) {
                    if (proc == null) return@remember "Click here to select game Process"
                    "[${proc.processId}] ${proc.processName}"
                }
                val color = remember(proc) {
                    proc?.let { Color(252, 252, 252) } ?: Color.White.copy(alpha = 0.78f)
                }
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = text,
                    style = MaterialTheme.typography.caption,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (openSelectProcess.value) SelectProcessWindow(
        state,
        onCloseRequest = { openSelectProcess.value = false }
    )
}

@Composable
private fun SelectProcessWindow(
    state: TrainerMainScreenState,
    onCloseRequest: () -> Unit
) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        state = rememberDialogState(
            size = DpSize(400.dp, 600.dp)
        ),
        title = "Select Game Process",
        icon = painterResource("drawable/palworld_p_icon_b.jpg")
    ) {
        remember(window) {
            window.minimumSize = Dimension(200, 200)
        }
        SelectProcessWindowContent(onChosen = { proc ->
            state.userSelectProcess(proc)
            onCloseRequest()
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectProcessWindowContent(
    onChosen: (WindowProcessInfo) -> Unit
) {
    val list = remember {
        mutableStateOf(listOf<WindowProcessInfo>(), neverEqualPolicy())
    }

    var consumed by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val querynano = System.nanoTime()
            list.value = WinHelper.queryProcessByExecName("Palworld-Win64-Shipping.exe").also {
                println("queryProcessByExecName took ${System.nanoTime() - querynano}ns")
            }
            delay(500)
        }
    }

    val style = MaterialTheme.typography.body2.let { style ->
        val fontSize = style.nonScaledFontSize()
        style.copy(
            color = Color(252, 252, 252),
            fontSize = fontSize
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(24, 24, 24) })
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        val selectedPID = remember {
            mutableStateOf(0L)
        }
        list.value.fastForEach { v ->
            key(v.id) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    selectedPID.value = v.id
                                },
                                onDoubleClick = {
                                    if (consumed) return@combinedClickable
                                    consumed = true
                                    onChosen(v)
                                }
                            )
                            .then(
                                if (v.id == selectedPID.value) {
                                    Modifier.background(remember { Color(0, 55, 255) })
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 4.dp),
                        text = "${v.id} - ${v.name}",
                        maxLines = 1,
                        style = style
                    )
                }
            }
        }
    }
}

private class TrainerContent(
    val id: String,
    val content: @Composable () -> Unit,
)

@Composable
private fun TrainerContent(
    modifier: Modifier,
    process: TrainerTargetProcess
) {
    val dest = remember {
        mutableStateOf<StableList<TrainerContent>>(StableList(emptyList()))
    }

    // the compose compiler will remember lambda for us
    val onSelectClicked: (TrainerContent) -> Unit = { select ->
        if (!dest.value.contains(select)) {
            dest.value = StableList(
                ArrayList<TrainerContent>()
                    .apply { addAll(dest.value) ; add(select) }
            )
        } else {
            dest.value = StableList(
                ArrayList<TrainerContent>()
                    .apply {
                        dest.value.forEach {
                            if (it.id != select.id) add(it)
                        }
                        add(select)
                    }
            )
        }

        println("destContents=${dest.value.map { "${it.id}" }}")
    }

    Row(
        modifier = modifier
            .padding(horizontal = MD3Spec.padding.incrementsDp(2).dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 200.dp)) {
            Row(modifier = Modifier.height(40.dp)) {
                val select = remember {
                    TrainerContent(
                        id = "player",
                        content =  { PlayerTrainer() }
                    )
                }
                val selected = dest.value.lastOrNull()?.id == "player"
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .defaultMinSize(minWidth = 100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(!selected) { onSelectClicked.invoke(select) }
                        .then(
                            if (selected) {
                                Modifier
                                    .background(
                                        remember { Color(48, 40, 56) },
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("drawable/simple_user_32px.png"),
                        contentDescription = null,
                        tint = remember { Color(168, 140, 196) }
                    )
                    WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                    Text(
                        "Player",
                        style = MaterialTheme.typography.body2
                            .nonFontScaled()
                            .copy(
                                color = remember { Color(250, 250, 250) },
                                fontWeight = FontWeight.SemiBold
                            ),
                        maxLines = 1
                    )
                }
            }

            HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
            Row(modifier = Modifier.height(40.dp)) {

                val select = remember {
                    TrainerContent(
                        id = "world",
                        content =  { WorldTrainer() }
                    )
                }
                val selected = dest.value.lastOrNull()?.id == "world"
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .defaultMinSize(minWidth = 100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onSelectClicked.invoke(select) }
                        .then(
                            if (selected) {
                                Modifier
                                    .background(
                                        remember { Color(48, 40, 56) },
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("drawable/simple_world_32px.png"),
                        contentDescription = null,
                        tint = remember { Color(168, 140, 196) }
                    )
                    WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                    Text(
                        "World",
                        style = MaterialTheme.typography.body2
                            .nonFontScaled()
                            .copy(
                                color = remember { Color(250, 250, 250) },
                                fontWeight = FontWeight.SemiBold
                            ),
                        maxLines = 1
                    )
                }
            }
        }

        if (dest.value.isNotEmpty()) {
            Box {
                dest.value.fastForEach { v ->
                    key(v.id) {
                        v.content.invoke()
                    }
                }
            }
        }
    }
}