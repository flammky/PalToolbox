package dev.dexsr.gmod.palworld.trainer.ue.gvas

import java.nio.ByteBuffer
import java.util.UUID

abstract class GvasReader(
    private val buf: ByteBuffer
) {

    abstract fun properties(path: String): GvasMap<String, GvasProperty>
    abstract fun property(typeName: String, size: Int, path: String, nestedCallerPath: String): GvasProperty

    abstract fun copy(buf: ByteBuffer): GvasReader

    abstract fun uuid(): UUID
    abstract fun uuidOrNull(): UUID?
    abstract fun fstring(): String
    abstract fun readArray(arrayType: String, count: Int, size: Int, path: String): GvasTypedArray<*>

    abstract fun <T> readArray(mapper: (GvasReader) -> T): ArrayList<T>

    abstract fun readByte(): Byte
    abstract fun readInt(): Int
    abstract fun readLong(): Long

    abstract fun readBytes(count: Int): ByteArray

    abstract fun isEof(): Boolean
}