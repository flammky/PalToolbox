package dev.dexsr.gmod.palworld.trainer.gvas

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.zip.DeflaterOutputStream

// TODO: try it

fun SavFileTransform.encodeZlib(
    data: ByteArray,
    type: ByteArray,
    magicBytes: ByteArray
) {
    val uncompressedLen = data.size
    val (compressed, compressedLen) = run {
        var compressed: ByteArray

        run {
            val out = ByteArrayOutputStream()
            deflateZlib(out, data.inputStream())
            compressed = out.toByteArray()
        }
        if (type.contentEquals(byteArrayOf(0x32))) {
            val out = ByteArrayOutputStream()
            deflateZlib(out, compressed.inputStream())
            compressed = out.toByteArray()
        }
        compressed to compressed.size
    }

    val result = ByteArrayOutputStream()
        .apply {
            writeInt(uncompressedLen)
            writeInt(compressedLen)
            write(magicBytes)
            write(type)
            write(compressed)
        }
        .toByteArray()

    setCompressedData(result, type)
}

private fun SavFileTransform.deflateZlib(
    out: OutputStream,
    iStream: InputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
) {
    iStream.copyTo(DeflaterOutputStream(out), bufferSize)
}

private fun OutputStream.writeInt(value: Int) = write(ByteBuffer.allocate(4).apply { putInt(value) }.array())
private fun OutputStream.writeString(value: String) = write(value.toByteArray())