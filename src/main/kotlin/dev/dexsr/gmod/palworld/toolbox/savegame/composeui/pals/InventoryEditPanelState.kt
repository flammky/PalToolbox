package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.uuidTextFieldChange
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberInventoryEditPanelState(
    palEditPanelState: PalEditPanelState
): InventoryEditPanelState {

    val state = remember(palEditPanelState) {
        InventoryEditPanelState(palEditPanelState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class InventoryEditPanelState(
    private val palEditPanelState: PalEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope)

    var opened by mutableStateOf(false)
        private set

    var expanded by mutableStateOf(false)
        private set

    var mutInventory by mutableStateOf<MutInventory?>(null)
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + UIFoundation.MainUIDispatcher)
    }
    fun stateExit() {
        coroutineScope.cancel()
    }

    fun userToggleExpand() {
        if (!opened) {
            opened = true
            expanded = true
            onInitialOpen()
            return
        }
        expanded = !expanded
    }

    private fun onInitialOpen() {

        coroutineScope.launch {
            val cache = palEditPanelState.cachedPalIndividualData()
            if (cache != null) {
                mutInventory = MutInventory(cache.inventory)
            }
            palEditPanelState.observePalIndividualData().collect { update ->
                if (update == null) {
                    mutInventory = null
                    return@collect
                }
                if (update === cache) {
                    // the flow emit the cache
                    return@collect
                }
                mutInventory?.update(update.inventory)
                    ?: MutInventory(update.inventory).also { mutInventory = it }
            }
        }
    }

    class MutInventory(
        val inventory: PalEditPanelState.Inventory
    ) {

        var mutEquipContainerId by mutableStateOf(
            inventory.equipItemContainerId.value.let(::TextFieldValue),
            neverEqualPolicy()
        )

        fun equipContainerIdChange(change: TextFieldValue) {
            ::mutEquipContainerId.uuidTextFieldChange(change)
        }

        fun equipContainerIdRevert() {
            mutEquipContainerId = inventory.equipItemContainerId.value.let(::TextFieldValue)
        }

        fun update(inventory: PalEditPanelState.Inventory) {
            mutEquipContainerId = inventory.equipItemContainerId.value.let(::TextFieldValue)
        }
    }
}