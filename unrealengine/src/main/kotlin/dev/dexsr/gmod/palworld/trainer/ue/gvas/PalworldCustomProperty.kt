package dev.dexsr.gmod.palworld.trainer.ue.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.Character
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.Group
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.decode
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.encode

val PALWORLD_TYPE_HINT = mapOf(
    ".worldSaveData.CharacterContainerSaveData.Key" to "StructProperty",
    ".worldSaveData.CharacterSaveParameterMap.Key" to "StructProperty",
    ".worldSaveData.CharacterSaveParameterMap.Value" to "StructProperty",
    ".worldSaveData.FoliageGridSaveDataMap.Key" to "StructProperty",
    ".worldSaveData.FoliageGridSaveDataMap.Value.ModelMap.Value" to "StructProperty",
    ".worldSaveData.FoliageGridSaveDataMap.Value.ModelMap.Value.InstanceDataMap.Key" to "StructProperty",
    ".worldSaveData.FoliageGridSaveDataMap.Value.ModelMap.Value.InstanceDataMap.Value" to "StructProperty",
    ".worldSaveData.FoliageGridSaveDataMap.Value" to "StructProperty",
    ".worldSaveData.ItemContainerSaveData.Key" to "StructProperty",
    ".worldSaveData.MapObjectSaveData.MapObjectSaveData.ConcreteModel.ModuleMap.Value" to "StructProperty",
    ".worldSaveData.MapObjectSaveData.MapObjectSaveData.Model.EffectMap.Value" to "StructProperty",
    ".worldSaveData.MapObjectSpawnerInStageSaveData.Key" to "StructProperty",
    ".worldSaveData.MapObjectSpawnerInStageSaveData.Value" to "StructProperty",
    ".worldSaveData.MapObjectSpawnerInStageSaveData.Value.SpawnerDataMapByLevelObjectInstanceId.Key" to "Guid",
    ".worldSaveData.MapObjectSpawnerInStageSaveData.Value.SpawnerDataMapByLevelObjectInstanceId.Value" to "StructProperty",
    ".worldSaveData.MapObjectSpawnerInStageSaveData.Value.SpawnerDataMapByLevelObjectInstanceId.Value.ItemMap.Value" to "StructProperty",
    ".worldSaveData.WorkSaveData.WorkSaveData.WorkAssignMap.Value" to "StructProperty",
    ".worldSaveData.BaseCampSaveData.Key" to "Guid",
    ".worldSaveData.BaseCampSaveData.Value" to "StructProperty",
    ".worldSaveData.BaseCampSaveData.Value.ModuleMap.Value" to "StructProperty",
    ".worldSaveData.ItemContainerSaveData.Value" to "StructProperty",
    ".worldSaveData.CharacterContainerSaveData.Value" to "StructProperty",
    ".worldSaveData.GroupSaveDataMap.Key" to "Guid",
    ".worldSaveData.GroupSaveDataMap.Value" to "StructProperty",
    ".worldSaveData.EnemyCampSaveData.EnemyCampStatusMap.Value" to "StructProperty",
    ".worldSaveData.DungeonSaveData.DungeonSaveData.MapObjectSaveData.MapObjectSaveData.Model.EffectMap.Value" to "StructProperty",
    ".worldSaveData.DungeonSaveData.DungeonSaveData.MapObjectSaveData.MapObjectSaveData.ConcreteModel.ModuleMap.Value" to "StructProperty",
)

val PALWORLD_CUSTOM_PROPERTY_CODEC = mapOf<String, Pair<GVAS_PROPERTY_DECODER, GVAS_PROPERTY_ENCODER>>(
    ".worldSaveData.GroupSaveDataMap" to (Group::decode to Group::encode),
    ".worldSaveData.CharacterSaveParameterMap.Value.RawData" to (Character::decode to Character::encode)
)

typealias GVAS_PROPERTY_DECODER = (GvasReader, String, Int, String) -> GvasDict
typealias GVAS_PROPERTY_ENCODER = (GvasReader, String, GvasMap<String, Any>) -> Int