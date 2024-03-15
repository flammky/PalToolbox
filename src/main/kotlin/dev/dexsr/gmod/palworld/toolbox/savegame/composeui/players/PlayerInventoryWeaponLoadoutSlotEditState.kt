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
fun rememberPlayerInventoryWeaponLoadOutSlotEditState(
    inventoryState: InventoryEditPanelState
): PlayerInventoryWeaponLoadOutSlotEditState {

    val state = remember(inventoryState) {
        PlayerInventoryWeaponLoadOutSlotEditState(inventoryState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

@Stable
class PlayerInventoryWeaponLoadOutSlotEditState(
    private val inventoryState: InventoryEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope get() = requireNotNull(_coroutineScope)

    val uid
        get() = inventoryState.slotUid(InventoryEditPanelState.DropSlot)

    val mutableEntries = mutableStateOf<List<MutEntry>?>(
        null,
        neverEqualPolicy()
    )

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
            editor.parseInventoryUIDMap()
            val entries = editor.getOrParseInventoryEntryAsync("WeaponLoadOutContainerId").await()
            mutableEntries.value = entries?.map { entry ->
                Entry(
                    entry.index,
                    entry.itemId,
                    entry.stackCount
                ).mut()
            }
        }
    }

    @Immutable
    class Entry(
        val index: Int,
        val itemId: String,
        val stackCount: Int
    )

    private fun Entry.mut() = MutEntry(this)

    @Stable
    class MutEntry(
        private val entry: Entry
    ) {

        // define stable key ?

        val index = entry.index

        var itemId by mutableStateOf(TextFieldValue(entry.itemId), neverEqualPolicy())
            private set

        var stackCount by mutableStateOf(TextFieldValue(entry.stackCount.toString()), neverEqualPolicy())
            private set

        fun itemIdChange(value: TextFieldValue) {
            // put hardcoded limit
            // as of this writing the max known length is only 73
            val maxLen = 512
            if (value.text.length > maxLen) return
            itemId = value
        }

        fun itemIdRevert() {
            itemId = TextFieldValue(entry.itemId)
        }

        fun stackCountChange(value: TextFieldValue) {
            val max = Int.MAX_VALUE
            if (value.text.length > max.toString().length) return
            if (value.text.isNotEmpty()) {
                if (!value.text.all(Char::isDigit)) return
                val num = value.text.toIntOrNull() ?: return
                stackCount = TextFieldValue(
                    value.text,
                    value.selection
                )
            } else {
                stackCount = TextFieldValue()
            }
        }

        fun stackCountRevert() {
            stackCount = TextFieldValue(entry.stackCount.toString())
        }
    }
}