package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import dev.dexsr.gmod.palworld.toolbox.composeui.WorkInProgressScreen
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.libint.DragData
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.libint.onExternalDrag
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players.PlayersEditPanel
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.LocalWindow
import dev.dexsr.gmod.palworld.trainer.composeui.StableList
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonFontScaled
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonScaledFontSize
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.JnaFileChooserWindowHost
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import java.awt.FileDialog
import java.io.File


@Composable
fun SaveGameFeaturesScreen() {
    val state = rememberSaveGameFeaturesScreenState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
            .dragAndDrop(state, showInBoundEffect = true)
    ) {
        CompositionLocalProvider(
            LocalIndication provides rememberRipple(),
        ) {
            Column {
                SaveGameFeaturesScreenFileSelectionPanel(
                    modifier = Modifier
                        .padding(8.dp)
                        .defaultMinSize(minWidth = 200.dp)
                        .widthIn(max = 800.dp)
                        .height(30.dp),
                    state
                )

                val editState = state.chosenFile?.let {
                    val coroutineScope = rememberCoroutineScope()
                    val keyS = remember { mutableStateOf(it to 0, neverEqualPolicy()) }
                        .apply { if (value.first !== it) value = it to value.second + 1 }
                    remember(keyS.value) { SaveGameEditState(it, coroutineScope) }
                }
                SaveGameFeaturesScreenFileInfoPanel(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = 0.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        ),
                    editState = editState
                )
                if (editState?.showEditor == true) {
                    /*SaveGameFeaturesScreenFileInfo(
                        Modifier.padding(
                            start = 8.dp,
                            top = 0.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        ),
                        editState
                    )*/
                    SaveGameFeaturesScreenEditPanel(editState)
                }
            }
        }
    }
}

@Composable
fun SaveGameFeaturesScreenFileSelectionPanel(
    modifier: Modifier,
    state: SaveGameFeaturesScreenState
) = key(state.chosenFile) {
    val openFilePicker = remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 30.dp)
        .clip(RoundedCornerShape(4.dp))
        .shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
        .clickable { openFilePicker.value = true }
        .background(remember { Color(41, 36, 46) })
        .padding(MD3Spec.padding.incrementsDp(1).dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
        ) {
            run {
                val f = state.chosenFile
                val fp = state.chosenFile?.absolutePath
                val fp1 = remember(f) {
                    var dash = false
                    fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                }
                val fp2 = remember(f) {
                    var dash = false
                    fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                }
                val color = remember(f) {
                    fp?.let { Color(250, 250, 250) } ?: Color.White.copy(alpha = 0.78f)
                }
                Text(
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    text = fp1?.plus(fp2) ?: "Click here to select File (*.sav)",
                    style = MaterialTheme.typography.caption,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)
            Icon(
                modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                painter = painterResource("drawable/simple_save_file_32px.png"),
                contentDescription = null,
                tint = remember { Color(168, 140, 196) }
            )
        }
    }
    if (openFilePicker.value) NativeFilePickerDialog(
        title = "Pick save game file to edit (*.sav)",
        initialDir = state.chosenFile?.parent,
        onCloseRequest = { state.filePick(it) ; openFilePicker.value = false }
    )
}

@Composable
private fun NativeFilePickerDialog(
    title: String,
    initialDir: String?,
    onCloseRequest: (File?) -> Unit
) {
    val window = LocalWindow.current
    AwtWindow(
        visible = false,
        create = {
            JnaFileChooserWindowHost(window, title, initialDir)
                .apply {
                    openAndInvokeOnCompletion { result ->
                        onCloseRequest(result.getOrNull())
                    }
                }
        },
        dispose = JnaFileChooserWindowHost::dispose
    )
}

@Composable
private fun AwtFilePickerDialog(
    onCloseRequest: (File?) -> Unit
) = AwtWindow(
    visible = false,
    create = {
        val frame: java.awt.Frame? = null
        object : FileDialog(frame, "Pick the File", LOAD) {

            init {
                setFilenameFilter { dir, name ->
                    name.endsWith(".sav")
                }
                isVisible = true
            }

            override fun setVisible(b: Boolean) {
                super.setVisible(b)
                if (!b) {
                    onCloseRequest(files.firstOrNull())
                    dispose()
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Modifier.dragAndDrop(
    state: SaveGameFeaturesScreenState,
    showInBoundEffect: Boolean
): Modifier {
    val draggingInBoundState = remember {
        mutableStateOf(false)
    }
    return this
        .onExternalDrag(
            LocalWindow.current,
            onDragStart = { start ->
                if (
                    start.dragData is DragData.FilesList &&
                    start.dragData.readFiles().any { it.endsWith(".sav") }
                ) {
                    draggingInBoundState.value = true
                }

            },
            onDrag = { drag ->
            },
            onDragExit = {
                draggingInBoundState.value = false
            }
        ) { drop ->
            draggingInBoundState.value = false
            if (drop.dragData is DragData.FilesList) {
                state.fileDrop(drop.dragData.readFiles().firstOrNull()?.let(::jFile))
            }
        }
        .then(
            if (draggingInBoundState.value && showInBoundEffect) {
                Modifier.border(
                    width = 1.dp,
                    color = Color.Green
                )
            } else Modifier
        )
}

@Composable
private fun SaveGameFeaturesScreenFileInfoPanel(
    modifier: Modifier,
    editState: SaveGameEditState?
) {
    val msg = editState?.topFileOperationMsg
        ?: return
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .shadow(2.dp, shape = RoundedCornerShape(4.dp))
            .background(remember { Color(74, 68, 88) })
            .padding(vertical = MD3Spec.padding.incrementsDp(1).dp, horizontal = MD3Spec.padding.incrementsDp(2).dp)
    ) {

        val style = MaterialTheme.typography.caption
            .copy(
                color = remember { Color(250, 250, 250) },
                fontWeight = FontWeight.Medium,
                fontSize = MaterialTheme.typography.caption.nonScaledFontSize()
            )

        Text(
            modifier = Modifier,
            text = msg,
            style = style,
            maxLines = 1,
        )

        /*if (loading) {

        } else if (editState != null) {
            *//*Text(
                text = "Editing file: ${editState.fileName}",
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)

            // fixme: let state object parse it and make UI logic simpler
            Text(
                text = editState.nameDescription,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )*//*
        }*/
    }
}

@Composable
private fun SaveGameFeaturesScreenFileInfo(
    modifier: Modifier,
    state: SaveGameEditState
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(24 * 2, 20 * 2, 28 * 2))
            // maybe no ?
            // TODO: make so ripple expand as container expand
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(12.dp).alpha(0.75f),
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
                text = "File Description",
                color = remember { Color(240, 240, 240, (255 * 0.85f).toInt()) },
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
            )
        }
        if (expanded) {
            HeightSpacer(MD3Spec.padding.incrementsDp(1).dp)
            Text(
                "name: ${state.fileName}",
                color = remember { Color(240, 240, 240, 230) },
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SaveGameFeaturesScreenEditPanel(
    state: SaveGameEditState
) {
    // TODO: don't hardcode
    SaveGameEditContent(Modifier.padding(
        start = 8.dp,
        top = 0.dp,
        end = 8.dp,
        bottom = 8.dp
    ), state)
}

private class SaveGameEditContent(
    val id: String,
    val content: @Composable () -> Unit,
)

// TODO: write navigator
@Composable
private fun SaveGameEditContent(
    modifier: Modifier,
    saveGameEditState: SaveGameEditState
) {
    val dest = remember {
        mutableStateOf<StableList<SaveGameEditContent>>(StableList(emptyList()))
    }

    // the compose compiler will remember lambda for us
    val onSelectClicked: (SaveGameEditContent) -> Unit = { select ->
        if (!dest.value.contains(select)) {
            dest.value = StableList(
                ArrayList<SaveGameEditContent>()
                    .apply { addAll(dest.value) ; add(select) }
            )
        } else {
            dest.value = StableList(
                ArrayList<SaveGameEditContent>()
                    .apply {
                        dest.value.forEach {
                            if (it.id != select.id) add(it)
                        }
                        add(select)
                    }
            )
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = MD3Spec.padding.incrementsDp(2).dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 200.dp)) {
            Row(modifier = Modifier.height(40.dp)) {
                val select = remember {
                    SaveGameEditContent(
                        id = "players",
                        content =  { PlayersEditPanel(
                            Modifier,
                            saveGameEditState
                        ) }
                    )
                }
                val selected = dest.value.lastOrNull()?.id == "players"
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
                        "Players",
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
                    SaveGameEditContent(
                        id = "pals",
                        content =  { WorkInProgressScreen(Modifier.background(remember { Color(29, 24, 34) })) }
                    )
                }
                val selected = dest.value.lastOrNull()?.id == "pals"
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
                        painter = painterResource("drawable/paw_32px.png"),
                        contentDescription = null,
                        tint = remember { Color(168, 140, 196) }
                    )
                    WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                    Text(
                        "Pals",
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
                    SaveGameEditContent(
                        id = "guilds",
                        content =  { WorkInProgressScreen(Modifier.background(remember { Color(29, 24, 34) })) }
                    )
                }
                val selected = dest.value.lastOrNull()?.id == "guilds"
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
                        painter = painterResource("drawable/wing_outline_32px.png"),
                        contentDescription = null,
                        tint = remember { Color(168, 140, 196) }
                    )
                    WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                    Text(
                        "Guilds",
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
            Box(modifier = Modifier.padding(horizontal = MD3Spec.padding.incrementsDp(2).dp)) {
                dest.value.fastForEach { v ->
                    key(v.id) {
                        v.content.invoke()
                    }
                }
            }
        }
    }
}