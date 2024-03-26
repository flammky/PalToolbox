package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.noMinConstraints
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun rememberSkillsEditPanelState(
    palEditPanelState: PalEditPanelState
): SkillsEditPanelState {

    val state = remember(palEditPanelState) {
        SkillsEditPanelState(palEditPanelState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class SkillsEditPanelState(
    private val palEditPanelState: PalEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope)

    var opened by mutableStateOf(false)
        private set

    var expanded by mutableStateOf(false)
        private set

    var mutSkills by mutableStateOf<MutSkills?>(null)
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
                mutSkills = MutSkills(cache.skills)
            }
            palEditPanelState.observePalIndividualData().collect { update ->
                if (update == null) {
                    mutSkills = null
                    return@collect
                }
                if (update === cache) {
                    // the flow emit the cache
                    return@collect
                }
                mutSkills?.update(update.skills)
                    ?: MutSkills(update.skills)
                        .also { mutSkills = it }
            }
        }
    }

    class MutSkills(
        val skills: PalEditPanelState.Skills
    ) {

        var mutEquip by mutableStateOf(
            MutEquip(skills.equipWaza)
        )
            private set

        var mutPassive by mutableStateOf(
            MutPassive(skills.passiveSkills)
        )
            private set

        var mutMastered by mutableStateOf(
            MutMastered(skills.masteredWaza)
        )
            private set

        fun update(skills: PalEditPanelState.Skills) {

        }

        class MutEquip(
            val equip: List<String>
        ) {

            var mutEntries by mutableStateOf(
                emptyList<String>(),
                neverEqualPolicy()
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
                    mutEntries = equip
                    return
                }
                expanded = !expanded
            }
        }

        class MutPassive(
            val passive: List<String>?
        ) {

            var mutEntries by mutableStateOf(
                emptyList<String>(),
                neverEqualPolicy()
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
                    mutEntries = passive ?: emptyList()
                    return
                }
                expanded = !expanded
            }
        }

        class MutMastered(
            val mastered: List<String>
        ) {

            var mutEntries by mutableStateOf(
                emptyList<String>(),
                neverEqualPolicy()
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
                    mutEntries = mastered
                    return
                }
                expanded = !expanded
            }
        }
    }
}
