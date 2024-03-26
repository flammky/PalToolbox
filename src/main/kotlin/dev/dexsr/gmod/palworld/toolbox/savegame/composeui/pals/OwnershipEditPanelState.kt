package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.longTextFieldChange
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.uuidTextFieldChange
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberOwnershipEditPanelState(
    palEditPanelState: PalEditPanelState
): OwnershipEditPanelState {

    val state = remember(palEditPanelState) {
        OwnershipEditPanelState(palEditPanelState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class OwnershipEditPanelState(
    private val palEditPanelState: PalEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope)

    var opened by mutableStateOf(false)
        private set

    var expanded by mutableStateOf(false)
        private set

    var mutOwnership by mutableStateOf<MutOwnership?>(null)
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
                mutOwnership = MutOwnership(cache.ownership)
            }
            palEditPanelState.observePalIndividualData().collect { update ->
                if (update == null) {
                    mutOwnership = null
                    return@collect
                }
                if (update === cache) {
                    // the flow emit the cache
                    return@collect
                }
                mutOwnership?.update(update.ownership)
                    ?: MutOwnership(update.ownership)
                        .also { mutOwnership = it }
            }
        }
    }

    class MutOwnership(
        val ownership: PalEditPanelState.Ownership
    ) {
        var mutOwnedTime by mutableStateOf(
            (ownership.ownedTime?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutOwnerPlayerUid by mutableStateOf(
            (ownership.ownerPlayerUid?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutOldOwnerUIds by mutableStateOf(
            MutOldOwnerUIDs(ownership.oldOwnerUIds),
            neverEqualPolicy()
        )
            private set

        fun ownedTimeChange(change: TextFieldValue) {
            ::mutOwnedTime.longTextFieldChange(change)
        }

        fun ownedTimeRevert() {
            mutOwnedTime = (ownership.ownedTime?.toString() ?: "").let(::TextFieldValue)
        }

        fun ownerPlayerUidChange(change: TextFieldValue) {
            ::mutOwnerPlayerUid.uuidTextFieldChange(change)
        }

        fun ownerPlayerUidRevert() {
            mutOwnerPlayerUid = (ownership.ownerPlayerUid?.toString() ?: "").let(::TextFieldValue)
        }

        fun update(ownership: PalEditPanelState.Ownership) {}

        class MutOldOwnerUIDs(
            val oldOwnerUIDs: List<String>,
        ) {

            var mutEntries by mutableStateOf(
                emptyList<String>(),
                neverEqualPolicy(),
            )
                private set

            var opened by mutableStateOf(
                false
            )
                private set

            var expanded by mutableStateOf(
                false
            )
                private set

            fun userToggleExpand() {
                if (!opened) {
                    opened = true
                    expanded = true
                    mutEntries = oldOwnerUIDs
                    return
                }
                expanded = !expanded
            }
        }
    }
}