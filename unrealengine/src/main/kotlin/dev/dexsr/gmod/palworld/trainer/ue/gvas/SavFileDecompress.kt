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
) {
    if (inputStream.available() < 1) {
        markFileEmpty()
        return
    }

    // for now assert that there should be compression info
    val compressionSizeInfoByteCount = 8
    val totalOffset = compressionSizeInfoByteCount + magicBytes.size
    if (inputStream.available() < totalOffset) {
        markFileTooSmall()
        return
    }

    // check if compressed
    val (uncompressedSize, compressedSize) = inputStream.compressionSizeInfo()
    if (uncompressedSize < 0 || compressedSize < 0 || compressedSize > uncompressedSize) {
        markInvalidFile(MSG_INVALID_COMPRESSION_INFO)
        return
    }
    if (compressedSize == 0) {
        markInvalidFile(MSG_COMPRESSION_INFO_EMPTY)
        return
    }

    checkMagicBytes(magicBytes, inputStream, 0)
    if (invalidFile) return

    decompressZlib(totalOffset, uncompressedSize, compressedSize)
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
        markInvalidFile(MSG_WRONG_MAGIC_BYTES)
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
                markInvalidFile(MSG_WRONG_COMPRESSION_INFO)
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
                markInvalidFile(MSG_WRONG_COMPRESSION_INFO)
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
            markInvalidFile(MSG_UNKNOWN_COMPRESSION_INFO)
            return
        }
    }
    if (unCompressedSizeInfo != decompressed.size) {
        markInvalidFile(MSG_WRONG_COMPRESSION_INFO)
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