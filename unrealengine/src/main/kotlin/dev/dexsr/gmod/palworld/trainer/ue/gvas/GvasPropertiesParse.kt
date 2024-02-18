package dev.dexsr.gmod.palworld.trainer.ue.gvas

import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.UUID

// TODO: complete this

fun ParseGvasProperties(buf: ByteBuffer): Result<GvasFileProperties> {
    return runCatching {
        GvasFileProperties(readGvasProperties(buf.order(ByteOrder.LITTLE_ENDIAN)))
    }.onFailure {
        it.printStackTrace()
    }
}

private fun readGvasStr(
    buf: ByteBuffer
): String {
    val size = buf.getInt()
    if (size == 0) return ""
    val (arr: ByteArray, encoding: Charset) = run {
        if (size < 0) {
            val sizeAbs = -size
            val arr = ByteArray(sizeAbs * 2 - 2)
            buf.get(arr)
            repeat(2) { buf.get() }
            arr to Charsets.UTF_16LE
        } else {
            val arr = ByteArray(size - 1)
            buf.get(arr)
            repeat(1) { buf.get() }
            arr to Charsets.US_ASCII
        }
    }
    return try {
        String(arr, encoding)
    } catch (e: Exception) {
        e.printStackTrace()
        encoding.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .replaceWith("�")
            .decode(ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN))
            .toString()
    }
}

private fun readGvasBool(
    buf: ByteBuffer
): Boolean {
    return buf.get() > 0
}

// LinkedHashMap ?
private fun readGvasProperty(buf: ByteBuffer, typeName: String, size: Int, path: String, nestedCallerPath: String = ""): GvasProperty {

    val value: GvasDict = run {
        PALWORLD_CUSTOM_PROPERTY_CODEC[path]?.let { codec ->
            if (nestedCallerPath.isNotEmpty() && path != nestedCallerPath) return@let
            return@run codec.first.invoke(DefaultGvasReader(buf), typeName, size, nestedCallerPath)
        }
        when (typeName) {
            "StructProperty" -> readGvasStruct(buf, path)
            "IntProperty" -> readGvasIntProperty(buf)
            "Int64Property" -> readGvasLongProperty(buf)
            "FixedPoint64Property" -> readGvasFixedPoint64Property(buf)
            "FloatProperty" -> readGvasFloatProperty(buf)
            "StrProperty" -> readGvasStringProperty(buf)
            "NameProperty" -> readGvasNameProperty(buf)
            "EnumProperty" -> readGvasEnumProperty(buf)
            "BoolProperty" -> readGvasBooleanProperty(buf)
            "ArrayProperty" -> readGvasArrayProperty(buf, size - 4, path)
            "MapProperty" -> readGvasMapProperty(buf, path)
            else -> error("Unknown type: $typeName (${path})")
        }
    }
    return GvasProperty(
        type = typeName,
        value = value
    )
}

private fun readGvasStruct(buf: ByteBuffer, path: String): GvasStructDict {
    val structType = readGvasStr(buf)
    val structId = readGvasUUID(buf).toString()
    val _id = readGvasOptionalUUID(buf)?.toString()
    val value = readGvasStructValue(buf, structType, path)
    return GvasStructDict(
        structType,
        structId,
        _id,
        value
    )
}

private fun readGvasUUID(buf: ByteBuffer): UUID {
    val b = ByteArray(16)
        .apply { buf.get(this) }
        .map { it.toInt() and 0xFF }
    val f = "%08x-%04x-%04x-%04x-%04x%08x".format(
        (b[3] shl 24) or (b[2] shl 16) or (b[1] shl 8) or b[0],
        (b[7] shl 8) or b[6],
        (b[5] shl 8) or b[4],
        (b[11] shl 8) or b[10],
        (b[9] shl 8) or b[8],
        (b[15] shl 24) or (b[14] shl 16) or (b[13] shl 8) or b[12]
    )
    return UUID.fromString(f)
}

private fun readGvasOptionalUUID(buf: ByteBuffer): UUID? {
    if (buf.get() == 0.toByte()) return null
    return readGvasUUID(buf)
}

private fun readGvasStructValue(buf: ByteBuffer, structType: String, path: String = ""): GvasStruct {
    return when (structType) {
        "Vector" -> readGvasPropertyVectorDict(buf)
        "DateTime" -> GvasDateTime(buf.getLong())
        "Guid" -> GvasGUID(readGvasUUID(buf).toString())
        "Quat" -> readGvasQuat(buf)
        "LinearColor" -> readGvasLinearColor(buf)
        else -> GvasStructMap(readGvasProperties(buf, path))
    }
}

private fun readGvasPropertyVectorDict(buf: ByteBuffer): GvasVector {
    return GvasVector(
        readGvasPropertyDoubleOrNull(buf),
        readGvasPropertyDoubleOrNull(buf),
        readGvasPropertyDoubleOrNull(buf)
    )
}

private fun readGvasPropertyDoubleOrNull(buf: ByteBuffer): Float? {
    val v = buf.getDouble()
    if (v.isNaN() || v.isInfinite()) return null
    return v.toFloat()
}

private fun readGvasFloat(buf: ByteBuffer): Float? {
    val v = buf.getDouble()
    if (v.isNaN() || v.isInfinite()) return null
    return v.toFloat()
}

private fun readGvasQuat(buf: ByteBuffer): GvasQuat {
    return GvasQuat(
        x = readGvasPropertyDoubleOrNull(buf),
        y = readGvasPropertyDoubleOrNull(buf),
        z = readGvasPropertyDoubleOrNull(buf),
        w = readGvasPropertyDoubleOrNull(buf)
    )
}

private fun readGvasLinearColor(buf: ByteBuffer): GvasLinearColor {
    return GvasLinearColor(
        r = readGvasFloat(buf),
        g = readGvasFloat(buf),
        b = readGvasFloat(buf),
        a = readGvasFloat(buf)
    )
}

private fun readGvasProperties(buf: ByteBuffer, path: String = ""): GvasMap<String, GvasProperty> {
    val properties = GvasMap<String, GvasProperty>()

    while (true) {
        val name = readGvasStr(buf)
        if (name == "None") break
        val typeName = readGvasStr(buf)
        val size = buf.getLong()
        check(size <= Int.MAX_VALUE) {
            "property size was unexpectedly big ($size)"
        }
        properties[name] = readGvasProperty(buf, typeName, size.toInt(), "$path.${name}")
    }

    return properties
}

private fun readGvasIntProperty(buf: ByteBuffer): GvasIntDict {
    return GvasIntDict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getInt()
    )
}

private fun readGvasLongProperty(buf: ByteBuffer): GvasInt64Dict {
    return GvasInt64Dict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getLong()
    )
}

private fun readGvasFixedPoint64Property(buf: ByteBuffer): GvasFixedPoint64Dict {
    return GvasFixedPoint64Dict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getInt()
    )
}

private fun readGvasFloatProperty(buf: ByteBuffer): GvasFloatDict {
    return GvasFloatDict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getFloat()
    )
}

private fun readGvasStringProperty(buf: ByteBuffer): GvasStrDict {
    return GvasStrDict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = readGvasStr(buf)
    )
}

private fun readGvasNameProperty(buf: ByteBuffer): GvasNameDict {
    return GvasNameDict(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = readGvasStr(buf)
    )
}

private fun readGvasEnumProperty(buf: ByteBuffer): GvasEnumDict {
    val enumType = readGvasStr(buf)
    val _id = readGvasOptionalUUID(buf)?.toString()
    val enum_value = readGvasStr(buf)
    return GvasEnumDict(
        id = _id,
        value = GvasEnumDictValue(
            type = enumType,
            value = enum_value
        )
    )
}

private fun readGvasBooleanProperty(buf: ByteBuffer): GvasBoolDict {
    return GvasBoolDict(
        value = readGvasBool(buf),
        id = readGvasOptionalUUID(buf)?.toString()
    )
}

private fun readGvasArrayProperty(buf: ByteBuffer, size: Int, path: String): GvasArrayDict {
    val arrayType = readGvasStr(buf)
    return GvasArrayDict(
        arrayType = arrayType,
        id = readGvasOptionalUUID(buf)?.toString(),
        value = readGvasArrayPropertyValue(buf, arrayType, size, path)
    )
}

private fun readGvasByteList(buf: ByteBuffer, size: Int): ByteArray {
    val arr = ByteArray(size)
    buf.get(arr)
    return arr
}

private fun readGvasArrayPropertyValue(buf: ByteBuffer, arrayType: String, size: Int, path: String): GvasArrayPropertyValue {
    val count = buf.getInt()
    val value = when(arrayType) {
        "StructProperty" -> {
            val propName = readGvasStr(buf)
            val propType = readGvasStr(buf)
            buf.getLong()
            val typeName = readGvasStr(buf)
            val id = readGvasUUID(buf)
            buf.get()
            val values = Array(count) { i ->
                readGvasStructValue(buf, typeName, "$${path}.${propName}")
            }
            GvasStructArrayPropertyValue(
                propName,
                propType,
                values,
                typeName,
                id.toString()
            )
        }
        else -> {
            GvasAnyArrayPropertyValue(
                values = readGvasArrayValues(buf, arrayType, count, size, path)
            )
        }
    }
    return value
}

private fun readGvasArrayValues(
    buf: ByteBuffer,
    arrayType: String,
    count: Int,
    size: Int,
    path: String
): GvasTypedArray<*> {
    return when (arrayType) {
        "EnumProperty" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(count) { _ -> readGvasStr(buf) }
            )
        }
        "NameProperty" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(count) { _ -> readGvasStr(buf) }
            )
        }
        "Guid" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(count) { _ -> readGvasUUID(buf).toString() }
            )
        }
        "ByteProperty" -> {
            if (size == count) {
                return GvasByteArrayValue(
                    arrayType,
                    ByteArray(size).apply { buf.get(this) }
                )
            }
            error("Labelled ByteProperty not implemented")
        }
        else -> error("Unknown array type: ${arrayType} (${path})")
    }
}

private fun readGvasMapProperty(buf: ByteBuffer, path: String): GvasMapDict {
    val keyType = readGvasStr(buf)
    val valueType = readGvasStr(buf)
    val id = readGvasOptionalUUID(buf)?.toString()
    buf.getInt()
    val count = buf.getInt()
    val keyPath = "$path.Key"
    val keyStructType =
        if (keyType == "StructProperty") getTypeOr(buf, keyPath, "Guid")
        else "None"
    val valuePath = "$path.Value"
    val valueStructType =
        if (valueType == "StructProperty") getTypeOr(buf, valuePath, "StructProperty")
        else "None"
    val values = List<GvasMap<String, Any>>(count) { i ->
        GvasMap<String, Any>()
            .apply {
                put("key", readGvasPropValue(buf, keyType, keyStructType ?: "", keyPath))
                put("value", readGvasPropValue(buf, valueType, valueStructType ?: "", valuePath))
            }
    }
    return GvasMapDict(
        keyType,
        valueType,
        keyStructType,
        valueStructType,
        id,
        values
    )
}

private fun getTypeOr(buf: ByteBuffer, path: String, default: String): String {
    return PALWORLD_TYPE_HINT[path] ?: default
}

private fun readGvasPropValue(
    buf: ByteBuffer,
    typeName: String,
    structTypeName: String,
    path: String
): Any {
    return when (typeName) {
        "StructProperty" -> readGvasStructValue(buf, structTypeName, path)
        "EnumProperty" -> readGvasStr(buf)
        "NameProperty" -> readGvasStr(buf)
        "IntProperty" -> buf.getInt()
        "BoolProperty" -> readGvasBool(buf)
        else -> error("Unknown Property value type: $typeName ($path)")
    }
}

private fun DefaultGvasReader(
    buf: ByteBuffer
): GvasReader {
    return object : GvasReader(buf) {
        override fun properties(path: String): GvasMap<String, GvasProperty> {
            return readGvasProperties(buf, path)
        }

        override fun property(typeName: String, size: Int, path: String, nestedCallerPath: String): GvasProperty {
            return readGvasProperty(buf, typeName, size, path, nestedCallerPath)
        }

        override fun copy(buf: ByteBuffer): GvasReader {
            return DefaultGvasReader(buf)
        }

        override fun uuid(): UUID {
            return readGvasUUID(buf)
        }

        override fun uuidOrNull(): UUID? {
            return readGvasOptionalUUID(buf)
        }

        override fun fstring(): String {
            return readGvasStr(buf)
        }

        override fun readArray(arrayType: String, count: Int, size: Int, path: String): GvasTypedArray<*> {
            return readGvasArrayValues(buf, arrayType, count, size, path)
        }

        override fun <T> readArray(mapper: (GvasReader) -> T): ArrayList<T> {
            val size = buf.getInt()
            return List(size) { mapper.invoke(this) }.cast()
        }

        override fun readByte(): Byte {
            return buf.get()
        }

        override fun readInt(): Int {
            return buf.getInt()
        }

        override fun readLong(): Long {
            return buf.getLong()


        }

        override fun readBytes(count: Int): ByteArray {
            return ByteArray(count).apply { buf.get(this) }
        }

        override fun isEof(): Boolean {
            return !buf.hasRemaining()
        }
    }
}