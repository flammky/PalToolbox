package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DynamicItem

sealed class DynamicItemDict() : OpenGvasDict()


class DynamicItemSaveData(
    val id: DynamicItemSaveDataId,
    val data: DynamicItemItemDict
) : DynamicItemDict()
class DynamicItemSaveDataId(
    val createdWorldId: String,
    val localIdInCreatedWorld: String,
    val staticId: String
)

sealed class DynamicItemItemDict(
    val type: String
) : DynamicItemDict()

class EggDynamicItem(
    val characterId: String,
    val `object`: GvasMap<String, GvasProperty>,
    val unknownBytes: ByteArray,
    val unknownId: String
) : DynamicItemItemDict("egg")

class ArmorDynamicItem(
    val durability: Float
) : DynamicItemItemDict("armor")

class WeaponDynamicItem(
    val durability: Float,
    val remainingBullets: Int,
    val passiveSkillList: ArrayList<String>,
) : DynamicItemItemDict("weapon")

class RawDynamicItem(
    val trailer: ArrayList<Int>,
) : DynamicItemItemDict("unknown")

fun DynamicItem.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String,
) : GvasProperty {
    require(typeName == "ArrayProperty") {
        "Expected ArrayProperty, got $typeName"
    }
    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value
        .cast<GvasArrayDict>()
    val dataBytes = arrayDict.value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = ByteArrayRawData(
        customType = path,
        id = arrayDict.id,
        value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(decodeBytes(reader, dataBytes))
        )
    )
    return value
}

private fun DynamicItem.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
): DynamicItemSaveData? {
    if (bytes.isEmpty()) return null
    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val reader = parentReader.copy(buf)

    val id = DynamicItemSaveDataId(
        createdWorldId = reader.uuid().toString(),
        localIdInCreatedWorld = reader.uuid().toString(),
        staticId = reader.fstring()
    )
    tryReadEgg(reader)?.let { egg ->
        return DynamicItemSaveData(
            id = id,
            data = egg
        )
    }

    if (reader.remaining == 4) {
        val data = ArmorDynamicItem(
            durability = reader.readFloat()
        )
        check(reader.isEof()) {
            "EOF not reached"
        }
        return DynamicItemSaveData(
            id,
            data
        )
    }

    val pos = reader.position
    var eof: Boolean? = null
    val data = try {
        WeaponDynamicItem(
            durability = reader.readFloat(),
            remainingBullets = reader.readInt(),
            passiveSkillList = reader.readArray { it.fstring() }
        ).also { eof = reader.isEof() }
    } catch (ex: Exception) {
        reader.position(pos)
        val trailer = reader.readRemaining()

        RawDynamicItem(
            trailer = ArrayList<Int>()
                .apply { trailer.forEach { e -> add(e.toInt() and 0xFF) } }
        )
    }
    if (eof != true) {
        error("EOF not reached")
    }
    return DynamicItemSaveData(
        id,
        data
    )
}

private fun DynamicItem.tryReadEgg(reader: GvasReader): DynamicItemItemDict? {
    val pos = reader.position
    var eof: Boolean? = null
    val egg = try {
        EggDynamicItem(
            characterId = reader.fstring(),
            `object` = reader.properties(""),
            unknownBytes = reader.readBytes(4),
            unknownId = reader.uuid().toString(),
        ).also { eof = reader.isEof() }
    } catch (ex: Exception) {
        reader.position(pos)
        return null
    }
    if (eof == false) {
        error( "DynamicItem.tryReadEgg: EOF not reached")
    }
    return egg
}

fun DynamicItem.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}