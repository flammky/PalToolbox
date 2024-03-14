package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayerInventoryEdit
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayerInventoryEditListener
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberInventoryEditPanelState(
    pState: SaveGamePlayerEditorState
): InventoryEditPanelState {

    val state = remember(pState) {
        InventoryEditPanelState(pState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

@Stable
class InventoryEditPanelState(
    private val playerEditorState: SaveGamePlayerEditorState,
) {

    var editor: SaveGamePlayerInventoryEdit? = null
        private set

    var currentSlotIndex by mutableStateOf<Int>(0)

    var expanded by mutableStateOf(false)
        private set

    var sourceNotFoundErr by mutableStateOf<SaveGamePlayerInventoryEdit.Error?>(null)
        private set

    var commonErr by mutableStateOf<SaveGamePlayerInventoryEdit.Error?>(null)
        private set

    var noContent by mutableStateOf(false)
        private set

    var showEditor by mutableStateOf(false)
        private set

    private var _coroutineScope: CoroutineScope? = null

    private var wasExpanded = false

    private val _slotUidMap = mutableStateOf(
        linkedMapOf<Slot, String>(),
        neverEqualPolicy()
    )

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "State wasn't initialized"
        }

    private val _slots = listOf(
        CommonSlot,
        DropSlot,
        EssentialSlot,
        FoodEquipSlot,
        EquipArmorSlot,
        WeaponLoadOutSlot
    )

    private var _slotsByZIndex by mutableStateOf<List<Slot>>(
        listOf(CommonSlot, DropSlot, EssentialSlot, EquipArmorSlot, FoodEquipSlot, WeaponLoadOutSlot).asReversed(),
        neverEqualPolicy()
    )

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + MainUIDispatcher)
    }

    fun stateExit() {
        coroutineScope.cancel()
    }

    fun slots() = _slots

    fun selectSlot(slot: Slot) {
        val index = when(slot) {
            is CommonSlot -> 0
            is DropSlot -> 1
            is EssentialSlot -> 2
            is EquipArmorSlot -> 3
            is FoodEquipSlot -> 4
            is WeaponLoadOutSlot -> 5
        }
        val change = index != currentSlotIndex
        if (!change) return
        currentSlotIndex = index
        slotAccessOrder(slot)
    }

    private fun slotAccessOrder(slot: Slot) {
        _slotsByZIndex = buildList {
            _slotsByZIndex.fastForEach { e -> if (e != slot) add(e) }
            add(slot)
        }
    }


    fun slotDisplayName(slot: Slot) = when(slot) {
        is CommonSlot -> "Common"
        is DropSlot -> "Drop"
        is EssentialSlot -> "Essential"
        is FoodEquipSlot -> "FoodEquip"
        is EquipArmorSlot -> "EquipArmor"
        is WeaponLoadOutSlot -> "WeaponLoadout"
    }

    fun slotAvailable(slot: Slot) = when(slot) {
        is CommonSlot -> true
        is DropSlot -> true
        is EquipArmorSlot -> true
        is EssentialSlot -> true
        is FoodEquipSlot -> true
        is WeaponLoadOutSlot -> true
    }

    fun userToggleExpand() {
        expanded = !expanded
        if (expanded) {
            if (!wasExpanded) {
                wasExpanded = true
                lazyInit()
            }
        }
    }

    fun isSlotSelected(slot: Slot) = when(slot) {
        is CommonSlot -> currentSlotIndex == 0
        is DropSlot -> currentSlotIndex == 1
        is EquipArmorSlot -> currentSlotIndex == 3
        is EssentialSlot -> currentSlotIndex == 2
        is FoodEquipSlot -> currentSlotIndex == 4
        is WeaponLoadOutSlot -> currentSlotIndex == 5
    }

    fun slotZIndex(slot: Slot) = _slotsByZIndex.indexOf(slot).toFloat()

    fun slotUid(slot: Slot) = _slotUidMap.value[slot]

    fun refresh() {

    }

    private fun lazyInit() {
        coroutineScope.launch {
            val editor = playerEditorState.editState.saveGameEditor ?: return@launch
            val inventoryEditor = editor.getOrOpenPlayerInventory(playerEditorState.player.attribute.uid)
            inventoryEditor.addListener(
                SaveGamePlayerInventoryEditListener(
                    onProgress = { event ->
                        when(event) {
                            SaveGamePlayerInventoryEdit.Progress.ResolvingFile -> {
                                // resolving file
                            }
                            else -> {

                            }
                        }
                    },
                    onError = { event ->
                        when (event) {
                            is SaveGamePlayerInventoryEdit.Error.PlayerFileFNF,
                            is SaveGamePlayerInventoryEdit.Error.PlayersFolderFNF -> {
                                commonErr = null
                                sourceNotFoundErr = event
                            }
                            else -> {
                                sourceNotFoundErr = null
                                commonErr = event
                            }
                        }
                    },
                )
            )

            inventoryEditor.prepare()
            inventoryEditor.parseInventoryUIDMap()
            _slotUidMap.value = linkedMapOf<Slot, String>()
                .apply {
                    inventoryEditor.inventoriesUidMap?.entries?.forEach { (slot, uid) ->
                        slotFromStr(slot)?.let { put(it, uid) }
                    }
                }
            this@InventoryEditPanelState.editor = inventoryEditor
            showEditor = true
        }
    }

    // todo: define on domain instead

    @Stable
    sealed class Slot()

    @Stable
    data object CommonSlot : Slot()

    @Stable
    data object DropSlot : Slot()

    @Stable
    data object EssentialSlot : Slot()

    @Stable
    data object FoodEquipSlot : Slot()

    @Stable
    data object EquipArmorSlot : Slot()

    @Stable
    data object WeaponLoadOutSlot : Slot()

    private fun slotFromStr(str: String) = when(str) {
        "CommonContainerId" -> CommonSlot
        "DropSlotContainerId" -> DropSlot
        "EssentialContainerId" -> EssentialSlot
        "WeaponLoadOutContainerId" -> WeaponLoadOutSlot
        "PlayerEquipArmorContainerId" -> EquipArmorSlot
        "FoodEquipContainerId" -> FoodEquipSlot
        else -> null
    }
}