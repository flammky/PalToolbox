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
        is OpenGvasTypedArray -> TODO()
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun GvasByteArrayValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("typeName", typeName)
        put("value", "")
    }
}

private fun GvasStringArrayValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("typeName", typeName)
        put("value", JsonArray(value.map { JsonPrimitive(it) }))
    }
}

private fun GvasStructArrayPropertyValue.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("prop_name", propName)
        put("prop_type", propType)
        put("values", buildJsonArray {
            values.fastForEach { struct ->
                when (struct) {
                    is GvasDateTime -> add(struct.v)
                    is GvasGUID -> add(struct.v)
                    is GvasLinearColor -> add(struct.toJsonElement())
                    is GvasQuat -> add(struct.toJsonElement())
                    is GvasStructMap -> add(struct.toJsonElement())
                    is GvasVector -> add(struct.toJsonElement())
                }
            }
        })
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
        put("type", GvasIntDict.TYPE_NAME)
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
    }
}

private fun GvasVector.toJsonElement(): JsonElement {
    return buildJsonObject {
        put("x", x)
        put("y", y)
        put("z", z)
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

private fun GvasDict.toJsonElement(): JsonElement {
    return when (this) {
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
        is OpenGvasDict -> {
            when (this) {
                is GvasGroupDict -> toJsonElement()
                is GvasCharacterData -> toJsonElement()
                else -> TODO()
            }
        }
    }
}