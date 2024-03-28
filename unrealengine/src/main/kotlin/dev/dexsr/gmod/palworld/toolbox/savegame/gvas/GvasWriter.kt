package dev.dexsr.gmod.palworld.toolbox.savegame.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.CustomGvasDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GVAS_PROPERTY_CODEC
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasAnyArrayPropertyValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasArrayDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasArrayPropertyValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasBoolDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasByteArrayValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasCustomProperty
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasDateTime
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasEnumDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFixedPoint64Dict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFloatDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasGUID
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasInt64Dict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasIntDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasLinearColor
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasMapDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasMapStruct
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasNameDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasProperty
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasQuat
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasStrDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasStringArrayValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasStruct
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasStructArrayPropertyValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasStructDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasTransformedArrayValue
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasVector
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasTypedArray
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.CustomByteArrayRawData
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.CustomRawData
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import dev.dexsr.gmod.palworld.trainer.ue.util.fastForEach
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

// TODO: write metrics on hotspot call, then inline the method
class GvasWriter(
    val customPropertyCodecs: Map<String, GVAS_PROPERTY_CODEC>,
    val output: OutputStream,
) {

    var pos = 0
        private set

    fun writeBytes(bytes: ByteArray) {
        output.write(bytes)
        pos += bytes.size
    }

    fun writeUShort(uShort: UShort) {
        writeUShort(uShort.toInt())
    }

    fun writeInt(int: Int) {
        writeBytes(int.bytesArrayLE())
    }

    fun writeUInt(uInt: UInt) {
        writeUInt(uInt.toLong())
    }

    fun writeStr(str: String): Int {
        val pos = this.pos
        if (str.isEmpty()) {
            output.write(0)
        } else if (str.isCharsCodeInAscii()) {
            val bytes = str.toByteArray(Charsets.US_ASCII)
            writeInt(bytes.size + 1)
            writeBytes(bytes)
            writeBytes(byteArrayOf(0x00))
        } else {
            val bytes = str.toByteArray(Charsets.UTF_16LE)
            check(bytes.size % 2 == 0)
            val len = -(bytes.size.div(2f).toInt() + 1)
            writeInt(len)
            writeBytes(bytes)
            writeBytes(byteArrayOf(0x00, 0x00))
        }
        return this.pos - pos
    }

    private fun writeUInt(long: Long) {
        writeBytes(
            byteArrayOf(
                (long ushr 0).toByte(),
                (long ushr 8).toByte(),
                (long ushr 16).toByte(),
                (long ushr 24).toByte(),
            )
        )
    }

    private fun writeUShort(int: Int) {

        writeBytes(
            byteArrayOf(
                (int ushr 0).toByte(),
                (int ushr 8).toByte(),
            )
        )
    }

    fun <T> writeList(
        list: List<T>,
        mapper: (GvasWriter, T) -> Unit
    ) {
        writeUInt(list.size.toUInt())
        list.fastForEach { e -> mapper.invoke(this, e) }
    }

    fun writeUId(
        uid: String
    ) {
        val b = uid.map { it.code.toByte() }
        val bytes = byteArrayOf(
            b[0x3],
            b[0x2],
            b[0x1],
            b[0x0],
            b[0x7],
            b[0x6],
            b[0x5],
            b[0x4],
            b[0xB],
            b[0xA],
            b[0x9],
            b[0x8],
            b[0xF],
            b[0xE],
            b[0xD],
            b[0xC],
        )
        writeBytes(bytes)
    }

    fun writeProperty(
        property: GvasProperty
    ) {
        writeStr(property.type)
        val buffer = ByteArrayOutputStream()
        val writer = GvasWriter(customPropertyCodecs, buffer)
        val type = property.type
        val size = writer.writePropertyInner(type, property)
        val bytes = buffer.toByteArray()
        writeULong(bytes.size.toULong())
        writeBytes(bytes)
    }

    fun writePropertyInner(
        type: String,
        property: GvasProperty
    ): Int {
        val size: Int?
        val dict = property.value
        if (dict is GvasCustomProperty) {
            TODO()
        } else if (dict is CustomGvasDict) {
            val name = dict
                .cast<CustomRawData>()
                .cast<CustomByteArrayRawData>()
                .customType
            val codec = requireNotNull(customPropertyCodecs[name]) {
                "Unknown custom property type $name"
            }
            size = codec.second.invoke(this, type, property)
        } else if (type == "StructProperty") {
            size = writeStructDict(dict.cast())
        } else if (type == "IntProperty") {
            val cast = dict.cast<GvasIntDict>()
            writeOptionalUid(cast.id)
            writeInt(cast.value)
            size = 4
        } else if (type == "Int64Property") {
            val cast = dict.cast<GvasInt64Dict>()
            writeOptionalUid(cast.id)
            writeLong(cast.value)
            size = 8
        } else if (type == "FixedPoint64Property") {
            val cast = dict.cast<GvasFixedPoint64Dict>()
            writeOptionalUid(cast.id)
            writeInt(cast.value)
            size = 4
        } else if (type == "FloatProperty") {
            val cast = dict.cast<GvasFloatDict>()
            writeOptionalUid(cast.id)
            writeFloat(cast.value)
            size = 4
        } else if (type == "StrProperty") {
            val cast = dict.cast<GvasStrDict>()
            writeOptionalUid(cast.id)
            size = writeStr(cast.value)
        } else if (type == "NameProperty") {
            val cast = dict.cast<GvasNameDict>()
            writeOptionalUid(cast.id)
            size = writeStr(cast.value)
        } else if (type == "EnumProperty") {
            val cast = dict.cast<GvasEnumDict>()
            writeStr(cast.enumValue.type)
            writeOptionalUid(cast.id)
            size = writeStr(cast.enumValue.value)
        } else if (type == "BoolProperty") {
            val cast = dict.cast<GvasBoolDict>()
            writeBool(cast.value)
            writeOptionalUid(cast.id)
            size = 0
        } else if (type == "ArrayProperty") {
            val cast = dict.cast<GvasArrayDict>()
            writeStr(cast.arrayType)
            writeOptionalUid(cast.id)

            val buffer = ByteArrayOutputStream()
            val writer = GvasWriter(customPropertyCodecs, buffer)
            writer.writeArrayProperty(cast.arrayType, cast.value)
            val bytes = buffer.toByteArray()
            size = bytes.size
            writeBytes(bytes)
        } else if (type == "MapProperty") {
            val cast = dict.cast<GvasMapDict>()
            writeStr(cast.keyType)
            writeStr(cast.valueType)
            writeOptionalUid(cast.id)

            val buffer = ByteArrayOutputStream()
            val writer = GvasWriter(customPropertyCodecs, buffer)
            writer.writeUInt(0L)
            writer.writeUInt(cast.value.size.toLong())

            cast.value.forEach { map ->
                writer.writePropValue(
                    typeName = cast.keyType,
                    structTypeName = cast.keyStructType,
                    value = requireNotNull(map["key"])
                )

                writer.writePropValue(
                    typeName = cast.valueType,
                    structTypeName = cast.valueStructType,
                    value = requireNotNull(map["value"])
                )
            }

            val bytes = buffer.toByteArray()
            size = bytes.size
            writeBytes(bytes)
        } else {
            throw IllegalArgumentException("Unknown property type: $property")
        }
        return size
    }

    fun writePropValue(
        typeName: String,
        structTypeName: String?,
        value: Any
    ) {
        when(typeName) {
            "StructProperty" -> {
                writeStructValue(requireNotNull(structTypeName), value.cast())
            }
            "EnumProperty" -> {
                writeStr(value.cast())
            }
            "NameProperty" -> {
                writeStr(value.cast())
            }
            "IntProperty" -> {
                writeInt(value.cast())
            }
            "BoolProperty" -> {
                writeBool(value.cast())
            }
            else -> throw IllegalArgumentException("Unknown property value type: $typeName")
        }

    }

    fun writeStructDict(
        struct: GvasStructDict
    ): Int {
        writeStr(struct.structType)
        writeUId(struct.structId)
        writeOptionalUid(struct.id)
        val start = pos
        writeStructValue(struct.structType, struct.value)
        return this.pos - start
    }

    fun writeStructValue(
        type: String,
        value: GvasStruct
    ) {
        when(type) {
            "Vector" -> writeVectorDict(value.cast())
            "DateTime" -> writeDateTimeDict(value.cast())
            "Guid" -> writeGuidDict(value.cast())
            "Quat" -> writeQuatDict(value.cast())
            "LinearColor" -> writeLinearColor(value.cast())
            else -> writeGvasMapStruct(value.cast())
        }
    }

    fun writeVectorDict(
        vector: GvasVector
    ) {
        writeDouble(vector.x ?: Double.NaN)
        writeDouble(vector.y ?: Double.NaN)
        writeDouble(vector.z ?: Double.NaN)
    }

    fun writeDateTimeDict(
        dateTime: GvasDateTime
    ) {
        writeLong(dateTime.v)
    }

    fun writeGuidDict(
        guid: GvasGUID
    ) {
        writeUId(guid.v)
    }

    fun writeQuatDict(
        quat: GvasQuat
    ) {
        writeDouble(quat.x ?: Double.NaN)
        writeDouble(quat.y ?: Double.NaN)
        writeDouble(quat.z ?: Double.NaN)
        writeDouble(quat.w ?: Double.NaN)
    }

    fun writeLinearColor(
        linearColor: GvasLinearColor
    ) {
        writeFloat(linearColor.r ?: Float.NaN)
        writeFloat(linearColor.g ?: Float.NaN)
        writeFloat(linearColor.b ?: Float.NaN)
        writeFloat(linearColor.a ?: Float.NaN)
    }

    fun writeGvasMapStruct(
        mapStruct: GvasMapStruct
    ) {
        writeGvasPropertyMap(mapStruct.v)
    }

    fun writeGvasPropertyMap(
        gvasMap: Map<String, GvasProperty>
    ) {
        gvasMap.entries.forEach { (k, v) ->
            writeStr(k)
            writeProperty(v)
        }
        writeStr("None")
    }

    fun writeArrayProperty(
        arrayType: String,
        value: GvasArrayPropertyValue
    ) {
        val sealed = value
        when(sealed) {
            is GvasAnyArrayPropertyValue -> when(sealed.values) {
                is GvasByteArrayValue -> {
                    val len = sealed.values.value.size
                    writeUInt(len.toLong())
                    writeArrayValue(sealed.values.typeName, len, sealed.values.value)
                }
                is GvasStringArrayValue -> {
                    val len = sealed.values.value.size
                    writeUInt(len.toLong())
                    writeArrayValue(sealed.values.typeName, sealed.values.value.size, sealed.values.value)
                }
                is OpenGvasTypedArray -> TODO("writer noImpl for OpenGvasTypedArray:${sealed.values::class.simpleName}")
            }
            is GvasStructArrayPropertyValue -> {
                val len = sealed.values.size
                writeUInt(len.toLong())
                writeStr(sealed.propName)
                writeStr(sealed.propType)

                val buffer = ByteArrayOutputStream()
                val writer = GvasWriter(customPropertyCodecs, buffer)
                repeat(len) {i ->
                    writer.writeStructValue(sealed.typeName, sealed.values[i])
                }
                val bytes = buffer.toByteArray()
                writeLong(bytes.size.toLong())
                writeStr(sealed.typeName)
                writeUId(sealed.id)
                writeBytes(byteArrayOf(0x0))
                writeBytes(bytes)
            }
            is GvasTransformedArrayValue -> TODO()
        }
    }

    fun writeArrayValue(
        arrayType: String,
        count: Int,
        values: Array<String>
    ) {
        if (arrayType == "NameProperty" || arrayType == "EnumProperty") {
            writeArrayValue(count) { i ->
                writeStr(values[i])
            }
        } else throw IllegalArgumentException("Unknown array type: $arrayType")
    }

    fun writeArrayValue(
        arrayType: String,
        count: Int,
        values: ByteArray
    ) {
        if (arrayType == "ByteProperty") {
            require(count == values.size)
            writeBytes(values)
        } else throw IllegalArgumentException("Unknown array type: $arrayType")
    }

    private inline fun writeArrayValue(count: Int, writer: (Int) -> Unit) {
        repeat(count) { i -> writer.invoke(i) }
    }

    fun writeOptionalUid(
        uid: String?
    ) {
        writeBool(uid != null)
        uid?.let(::writeUId)
    }

    fun writeBool(bool: Boolean) = writeBytes(byteArrayOf(if (bool) 0x1 else 0x0))

    fun writeDouble(double: Double) {
        val bytes = ByteBuffer.allocate(Double.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            .putDouble(double)
            .array()
        writeBytes(bytes)
    }

    fun writeFloat(float: Float) {
        val bytes = ByteBuffer.allocate(Float.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            .putFloat(float)
            .array()
        writeBytes(bytes)
    }

    fun writeLong(long: Long) {
        val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            .putLong(long)
            .array()
        writeBytes(bytes)
    }

    fun writeULong(uLong: ULong) {
        writeBytes(
            byteArrayOf(
                (uLong shr 0).toByte(),
                (uLong shr 8).toByte(),
                (uLong shr 16).toByte(),
                (uLong shr 24).toByte(),
                (uLong shr 32).toByte(),
                (uLong shr 40).toByte(),
                (uLong shr 48).toByte(),
                (uLong shr 56).toByte(),
            )
        )
    }
}

private fun String.isCharsCodeInAscii(): Boolean {
    fastForEach { c ->
        if (c.code !in 0..127) return false
    }
    return true
}

private fun String.isCharsCodeInPrintableAscii(): Boolean {
    fastForEach { c ->
        if (c.code !in 32..127) return false
    }
    return true
}

private fun Int.bytesArray(order: ByteOrder): ByteArray {
    return ByteBuffer.allocate(Int.SIZE_BYTES).order(order).putInt(this).array()
}

private fun Int.bytesArrayLE() = bytesArray(ByteOrder.LITTLE_ENDIAN)

private fun Int.unsignedToLong() = toLong() and 0xFFFFFFFFL