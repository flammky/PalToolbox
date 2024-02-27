package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WorkCollection

sealed class WorkCollectionDict() : OpenGvasDict()


class WorkCollectionData(
    val id: String,
    val workIds: ArrayList<String>
) : WorkCollectionDict()

fun WorkCollection.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
) : GvasProperty {

    require(typeName == "ArrayProperty") {
        "Expected ArrayProperty, got=$typeName"
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

private fun WorkCollection.decodeBytes(
    parentReader: GvasReader,
    dataBytes: ByteArray
): WorkCollectionData {

    val reader = parentReader.copy(ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = WorkCollectionData(
        id = reader.uuid().toString(),
        workIds = reader.readArray { reader.uuid().toString() }
    )
    
    check(reader.isEof()) {
        "EOF not reached"
    }
    
    return data
}

fun WorkCollection.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}