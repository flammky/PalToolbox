package dev.dexsr.gmod.palworld.trainer.ue.gvas

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.InflaterInputStream

// TODO: try it
// TODO: make suspend

fun SavFileTransform.decodeZlibCompressed(
    magicBytes: ByteArray = PALWD_SAV_MAGICBYTES.toByteArray()
): SavFileTransform {
    if (inputStream.available() < 1) {
        markFileEmpty()
        return this
    }

    // for now assert that there should be compression info
    val compressionSizeInfoByteCount = 8
    val totalOffset = compressionSizeInfoByteCount + magicBytes.size
    if (inputStream.available() < totalOffset) {
        markFileTooSmall()
        return this
    }

    // check if compressed
    val (uncompressedSize, compressedSize) = inputStream.compressionSizeInfo()
    if (uncompressedSize < 0 || compressedSize < 0 || compressedSize > uncompressedSize) {
        markInvalidFile(MSG_INVALID_COMPRESSION_INFO, "compressedSize=$compressedSize, uncompressedSize=$uncompressedSize")
        return this
    }
    if (compressedSize == 0) {
        markInvalidFile(MSG_COMPRESSION_INFO_EMPTY, "compressedSize=0")
        return this
    }

    checkMagicBytes(magicBytes, inputStream, 0)
    if (invalidFile) return this

    decompressZlib(totalOffset, uncompressedSize, compressedSize)
    return this
}

private fun InputStream.compressionSizeInfo(): Pair<Int, Int> = unCompressedSizeInfo() to compressedSizeInfo()

private fun InputStream.unCompressedSizeInfo(): Int {
    val arr = ByteArray(4)
    read(/* b = */ arr, /* off = */ 0, /* len = */ 4)
    return ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN).getInt()
}

private fun InputStream.compressedSizeInfo(): Int {
    val arr = ByteArray(4)
    read(/* b = */ arr, /* off = */ 0, /* len = */ 4)
    return ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN).getInt()
}

private fun InputStream.compressionType(extraOffset: Int): Int {
    val arr = ByteArray(4)
    read(/* b = */ arr, /* off = */ 8 + extraOffset, /* len = */ 4)
    return ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN).getInt()
}

private fun SavFileTransform.checkMagicBytes(
    magicBytes: ByteArray,
    inStream: InputStream,
    offset: Int
) {
    val arr = ByteArray(magicBytes.size)
    inStream.read(arr, offset, magicBytes.size)
    if (!magicBytes.contentEquals(arr)) {
        markInvalidFile(MSG_WRONG_MAGIC_BYTES, "magic byte was ${arr.contentToString()}")
        return
    }
    setMagicBytes(magicBytes)
}

private fun SavFileTransform.decompressZlib(
    offset: Int,
    unCompressedSizeInfo: Int,
    compressedSizeInfo: Int,
) {
    if (inputStream.available() < 1) {
        markFileTooSmall()
        return
    }
    val type = run {
        val arr = ByteArray(1)
        inputStream.read(arr, 0, 1)
        arr.first()
    }
    val contentLength = inputStream.available()
    var decompressed: ByteArray
    when (type) {
        0x30.toByte() -> {
            markUnhandled()
            return
        }
        0x31.toByte() -> {
            if (compressedSizeInfo != contentLength) {
                markInvalidFile(MSG_WRONG_COMPRESSION_INFO, "compressedSizeInfo($compressedSizeInfo) does not equal inputLength($contentLength)")
                return
            }
            decompressed = run {
                val out = ByteArrayOutputStream()
                decompressZlib(
                    0,
                    inputStream,
                    out,
                    8192,
                )
                out.toByteArray()
            }
        }
        0x32.toByte() -> {
            decompressed = run {
                val out = ByteArrayOutputStream()
                decompressZlib(
                    0,
                    inputStream,
                    out,
                    8192,
                )
                out.toByteArray()
            }
            if (compressedSizeInfo != decompressed.size) {
                markInvalidFile(MSG_WRONG_COMPRESSION_INFO, "compressedSizeInfo($compressedSizeInfo) does not equal compressedLength($contentLength)")
                return
            }
            decompressed = run {
                val out = ByteArrayOutputStream()
                decompressZlib(
                    0,
                    decompressed.inputStream(),
                    out,
                    8192,
                )
                out.toByteArray()
            }
        }
        else -> {
            markInvalidFile(MSG_UNKNOWN_COMPRESSION_INFO, "unknown compression type=$type")
            return
        }
    }
    if (unCompressedSizeInfo != decompressed.size) {
        markInvalidFile(MSG_WRONG_COMPRESSION_INFO, "unCompressedSizeInfo($unCompressedSizeInfo) does not equal decompressedLength(${decompressed.size})")
        return
    }
    setDecompressedData(decompressed, byteArrayOf(type))
}


private fun decompressZlib(
    offset: Int,
    input: InputStream,
    outStream: OutputStream,
    bufferSize: Int,
) {
    InflaterInputStream(
        input.buffered(bufferSize)
            .apply { skip(offset.toLong()) }
    ).copyTo(outStream, bufferSize)
}