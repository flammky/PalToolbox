package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import androidx.compose.ui.util.fastForEachIndexed

@Composable
fun rememberInventoryEditPanelState(
    pState: SaveGamePlayerEditorState
): InventoryEditPanelState {

    val state = remember(pState) {
        InventoryEditPanelState()
    }

    DisposableEffect(state) {

        onDispose {  }
    }

    return state
}

@Stable
class InventoryEditPanelState(

) {

    var currentSlotIndex by mutableStateOf<Int>(0)

    var expanded by mutableStateOf(false)

    val commonSlot = CommonSlot

    val dropSlot = DropSlot

    val essentialSlot = EssentialSlot

    val foodEquipSlot = FoodEquipSlot

    val equipArmorSlot = EquipArmorSlot

    val weaponLoadOutSlot = WeaponLoadOutSlot

    private val _slots = listOf(
        commonSlot,
        dropSlot,
        essentialSlot,
        foodEquipSlot,
        equipArmorSlot,
        weaponLoadOutSlot
    )

    private val _slotsByZIndex by mutableStateOf<List<Slot>>(
        emptyList(),
        neverEqualPolicy()
    )

    fun slots() = _slots

    fun selectSlot(slot: Slot) {
        when(slot) {
            is CommonSlot -> {
                if (slot == commonSlot) currentSlotIndex = 0
            }
            is DropSlot -> {
                if (slot == dropSlot) currentSlotIndex = 1
            }
            is EssentialSlot -> {
                if (slot == essentialSlot) currentSlotIndex = 2
            }
            is EquipArmorSlot -> {
                if (slot == equipArmorSlot) currentSlotIndex = 3
            }
            is FoodEquipSlot -> {
                if (slot == foodEquipSlot) currentSlotIndex = 4
            }
            is WeaponLoadOutSlot -> {
                if (slot == weaponLoadOutSlot) currentSlotIndex = 5
            }
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
}