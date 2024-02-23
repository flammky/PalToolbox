package dev.dexsr.gmod.palworld.trainer.savegame.composeui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.LocalWindow
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonScaledFontSize
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.JnaFileChooserWindowHost
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.libint.DragData
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.libint.onExternalDrag
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

                // move to state ?
                val editState = state.gvas?.let { gvas -> state.chosenFile?.let { jF ->
                    remember(gvas, jF) { SaveGameEditState(jF, gvas) }
                } }
                SaveGameFeaturesScreenFileInfoPanel(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = 0.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        ),
                    loading = state.loadingFile,
                    editState = editState
                )
                editState?.let { SaveGameFeaturesScreenEditPanel(editState) }
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
                modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
                painter = painterResource("drawable/savegame_save2.png"),
                contentDescription = null,
                tint = Color.Unspecified
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
    loading: Boolean,
    editState: SaveGameEditState?
) {
    if (!loading && editState == null) {
        return
    }
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

        if (loading) {
            Text(
                modifier = Modifier,
                text = "Parsing File ...",
                style = style,
                maxLines = 1,
            )
        } else if (editState != null) {
            Text(
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
            )
        }
    }
}

@Composable
private fun SaveGameFeaturesScreenEditPanel(
    state: SaveGameEditState
) {
    state.properties.elements.fastForEach { prop ->
        key(prop) {
            Text(prop.nameString)
        }
    }
}