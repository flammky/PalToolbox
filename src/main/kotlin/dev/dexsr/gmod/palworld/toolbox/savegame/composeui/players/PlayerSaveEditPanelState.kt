package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    var inventoryInfo by mutableStateOf<InventoryInfo?>(null)
        private set

    var mutInventoryInfo by mutableStateOf<MutInventoryInfo?>(null)
        private set

    var otomoContainerId by mutableStateOf<String?>(null)
        private set

    var mutOtomoContainerId by mutableStateOf<MutOtomoContainerId?>(null)
        private set

    var mutTechnologyPoint by mutableStateOf<MutTechnologyPoint?>(null)
        private set

    var mutUnlockedTechnologyRecipe by mutableStateOf<MutUnlockedTechnologyRecipe?>(null)
        private set

    var mutPalStorageContainerId by mutableStateOf<MutPalStorageContainerId?>(null)
        private set

    var mutRecordData by mutableStateOf<MutRecordData?>(null)
        private set

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

    class MutTechnologyPoint(
        private val initial: Int
    ) {
        var technologyPoint by mutableStateOf(TextFieldValue(initial.toString()))
            private set

        fun technologyPointChange(value: TextFieldValue) = intTextFieldChange(
            textFieldValue = value,
            update = ::technologyPoint::set
        )

        fun technologyPointRevert() {
            technologyPoint = TextFieldValue(initial.toString())
        }
    }

    class MutOtomoContainerId(
        private val initial: String
    ) {
        var otomoContainerId by mutableStateOf(TextFieldValue(initial))
            private set

        fun otomoContainerIdChange(value: TextFieldValue) = uuidTextFieldChange(
            textFieldValue = value,
            getVarTextField = ::otomoContainerId::get,
            update = ::otomoContainerId::set
        )

        fun otomoContainerIdRevert() {
            otomoContainerId = TextFieldValue(initial)
        }
    }

    class InventoryInfo(
        val commonContainerId: String,
        val dropSlotContainerId: String,
        val essentialContainerId: String,
        val weaponLoadOutContainerId: String,
        val playerEquipArmorContainerId: String,
        val foodEquipContainerId: String
    )

    class MutInventoryInfo(
        private val inventoryInfo: InventoryInfo
    ) {
        var commonContainerId by mutableStateOf(TextFieldValue(inventoryInfo.commonContainerId))
            private set
        var dropSlotContainerId by mutableStateOf(TextFieldValue(inventoryInfo.dropSlotContainerId))
            private set
        var essentialContainerId by mutableStateOf(TextFieldValue(inventoryInfo.essentialContainerId))
            private set
        var weaponLoadOutContainerId by mutableStateOf(TextFieldValue(inventoryInfo.weaponLoadOutContainerId))
            private set
        var playerEquipArmorContainerId by mutableStateOf(TextFieldValue(inventoryInfo.playerEquipArmorContainerId))
            private set
        var foodEquipContainerId by mutableStateOf(TextFieldValue(inventoryInfo.foodEquipContainerId))
            private set

        fun commonContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::commonContainerId::get,
            ::commonContainerId::set
        )

        fun commonContainerIdRevert() {
            commonContainerId = TextFieldValue(inventoryInfo.commonContainerId)
        }

        fun dropSlotContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::dropSlotContainerId::get,
            ::dropSlotContainerId::set
        )

        fun dropSlotContainerIdRevert() {
            dropSlotContainerId = TextFieldValue(inventoryInfo.dropSlotContainerId)
        }

        fun essentialContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::essentialContainerId::get,
            ::essentialContainerId::set
        )

        fun essentialContainerIdRevert() {
            essentialContainerId = TextFieldValue(inventoryInfo.essentialContainerId)
        }

        fun weaponLoadOutContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::weaponLoadOutContainerId::get,
            ::weaponLoadOutContainerId::set
        )

        fun weaponLoadOutContainerIdRevert() {
            weaponLoadOutContainerId = TextFieldValue(inventoryInfo.weaponLoadOutContainerId)
        }

        fun playerEquipArmorContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::playerEquipArmorContainerId::get,
            ::playerEquipArmorContainerId::set
        )

        fun playerEquipArmorContainerIdRevert() {
            playerEquipArmorContainerId = TextFieldValue(inventoryInfo.playerEquipArmorContainerId)
        }

        fun foodEquipContainerIdChange(textFieldValue: TextFieldValue) = uuidTextFieldChange(
            textFieldValue,
            ::foodEquipContainerId::get,
            ::foodEquipContainerId::set
        )

        fun foodEquipContainerIdRevert() {
            foodEquipContainerId = TextFieldValue(inventoryInfo.foodEquipContainerId)
        }


    }

    class UnlockedTechnologyRecipe(
        val entries: List<Entry>
    ) {

        class Entry(
            val index: Int,
            val value: String
        )
    }

    class MutUnlockedTechnologyRecipe(
        private val unlockedTechnologyRecipe: UnlockedTechnologyRecipe
    ) {

        // TODO: look into mutableStateList
        var mutEntries by mutableStateOf(
            unlockedTechnologyRecipe.entries,
            neverEqualPolicy()
        )
            private set

        fun add(value: String) {
            val current = mutEntries
            val new = buildList {
                addAll(current)
                add(UnlockedTechnologyRecipe.Entry(current.lastIndex + 1, value))
            }
            mutEntries = new
        }
    }

    class MutPalStorageContainerId(
        private val initial: String
    ) {
        var palStorageContainerId by mutableStateOf(TextFieldValue(initial))
            private set

        fun palStorageContainerIdChange(value: TextFieldValue) = uuidTextFieldChange(
            textFieldValue = value,
            getVarTextField = ::palStorageContainerId::get,
            update = ::palStorageContainerId::set
        )

        fun palStorageContainerIdRevert() {
            palStorageContainerId = TextFieldValue(initial)
        }
    }

    class RecordData(
        val tribeCaptureCount: TribeCaptureCount,
        val palCaptureCount: PalCaptureCount,
        val paldeckUnlockFlag: PaldeckUnlockFlag,
        val noteObtainForInstanceFlag: NoteObtainForInstanceFlag,
        val fastTravelPointUnlockFlag: FastTravelPointUnlockFlag
    ) {

        class TribeCaptureCount(
            val value: Int
        )

        class PalCaptureCount(
            val value: List<Pair<String, Int>>
        )

        class PaldeckUnlockFlag(
            val entries: List<Pair<String, Boolean>>
        )

        class NoteObtainForInstanceFlag(
            val entries: List<Pair<String, Boolean>>
        )

        class FastTravelPointUnlockFlag(
            val entries: List<Pair<String, Boolean>>
        )
    }

    class MutRecordData(
        val recordData: RecordData
    ) {

        val mutTribeCaptureCount = MutTribeCaptureCount(recordData.tribeCaptureCount)
        val mutPalCaptureCount = MutPalCaptureCount(recordData.palCaptureCount)
        val mutPaldeckUnlockFlag = MutPaldeckUnlockFlag(recordData.paldeckUnlockFlag)
        val mutNoteObtainForInstanceFlag = MutNoteObtainForInstanceFlag(recordData.noteObtainForInstanceFlag)
        val mutFastTravelPointUnlockFlag = MutFastTravelPointUnlockFlag(recordData.fastTravelPointUnlockFlag)

        class MutTribeCaptureCount(
            val tribeCaptureCount: RecordData.TribeCaptureCount
        ) {
            var mutValue by mutableStateOf(TextFieldValue(tribeCaptureCount.value.toString()))
                private set

            fun mutValueChange(value: TextFieldValue) {
                intTextFieldChange(value, ::mutValue::set)
            }

            fun mutValueReset() {
                mutValue = TextFieldValue(tribeCaptureCount.value.toString())
            }
        }

        class MutPalCaptureCount(
            val palCaptureCount: RecordData.PalCaptureCount
        ) {
            var mutEntries by mutableStateOf(
                palCaptureCount.value,
                neverEqualPolicy()
            )
                private set

            fun add(key: String, value: Int) {
                val current = mutEntries
                val new = buildList {
                    addAll(current)
                    add(Pair(key, value))
                }
                mutEntries = new
            }
        }

        class MutPaldeckUnlockFlag(
            val paldeckUnlockFlag: RecordData.PaldeckUnlockFlag
        ) {
            var mutEntries by mutableStateOf(
                paldeckUnlockFlag.entries,
                neverEqualPolicy()
            )
                private set

            fun add(key: String, value: Boolean) {
                val current = mutEntries
                val new = buildList {
                    addAll(current)
                    add(Pair(key, value))
                }
                mutEntries = new
            }
        }

        class MutNoteObtainForInstanceFlag(
            val noteObtainForInstanceFlag: RecordData.NoteObtainForInstanceFlag
        ) {
            var mutEntries by mutableStateOf(
                noteObtainForInstanceFlag.entries,
                neverEqualPolicy()
            )
                private set

            fun add(key: String, value: Boolean) {
                val current = mutEntries
                val new = buildList {
                    addAll(current)
                    add(Pair(key, value))
                }
                mutEntries = new
            }
        }

        class MutFastTravelPointUnlockFlag(
            val fastTravelPointUnlockFlag: RecordData.FastTravelPointUnlockFlag
        ) {
            var mutEntries by mutableStateOf(
                fastTravelPointUnlockFlag.entries,
                neverEqualPolicy()
            )
                private set

            fun add(key: String, value: Boolean) {
                val current = mutEntries
                val new = buildList {
                    addAll(current)
                    add(Pair(key, value))
                }
                mutEntries = new
            }
        }
    }

    inner class Mock {

        fun mockInit() {
            stateEnter()
            mockInventoryInfo()
            mockOtomoContainerId()
            mockTechnologyPoint()
            mockUnlockedTechnologyRecipe()
            mockPalStorageContainerId()
            mockRecordData()
            noContent = false
        }

        fun mockInventoryInfo() {
            inventoryInfo = InventoryInfo(
                commonContainerId = "00000000000000000000000000000000",
                dropSlotContainerId = "00000000000000000000000000000000",
                essentialContainerId = "00000000000000000000000000000000",
                weaponLoadOutContainerId = "00000000000000000000000000000000",
                playerEquipArmorContainerId = "00000000000000000000000000000000",
                foodEquipContainerId = "00000000000000000000000000000000"
            )
            mutInventoryInfo = MutInventoryInfo(inventoryInfo!!)
        }

        fun mockOtomoContainerId() {
            otomoContainerId = "00000000000000000000000000000000"
            mutOtomoContainerId = MutOtomoContainerId(otomoContainerId!!)
        }

        fun mockTechnologyPoint() {
            mutTechnologyPoint = MutTechnologyPoint(Int.MAX_VALUE)
        }

        fun mockUnlockedTechnologyRecipe() {
            mutUnlockedTechnologyRecipe = MutUnlockedTechnologyRecipe(
                unlockedTechnologyRecipe = UnlockedTechnologyRecipe(
                    entries = listOf(
                        "Workbench",
                        "Product_Axe_Grade_01",
                        "Product_Pickaxe_Grade_01",
                        "HandTorch",
                        "Battle_MeleeWeapon_Bat",
                        "PalBox",
                        "Special_PalSphere_Grade_01",
                        "Product_Cooking_Grade_01",
                        "Infra_ItemChest_Grade_01",
                        "RepairBench",
                        "Wooden_houseset",
                        "Battle_RangeWeapon_Bow1",
                        "Arrow",
                        "Infra_PlayerBed_Grade_01",
                        "Infra_PalBed_Grade_01",
                        "RepairKit",
                        "Battle_Cloth",
                        "Shield_01",
                        "Spear",
                        "Battle_Armor_Grade_01_Cloth",
                        "PalFoodBox",
                        "BaseCampBattleDirector",
                        "Trap_Noose",
                        "Product_Farm_Berries",
                        "MonsterFarm",
                        "Battle_Glider_Grade_01",
                        "Battle_RangeWeapon_Bow_Fire",
                        "Arrow_Fire",
                        "Product_WorkBench_SkillUnlock",
                        "BuildableGoddessStatue",
                        "Torch",
                        "SkillUnlock_Kitsunebi",
                        "Product_StationDeforest",
                        "Product_StonePit",
                        "Special_HatchingPalEgg",
                        "Crusher",
                        "SkillUnlock_DreamDemon",
                        "Battle_Armor_Grade_01_Cloth_Heat",
                        "Battle_Armor_Grade_01_Cloth_Cold",
                        "Spa",
                        "Battle_RangeWeapon_Bow3",
                        "Product_Ingot_Grade_01_Copper",
                        "Infra_MachineParts",
                        "AutoMealPouch_Tier1",
                        "Product_Axe_Grade_02",
                        "Product_Pickaxe_Grade_02",
                        "Product_Factory_Hard_Grade_01",
                        "SkillUnlock_Carbunclo",
                        "Battle_Armor_Grade_02_Fur",
                        "MeatCutterKnife",
                        "Product_Medicine_Grade_01",
                        "GrapplingGun",
                        "Battle_RangeWeapon_BowGun",
                        "Spear_2",
                        "DamagedScarecrow",
                        "CoolerBox",
                        "SkillUnlock_WeaselDragon",
                        "Special_PalSphere_Grade_02",
                        "Special_SphereFactory_Black_Grade_01",
                        "Special_PalRankUp",
                        "Battle_RangeWeapon_BowGun_Fire",
                        "Product_Farm_wheat",
                        "FlourMill",
                        "BaseCampWorkHard",
                        "DisplayCharacter",
                        "Shield_02",
                        "Battle_Armor_Grade_02_Fur_Heat",
                        "Infra_ItemChest_Grade_02",
                        "Product_Cooking_Grade_02",
                        "Heater",
                        "SkillUnlock_FlowerRabbit",
                        "Battle_Armor_Grade_02_Fur_Cold",
                        "BreedFarm",
                        "ToolBoxV1",
                        "Special_PalSphere_Grade_03",
                        "Product_WeaponFactory_Dirty_Grade_01",
                        "Musket",
                        "Battle_GunPowder_Grade_02",
                        "RoughBullet",
                        "SkillUnlock_BirdDragon",
                        "SkillUnlock_FairyDragon",
                        "Lantern",
                        "Battle_Armor_Grade_03_Copper",
                        "Infra_PalBed_Grade_02",
                        "MakeshiftHandgun",
                        "Battle_Defense_BowGun",
                        "Special_PalSphere_Grade_04",
                        "Shield_03",
                        "Battle_RangeWeapon_HandGun",
                        "HandgunBullet",
                        "FurnitureSet_1",
                        "FurnitureSet_3"
                    ).mapIndexed(UnlockedTechnologyRecipe::Entry)
                )
            )
        }

        fun mockPalStorageContainerId() {
            mutPalStorageContainerId = MutPalStorageContainerId(
                initial = "00000000000000000000000000000000"
            )
        }

        fun mockRecordData() {
            mutRecordData = MutRecordData(
                RecordData(
                    tribeCaptureCount = RecordData.TribeCaptureCount(Int.MAX_VALUE),
                    palCaptureCount = RecordData.PalCaptureCount(
                        run {
                            val str = """
                            [
								{
									"key": "PinkCat",
									"value": 7
								},
                                {
									"key": "PinkCat1",
									"value": ${Int.MAX_VALUE}
								},
								{
									"key": "Kitsunebi",
									"value": 10
								},
								{
									"key": "CuteFox",
									"value": 6
								},
								{
									"key": "Carbunclo",
									"value": 3
								},
								{
									"key": "ColorfulBird",
									"value": 1
								},
								{
									"key": "Boar",
									"value": 3
								},
								{
									"key": "SheepBall",
									"value": 9
								},
								{
									"key": "Penguin",
									"value": 7
								},
								{
									"key": "PlantSlime",
									"value": 5
								},
								{
									"key": "CuteMole",
									"value": 3
								},
								{
									"key": "ChickenPal",
									"value": 4
								},
								{
									"key": "WoolFox",
									"value": 3
								},
								{
									"key": "DreamDemon",
									"value": 3
								},
								{
									"key": "ElecCat",
									"value": 1
								},
								{
									"key": "LavaGirl",
									"value": 1
								},
								{
									"key": "BerryGoat",
									"value": 2
								},
								{
									"key": "Ganesha",
									"value": 1
								},
								{
									"key": "Monkey",
									"value": 2
								},
								{
									"key": "WeaselDragon",
									"value": 2
								},
								{
									"key": "CatBat",
									"value": 1
								},
								{
									"key": "Garm",
									"value": 8
								},
								{
									"key": "Alpaca",
									"value": 6
								},
								{
									"key": "LazyCatfish",
									"value": 3
								},
								{
									"key": "Werewolf",
									"value": 1
								},
								{
									"key": "LittleBriarRose",
									"value": 5
								},
								{
									"key": "Deer",
									"value": 2
								},
								{
									"key": "FlyingManta",
									"value": 2
								},
								{
									"key": "NaughtyCat",
									"value": 3
								},
								{
									"key": "FlameBuffalo",
									"value": 3
								},
								{
									"key": "LizardMan",
									"value": 2
								},
								{
									"key": "Baphomet",
									"value": 1
								},
								{
									"key": "WizardOwl",
									"value": 1
								},
								{
									"key": "SweetsSheep",
									"value": 1
								},
								{
									"key": "FlowerRabbit",
									"value": 2
								},
								{
									"key": "CaptainPenguin",
									"value": 2
								},
								{
									"key": "CowPal",
									"value": 4
								},
								{
									"key": "Bastet",
									"value": 5
								},
								{
									"key": "NegativeOctopus",
									"value": 1
								},
								{
									"key": "LazyDragon",
									"value": 2
								},
								{
									"key": "BirdDragon",
									"value": 1
								},
								{
									"key": "Ronin",
									"value": 1
								},
								{
									"key": "Kirin",
									"value": 4
								},
								{
									"key": "MopKing",
									"value": 2
								},
								{
									"key": "MopBaby",
									"value": 4
								},
								{
									"key": "KingAlpaca",
									"value": 1
								},
								{
									"key": "NightFox",
									"value": 4
								},
								{
									"key": "CatMage",
									"value": 1
								},
								{
									"key": "Bastet_Ice",
									"value": 1
								},
								{
									"key": "Mutant",
									"value": 2
								},
								{
									"key": "HerculesBeetle",
									"value": 1
								},
								{
									"key": "RaijinDaughter",
									"value": 1
								},
								{
									"key": "Hedgehog_Ice",
									"value": 1
								},
								{
									"key": "SakuraSaurus",
									"value": 4
								},
								{
									"key": "FlowerDoll",
									"value": 1
								},
								{
									"key": "FlowerDinosaur",
									"value": 1
								},
								{
									"key": "RedArmorBird",
									"value": 1
								},
								{
									"key": "Eagle",
									"value": 1
								},
								{
									"key": "ThunderBird",
									"value": 1
								},
								{
									"key": "PinkLizard",
									"value": 2
								},
								{
									"key": "DrillGame",
									"value": 5
								},
								{
									"key": "FairyDragon",
									"value": 1
								},
								{
									"key": "ThunderDog",
									"value": 1
								},
								{
									"key": "BluePlatypus",
									"value": 4
								},
								{
									"key": "Serpent",
									"value": 1
								},
								{
									"key": "Anubis",
									"value": 1
								},
								{
									"key": "GrassMammoth",
									"value": 2
								},
								{
									"key": "DarkCrow",
									"value": 1
								}
							]
                        """
                            Json
                                .parseToJsonElement(str)
                                .jsonArray
                                .map { e -> val o = e.jsonObject ; o["key"]!!.jsonPrimitive.content to o["value"]!!.jsonPrimitive.content.toInt() }
                        }
                    ),
                    paldeckUnlockFlag = RecordData.PaldeckUnlockFlag(
                        run {
                            val str = """
                                [
								{
									"key": "PinkCat",
									"value": true
								},
								{
									"key": "WizardOwl",
									"value": true
								},
								{
									"key": "ChickenPal",
									"value": true
								},
								{
									"key": "CuteFox",
									"value": true
								},
								{
									"key": "WoolFox",
									"value": true
								},
								{
									"key": "ElecCat",
									"value": true
								},
								{
									"key": "Boar",
									"value": true
								},
								{
									"key": "BerryGoat",
									"value": true
								}
							]
                            """
                            Json
                                .parseToJsonElement(str)
                                .jsonArray
                                .map { e -> val o = e.jsonObject ; o["key"]!!.jsonPrimitive.content to o["value"]!!.jsonPrimitive.content.toBoolean() }

                        }
                    ),
                    noteObtainForInstanceFlag = RecordData.NoteObtainForInstanceFlag(
                        run {
                            val str =
                                """
                                [
								    {
									    "key": "Day2",
									    "value": true
								    }
							    ]
                                """
                            Json
                                .parseToJsonElement(str)
                                .jsonArray
                                .map { e -> val o = e.jsonObject ; o["key"]!!.jsonPrimitive.content to o["value"]!!.jsonPrimitive.content.toBoolean() }

                        }
                    ),
                    fastTravelPointUnlockFlag = RecordData.FastTravelPointUnlockFlag(
                        run {
                            val str =
                                """
                                    [
								        {
									        "key": "6E03F8464BAD9E458B843AA30BE1CC8F",
									        "value": true
								        }
							        ]
                                """
                            Json
                                .parseToJsonElement(str)
                                .jsonArray
                                .map { e -> val o = e.jsonObject ; o["key"]!!.jsonPrimitive.content to o["value"]!!.jsonPrimitive.content.toBoolean() }
                        }
                    )
                )
            )
        }
    }
}

private fun uuidTextFieldChange(
    textFieldValue: TextFieldValue,
    getVarTextField: () -> TextFieldValue,
    update: (TextFieldValue) -> Unit
) {
    if (textFieldValue.text.length > 36) return
    var n = 0
    var take = 0
    val filter = StringBuilder()
        .apply {
            textFieldValue.text.forEach { c ->
                if (!c.isLetterOrDigit()) {
                    if (n != 9-1 && n != 14-1 && n != 19-1 && n != 24-1) return
                    if (c != '-') return
                    n++ ; return@forEach
                }
                n++
                append(c)
                if (++take == 32) return@apply
            }
        }
        .toString()
    if (textFieldValue.text.length != 36 && textFieldValue.text.length > 32 && getVarTextField().text.length > 31) {
        return
    }
    update(textFieldValue)
}

private fun intTextFieldChange(
    textFieldValue: TextFieldValue,
    update: (TextFieldValue) -> Unit
) {
    val max = Int.MAX_VALUE
    if (textFieldValue.text.length > max.toString().length) return
    if (textFieldValue.text.isNotEmpty()) {
        if (!textFieldValue.text.all(Char::isDigit)) return
        val num = textFieldValue.text.toIntOrNull() ?: return
        update(
            TextFieldValue(
                textFieldValue.text,
                textFieldValue.selection
            )
        )
    } else {
        update(
            TextFieldValue()
        )
    }
}