package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberPlayerSaveEditPanelState(): PlayerSaveEditPanelState {

    val state = remember() {
        PlayerSaveEditPanelState()
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class PlayerSaveEditPanelState {

    var expanded by mutableStateOf(false)
        private set

    var noContent by mutableStateOf(false)
        private set

    private var _coroutineScope: CoroutineScope? = null

    private var wasExpanded = false

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "State wasn't initialized"
        }

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + MainUIDispatcher)
    }

    fun stateExit() {
        coroutineScope.cancel()
    }

    fun userToggleExpand() {
        expanded = !expanded
        if (expanded) {
            if (!wasExpanded) {
                wasExpanded = true
            }
        }
    }
}