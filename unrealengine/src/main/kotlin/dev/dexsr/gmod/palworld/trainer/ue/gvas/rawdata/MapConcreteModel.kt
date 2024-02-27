package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasMap
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasProperty
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MapConcreteModel

val MAP_OBJECT_NAME_TO_CONCRETE_MODEL_CLASS = mapOf(
    "droppedcharacter" to "PalMapObjectDeathDroppedCharacterModel",
    "blastfurnace" to "PalMapObjectConvertItemModel",
    "blastfurnace2" to "PalMapObjectConvertItemModel",
    "blastfurnace3" to "PalMapObjectConvertItemModel",
    "blastfurnace4" to "PalMapObjectConvertItemModel",
    "blastfurnace5" to "PalMapObjectConvertItemModel",
    "campfire" to "PalMapObjectConvertItemModel",
    "characterrankup" to "PalMapObjectRankUpCharacterModel",
    "commondropitem3d" to "PalMapObjectDropItemModel",
    "cookingstove" to "PalMapObjectConvertItemModel",
    "damagablerock_pv" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0001" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0002" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0003" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0004" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0005" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0017" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0006" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0007" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0008" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0009" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0010" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0011" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0012" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0013" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0014" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0015" to "PalMapObjectItemDropOnDamagModel",
    "damagablerock0016" to "PalMapObjectItemDropOnDamagModel",
    "deathpenaltychest" to "PalMapObjectDeathPenaltyStorageModel",
    "defensegatlinggun" to "PalMapObjectDefenseBulletLauncherModel",
    "defensemachinegun" to "PalMapObjectDefenseBulletLauncherModel",
    "defenseminigun" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "defensebowgun" to "PalMapObjectDefenseBulletLauncherModel",
    "defensemissile" to "PalMapObjectDefenseBulletLauncherModel",
    "defensewait" to "PalMapObjectDefenseWaitModel",
    "electricgenerator" to "PalMapObjectGenerateEnergyModel",
    "electricgenerator_slave" to "PalMapObjectGenerateEnergyModel",
    "electricgenerator2" to "PalMapObjectGenerateEnergyModel",
    "electricgenerator3" to "PalMapObjectGenerateEnergyModel",
    "electrickitchen" to "PalMapObjectConvertItemModel",
    "factory_comfortable_01" to "PalMapObjectConvertItemModel",
    "factory_comfortable_02" to "PalMapObjectConvertItemModel",
    "factory_hard_01" to "PalMapObjectConvertItemModel",
    "factory_hard_02" to "PalMapObjectConvertItemModel",
    "factory_hard_03" to "PalMapObjectConvertItemModel",
    "farmblockv2_grade01" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_grade02" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_grade03" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_wheet" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_tomato" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_lettuce" to "PalMapObjectFarmBlockV2Model",
    "farmblockv2_berries" to "PalMapObjectFarmBlockV2Model",
    "fasttravelpoint" to "PalMapObjectFastTravelPointModel",
    "hightechkitchen" to "PalMapObjectConvertItemModel",
    "itemchest" to "PalMapObjectItemChestModel",
    "itemchest_02" to "PalMapObjectItemChestModel",
    "itemchest_03" to "PalMapObjectItemChestModel",
    "dev_itemchest" to "PalMapObjectItemChestModel",
    "medicalpalbed" to "PalMapObjectMedicalPalBedModel",
    "medicalpalbed_02" to "PalMapObjectMedicalPalBedModel",
    "medicalpalbed_03" to "PalMapObjectMedicalPalBedModel",
    "medicalpalbed_04" to "PalMapObjectMedicalPalBedModel",
    "medicinefacility_01" to "PalMapObjectConvertItemModel",
    "medicinefacility_02" to "PalMapObjectConvertItemModel",
    "medicinefacility_03" to "PalMapObjectConvertItemModel",
    "palfoodbox" to "PalMapObjectPalFoodBoxModel",
    "palboxv2" to "PalMapObjectBaseCampPoint",
    "displaycharacter" to "PalMapObjectDisplayCharacterModel",
    "pickupitem_flint" to "PalMapObjectPickupItemOnLevelModel",
    "pickupitem_log" to "PalMapObjectPickupItemOnLevelModel",
    "pickupitem_redberry" to "PalMapObjectPickupItemOnLevelModel",
    "pickupitem_stone" to "PalMapObjectPickupItemOnLevelModel",
    "pickupitem_potato" to "PalMapObjectPickupItemOnLevelModel",
    "pickupitem_poppy" to "PalMapObjectPickupItemOnLevelModel",
    "playerbed" to "PalMapObjectPlayerBedModel",
    "playerbed_02" to "PalMapObjectPlayerBedModel",
    "playerbed_03" to "PalMapObjectPlayerBedModel",
    "shippingitembox" to "PalMapObjectShippingItemModel",
    "spherefactory_black_01" to "PalMapObjectConvertItemModel",
    "spherefactory_black_02" to "PalMapObjectConvertItemModel",
    "spherefactory_black_03" to "PalMapObjectConvertItemModel",
    "spherefactory_white_01" to "PalMapObjectConvertItemModel",
    "spherefactory_white_02" to "PalMapObjectConvertItemModel",
    "spherefactory_white_03" to "PalMapObjectConvertItemModel",
    "stonehouse1" to "PalBuildObject",
    "stonepit" to "PalMapObjectProductItemModel",
    "strawhouse1" to "PalBuildObject",
    "weaponfactory_clean_01" to "PalMapObjectConvertItemModel",
    "weaponfactory_clean_02" to "PalMapObjectConvertItemModel",
    "weaponfactory_clean_03" to "PalMapObjectConvertItemModel",
    "weaponfactory_dirty_01" to "PalMapObjectConvertItemModel",
    "weaponfactory_dirty_02" to "PalMapObjectConvertItemModel",
    "weaponfactory_dirty_03" to "PalMapObjectConvertItemModel",
    "well" to "PalMapObjectProductItemModel",
    "woodhouse1" to "PalBuildObject",
    "workbench" to "PalMapObjectConvertItemModel",
    "recoverotomo" to "PalMapObjectRecoverOtomoModel",
    "palegg" to "PalMapObjectPalEggModel",
    "palegg_fire" to "PalMapObjectPalEggModel",
    "palegg_water" to "PalMapObjectPalEggModel",
    "palegg_leaf" to "PalMapObjectPalEggModel",
    "palegg_electricity" to "PalMapObjectPalEggModel",
    "palegg_ice" to "PalMapObjectPalEggModel",
    "palegg_earth" to "PalMapObjectPalEggModel",
    "palegg_dark" to "PalMapObjectPalEggModel",
    "palegg_dragon" to "PalMapObjectPalEggModel",
    "hatchingpalegg" to "PalMapObjectHatchingEggModel",
    "treasurebox" to "PalMapObjectTreasureBoxModel",
    "treasurebox_visiblecontent" to "PalMapObjectPickupItemOnLevelModel",
    "treasurebox_visiblecontent_skillfruits" to "PalMapObjectPickupItemOnLevelModel",
    "stationdeforest2" to "PalMapObjectProductItemModel",
    "workbench_skillunlock" to "PalMapObjectConvertItemModel",
    "workbench_skillcard" to "PalMapObjectConvertItemModel",
    "wooden_foundation" to "PalBuildObject",
    "wooden_wall" to "PalBuildObject",
    "wooden_roof" to "PalBuildObject",
    "wooden_stair" to "PalBuildObject",
    "wooden_doorwall" to "PalMapObjectDoorModel",
    "stone_foundation" to "PalBuildObject",
    "stone_wall" to "PalBuildObject",
    "stone_roof" to "PalBuildObject",
    "stone_stair" to "PalBuildObject",
    "stone_doorwall" to "PalMapObjectDoorModel",
    "metal_foundation" to "PalBuildObject",
    "metal_wall" to "PalBuildObject",
    "metal_roof" to "PalBuildObject",
    "metal_stair" to "PalBuildObject",
    "metal_doorwall" to "PalMapObjectDoorModel",
    "buildablegoddessstatue" to "PalMapObjectCharacterStatusOperatorModel",
    "spa" to "PalMapObjectAmusementModel",
    "spa2" to "PalMapObjectAmusementModel",
    "pickupitem_mushroom" to "PalMapObjectPickupItemOnLevelModel",
    "defensewall_wood" to "PalBuildObject",
    "defensewall" to "PalBuildObject",
    "defensewall_metal" to "PalBuildObject",
    "heater" to "PalMapObjectHeatSourceModel",
    "electricheater" to "PalMapObjectHeatSourceModel",
    "cooler" to "PalMapObjectHeatSourceModel",
    "electriccooler" to "PalMapObjectHeatSourceModel",
    "torch" to "PalMapObjectTorchModel",
    "walltorch" to "PalMapObjectTorchModel",
    "lamp" to "PalMapObjectLampModel",
    "ceilinglamp" to "PalMapObjectLampModel",
    "largelamp" to "PalMapObjectLampModel",
    "largeceilinglamp" to "PalMapObjectLampModel",
    "crusher" to "PalMapObjectConvertItemModel",
    "woodcrusher" to "PalMapObjectConvertItemModel",
    "flourmill" to "PalMapObjectConvertItemModel",
    "trap_leghold" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_leghold_big" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_noose" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_movingpanel" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_mineelecshock" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_minefreeze" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "trap_mineattack" to "DEFAULT_UNKNOWN_PalMapObjectConcreteModelBase",
    "breedfarm" to "PalMapObjectBreedFarmModel",
    "wood_gate" to "PalMapObjectDoorModel",
    "stone_gate" to "PalMapObjectDoorModel",
    "metal_gate" to "PalMapObjectDoorModel",
    "repairbench" to "PalMapObjectRepairItemModel",
    "skillfruit_test" to "PalMapObjectPickupItemOnLevelModel",
    "toolboxv1" to "PalMapObjectBaseCampPassiveEffectModel",
    "toolboxv2" to "PalMapObjectBaseCampPassiveEffectModel",
    "fountain" to "PalMapObjectBaseCampPassiveEffectModel",
    "silo" to "PalMapObjectBaseCampPassiveEffectModel",
    "transmissiontower" to "PalMapObjectBaseCampPassiveEffectModel",
    "flowerbed" to "PalMapObjectBaseCampPassiveEffectModel",
    "stump" to "PalMapObjectBaseCampPassiveEffectModel",
    "miningtool" to "PalMapObjectBaseCampPassiveEffectModel",
    "cauldron" to "PalMapObjectBaseCampPassiveEffectModel",
    "snowman" to "PalMapObjectBaseCampPassiveEffectModel",
    "olympiccauldron" to "PalMapObjectBaseCampPassiveEffectModel",
    "basecampworkhard" to "PalMapObjectBaseCampPassiveWorkHardModel",
    "coolerbox" to "PalMapObjectItemChest_AffectCorruption",
    "refrigerator" to "PalMapObjectItemChest_AffectCorruption",
    "damagedscarecrow" to "PalMapObjectDamagedScarecrowModel",
    "signboard" to "PalMapObjectSignboardModel",
    "basecampbattledirector" to "PalMapObjectBaseCampWorkerDirectorModel",
    "monsterfarm" to "PalMapObjectMonsterFarmModel",
    "wood_windowwall" to "PalBuildObject",
    "stone_windowwall" to "PalBuildObject",
    "metal_windowwall" to "PalBuildObject",
    "wood_trianglewall" to "PalBuildObject",
    "stone_trianglewall" to "PalBuildObject",
    "metal_trianglewall" to "PalBuildObject",
    "wood_slantedroof" to "PalBuildObject",
    "stone_slantedroof" to "PalBuildObject",
    "metal_slantedroof" to "PalBuildObject",
    "table1" to "PalBuildObject",
    "barrel_wood" to "PalMapObjectItemChestModel",
    "box_wood" to "PalMapObjectItemChestModel",
    "box01_iron" to "PalMapObjectItemChestModel",
    "box02_iron" to "PalMapObjectItemChestModel",
    "shelf_wood" to "PalMapObjectItemChestModel",
    "shelf_cask_wood" to "PalMapObjectItemChestModel",
    "shelf_hang01_wood" to "PalMapObjectItemChestModel",
    "shelf01_iron" to "PalMapObjectItemChestModel",
    "shelf02_iron" to "PalMapObjectItemChestModel",
    "shelf03_iron" to "PalMapObjectItemChestModel",
    "shelf04_iron" to "PalMapObjectItemChestModel",
    "shelf05_stone" to "PalMapObjectItemChestModel",
    "shelf06_stone" to "PalMapObjectItemChestModel",
    "shelf07_stone" to "PalMapObjectItemChestModel",
    "shelf01_wall_stone" to "PalMapObjectItemChestModel",
    "shelf01_wall_iron" to "PalMapObjectItemChestModel",
    "shelf01_stone" to "PalMapObjectItemChestModel",
    "shelf02_stone" to "PalMapObjectItemChestModel",
    "shelf03_stone" to "PalMapObjectItemChestModel",
    "shelf04_stone" to "PalMapObjectItemChestModel",
    "container01_iron" to "PalMapObjectItemChestModel",
    "tablesquare_wood" to "PalBuildObject",
    "tablecircular_wood" to "PalBuildObject",
    "bench_wood" to "PalBuildObject",
    "stool_wood" to "PalBuildObject",
    "decal_palsticker_pinkcat" to "PalBuildObject",
    "stool_high_wood" to "PalBuildObject",
    "counter_wood" to "PalBuildObject",
    "rug_wood" to "PalBuildObject",
    "shelf_hang02_wood" to "PalBuildObject",
    "ivy01" to "PalBuildObject",
    "ivy02" to "PalBuildObject",
    "ivy03" to "PalBuildObject",
    "chair01_wood" to "PalBuildObject",
    "box01_stone" to "PalBuildObject",
    "barrel01_iron" to "PalBuildObject",
    "barrel02_iron" to "PalBuildObject",
    "barrel03_iron" to "PalBuildObject",
    "cablecoil01_iron" to "PalBuildObject",
    "chair01_iron" to "PalBuildObject",
    "chair02_iron" to "PalBuildObject",
    "clock01_wall_iron" to "PalBuildObject",
    "garbagebag_iron" to "PalBuildObject",
    "goalsoccer_iron" to "PalBuildObject",
    "machinegame01_iron" to "PalBuildObject",
    "machinevending01_iron" to "PalBuildObject",
    "pipeclay01_iron" to "PalBuildObject",
    "signexit_ceiling_iron" to "PalBuildObject",
    "signexit_wall_iron" to "PalBuildObject",
    "sofa01_iron" to "PalBuildObject",
    "sofa02_iron" to "PalBuildObject",
    "stool01_iron" to "PalBuildObject",
    "tablecircular01_iron" to "PalBuildObject",
    "tableside01_iron" to "PalBuildObject",
    "tablesquare01_iron" to "PalBuildObject",
    "tablesquare02_iron" to "PalBuildObject",
    "tire01_iron" to "PalBuildObject",
    "trafficbarricade01_iron" to "PalBuildObject",
    "trafficbarricade02_iron" to "PalBuildObject",
    "trafficbarricade03_iron" to "PalBuildObject",
    "trafficbarricade04_iron" to "PalBuildObject",
    "trafficbarricade05_iron" to "PalBuildObject",
    "trafficcone01_iron" to "PalBuildObject",
    "trafficcone02_iron" to "PalBuildObject",
    "trafficcone03_iron" to "PalBuildObject",
    "trafficlight01_iron" to "PalBuildObject",
    "bathtub_stone" to "PalBuildObject",
    "chair01_stone" to "PalBuildObject",
    "chair02_stone" to "PalBuildObject",
    "clock01_stone" to "PalBuildObject",
    "curtain01_wall_stone" to "PalBuildObject",
    "desk01_stone" to "PalBuildObject",
    "globe01_stone" to "PalBuildObject",
    "mirror01_stone" to "PalBuildObject",
    "mirror02_stone" to "PalBuildObject",
    "mirror01_wall_stone" to "PalBuildObject",
    "partition_stone" to "PalBuildObject",
    "piano01_stone" to "PalBuildObject",
    "piano02_stone" to "PalBuildObject",
    "rug01_stone" to "PalBuildObject",
    "rug02_stone" to "PalBuildObject",
    "rug03_stone" to "PalBuildObject",
    "rug04_stone" to "PalBuildObject",
    "sofa01_stone" to "PalBuildObject",
    "sofa02_stone" to "PalBuildObject",
    "sofa03_stone" to "PalBuildObject",
    "stool01_stone" to "PalBuildObject",
    "stove01_stone" to "PalBuildObject",
    "tablecircular01_stone" to "PalBuildObject",
    "tabledresser01_stone" to "PalBuildObject",
    "tablesink01_stone" to "PalBuildObject",
    "toilet01_stone" to "PalBuildObject",
    "toiletholder01_stone" to "PalBuildObject",
    "towlrack01_stone" to "PalBuildObject",
    "plant01_plant" to "PalBuildObject",
    "plant02_plant" to "PalBuildObject",
    "plant03_plant" to "PalBuildObject",
    "plant04_plant" to "PalBuildObject",
    "light_floorlamp01" to "PalMapObjectLampModel",
    "light_floorlamp02" to "PalMapObjectLampModel",
    "light_lightpole01" to "PalMapObjectLampModel",
    "light_lightpole02" to "PalMapObjectLampModel",
    "light_lightpole03" to "PalMapObjectLampModel",
    "light_lightpole04" to "PalMapObjectLampModel",
    "light_fireplace01" to "PalMapObjectTorchModel",
    "light_fireplace02" to "PalMapObjectTorchModel",
    "light_candlesticks_top" to "PalMapObjectLampModel",
    "light_candlesticks_wall" to "PalMapObjectLampModel",
    "television01_iron" to "PalBuildObject",
    "desk01_iron" to "PalBuildObject",
    "trafficsign01_iron" to "PalBuildObject",
    "trafficsign02_iron" to "PalBuildObject",
    "trafficsign03_iron" to "PalBuildObject",
    "trafficsign04_iron" to "PalBuildObject",
    "chair01_pal" to "PalBuildObject",
)

val NO_OP_TYPES = setOf(
    "Default_PalMapObjectConcreteModelBase",
    "PalBuildObject",
    "PalMapObjectRankUpCharacterModel",
    "PalMapObjectDefenseWaitModel",
    "PalMapObjectItemChestModel",
    "PalMapObjectMedicalPalBedModel",
    "PalMapObjectPalFoodBoxModel",
    "PalMapObjectPlayerBedModel",
    "PalMapObjectDisplayCharacterModel",
    "PalMapObjectDoorModel",
    "PalMapObjectCharacterStatusOperatorModel",
    "PalMapObjectAmusementModel",
    "PalMapObjectRepairItemModel",
    "PalMapObjectBaseCampPassiveEffectModel",
    "PalMapObjectBaseCampPassiveWorkHardModel",
    "PalMapObjectItemChest_AffectCorruption",
    "PalMapObjectDamagedScarecrowModel",
    "PalMapObjectBaseCampWorkerDirectorModel",
    "PalMapObjectMonsterFarmModel",
    "PalMapObjectLampModel",
    "PalMapObjectHeatSourceModel",
)

sealed class MapConcreteModelDict() : OpenGvasDict()

class MapConcreteModelRawData(
    val bytes: ByteArray
) : MapConcreteModelDict()

class MapConcreteModelData(
    val instanceId: String,
    val modelInstanceId: String,
    val concreteModelType: String,
    val concreteModel: MapConcreteModelItem?
) : MapConcreteModelDict()

sealed class MapConcreteModelItem : MapConcreteModelDict()

class DeathDroppedCharacterModel(
    val storedParameterId: String,
    val ownerPlayerId: String,
) : MapConcreteModelItem()

class ConvertItemModel(
    val currentRecipeId: String,
    val remainProductNum: Int,
    val requestedProductNum: Int,
    val workSpeedAdditionalRate: Float,
) : MapConcreteModelItem()

class PickupItemOnLevelModel(
    val autoPickedUp: Boolean
) : MapConcreteModelItem()

class DropItemModel(
    val autoPickedUp: Boolean,
    val itemId: ItemModelId
) : MapConcreteModelItem()

class ItemModelId(
    val staticId: String,
    val dynamicId: ItemModelDynamicId
) : MapConcreteModelItem()

class ItemModelDynamicId(
    val createdWorldId: String,
    val localIdInCreatedWorld: String
) : MapConcreteModelItem()

class ItemDropOnDamageModel(
    val dropItemInfos: ArrayList<PalItemAndNumRead>
) : MapConcreteModelItem()

class DeathPenaltyStorageModel(
    val ownerPlayerUid: String
) : MapConcreteModelItem()

class DefenseBulletLauncherModel(
    val remainingBullets: Int,
    val magazineSize: Int,
    val bulletItemName: String
) : MapConcreteModelItem()

class GenerateEnergyModel(
    val storedEnergyAmount: Float
) : MapConcreteModelItem()

class FarmBlockV2Model(
    val cropDataId: String,
    val currentState: Byte,
    val cropProgressRateValue: Float,
    val waterStackRateValue: Float,
    val stateMachine: StateMachine?
) : MapConcreteModelItem()

class StateMachine(
    val growUpRequiredTime: Float,
    val growUpProgressTime: Float,
) : MapConcreteModelItem()

class FastTravelPointModel(
    val locationInstanceId: String
) : MapConcreteModelItem()

class ShippingItemModel(
    val shippingHours: ArrayList<Int>
) : MapConcreteModelItem()

class ProductItemModel(
    val workSpeedAdditionalRate: Float,
    val productItemId: String
) : MapConcreteModelItem()

class RecoverOtomoModel(
    val recoverAmountBySec: Float
) : MapConcreteModelItem()

class HatchingEggModel(
    val hatchedCharacterSaveParameter: GvasMap<String, GvasProperty>,
    val unknownBytes: Int,
    val hatchedCharacterGuid: String
) : MapConcreteModelItem()

class TreasureBoxModel(
    val treasureGradeType: Byte
) : MapConcreteModelItem()

class BreedFarmModel(
    val spawnedEggInstanceIds: ArrayList<String>
) : MapConcreteModelItem()

class SignboardModel(
    val signboardText: String
) : MapConcreteModelItem()

class TorchModel(
    val extinctionDateTime: Long
) : MapConcreteModelItem()

class PalEggModel(
    val unknownBytes: Int
) : MapConcreteModelItem()

class BaseCampPoint(
    val baseCampId: String
) : MapConcreteModelItem()



fun MapConcreteModel.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
    objectId: String
): MapConcreteModelDict {
    if (bytes.isEmpty()) return MapConcreteModelRawData(bytes)

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val modelId = MAP_OBJECT_NAME_TO_CONCRETE_MODEL_CLASS[objectId.lowercase()]
        ?: return MapConcreteModelRawData(bytes)

    val data = MapConcreteModelData(
        instanceId = reader.uuid().toString(),
        modelInstanceId = reader.uuid().toString(),
        concreteModelType = modelId,
        concreteModel = when(modelId) {
            in NO_OP_TYPES -> null
            "PalMapObjectDeathDroppedCharacterModel" -> DeathDroppedCharacterModel(
                storedParameterId = reader.uuid().toString(),
                ownerPlayerId = reader.uuid().toString()
            )
            "PalMapObjectConvertItemModel" -> ConvertItemModel(
                currentRecipeId = reader.fstring(),
                remainProductNum = reader.readInt(),
                requestedProductNum = reader.readInt(),
                workSpeedAdditionalRate = reader.readFloat()
            )
            "PalMapObjectPickupItemOnLevelModel" -> PickupItemOnLevelModel(
                autoPickedUp = reader.readInt() > 0
            )
            "PalMapObjectDropItemModel" -> DropItemModel(
                autoPickedUp = reader.readInt() > 0,
                itemId = ItemModelId(
                    staticId = reader.fstring(),
                    dynamicId = ItemModelDynamicId(
                        createdWorldId = reader.uuid().toString(),
                        localIdInCreatedWorld = reader.uuid().toString()
                    )
                )
            )
            "PalMapObjectItemDropOnDamagModel" -> ItemDropOnDamageModel(
                dropItemInfos = reader.readArray(PalItemAndNumRead.Companion::fromBytes)
            )
            "PalMapObjectDeathPenaltyStorageModel" -> DeathPenaltyStorageModel(
                ownerPlayerUid = reader.uuid().toString()
            )
            "PalMapObjectDefenseBulletLauncherModel" -> DefenseBulletLauncherModel(
                remainingBullets = reader.readInt(),
                magazineSize = reader.readInt(),
                bulletItemName = reader.fstring()
            )
            "PalMapObjectGenerateEnergyModel" -> GenerateEnergyModel(
                storedEnergyAmount = reader.readFloat()
            )
            "PalMapObjectFarmBlockV2Model" -> FarmBlockV2Model(
                cropDataId = reader.fstring(),
                currentState = reader.readByte(),
                cropProgressRateValue = reader.readFloat(),
                waterStackRateValue = reader.readFloat(),
                stateMachine = if (!reader.isEof()) {
                    StateMachine(
                        growUpProgressTime = reader.readFloat(),
                        growUpRequiredTime = reader.readFloat()
                    )
                } else null
            )
            "PalMapObjectFastTravelPointModel" -> FastTravelPointModel(
                locationInstanceId = reader.uuid().toString()
            )
            "PalMapObjectShippingItemModel" -> ShippingItemModel(
                shippingHours = reader.readArray { it.readInt() }
            )
            "PalMapObjectProductItemModel" -> ProductItemModel(
                workSpeedAdditionalRate = reader.readFloat(),
                productItemId = reader.fstring()
            )
            "PalMapObjectRecoverOtomoModel" -> RecoverOtomoModel(
                recoverAmountBySec = reader.readFloat()
            )
            "PalMapObjectHatchingEggModel" -> HatchingEggModel(
                hatchedCharacterSaveParameter = reader.properties(""),
                unknownBytes = reader.readInt(),
                hatchedCharacterGuid = reader.uuid().toString()
            )
            "PalMapObjectTreasureBoxModel" -> TreasureBoxModel(
                treasureGradeType = reader.readByte()
            )
            "PalMapObjectBreedFarmModel" -> BreedFarmModel(
                spawnedEggInstanceIds = reader.readArray { it.uuid().toString() }
            )
            "PalMapObjectSignboardModel" -> SignboardModel(
                signboardText = reader.fstring()
            )
            "PalMapObjectTorchModel" -> TorchModel(
                extinctionDateTime = reader.readLong()
            )
            "PalMapObjectPalEggModel" -> PalEggModel(
                unknownBytes = reader.readInt()
            )
            "PalMapObjectBaseCampPoint" -> BaseCampPoint(
                baseCampId = reader.uuid().toString()
            )
            else -> {
                println(
                    "Warning: Unknown map object concrete model $modelId, skipping"
                )
                return MapConcreteModelRawData(bytes)
            }
        }
    )

    check(reader.isEof()) {
        "EOF not reached for $objectId|$modelId, remaining=${reader.remaining}"
    }

    return data
}