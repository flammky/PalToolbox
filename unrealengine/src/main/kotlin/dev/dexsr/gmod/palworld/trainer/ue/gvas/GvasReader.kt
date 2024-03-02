package dev.dexsr.gmod.palworld.trainer.ue.gvas

import java.nio.ByteBuffer
import java.util.UUID

abstract class GvasReader() {

    abstract val position: Int

    abstract val remaining: Int

    abstract fun position(pos: Int)

    abstract fun properties(path: String): GvasMap<String, GvasProperty>
    abstract fun property(typeName: String, size: Int, path: String, nestedCallerPath: String): GvasProperty

    abstract fun copy(buf: ByteBuffer): GvasReader

    abstract fun uuid(): UUID
    abstract fun uuidOrNull(): UUID?
    abstract fun fstring(): String
    abstract fun readArray(arrayType: String, count: Int, size: Int, path: String): GvasTypedArray<*>

    abstract fun <T> readArray(mapper: (GvasReader) -> T): ArrayList<T>

    abstract fun readByteArray(): ByteArray


    abstract fun readBoolean(): Boolean
    abstract fun readByte(): Byte
    abstract fun readShort(): Short
    abstract fun readUShortAsInt(): Int
    abstract fun readInt(): Int
    abstract fun readUIntAsLong(): Long
    abstract fun readLong(): Long
    abstract fun readFloat(): Float
    abstract fun readDouble(): Double

    abstract fun readBytes(count: Int): ByteArray

    abstract fun isEof(): Boolean

    abstract fun readRemaining(): ByteArray

    abstract fun compressedShortRotator(): Triple<Float, Float, Float>

    abstract fun packedVector(scaleFactor: Int): Triple<Float?, Float?, Float?>

    abstract fun ftransform(): GvasTransform

    abstract fun readVector(): GvasVector

    abstract fun readQuat(): GvasQuat
}