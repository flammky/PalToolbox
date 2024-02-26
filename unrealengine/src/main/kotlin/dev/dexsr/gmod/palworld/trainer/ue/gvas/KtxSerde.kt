package dev.dexsr.gmod.palworld.trainer.ue.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.*
import dev.dexsr.gmod.palworld.trainer.ue.util.castOrNull
import dev.dexsr.gmod.palworld.trainer.ue.util.fastForEach
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

typealias KtxJson = kotlinx.serialization.json.Json

fun KtxJson.parseGvasFile(
    file: GvasFile
): JsonObject {

    return buildJsonObject {
        putGvasHeader(file.header)
        putGvasProperties(file.properties)
        putGvasTrailer(file.trailer)
    }
}

private fun JsonObjectBuilder.putGvasHeader(header: GvasFileHeader) {
    put("header", header.toJsonObject())
}

private fun JsonObjectBuilder.putGvasProperties(properties: GvasFileProperties) {
    put("properties", properties.toJsonObject())
}

@OptIn(ExperimentalEncodingApi::class)
private fun JsonObjectBuilder.putGvasTrailer(trailer: ByteArray) {
    put("trailer", Base64.encode(trailer))
}

@OptIn(ExperimentalSerializationApi::class)
private fun GvasFileHeader.toJsonObject(): JsonObject {
    return buildJsonObject {
        put(GvasFileHeader.MAGIC_BYTES_NAME, JsonPrimitive(magicBytesToJsonValue()))
        put(GvasFileHeader.SAVE_GAME_VERSION_NAME, JsonPrimitive(saveGameVersion))
        put(GvasFileHeader.PACKAGE_FILE_VERSION_UE4_NAME, JsonPrimitive(packageFileVersionUE4))
        put(GvasFileHeader.PACKAGE_FILE_VERSION_UE5_NAME, JsonPrimitive(packageFileVersionUE5))
        put(GvasFileHeader.ENGINE_VERSION_MAJOR_NAME, JsonPrimitive(engineVersionMajor))
        put(GvasFileHeader.ENGINE_VERSION_MINOR_NAME, JsonPrimitive(engineVersionMinor))
        put(GvasFileHeader.ENGINE_VERSION_PATCH_NAME, JsonPrimitive(engineVersionPatch))
        put(GvasFileHeader.ENGINE_VERSION_CHANGELIST_NAME, JsonPrimitive(engineVersionChangelist))
        put(GvasFileHeader.ENGINE_VERSION_BRANCH_NAME, JsonPrimitive(engineVersionBranch))
        put(GvasFileHeader.CUSTOM_VERSION_FORMAT_NAME, JsonPrimitive(customVersionFormat))
        put(
            GvasFileHeader.CUSTOM_VERSIONS_NAME,
            element = buildJsonArray {
                customVersions.forEach { version ->
                    add(buildJsonArray { add(version.first) ; add(version.second) })
                }
            }
        )
        put(GvasFileHeader.SAVE_GAME_CLASSNAME_NAME, JsonPrimitive(saveGameClassName))
    }
}

private fun GvasFileProperties.toJsonObject(): JsonObject {
    return buildJsonObject {
        entries.forEach { (k, v) ->
            put(
                k,
                v.value.toJsonElement()
            )
        }
    }
}

private fun GvasArrayDict.toJsonElement(): JsonElement = buildJsonObject {
    put("array_type", JsonPrimitive(arrayType))
    put("id", JsonPrimitive(id))
    put("value", value.toJsonElement())
    put("type", GvasArrayDict.TYPE_NAME)
}

private fun GvasArrayPropertyValue.toJsonElement(): JsonElement {
    return when(this) {
        is GvasAnyArrayPropertyValue -> toJsonElement()
        is GvasStructArrayPropertyValue -> toJsonElement()
    }
}

private fun GvasAnyArrayPropertyValue.toJsonElement(): JsonElement {
    return when(this.values) {
        is GvasByteArrayValue -> values.toJsonElement()
        is GvasStringArrayValue -> values.toJsonElement()
        is OpenGvasTypedArray<*> -> TODO()
    }
}


@OptIn(ExperimentalEncodingApi::class)
private fun GvasByteArrayValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("typeName", typeName)
        put("value", buildJsonArray { value.forEach { add(it.toInt()) }})
    }
}

private fun GvasStringArrayValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        /*put("typeName", typeName)*/
        put("values", JsonArray(value.map { JsonPrimitive(it) }))
    }
}

private fun GvasStructArrayPropertyValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("prop_name", propName)
        put("prop_type", propType)
        put("values", buildJsonArray {
            values.fastForEach { struct ->
                add(struct.toJsonElement())
            }
        })
        put("typeName", typeName)
        put("id", id)
    }
}

private fun GvasLinearColor.toJsonElement() = buildJsonObject {
    put("r", r)
    put("g", g)
    put("b", b)
    put("a", a)
}

private fun GvasQuat.toJsonElement() = buildJsonObject {
    put("x", x)
    put("y", y)
    put("z", z)
    put("w", w)
}

private fun GvasStructMap.toJsonElement() = buildJsonObject {
    v.entries.forEach { (k, v) ->
        put(
            k,
            v.value.toJsonElement()
        )
    }
}

private fun GvasBoolDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", "BoolProperty")
        put("type", GvasBoolDict.TYPE_NAME)
    }
}

private fun GvasEnumDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", value.toJsonElement())
        put("type", GvasEnumDict.TYPE_NAME)
    }
}

private fun GvasEnumDictValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("type", type)
        put("value", JsonPrimitive(value))
    }
}

private fun GvasFixedPoint64Dict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", GvasFixedPoint64Dict.TYPE_NAME)
    }
}

private fun GvasFloatDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", GvasFloatDict.TYPE_NAME)
    }
}

private fun GvasIntDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", GvasIntDict.TYPE_NAME)
    }
}

private fun GvasInt64Dict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", GvasInt64Dict.TYPE_NAME)
    }
}

private fun GvasStrDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", id)
        put("value", JsonPrimitive(value))
        put("type", GvasStrDict.TYPE_NAME)
    }
}

private fun GvasMapDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("keyType", keyType)
        put("valueType", valueType)
        put("keyStructType", keyStructType)
        put("valueStructType", valueStructType)
        put("id", id)
        put(
            "value",
            buildJsonArray {
                value.fastForEach { map ->
                    add(
                        buildJsonObject {
                            map.forEach { (k, v) ->
                                put(
                                    k,
                                    v.jsonPrimitiveOrNull()
                                        ?: v.castOrNull<GvasStruct>()?.toJsonElement()
                                        ?: error("Unknown GvasMapDict value Type: ${v::class.qualifiedName}")
                                )
                            }
                        }
                    )
                }
            }
        )
        put("type", GvasMapDict.TYPE_NAME)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun Any.jsonPrimitiveOrNull() = when(this) {
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is UShort -> JsonPrimitive(this)
    else -> null
}

private fun GvasStruct.toJsonElement(): JsonElement {
    return when (this) {
        is GvasDateTime -> JsonPrimitive(v)
        is GvasGUID -> JsonPrimitive(v)
        is GvasLinearColor -> toJsonElement()
        is GvasQuat -> toJsonElement()
        is GvasStructMap -> toJsonElement()
        is GvasVector -> toJsonElement()
        is GvasTransform -> toJsonElement()
    }
}

private fun GvasVector.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("x", x)
        put("y", y)
        put("z", z)
    }
}

private fun GvasTransform.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("rotation", rotation.toJsonElement())
        put("translation", translation.toJsonElement())
        put("scale3D", scale3D.toJsonElement())
    }
}

private fun GvasNameDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("id", JsonPrimitive(id))
        put("value", JsonPrimitive(value))
        put("type", GvasNameDict.TYPE_NAME)
    }
}

private fun GvasStructDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("structType", structType)
        put("structId", structId)
        put("id", id)
        put("value", value.toJsonElement())
        put("type", GvasStructDict.TYPE_NAME)
    }
}

private fun GvasGroupDict.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("groupType", groupType)
        put("groupId", groupId)
        put("groupName", groupName)
        put("individual_character_handle_ids", buildJsonArray {
            individual_character_handle_ids.fastForEach { add(it.toJsonElement()) }
        })

        org?.let {
            put("orgType", org.orgType)
            put("baseIds", buildJsonArray { org.baseIds.fastForEach { add(it) } })
        }

        guild?.let {
            put("baseCampLevel", guild.baseCampLevel)
            put("mapObjectInstanceIdsBaseCampPoints", buildJsonArray {
                guild.mapObjectInstanceIdsBaseCampPoints.fastForEach { add(it) }
            })
        }

        independentGuildData?.let {
            put("playerUid", it.playerUid)
            put("guildName2", it.guildName2)
            put("playerInfo", it.playerInfo.toJsonElement())
        }

        guildPlayersData?.let {
            put("adminPlayerUuid", it.adminPlayerUuid)
            put("players", buildJsonArray { guildPlayersData.players.fastForEach { p ->
                add(p.playerInfo.toJsonElement())
            } })
        }
    }
}

private fun InstanceID.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("guid", guid)
        put("instanceId", instanceID)
    }
}

private fun GvasGroupGuildPlayerInfo.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("lastOnlineRealTime", lastOnlineRealTime)
        put("playerName", playerName)
    }
}

private fun GvasCharacterData.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("object", buildJsonObject {
            `object`.entries.forEach { (k, v) ->
                put(k, v.value.toJsonElement())
            }
        })
        // TODO: Base64
        put("unknownBytes", Json.encodeToJsonElement(unknownBytes))
        put("groupId", JsonPrimitive(groupId))
    }
}

private fun GvasDict?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonNull
        is GvasArrayDict -> toJsonElement()
        is GvasBoolDict -> toJsonElement()
        is GvasEnumDict -> toJsonElement()
        is GvasEnumDictValue -> toJsonElement()
        is GvasFixedPoint64Dict -> toJsonElement()
        is GvasFloatDict -> toJsonElement()
        is GvasIntDict -> toJsonElement()
        is GvasInt64Dict -> toJsonElement()
        is GvasMapDict -> toJsonElement()
        is GvasNameDict -> toJsonElement()
        is GvasStrDict -> toJsonElement()
        is GvasStructDict -> toJsonElement()
        is GvasCustomProperty -> toJsonElement()
        is OpenGvasDict -> {
            when (this) {
                is CustomRawData -> toJsonElement()
                is GvasGroupDict -> toJsonElement()
                is GvasCharacterData -> toJsonElement()
                is GvasItemContainerData -> toJsonElement()
                is GvasItemContainerSlotData -> toJsonElement()
                is DynamicItemSaveData -> toJsonElement()
                is DynamicItemDict -> toJsonElement()
                is FoliageModelInstanceDict -> toJsonElement()
                is FoliageModelDict -> toJsonElement()
                is BaseCampDict -> toJsonElement()
                is WorkerDirectorDict -> toJsonElement()
                is WorkCollectionDict -> toJsonElement()
                is BaseCampModuleData -> toJsonElement()
                else -> TODO()
            }
        }
    }
}

private fun CustomRawData.toJsonElement(): JsonElement {
    return when(this) {
        is ByteArrayRawData -> toJsonElement()
    }
}

private fun ByteArrayRawData.toJsonElement() = buildJsonObject {
    put("arrayType", "ByteProperty")
    put("id", id)
    put("value", value.toJsonElement())
    put("type", "ArrayProperty")
    put("customType", customType)
}

private fun GvasCustomProperty.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("value", value.toJsonElement())
        put("customType", customType)
    }
}

private fun GvasItemContainerData.toJsonElement() = buildJsonObject {
    put("permission", permission.toJsonElement())
}

private fun GvasItemContainerPermission.toJsonElement() = buildJsonObject {
    put("typeA", typeA.toJsonIntArray())
    put("typeB", typeB.toJsonIntArray())
    put("itemStaticIds", itemStaticIds.toJsonStringArray())
}

private fun GvasItemContainerSlotData.toJsonElement() = buildJsonObject {
    put("permission", permission.toJsonElement())
    put("corruptionProgressValue", corruptionProgressValue)
}

private fun DynamicItemSaveData.toJsonElement() = buildJsonObject {
    put("id", id.toJsonElement())
    put("data", data.toJsonElement())
}

private fun DynamicItemSaveDataId.toJsonElement() = buildJsonObject {
    put("createdWorldId", createdWorldId)
    put("localIdInCreatedWorld", localIdInCreatedWorld)
    put("staticId", staticId)
}

private fun DynamicItemDict.toJsonElement() = when(this) {
    is ArmorDynamicItem -> toJsonElement()
    is EggDynamicItem -> toJsonElement()
    is RawDynamicItem -> toJsonElement()
    is WeaponDynamicItem -> toJsonElement()
}

private fun ArmorDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("durability", durability)
}

private fun EggDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("characterId", characterId)
    put("object", `object`.toJsonMap())
    put("unknownBytes", unknownBytes.toJsonIntArray())
    put("unknownId", unknownId)
}

private fun RawDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("trailer", trailer.toJsonIntArray())
}

private fun WeaponDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("durability", durability)
    put("remainingBullets", remainingBullets)
    put("passiveSkillList", passiveSkillList.toJsonStringArray())
}

private fun ByteArray.toJsonIntArray() = buildJsonArray {
    fastForEach { add(it.toInt()) }
}

private fun List<String>.toJsonStringArray() = buildJsonArray {
    fastForEach { add(it) }
}

private fun GvasMap<String, GvasProperty>.toJsonMap() = buildJsonObject {
    entries.forEach { (k, v) ->
        put(k, v.value.toJsonElement())
    }
}

// assume no node list usage
private fun List<Int>.toJsonIntArray() = buildJsonArray {
    fastForEach { add(it.toInt()) }
}

private fun FoliageModelInstanceDict.toJsonElement() = when(this) {
    is FoliageModelInstanceData -> toJsonElement()
    is FoliageModelInstanceWorldTransform -> toJsonElement()
}

private fun FoliageModelInstanceData.toJsonElement() = buildJsonObject {
    put("modelInstanceId", modelInstanceId)
    put("worldTransform", worldTransform.toJsonElement())
    put("hp", hp)
}

private fun FoliageModelInstanceWorldTransform.toJsonElement() = buildJsonObject {
    put("rotator", rotator.toJsonElement())
    put("vector", vector.toJsonElement())
    put("scaleX", scaleX)
}

private fun FoliageModelInstanceRotator.toJsonElement() = buildJsonObject {
    put("pitch", pitch)
    put("yaw", yaw)
    put("roll", roll)
}

private fun FoliageModelInstanceVector.toJsonElement() = buildJsonObject {
    put("x", x)
    put("y", y)
    put("z", z)
}

private fun FoliageModelDict.toJsonElement() = when (this) {
    is FoliageModelCellCoord -> toJsonElement()
    is FoliageModelData -> toJsonELement()
}

private fun FoliageModelCellCoord.toJsonElement() = buildJsonObject {
    putVector(x, y, z)
}

private fun FoliageModelData.toJsonELement() = buildJsonObject {
    put("modelId", modelId)
    put("foliagePresetType", foliagePresetType)
    put("cellCoord", cellCoord.toJsonElement())
}

private fun JsonObjectBuilder.putVector(x: Long, y: Long, z: Long) {
    put("x", x)
    put("y", y)
    put("z", z)
}

private fun BaseCampDict.toJsonElement() = when(this) {
    is BaseCampData -> toJsonElement()
}

private fun BaseCampData.toJsonElement() = buildJsonObject {
    put("id", id)
    put("name", name)
    put("state", state)
    put("transform", transform.toJsonElement())
    put("areaRange", areaRange)
    put("groupIdBelongTo", groupIdBelongTo)
    put("fastTravelLocalTransform", fastTravelLocalTransform.toJsonElement())
    put("ownerMapObjectInstanceId", ownerMapObjectInstanceId)
}

private fun WorkerDirectorDict.toJsonElement() = when(this) {
    is WorkerDirectorData -> toJsonElement()
}

private fun WorkerDirectorData.toJsonElement() = buildJsonObject {
    put("id", id)
    put("spawnTransform", spawnTransform.toJsonElement())
    put("currentOrderType", currentOrderType)
    put("currentBattleType", currentBattleType)
    put("containerId", containerId)
}

private fun WorkCollectionDict.toJsonElement() = when(this) {
    is WorkCollectionData -> toJsonElement()
}

private fun WorkCollectionData.toJsonElement() = buildJsonObject {
    put("id", id)
    put("workIds", workIds.toJsonStringArray())
}

private fun BaseCampModuleDict.toJsonElement() = when(this) {
    is BaseCampModuleData -> toJsonElement()
    is BaseCampModulePassiveEffect -> toJsonElement()
    is BaseCampModuleTransportItemCharacterInfo -> toJsonElement()
}

private fun BaseCampModuleData.toJsonElement() = buildJsonObject {
    transportItemCharacterInfos?.let { l ->
        put(
            "transportItemCharacterInfos",
            buildJsonArray { l.fastForEach { add(it.toJsonElement()) } }
        )
    }
    passiveEffects?.let { l ->
        put(
            "passiveEffects",
            buildJsonArray { l.fastForEach { add(it.toJsonElement()) } }
        )
    }
    values?.let {
        put(
            "values",
            values.toJsonIntArray()
        )
    }
}


private fun BaseCampModuleTransportItemCharacterInfo.toJsonElement() = buildJsonObject {
    put("itemInfos", buildJsonArray {
        itemInfos.fastForEach { e ->
            add(e.toJsonElement())
        }
    })
    put("characterLocation", characterLocation.toJsonElement())
}

private fun PalItemAndNumRead.toJsonElement() = buildJsonObject {
    put("itemId", itemId.toJsonElement())
    put("num", num)
}

private fun PalItemId.toJsonElement() = buildJsonObject {
    put("staticId", staticId)
    put("dynamicId", dynamicId.toJsonElement())
}

private fun PalItemDynamicId.toJsonElement() = buildJsonObject {
    put("createdWorldId", createdWorldId)
    put("localIdInCreatedWorld", localIdInCreatedWorld)
}

private fun BaseCampModulePassiveEffect.toJsonElement() = buildJsonObject {
    put("type", type)
    workHardType?.let { put("workHardType", workHardType) }
    unknownTrailer?.let { put("unknownTrailer", unknownTrailer.toJsonIntArray()) }
}