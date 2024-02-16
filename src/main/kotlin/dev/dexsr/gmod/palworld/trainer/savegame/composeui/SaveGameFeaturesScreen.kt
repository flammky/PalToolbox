package dev.dexsr.gmod.palworld.trainer.savegame.composeui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.libint.onExternalDrag
import androidx.compose.ui.window.AwtWindow
import dev.dexsr.gmod.palworld.trainer.composeui.LocalWindow
import dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.JnaFileChooserWindowHost
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.libint.DragData
import java.awt.FileDialog
import java.io.File


@Composable
fun SaveGameFeaturesScreen() {

    val draggingInBounds = remember {
        mutableStateOf(false)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onExternalDrag(
                LocalWindow.current,
                onDragStart = { start ->
                    println("dragStart=${start.dragPosition}")
                    if (
                        start.dragData is DragData.FilesList &&
                        start.dragData.readFiles().any { it.endsWith(".sav") }
                    ) {
                        draggingInBounds.value = true
                    }

                },
                onDrag = { drag ->
                    check(drag.dragData is DragData.FilesList)
                    println("onDrag=${drag.dragPosition}")
                },
                onDragExit = {
                    println("dragExit")
                    draggingInBounds.value = false
                }
            ) { drop ->
                println("dropped=$drop, data=${drop.dragData}")
                draggingInBounds.value = false
            }
            .composed {
                var acc: Modifier = Modifier
                if (draggingInBounds.value) {
                    acc = acc.border(
                        width = 1.dp,
                        color = Color.Green
                    )
                }
                acc
            }
    ) {
        val chosenFile = remember {
            mutableStateOf<File?>(null)
        }
        if (chosenFile.value == null) {
            val openFilePickerDialogState = remember {
                mutableStateOf(false)
            }
            Button(
                modifier = Modifier.align(Alignment.Center),
                enabled = true,
                onClick = { openFilePickerDialogState.value = true }
            ) {
                Text(
                    text = "PICK FILE TO EDIT",
                    color = Color.White
                )
            }
            if (openFilePickerDialogState.value) {
                NativeFilePickerDialog("Open File (save game edit)") { file ->
                    println("picked: $file")
                    openFilePickerDialogState.value = false
                }
            }
        }
    }
}

@Composable
private fun NativeFilePickerDialog(
    title: String,
    onCloseRequest: (File?) -> Unit
) {
    val window = LocalWindow.current
    AwtWindow(
        visible = false,
        create = {
            JnaFileChooserWindowHost(window, title)
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