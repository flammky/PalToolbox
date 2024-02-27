package dev.dexsr.gmod.palworld.trainer.ue.gvas

sealed class GvasStruct

class GvasQuat(
    val x: Double?,
    val y: Double?,
    val z: Double?,
    val w: Double?
) : GvasStruct()

class GvasLinearColor(
    val r: Float?,
    val g: Float?,
    val b: Float?,
    val a: Float?
) : GvasStruct()

class GvasGUID(
    val v: String
) : GvasStruct()

class GvasVector(
    val x: Double?,
    val y: Double?,
    val z: Double?
) : GvasStruct()

class GvasDateTime(
    val v: Long
) : GvasStruct()

class GvasStructMap(
    val v: GvasMap<String, GvasProperty>
) : GvasStruct()

class GvasTransform(
    val rotation: GvasQuat,
    val translation: GvasVector,
    val scale3D: GvasVector
) : GvasStruct()

sealed class GvasDict {

}

open class OpenGvasDict : GvasDict()

// TODO: should all extend GvasAny ?

// should this be GvasDict ?
class GvasMap<K, V> private constructor(
    private val backing: LinkedHashMap<K, V>
): MutableMap<K, V> by backing {

    constructor() : this(LinkedHashMap())
}

class GvasProperty(
    val type: String,
    // fixme: make it immutable ?
    var value: GvasDict?
)

class GvasCustomProperty(
    val customType: String,
    var value: GvasDict?
) : GvasDict()

class GvasStructDict(
    val structType: String,
    val structId: String,
    val id: String?,
    val value: GvasStruct
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "StructProperty"
    }
}



class GvasIntDict(
    val id: String?,
    val value: Int
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "IntProperty"
    }
}

class GvasInt64Dict(
    val id: String?,
    val value: Long
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "Int64Property"
    }
}

class GvasFixedPoint64Dict(
    val id: String?,
    val value: Int
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "FixedPoint64Property"
    }
}

class GvasFloatDict(
    val id: String?,
    val value: Float
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "FloatProperty"
    }
}

class GvasNameDict(
    val id: String?,
    val value: String
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "NameProperty"
    }
}

class GvasEnumDict(
    val id: String?,
    val enumValue: GvasEnumDictValue,
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "EnumProperty"
    }
}

class GvasEnumDictValue(
    val type: String,
    val value: String
) : GvasDict() {
}

class GvasBoolDict(
    val id: String?,
    val value: Boolean
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "BoolProperty"
    }
}

class GvasArrayDict(
    val arrayType: String,
    val id: String?,
    val value: GvasArrayPropertyValue
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "ArrayProperty"
    }
}


sealed class GvasArrayPropertyValue

class GvasStructArrayPropertyValue(
    val propName: String,
    val propType: String,
    // decide on subclasses
    val values: Array<out GvasStruct>,
    val typeName: String,
    val id: String
) : GvasArrayPropertyValue()

class GvasAnyArrayPropertyValue(
    val values: GvasTypedArray<*>
): GvasArrayPropertyValue()

sealed class GvasTypedArray<T> {
    abstract val typeName: String
}

abstract class OpenGvasTypedArray<T> : GvasTypedArray<T>()

class GvasStringArrayValue(
    override val typeName: String,
    val value: Array<String>
) : GvasTypedArray<String>()

class GvasByteArrayValue(
    override val typeName: String,
    val value: ByteArray
) : GvasTypedArray<Byte>()

class GvasStrDict(
    val id: String?,
    val value: String
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "StrProperty"
    }
}

class GvasMapDict(
    val keyType: String,
    val valueType: String,
    val keyStructType: String?,
    val valueStructType: String?,
    val id: String?,
    val value: List<GvasMap<String, Any>>,
) : GvasDict() {

    companion object {
        const val TYPE_NAME = "MapProperty"
    }
}

class GvasFileProperties(
    private val value: GvasMap<String, GvasProperty>
) : Map<String, GvasProperty> by value {
}