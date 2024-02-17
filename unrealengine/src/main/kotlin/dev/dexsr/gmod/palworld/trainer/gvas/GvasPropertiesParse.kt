package dev.dexsr.gmod.palworld.trainer.gvas

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.UUID

fun ParseGvasProperties(buf: ByteBuffer): Result<GvasMap<String, Any>> {
    return runCatching {
        readGvasProperties(buf.order(ByteOrder.LITTLE_ENDIAN))
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
            val data = ByteArray(size - 1)
            buf.get(data)
            repeat(1) { buf.get() }
            data to Charsets.US_ASCII
        }
    }
    return try {
        String(arr, encoding)
    } catch (e: Exception) {
        encoding.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .replaceWith("�")
            .decode(ByteBuffer.wrap(arr))
            .toString()
    }
}

private fun readGvasBool(
    buf: ByteBuffer
): Boolean {
    return buf.get() > 0
}

// LinkedHashMap ?
private fun readGvasProperty(buf: ByteBuffer, typeName: String, size: Int, path: String): GvasProperty {

    val value: Any = when (typeName) {
        "StructProperty" -> readGvasStruct(buf)
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
    return GvasProperty(
        type = typeName,
        value = value
    )
}

private fun readGvasStruct(buf: ByteBuffer): GvasStruct {
    val structType = readGvasStr(buf)
    val structId = readGvasUUID(buf).toString()
    val _id = readGvasOptionalUUID(buf)?.toString()
    val value = readGvasStructValue(buf, structType)
    return GvasStruct(
        structType,
        structId,
        _id,
        value
    )
}

private fun readGvasUUID(buf: ByteBuffer): UUID {
    return UUID(/* mostSigBits = */ buf.getLong(), /* leastSigBits = */ buf.getLong())
}

private fun readGvasOptionalUUID(buf: ByteBuffer): UUID? {
    if (buf.get() == 0.toByte()) return null
    return UUID(/* mostSigBits = */ buf.getLong(), /* leastSigBits = */ buf.getLong())
}

private fun readGvasStructValue(buf: ByteBuffer, structType: String, path: String = ""): Any {
    return when (structType) {
        "Vector" -> readGvasPropertyVectorDict(buf)
        "DateTime" -> buf.getLong()
        "Guid" -> readGvasUUID(buf)
        "Quat" -> readGvasQuat(buf)
        "LinearColor" -> readGvasLinearColor(buf)
        else -> readGvasProperties(buf, path)
    }
}

private fun readGvasPropertyVectorDict(buf: ByteBuffer): List<Pair<String, Float?>> {
    return listOf(
        "x" to readGvasPropertyDoubleOrNull(buf),
        "y" to readGvasPropertyDoubleOrNull(buf),
        "z" to readGvasPropertyDoubleOrNull(buf)
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

private fun readGvasQuat(buf: ByteBuffer): Quat {
    return Quat(
        x = readGvasPropertyDoubleOrNull(buf),
        y = readGvasPropertyDoubleOrNull(buf),
        z = readGvasPropertyDoubleOrNull(buf),
        w = readGvasPropertyDoubleOrNull(buf)
    )
}

private fun readGvasLinearColor(buf: ByteBuffer): LinearColor {
    return LinearColor(
        r = readGvasFloat(buf),
        g = readGvasFloat(buf),
        b = readGvasFloat(buf),
        a = readGvasFloat(buf)
    )
}

private fun readGvasProperties(buf: ByteBuffer, path: String = ""): GvasMap<String, Any> {
    val properties = GvasMap<String, Any>()

    while (true) {
        val name = readGvasStr(buf)
        if (name == "None") break
        val typeName = readGvasStr(buf)
        val size = buf.getLong()
        check(size <= Int.MAX_VALUE)
        properties[name] = readGvasProperty(buf, typeName, size.toInt(), "$path.${name}")
    }

    return properties
}

private fun readGvasIntProperty(buf: ByteBuffer): GvasIntProperty {
    return GvasIntProperty(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getInt()
    )
}

private fun readGvasLongProperty(buf: ByteBuffer): GvasLongProperty {
    return GvasLongProperty(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getLong()
    )
}

private fun readGvasFixedPoint64Property(buf: ByteBuffer): GvasFixedPoint64Property {
    return GvasFixedPoint64Property(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getInt()
    )
}

private fun readGvasFloatProperty(buf: ByteBuffer): GvasFloatProperty {
    return GvasFloatProperty(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = buf.getFloat()
    )
}

private fun readGvasStringProperty(buf: ByteBuffer): GvasStringProperty {
    return GvasStringProperty(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = readGvasStr(buf)
    )
}

private fun readGvasNameProperty(buf: ByteBuffer): GvasNameProperty {
    return GvasNameProperty(
        id = readGvasOptionalUUID(buf)?.toString(),
        value = readGvasStr(buf)
    )
}

private fun readGvasEnumProperty(buf: ByteBuffer): GvasEnumProperty {
    val enumType = readGvasStr(buf)
    val _id = readGvasOptionalUUID(buf)?.toString()
    val enum_value = readGvasStr(buf)
    return GvasEnumProperty(
        id = _id,
        value = GvasEnumPropertyValue(
            type = enumType,
            value = enum_value
        )
    )
}

private fun readGvasBooleanProperty(buf: ByteBuffer): GvasBoolProperty {
    return GvasBoolProperty(
        value = readGvasBool(buf),
        id = readGvasOptionalUUID(buf)?.toString()
    )
}

private fun readGvasArrayProperty(buf: ByteBuffer, size: Int, path: String): GvasArrayProperty {
    val arrayType = readGvasStr(buf)
    return GvasArrayProperty(
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
): GvasArray<*> {
    return when (arrayType) {
        "EnumProperty" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(size) { _ -> readGvasStr(buf) }
            )
        }
        "NameProperty" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(size) { _ -> readGvasStr(buf) }
            )
        }
        "Guid" -> {
            GvasStringArrayValue(
                arrayType,
                value = Array<String>(size) { _ -> readGvasUUID(buf).toString() }
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

private fun readGvasMapProperty(buf: ByteBuffer, path: String): GvasMapProperty {
    val keyType = readGvasStr(buf)
    val valueType = readGvasStr(buf)
    val id = readGvasOptionalUUID(buf)?.toString()
    buf.getInt()
    val count = buf.getInt()
    val keyPath = "$path.Key"
    val keyStructType =
        if (keyType == "StructProperty") getTypeOr(buf, keyPath, "Guid")
        else null
    val valuePath = "$path.Value"
    val valueStructType =
        if (valueType == "StructProperty") getTypeOr(buf, valuePath, "StructProperty")
        else null
    val values = List<GvasMap<String, Any>>(count) { i ->
        GvasMap<String, Any>()
            .apply {
                put("key", readGvasPropValue(buf, keyType, keyStructType ?: "", keyPath))
                put("value", readGvasPropValue(buf, valueType, valueStructType ?: "", valuePath))
            }
    }
    return GvasMapProperty(
        keyType,
        valueType,
        keyStructType,
        valueStructType,
        id,
        values
    )
}

private fun getTypeOr(buf: ByteBuffer, path: String, default: String): String {
    // todo
    return default
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
        "NameProperty" -> readGvasNameProperty(buf)
        "IntProperty" -> buf.getInt()
        "BoolProperty" -> readGvasBool(buf)
        else -> error("Unknown Property value type: $typeName ($path)")
    }
}