package dev.dexsr.gmod.palworld.trainer.gvas

class Quat(
    val x: Float?,
    val y: Float?,
    val z: Float?,
    val w: Float?
)

class LinearColor(
    val r: Float?,
    val g: Float?,
    val b: Float?,
    val a: Float?
)

open class GvasAny

// TODO: should all extend GvasAny ?

class GvasMap<K, V> private constructor(
    private val backing: LinkedHashMap<K, V>
): MutableMap<K, V> by backing {

    constructor() : this(LinkedHashMap())
}

class GvasStruct(
    val structType: String,
    val structId: String,
    val _id: String?,
    val value: Any
)

class GvasProperty(
    val type: String,
    val value: Any
)

class GvasIntProperty(
    val id: String?,
    val value: Int
)

class GvasLongProperty(
    val id: String?,
    val value: Long
)

class GvasFixedPoint64Property(
    val id: String?,
    val value: Int
)

class GvasFloatProperty(
    val id: String?,
    val value: Float
)

class GvasStrProperty(
    val id: String?,
    val value: String
)

class GvasNameProperty(
    val id: String?,
    val value: String
)

class GvasEnumProperty(
    val id: String?,
    val value: GvasEnumPropertyValue,
)

class GvasEnumPropertyValue(
    val type: String,
    val value: String
)

class GvasBoolProperty(
    val id: String?,
    val value: Boolean
)

class GvasArrayProperty(
    val arrayType: String,
    val id: String?,
    val value: GvasArrayPropertyValue
)


abstract class GvasArrayPropertyValue

class GvasStructArrayPropertyValue(
    val propName: String,
    val propType: String,
    // decide on subclasses
    val values: Array<Any>,
    val typeName: String,
    val id: String
) : GvasArrayPropertyValue()

class GvasAnyArrayPropertyValue(
    val values: GvasArray<*>
): GvasArrayPropertyValue()

abstract class GvasArray<T> {
    abstract val typeName: String
}

class GvasStringArrayValue(
    override val typeName: String,
    val value: Array<String>
) : GvasArray<Array<String>>()

class GvasByteArrayValue(
    override val typeName: String,
    val value: ByteArray
) : GvasArray<ByteArray>()

class GvasStringProperty(
    val id: String?,
    val value: String
)

class GvasMapProperty(
    val keyType: String,
    val valueType: String,
    val keyStructType: String?,
    val valueStructType: String?,
    val id: String?,
    val value: List<GvasMap<String, Any>>,
)

class GvasProperties(

) : List<Pair<String, String>> {
    private val backing = ArrayList<Pair<String, String>>()
    private var result: List<Pair<String, String>>? = null

    override val size: Int
        get() = backing.size

    override fun contains(element: Pair<String, String>): Boolean = backing.contains(element)

    override fun containsAll(elements: Collection<Pair<String, String>>): Boolean = backing.containsAll(elements)

    override fun get(index: Int): Pair<String, String> = backing.get(index)

    override fun indexOf(element: Pair<String, String>): Int = backing.indexOf(element)

    override fun isEmpty(): Boolean = backing.isEmpty()

    override fun iterator(): Iterator<Pair<String, String>> = backing.iterator()

    override fun lastIndexOf(element: Pair<String, String>): Int = backing.lastIndexOf(element)

    override fun listIterator(): ListIterator<Pair<String, String>> = backing.listIterator()

    override fun listIterator(index: Int): ListIterator<Pair<String, String>> = backing.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<Pair<String, String>> = backing.subList(fromIndex, toIndex)
}