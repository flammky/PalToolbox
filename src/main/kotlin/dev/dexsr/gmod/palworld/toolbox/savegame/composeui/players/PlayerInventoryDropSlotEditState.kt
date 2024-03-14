package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberPlayerInventoryDropSlotEditState(
    inventoryState: InventoryEditPanelState
): PlayerInventoryDropSlotEditState {

    val state = remember(inventoryState) {
        PlayerInventoryDropSlotEditState(inventoryState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

@Stable
class PlayerInventoryDropSlotEditState(
    private val inventoryState: InventoryEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope get() = requireNotNull(_coroutineScope)

    val uid
        get() = inventoryState.slotUid(InventoryEditPanelState.DropSlot)

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + UIFoundation.MainUIDispatcher)
        init()
    }

    fun stateExit() {
        coroutineScope.cancel()
    }

    private fun init() {
        coroutineScope.launch {
            val editor = inventoryState.editor ?: return@launch
            editor.prepare()
        }
    }
}