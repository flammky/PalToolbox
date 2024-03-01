package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.savegame.parser.SaveGameParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberSaveGamePlayerEditorState(
    uid: String
): SaveGamePlayerEditorState {
    val coroutineScope = rememberCoroutineScope()

    val state = remember(uid) {
        SaveGamePlayerEditorState(uid, coroutineScope)
    }

    DisposableEffect(state) {
        state.onRemembered()
        onDispose { state.onDispose() }
    }

    return state
}

class SaveGamePlayerEditorState(
    val uid: String,
    val coroutineScope: CoroutineScope
) {

    private val lifetime = SupervisorJob()
    private val parser = SaveGameParser(CoroutineScope(lifetime))

    var initialName by mutableStateOf<String?>("Turtl")
    var mutName by mutableStateOf<String?>(initialName)

    var initialUid by mutableStateOf<String?>("0".padStart(20, '0'))
    var mutUid by mutableStateOf<String?>(initialUid)

    var loading by mutableStateOf(false)

    var showEditor by mutableStateOf(false)

    fun onRemembered() {
        init()
    }

    fun onDispose() {
        lifetime.cancel()
    }

    fun userChangeNickName(name: String) {
        mutName = name.take(24)
    }

    fun userChangeUID(uid: String) {
        mutUid = uid.take(20)
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
            delay(400)
            loading = false
            showEditor = true
        }
    }
}