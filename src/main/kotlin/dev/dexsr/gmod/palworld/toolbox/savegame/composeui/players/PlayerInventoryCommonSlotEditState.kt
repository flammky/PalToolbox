package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberPlayerInventoryCommonSlotEditState(
    inventoryState: InventoryEditPanelState
): PlayerInventoryCommonSlotEditState {

    val state = remember(inventoryState) {
        PlayerInventoryCommonSlotEditState(inventoryState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

@Stable
class PlayerInventoryCommonSlotEditState(
    private val inventoryState: InventoryEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope get() = requireNotNull(_coroutineScope)

    val uid
        get() = inventoryState.slotUid(InventoryEditPanelState.CommonSlot)

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

            editor
        }
    }

    @Immutable
    class Entry(
        val index: Int,
        val itemId: String,
        val stackCount: Int
    )

    @Stable
    class MutEntry(
        val entry: Entry
    ) {
        val index = entry.index

        var itemId by mutableStateOf(TextFieldValue(entry.itemId))
            private set

        var stackCount by mutableStateOf(TextFieldValue(entry.stackCount.toString()))
            private set

        fun itemIdChange(itemId: TextFieldValue) {
            // put hard limit
            // as of this writing the max known length is only 73
            val maxLen = 512
            if (itemId.text.length > maxLen) return
        }
    }
}