package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.savegame.parser.SaveGameParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberSaveGamePlayerEditorState(
    uid: String,
    name: String
): SaveGamePlayerEditorState {
    val coroutineScope = rememberCoroutineScope()

    val state = remember(uid, name) {
        SaveGamePlayerEditorState(uid, name, coroutineScope,)
    }

    DisposableEffect(state) {
        state.onRemembered()
        onDispose { state.onDispose() }
    }

    return state
}

class SaveGamePlayerEditorState(
    val uid: String,
    val name: String,
    val coroutineScope: CoroutineScope
) {

    private val lifetime = SupervisorJob()
    private val parser = SaveGameParser(CoroutineScope(lifetime))

    var initialName by mutableStateOf<String?>(name)
    var mutName by mutableStateOf<String?>(initialName)
    var mutNameCursor by mutableStateOf(TextRange(0))

    var initialUid by mutableStateOf<String?>(uid.filter(Char::isLetterOrDigit))
    var mutUid by mutableStateOf<String?>(initialUid)
    var mutUidCursor by mutableStateOf(TextRange(0))

    var loading by mutableStateOf(false)

    var showEditor by mutableStateOf(false)

    fun onRemembered() {
        init()
    }

    fun onDispose() {
        lifetime.cancel()
    }

    fun nickNameFieldChange(
        textFieldValue: TextFieldValue
    ) {
        if (textFieldValue.text.length > 24 && (mutName?.length ?: 0) > 23) {
            return
        }
        var n = 0
        mutName =  StringBuilder()
            .apply {
                textFieldValue.text.forEach { c ->
                    if (!c.isLetterOrDigit()) return@forEach
                    append(c)
                    if (++n == 32) return@apply
                }
            }
            .toString()
        this.mutNameCursor = textFieldValue.selection
    }

    fun uidTextFieldChange(
        textFieldValue: TextFieldValue
    ) {
        if (textFieldValue.text.length > 32 && (mutUid?.length ?: 0) > 31) {
            return
        }
        var n = 0
        mutUid =  StringBuilder()
            .apply {
                textFieldValue.text.forEach { c ->
                    if (!c.isLetterOrDigit()) return@forEach
                    append(c)
                    if (++n == 32) return@apply
                }
            }
            .toString()
        this.mutUidCursor = textFieldValue.selection

        /*var seg1Take = 0
        val seg1 = StringBuilder()
            .apply {
                uid.forEachIndexed { i, c ->
                    if (length == 8) {
                        if (c == '-') {
                            append(c)
                            seg1Take = i + 1
                        } else {
                            append('-')
                            seg1Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@forEachIndexed
                    append(c)
                }
            }
        if (seg1.length < 9) {
            mutUid = uid
            return
        }

        var seg2Take = 0
        val seg2 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - seg1Take)) { i ->
                    val c = uid[i + seg1Take]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg2Take = i + 1
                        } else {
                            append('-')
                            seg2Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        println("seg2=$seg2")
        if (seg2.length < 5) {
            mutUid = seg1.toString() + seg2.toString()
            return
        }

        var seg3Take = 0
        val seg3 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take)]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg3Take = i + 1
                        } else {
                            append('-')
                            seg3Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        if (seg3.length < 5) {
            mutUid = seg1.toString() + seg2.toString() + seg3.toString()
            return
        }

        var seg4Take = 0
        val seg4 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take + seg3Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take + seg3Take)]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg4Take = i + 1
                        } else {
                            append('-')
                            seg4Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        if (seg4.length < 5) {
            mutUid = seg1.toString() + seg2.toString() + seg3.toString() + seg4.toString()
            return
        }

        val seg5 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take + seg3Take + seg4Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take + seg3Take + seg4Take)]
                    if (c == '-') return@repeat
                    append(c)
                    if (length == 12) return@apply
                }
            }

        mutUid = seg1.toString() + seg2.toString() + seg3.toString() + seg4.toString() + seg5.toString()*/
    }

    fun revertNickName() {
        mutName = initialName
    }

    fun revertUid() {
        mutUid = initialUid
    }

    private fun init() {

        coroutineScope.launch(MainUIDispatcher + lifetime) {

            loading = true
            loading = false
            showEditor = true
        }
    }
}