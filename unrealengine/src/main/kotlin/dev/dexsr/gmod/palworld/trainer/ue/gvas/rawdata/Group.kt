package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import dev.dexsr.gmod.palworld.trainer.ue.util.fastForEach
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Group

class InstanceID(
    val guid: String,
    val instanceID: String
)

fun Group.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasDict {
    check(typeName == "MapProperty") {
        "Expected MapProperty, got $typeName"
    }
    val property = reader.property(typeName, size, path, path)
    val groupMap = property.value as? GvasMapDict
        ?: error("cannot cast typeName=MapProperty to GvasMapDict")
    groupMap.value.fastForEach { v ->
        val type = v["value"]!!
            .cast<GvasStructMap>().v["GroupType"]!!
            .cast<GvasProperty>().value
            .cast<GvasEnumDict>().value.value
        val bytes = v["value"]!!
            .cast<GvasStructMap>().v["RawData"]!!
            .cast<GvasProperty>().value
            .cast<GvasArrayDict>().value
            .cast<GvasAnyArrayPropertyValue>().values
            .cast<GvasByteArrayValue>().value
        v["value"]!!
            .cast<GvasStructMap>().v["RawData"] = GvasProperty(
                typeName,
                decodeBytes(reader, bytes, type)
            )
    }
    return groupMap
}

fun Group.encode(
    reader: GvasReader,
    typeName: String,
    map: GvasMap<String, Any>
): Int {
    TODO()
}

private fun Any.castToStructMap(): GvasStructMap = cast()

private fun Group.decodeBytes(parentReader: GvasReader, bytes: ByteArray, groupType: String): GvasGroupDict {
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val groupData = GvasGroupDict(
        groupType = groupType,
        groupId = reader.uuid().toString(),
        groupName = reader.fstring(),
        individual_character_handle_ids = reader.readArray { r -> InstanceID(
            guid = r.uuid().toString(),
            instanceID = r.uuid().toString()
        )},
        org = run {
            val types = listOf(
                "EPalGroupType::Guild",
                "EPalGroupType::IndependentGuild",
                "EPalGroupType::Organization"
            )
            if (groupType in types) GvasGroupOrgData(
                orgType = reader.readByte(),
                baseIds = reader.readArray { it.uuid().toString() }
            ) else null
        },
        guild = run {
            val types = listOf(
                "EPalGroupType::Guild", "EPalGroupType::IndependentGuild"
            )
            if (groupType in types) GvasGroupGuildData(
                baseCampLevel = reader.readInt(),
                mapObjectInstanceIdsBaseCampPoints = reader.readArray { it.uuid().toString() },
                guildName = reader.fstring()
            ) else null
        },
        independentGuildData = run {
            if (groupType == "EPalGroupType::IndependentGuild") GvasGroupIndependentGuildData(
                playerUid = reader.uuid().toString(),
                guildName2 = reader.fstring(),
                playerInfo = GvasGroupGuildPlayerInfo(
                    lastOnlineRealTime = reader.readLong(),
                    playerName = reader.fstring()
                )
            ) else null
        },
        guildPlayersData = run {
            if (groupType == "EPalGroupType::Guild") GvasGroupGuildPlayersData(
                adminPlayerUuid = reader.uuid().toString(),
                players = run {
                    List(reader.readInt()) {
                        GvasGroupGuildPlayer(
                            playerUid = reader.uuid().toString(),
                            playerInfo = GvasGroupGuildPlayerInfo(
                                lastOnlineRealTime = reader.readLong(),
                                playerName = reader.fstring()
                            )
                        )
                    }
                }
            ) else null
        },
    )

    check(reader.isEof()) {
        "EOF not reached while decoding Group bytes"
    }
    return groupData
}

class GvasGroupDict(
    val groupType: String,
    val groupId: String,
    val groupName: String,
    val individual_character_handle_ids: List<InstanceID>,
    val org: GvasGroupOrgData?,
    val guild: GvasGroupGuildData?,
    val independentGuildData: GvasGroupIndependentGuildData?,
    val guildPlayersData: GvasGroupGuildPlayersData?,
) : OpenGvasDict() {

}

class GvasGroupOrgData(
    val orgType: Byte,
    val baseIds: List<String>
)

class GvasGroupGuildData(
    val baseCampLevel: Int,
    val mapObjectInstanceIdsBaseCampPoints: List<String>,
    val guildName: String
)

class GvasGroupGuildPlayersData(
    val adminPlayerUuid: String,
    val players: List<GvasGroupGuildPlayer>
)

class GvasGroupIndependentGuildData(
    val playerUid: String,
    val guildName2: String,
    val playerInfo: GvasGroupGuildPlayerInfo
)

class GvasGroupGuildPlayer(
    val playerUid: String,
    val playerInfo: GvasGroupGuildPlayerInfo
)

class GvasGroupGuildPlayerInfo(
    val lastOnlineRealTime: Long,
    val playerName: String
)