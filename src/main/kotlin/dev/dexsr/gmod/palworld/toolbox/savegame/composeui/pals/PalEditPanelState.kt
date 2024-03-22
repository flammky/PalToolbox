package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberPalEditPanelState(
    palsEditPanelState: PalsEditPanelState,
    pal: String
): PalEditPanelState {

    val state = remember(palsEditPanelState, pal) {
        PalEditPanelState(palsEditPanelState, pal)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class PalEditPanelState(
    private val palsEditPanelState: PalsEditPanelState,
    private val pal: String
) {

    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope get() = requireNotNull(_coroutineScope)

    var mutAttribute by mutableStateOf<MutAttribute?>(null)
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + UIFoundation.MainUIDispatcher)
        init()
    }

    fun stateExit() {
        coroutineScope.cancel()
    }

    fun exit() { palsEditPanelState.editPal(null) }

    fun init() {
        coroutineScope.launch {

            coroutineScope {
                launch {
                    palsEditPanelState.observePalIndividualData(pal)
                        .collect { data ->
                            mutAttribute = MutAttribute(
                                data.attribute
                            )
                        }
                }
            }
        }
    }

    class PalIndividualData(
        val attribute: Attribute,
        val ownership: Ownership,
        val skills: Skills,
        val inventory: Inventory,

        // val world: WorldData
        val displayData: AttributeDisplayData
    )

    class Attribute(
        val nickName: String?,
        val uid: String,
        val characterId: String,
        val gender: PalGender?,
        val level: Int?,
        val exp: Int?,
        val hp: Long?,
        val maxHp: Long?,
        val fullStomach: Float?,
        val maxFullStomach: Float?,
        val mp: Long,
        val sanityValue: Float?,

        val talentHp: Int?,
        val talentMelee: Int?,
        val talentShot: Int?,
        val talentDefense: Int?,

        val craftSpeed: Int,
        val craftSpeeds: List<CraftSpeed>,

        val maxSp: Long?,

        val isRarePal: Boolean?,
        val rank: Int?
    )

    class AttributeDisplayData(
        val displayName: String,
        val breed: String,
        val isAlpha: Boolean,
        val isLucky: Boolean
    )

    class MutAttribute(
        val attribute: Attribute
    ) {

        var nickName by mutableStateOf<TextFieldValue?>(attribute.nickName?.let(::TextFieldValue))
            private set

        var uid by mutableStateOf(TextFieldValue(attribute.uid))
            private set

        var breed by mutableStateOf(TextFieldValue(attribute.characterId))
            private set
    }

    class Skills(
        val equipWaza: List<String>,
        val masteredWaza: List<String>,
        val passiveSkills: List<String>,
    )

    class Ownership(
        val ownedTime: Long,
        val ownerPlayerUid: String,
        val oldOwnerUIds: List<String>
    )

    class CraftSpeed(
        val name: String,
        val rank: Int
    )



    class SlotId(
        val containerId: String,
        val slotIndex: Int
    )

    class StatusPoint(
        val name: String,
        val value: Int
    )

    class Inventory(
        val equipItemContainerId: EquipItemContainerId
    ) {

        class EquipItemContainerId(
            val value: String
        )
    }


    inner class Mock() {

        fun mockInit() {
            mockAttribute()
        }

        fun mockAttribute() {

        }
    }
}