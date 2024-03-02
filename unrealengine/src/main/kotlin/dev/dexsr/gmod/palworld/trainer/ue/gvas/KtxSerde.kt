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
        is GvasTransformedArrayValue -> value.toJsonElement()
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
        /*put("typeName", typeName)*/
        put("values", buildJsonArray { value.forEach { add(it.toInt()) }})
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
        put("value", enumValue.toJsonElement())
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

private fun GvasGroupData.toJsonElement(): JsonElement {
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
            put("guildName", guild.guildName)
        }

        independentGuildData?.let {
            put("playerUid", it.playerUid)
            put("guildName2", it.guildName2)
            put("playerInfo", it.playerInfo.toJsonElement())
        }

        guildPlayersData?.let {
            put("adminPlayerUuid", it.adminPlayerUuid)
            put("players", buildJsonArray { guildPlayersData.players.fastForEach { p ->
                add(p.toJsonElement())
            } })
        }
    }
}

private fun GvasGroupGuildPlayer.toJsonElement() = buildJsonObject {
    put("playerUid", playerUid)
    put("playerInfo", playerInfo.toJsonElement())
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
            // TODO: seal
            when (this) {
                is CustomRawData -> toJsonElement()
                is CharacterDict -> toJsonElement()
                is GvasGroupDict -> toJsonElement()
                is GvasItemContainerData -> toJsonElement()
                is GvasItemContainerSlotData -> toJsonElement()
                is DynamicItemSaveData -> toJsonElement()
                is DynamicItemItemDict -> toJsonElement()
                is CharacterContainerData -> toJsonElement()
                is FoliageModelInstanceDict -> toJsonElement()
                is FoliageModelDict -> toJsonElement()
                is BaseCampDict -> toJsonElement()
                is WorkerDirectorDict -> toJsonElement()
                is WorkCollectionDict -> toJsonElement()
                is BaseCampModuleData -> toJsonElement()
                is WorkSaveDataDict -> toJsonElement()
                is MapModelDict -> toJsonElement()
                is ConnectorDict -> toJsonElement()
                is BuildProcessDict -> toJsonElement()
                is MapConcreteModelDict -> toJsonElement()
                is MapConcreteModelModuleDict -> toJsonElement()
                else -> TODO("no JsonElement serializer for type=${this::class.qualifiedName}")
            }
        }
    }
}

private fun CharacterContainerDict.toJsonElement(): JsonElement = when(this) {
    is CharacterContainerData -> toJsonElement()
}

private fun CharacterContainerData.toJsonElement() = buildJsonObject {
    put("playerUid", playerUid)
    put("instanceId", instanceId)
    put("permissionTribeId", permissionTribeId)
}

private fun MapConcreteModelModuleDict.toJsonElement() = when(this) {
    is MapConcreteModelModuleData -> toJsonElement()
    is MapConcreteModelModuleRawData -> buildJsonObject {
        put("values", buildJsonArray { values.fastForEach {
            add(it.toInt())
        } })
    }
    is MapConcreteModelModuleItemDict -> toJsonElement()
}

// TODO: write
private fun MapConcreteModelModuleData.toJsonElement() = buildJsonObject {
    item?.let {
        item.putToJsonBuilder(this)
    }
}

private fun MapConcreteModelModuleItemDict.toJsonElement() = when(this) {
    is MapConcreteModelModuleItemContainer -> toJsonElement()
    is MapConcreteModuleCharacterContainer -> toJsonElement()
    is MapConcreteModulePasswordLock -> toJsonElement()
    is MapConcreteModuleSwitch -> toJsonElement()
    is MapConcreteModuleWorkee -> toJsonElement()
    is ModuleSlotIndexes -> toJsonElement()
    is PlayerLockInfo -> toJsonElement()
}

private fun MapConcreteModelModuleItemDict.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is MapConcreteModelModuleItemContainer -> putToJsonBuilder(builder)
    is MapConcreteModuleCharacterContainer -> putToJsonBuilder(builder)
    is MapConcreteModulePasswordLock -> putToJsonBuilder(builder)
    is MapConcreteModuleSwitch -> putToJsonBuilder(builder)
    is MapConcreteModuleWorkee -> putToJsonBuilder(builder)
    is ModuleSlotIndexes -> putToJsonBuilder(builder)
    is PlayerLockInfo -> putToJsonBuilder(builder)
}

private fun MapConcreteModuleWorkee.toJsonElement() = buildJsonObject {
    put("targetWorkId", targetWorkId)
}
private fun MapConcreteModuleWorkee.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("targetWorkId", targetWorkId)
}

private fun MapConcreteModuleSwitch.toJsonElement() = buildJsonObject {
    put("switchState", switchState)
}

private fun MapConcreteModuleSwitch.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("switchState", switchState)
}

private fun MapConcreteModulePasswordLock.toJsonElement() = buildJsonObject {
    put("lockState", lockState)
    put("password", password)
    put("playerInfos", buildJsonArray {
        playerInfos.fastForEach { add(it.toJsonElement()) }
    })
}

private fun MapConcreteModulePasswordLock.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("lockState", lockState)
    put("password", password)
    put("playerInfos", buildJsonArray {
        playerInfos.fastForEach { add(it.toJsonElement()) }
    })
}

private fun PlayerLockInfo.toJsonElement() = buildJsonObject {
    put("playerUid", playerUid)
    put("tryFailedCount", tryFailedCount)
    put("trySuccessCache", trySuccessCache)
}

private fun PlayerLockInfo.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("playerUid", playerUid)
    put("tryFailedCount", tryFailedCount)
    put("trySuccessCache", trySuccessCache)
}

private fun MapConcreteModuleCharacterContainer.toJsonElement() = buildJsonObject {
    put("targetContainerId", targetContainerId)
}

private fun MapConcreteModuleCharacterContainer.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("targetContainerId", targetContainerId)
}

private fun MapConcreteModelModuleItemContainer.toJsonElement() = buildJsonObject {
    put("targetContainerId", targetContainerId)
    put("slotAttributeIndexes", buildJsonArray {
        slotAttributeIndexes.fastForEach { add(it.toJsonElement()) }
    })
    put("allSlotAttribute", buildJsonArray {
        allSlotAttribute.fastForEach { add(it) }
    })
    put("dropItemAtDisposed", dropItemAtDisposed)
    put("usageType", usageType)
}

private fun MapConcreteModelModuleItemContainer.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("targetContainerId", targetContainerId)
    put("slotAttributeIndexes", buildJsonArray {
        slotAttributeIndexes.fastForEach { add(it.toJsonElement()) }
    })
    put("allSlotAttribute", buildJsonArray {
        allSlotAttribute.fastForEach { add(it) }
    })
    put("dropItemAtDisposed", dropItemAtDisposed)
    put("usageType", usageType)
}

private fun ModuleSlotIndexes.toJsonElement() = buildJsonObject {
    put("attribute", attribute)
    put("indexes", buildJsonArray {
        indexes.fastForEach { add(it.toInt()) }
    })
}

private fun ModuleSlotIndexes.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("attribute", attribute)
    put("indexes", buildJsonArray {
        indexes.fastForEach { add(it.toInt()) }
    })
}

private fun MapConcreteModelDict.toJsonElement(): JsonElement = when(this) {
    is MapConcreteModelData -> toJsonElement()
    is BaseCampPoint -> toJsonElement()
    is BreedFarmModel -> toJsonElement()
    is ConvertItemModel -> buildJsonObject { putToJsonBuilder(this) }
    is DeathDroppedCharacterModel -> buildJsonObject { putToJsonBuilder(this) }
    is DeathPenaltyStorageModel -> buildJsonObject { putToJsonBuilder(this) }
    is DefenseBulletLauncherModel -> buildJsonObject { putToJsonBuilder(this) }
    is DropItemModel -> buildJsonObject { putToJsonBuilder(this) }
    is FarmBlockV2Model -> buildJsonObject { putToJsonBuilder(this) }
    is FastTravelPointModel -> buildJsonObject { putToJsonBuilder(this) }
    is GenerateEnergyModel -> buildJsonObject { putToJsonBuilder(this) }
    is HatchingEggModel -> buildJsonObject { putToJsonBuilder(this) }
    is ItemDropOnDamageModel -> buildJsonObject { putToJsonBuilder(this) }
    is ItemModelDynamicId -> buildJsonObject { putToJsonBuilder(this) }
    is ItemModelId -> buildJsonObject { putToJsonBuilder(this) }
    is PalEggModel -> buildJsonObject { putToJsonBuilder(this) }
    is PickupItemOnLevelModel -> buildJsonObject { putToJsonBuilder(this) }
    is ProductItemModel -> buildJsonObject { putToJsonBuilder(this) }
    is RecoverOtomoModel -> buildJsonObject { putToJsonBuilder(this) }
    is ShippingItemModel -> buildJsonObject { putToJsonBuilder(this) }
    is SignboardModel -> buildJsonObject { putToJsonBuilder(this) }
    is StateMachine -> buildJsonObject { putToJsonBuilder(this) }
    is TorchModel -> buildJsonObject { putToJsonBuilder(this) }
    is TreasureBoxModel -> buildJsonObject { putToJsonBuilder(this) }
    is MapConcreteModelRawData -> buildJsonObject {
        put("values", buildJsonArray { values.fastForEach { add(it.toInt()) } })
    }
}

private fun BreedFarmModel.toJsonElement() = buildJsonObject { putToJsonBuilder(this) }

private fun BaseCampPoint.toJsonElement() = buildJsonObject { putToJsonBuilder(this) }

private fun MapConcreteModelData.toJsonElement() = buildJsonObject {
    put("instanceId", instanceId)
    put("modelInstanceId", modelInstanceId)
    put("concreteModelType", concreteModelType)
    concreteModel?.let {
        concreteModel.putToJsonBuilder(this)
    }
}

private fun MapConcreteModelItem.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is BaseCampPoint -> putToJsonBuilder(builder)
    is BreedFarmModel -> putToJsonBuilder(builder)
    is ConvertItemModel -> putToJsonBuilder(builder)
    is DeathDroppedCharacterModel -> putToJsonBuilder(builder)
    is DeathPenaltyStorageModel -> putToJsonBuilder(builder)
    is DefenseBulletLauncherModel -> putToJsonBuilder(builder)
    is DropItemModel -> putToJsonBuilder(builder)
    is FarmBlockV2Model -> putToJsonBuilder(builder)
    is FastTravelPointModel -> putToJsonBuilder(builder)
    is GenerateEnergyModel -> putToJsonBuilder(builder)
    is HatchingEggModel -> putToJsonBuilder(builder)
    is ItemDropOnDamageModel -> putToJsonBuilder(builder)
    is ItemModelDynamicId -> putToJsonBuilder(builder)
    is ItemModelId -> putToJsonBuilder(builder)
    is PalEggModel -> putToJsonBuilder(builder)
    is PickupItemOnLevelModel -> putToJsonBuilder(builder)
    is ProductItemModel -> putToJsonBuilder(builder)
    is RecoverOtomoModel -> putToJsonBuilder(builder)
    is ShippingItemModel -> putToJsonBuilder(builder)
    is SignboardModel -> putToJsonBuilder(builder)
    is StateMachine -> putToJsonBuilder(builder)
    is TorchModel -> putToJsonBuilder(builder)
    is TreasureBoxModel -> putToJsonBuilder(builder)
}

private fun ItemDropOnDamageModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("dropItemInfos", buildJsonArray {
        dropItemInfos.fastForEach { add(it.toJsonElement()) }
    })
}

private fun ItemModelId.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("staticId", staticId)
    put("dynamicId", dynamicId.toJsonElement())
}

private fun ItemModelDynamicId.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("createdWorldId", createdWorldId)
    put("localIdInCreatedWorld", localIdInCreatedWorld)
}

private fun HatchingEggModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("hatchedCharacterSaveParameter", buildJsonObject {
        hatchedCharacterSaveParameter.entries.forEach { (k, v) ->
            put(k, v.value.toJsonElement())
        }
    })
    put("unknownBytes", unknownBytes)
    put("hatchedCharacterGuid", hatchedCharacterGuid)
}

private fun GenerateEnergyModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("storedEnergyAmount", storedEnergyAmount)
}

private fun FastTravelPointModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("locationInstanceId", locationInstanceId)
}

private fun FarmBlockV2Model.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("cropDataId", cropDataId)
    put("currentState", currentState)
    put("cropProgressRateValue", cropProgressRateValue)
    put("waterStackRateValue", waterStackRateValue)
    stateMachine?.let {
        put("stateMachine", stateMachine.toJsonElement())
    }
}

private fun TreasureBoxModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("treasureGradeType", treasureGradeType)
}

private fun TorchModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("extinctionDateTime", extinctionDateTime)
}

private fun StateMachine.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("growUpRequiredTime", growUpRequiredTime)
    put("growUpProgressTime", growUpProgressTime)
}

private fun SignboardModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("signboardText", signboardText)
}

private fun ShippingItemModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("shippingHours", buildJsonArray {
        shippingHours.fastForEach { add(it) }
    })
}

private fun RecoverOtomoModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("recoverAmountBySec", recoverAmountBySec)
}

private fun ProductItemModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("workSpeedAdditionalRate", workSpeedAdditionalRate)
    put("productItemId", productItemId)
}

private fun PickupItemOnLevelModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("autoPickedUp", autoPickedUp)
}

private fun PalEggModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("unknownBytes", unknownBytes)
}

private fun BaseCampPoint.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("baseCampId", baseCampId)
}

private fun BreedFarmModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("spawnedEggInstanceIds", buildJsonArray {
        spawnedEggInstanceIds.fastForEach { add(it) }
    })
}

private fun ConvertItemModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("currentRecipeId", currentRecipeId)
    put("remainProductNum", remainProductNum)
    put("requestedProductNum", requestedProductNum)
    put("workSpeedAdditionalRate", workSpeedAdditionalRate)
}

private fun DeathDroppedCharacterModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("storedParameterId", storedParameterId)
    put("ownerPlayerId", ownerPlayerId)
}

private fun DeathPenaltyStorageModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("ownerPlayerUid", ownerPlayerUid)
}

private fun DefenseBulletLauncherModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("remainingBullets", remainingBullets)
    put("magazineSize", magazineSize)
    put("bulletItemName", bulletItemName)
}

private fun DropItemModel.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("autoPickedUp", autoPickedUp)
    put("itemId", itemId.toJsonElement())
}

private fun ItemModelId.toJsonElement() = buildJsonObject {
    put("staticId", staticId)
    put("dynamicId", dynamicId.toJsonElement())
}

private fun ItemModelDynamicId.toJsonElement() = buildJsonObject {
    put("createdWorldId", createdWorldId)
    put("localIdInCreatedWorld", localIdInCreatedWorld)
}

private fun BuildProcessDict.toJsonElement() = when(this) {
    is BuildProcessData -> toJsonElement()
}

private fun BuildProcessData.toJsonElement() = buildJsonObject {
    put("state", state)
    put("id", id)
}

private fun ConnectorDict.toJsonElement() = when(this) {
    is ConnectorData -> toJsonElement()
    is ConnectorRawData -> buildJsonObject {
        put("values", buildJsonArray { values.fastForEach { add(it.toInt()) } })
    }
}

private fun ConnectorData.toJsonElement() = buildJsonObject {
    put("supportedLevel", supportedLevel)
    put("connect", connect.toJsonElement())
    otherConnectors?.let {
        put("otherConnectors", buildJsonArray {
            otherConnectors.fastForEach { add(it.toJsonElement()) }
        })
    }
}

private fun ConnectorOtherConnector.toJsonElement() = buildJsonObject {
    put("index", index)
    put("connect", buildJsonArray {
        connect.fastForEach { add(it.toJsonElement()) }
    })
}

private fun ConnectorConnect.toJsonElement() = buildJsonObject {
    put("index", index)
    put("anyPlace", buildJsonArray {
        anyPlace.fastForEach { add(it.toJsonElement()) }
    })
}

private fun ConnectorConnectInfo.toJsonElement() = buildJsonObject {
    put("connectToModelInstanceId", connectToModelInstanceId)
    put("index", index)
}

private fun MapModelDict.toJsonElement() = when(this) {
    is MapModelData -> toJsonElement()
    is MapModelHpData -> toJsonElement()
    is StageInstanceIdOwner -> toJsonElement()
}

private fun MapModelData.toJsonElement() = buildJsonObject {
    put("instanceId", instanceId)
    put("concreteModelInstanceId", concreteModelInstanceId)
    put("baseCampIdBelongTo", baseCampIdBelongTo)
    put("groupIdBelongTo", groupIdBelongTo)
    put("hp", hp.toJsonElement())
    put("initialTransformCache", initialTransformCache.toJsonElement())
    put("repairWorkId", repairWorkId)
    put("ownerSpawnerLevelObjectInstanceId", ownerSpawnerLevelObjectInstanceId)
    put("ownerInstanceId", ownerInstanceId)
    put("buildPlayerUid", buildPlayerUid)
    put("interactRestrictType", interactRestrictType)
    put("stageInstanceIdBelongTo", stageInstanceIdBelongTo.toJsonElement())
    put("createdAt", createdAt)
}

private fun MapModelHpData.toJsonElement() = buildJsonObject {
    put("current", current)
    put("max", max)
}

private fun StageInstanceIdOwner.toJsonElement() = buildJsonObject {
    put("id", id)
    put("valid", valid)
}

private fun CharacterDict.toJsonElement() = when(this) {
    is GvasCharacterData -> toJsonElement()
}

private fun WorkSaveDataDict.toJsonElement() = when(this) {
    is WorkSaveDataClass -> toJsonElement()
    is WorkSaveDataRaw -> toJsonElement()
    is WorkSaveWorkAssignData -> toJsonElement()
}

private fun WorkSaveWorkAssignData.toJsonElement() = buildJsonObject {
    put("id", id)
    put("locationIndex", locationIndex)
    put("assignType", assignType)
    put("assignedIndividualId", assignedIndividualId.toJsonElement())
    put("state", state)
    put("fixed", fixed)
}

private fun WorkSaveDataRaw.toJsonElement() = buildJsonArray { values.fastForEach { add(it.toInt()) } }

private fun WorkSaveDataClass.toJsonElement() = buildJsonObject {
    workableData?.let {
        workableData.putToJsonBuilder(this)
    }
    put("transform", transform.toJsonElement())
}

private fun WorkSaveDataTransform.toJsonElement() = buildJsonObject {
    put("type", transformType)
    put("v2", v2)
    data.putToJsonBuilder(this)
}

private fun WorkSaveDataTransformData.toJsonElement() = when(this) {
    is WorkSaveDataTransformRawData -> toJsonElement()
    is WorkSaveDataTransformType1 -> toJsonElement()
    is WorkSaveDataTransformType2 -> toJsonElement()
    is WorkSaveDataTransformType3 -> toJsonElement()
}

private fun WorkSaveDataTransformData.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is WorkSaveDataTransformRawData -> putToJsonBuilder(builder)
    is WorkSaveDataTransformType1 -> putToJsonBuilder(builder)
    is WorkSaveDataTransformType2 -> putToJsonBuilder(builder)
    is WorkSaveDataTransformType3 -> putToJsonBuilder(builder)
}

private fun WorkSaveDataTransformRawData.toJsonElement() = buildJsonArray { rawData.forEach { add(it.toInt()) }}
private fun WorkSaveDataTransformRawData.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("rawData", buildJsonArray { rawData.forEach { add(it.toInt()) }})
}

private fun WorkSaveDataTransformType1.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveDataTransformType1.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("rotation", rotation.toJsonElement())
    put("translation", translation.toJsonElement())
    put("scale3D", scale3D.toJsonElement())
}

private fun WorkSaveDataTransformType2.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveDataTransformType2.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("mapObjectInstanceId", mapObjectInstanceId)
}

private fun WorkSaveDataTransformType3.toJsonElement() = buildJsonObject {
    put("guid", guid)
    put("instanceId", instanceId)
}

private fun WorkSaveDataTransformType3.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("guid", guid)
    put("instanceId", instanceId)
}

private fun WorkSaveDataWorkableData.toJsonElement() = when(this) {
    is WorkSaveBaseWorkableDefense -> toJsonElement()
    is WorkSaveBaseWorkableProgress -> toJsonElement()
    is WorkSaveBaseWorkableReviveCharacter -> toJsonElement()
    is WorkSaveWorkableAssign -> toJsonElement()
    is WorkSaveWorkableBase -> toJsonElement()
    is WorkSaveWorkableLevelObject -> toJsonElement()
}

private fun WorkSaveDataWorkableData.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is WorkSaveBaseWorkableDefense -> putToJsonBuilder(builder)
    is WorkSaveBaseWorkableProgress -> putToJsonBuilder(builder)
    is WorkSaveBaseWorkableReviveCharacter -> putToJsonBuilder(builder)
    is WorkSaveWorkableAssign -> putToJsonBuilder(builder)
    is WorkSaveWorkableBase -> putToJsonBuilder(builder)
    is WorkSaveWorkableLevelObject -> putToJsonBuilder(builder)
}

private fun WorkSaveWorkableLevelObject.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveWorkableLevelObject.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("assign", assign.toJsonElement())
    put("targetMapObjectModelId", targetMapObjectModelId)
}

private fun WorkSaveBaseWorkableDefense.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveBaseWorkableDefense.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("defenseCombatType", defenseCombatType)
}

private fun WorkSaveBaseWorkableProgress.toJsonElement() = buildJsonObject {
    put("requiredWorkAmount", requiredWorkAmount)
    put("workExp", workExp)
    put("currentWorkAmount", currentWorkAmount)
    put("autoWorkSelfAmountBySec", autoWorkSelfAmountBySec)
}

private fun WorkSaveBaseWorkableProgress.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("requiredWorkAmount", requiredWorkAmount)
    put("workExp", workExp)
    put("currentWorkAmount", currentWorkAmount)
    put("autoWorkSelfAmountBySec", autoWorkSelfAmountBySec)
}

private fun WorkSaveBaseWorkableReviveCharacter.toJsonElement() = buildJsonObject {
    put("targetIndividualId", targetIndividualId.toJsonElement())
}

private fun WorkSaveBaseWorkableReviveCharacter.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("targetIndividualId", targetIndividualId.toJsonElement())
}

private fun TargetIndividualId.toJsonElement() = buildJsonObject {
    put("playerUid", playerUid)
    put("instanceId", instanceId)
}

private fun WorkSaveWorkableAssign.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveWorkableAssign.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("handleId", handleId)
    put("locationIndex", locationIndex)
    put("assignType", assignType)
    put("assignedIndividualId", assignedIndividualId.toJsonElement())
    put("state", state)
    put("fixed", fixed)
}

private fun WorkSaveWorkableBase.toJsonElement() = buildJsonObject {
    putToJsonBuilder(this)
}

private fun WorkSaveWorkableBase.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("id", id)
    put("workableBounds", workableBounds.toJsonElement())
    put("baseCampIdBelongTo", baseCampIdBelongTo)
    put("ownerMapObjectModelId", ownerMapObjectModelId)
    put("ownerMapObjectConcreteModelId", ownerMapObjectConcreteModelId)
    put("currentState", currentState)
    put("assignLocations", buildJsonArray {
        assignLocations.fastForEach { add(it.toJsonElement()) }
    })
    put("behaviorType", behaviorType)
    put("assignDefineDataId", assignDefineDataId)
    put("overrideWorkType", overrideWorkType)
    put("assignableFixedType", assignableFixedType)
    put("assignableOtomo", assignableOtomo)
    put("canTriggerWorkerEvent", canTriggerWorkerEvent)
    put("canStealAssign", canStealAssign)
    workableData?.let {
        workableData.putToJsonBuilder(this)
    }
}

private fun WorkSaveBaseWorkableData.toJsonElement() = when(this) {
    is WorkSaveBaseWorkableDefense -> toJsonElement()
    is WorkSaveBaseWorkableProgress -> toJsonElement()
    is WorkSaveBaseWorkableReviveCharacter -> toJsonElement()
}

private fun WorkSaveBaseWorkableData.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is WorkSaveBaseWorkableDefense -> putToJsonBuilder(builder)
    is WorkSaveBaseWorkableProgress -> putToJsonBuilder(builder)
    is WorkSaveBaseWorkableReviveCharacter -> putToJsonBuilder(builder)
}



private fun WorkSaveBaseWorkableBounds.toJsonElement() = buildJsonObject {
    put("location", location.toJsonElement())
    put("rotation", rotation.toJsonElement())
    put("boxSphereBounds", boxSphereBounds.toJsonElement())
}

private fun WorkSaveBaseBoxSphereBounds.toJsonElement() = buildJsonObject {
    put("origin", origin.toJsonElement())
    put("boxExtent", boxExtent.toJsonElement())
    put("sphereRadius", sphereRadius)
}

private fun WorkSaveBaseAssignLocation.toJsonElement() = buildJsonObject {
    put("location", location.toJsonElement())
    put("facingDirection", facingDirection.toJsonElement())
}

private fun GvasGroupDict.toJsonElement() = when (this) {
    is GvasGroupData -> toJsonElement()
}

private fun CustomRawData.toJsonElement(): JsonElement {
    return when(this) {
        is CustomByteArrayRawData -> toJsonElement()
    }
}

private fun CustomByteArrayRawData.toJsonElement() = buildJsonObject {
    value.toJsonElement().castOrNull<JsonObject>()?.entries?.forEach { (k, v) ->
        put(k, v)
    }
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
    data.putToJsonBuilder(this)
}

private fun DynamicItemSaveDataId.toJsonElement() = buildJsonObject {
    put("createdWorldId", createdWorldId)
    put("localIdInCreatedWorld", localIdInCreatedWorld)
    put("staticId", staticId)
}

private fun DynamicItemItemDict.toJsonElement() = when(this) {
    is ArmorDynamicItem -> toJsonElement()
    is EggDynamicItem -> toJsonElement()
    is RawDynamicItem -> toJsonElement()
    is WeaponDynamicItem -> toJsonElement()
}

private fun DynamicItemItemDict.putToJsonBuilder(builder: JsonObjectBuilder) = when(this) {
    is ArmorDynamicItem -> putToJsonBuilder(builder)
    is EggDynamicItem -> putToJsonBuilder(builder)
    is RawDynamicItem -> putToJsonBuilder(builder)
    is WeaponDynamicItem -> putToJsonBuilder(builder)
}

private fun ArmorDynamicItem.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("type", type)
    put("durability", durability)
}

private fun ArmorDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("durability", durability)
}

private fun EggDynamicItem.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("type", type)
    put("characterId", characterId)
    put("object", `object`.toJsonMap())
    put("unknownBytes", unknownBytes.toJsonIntArray())
    put("unknownId", unknownId)
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

private fun RawDynamicItem.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
    put("type", type)
    put("trailer", trailer.toJsonIntArray())
}

private fun WeaponDynamicItem.toJsonElement() = buildJsonObject {
    put("type", type)
    put("durability", durability)
    put("remainingBullets", remainingBullets)
    put("passiveSkillList", passiveSkillList.toJsonStringArray())
}

private fun WeaponDynamicItem.putToJsonBuilder(builder: JsonObjectBuilder) = builder.apply {
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
    put("location", location.toJsonElement())
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