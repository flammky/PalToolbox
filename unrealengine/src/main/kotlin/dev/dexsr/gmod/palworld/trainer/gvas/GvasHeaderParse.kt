package dev.dexsr.gmod.palworld.trainer.gvas

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object GvasHeaderConstants {

    // 1396790855
    // 0x53415647
    val MAGICBYTES = byteArrayOf(71, 86, 65, 83)
}

class GvasHeaderParseResult(
    private val data: GvasHeader? = null
) {

    val valueOrNull
        get() = data

    var errorKindSet: LinkedHashSet<String>? = null
        private set

    var errorMsg: String? = null
        private set

    fun invalidMagicBytes(msg: String) {
        errorKindSet = linkedSetOf(INVALID_IDENTIFIER, INVALID_MAGICBYTES)
        errorMsg = msg
    }

    fun invalidGameVersion(msg: String) {
        errorKindSet = linkedSetOf(INVALID_IDENTIFIER, INVALID_GAMEVERSION)
        errorMsg = msg
    }

    fun invalidCustomVersionFormat(msg: String) {
        errorKindSet = linkedSetOf(INVALID_IDENTIFIER, INVALID_CUSTOM_VERSION_FORMAT)
        errorMsg = msg
    }

    fun onSuccess(block: (GvasHeader) -> Unit) {
        if (data != null) block(data)
    }
    fun onFailure(block: (GvasHeaderParseResult) -> Unit) {
        if (data == null) block(this)
    }

    companion object {
        const val INVALID_IDENTIFIER = "INVALID_IDENTIFIER"
        const val INVALID_MAGICBYTES = "INVALID_MAGICBYTES"
        const val INVALID_GAMEVERSION = "INVALID_GVER"
        const val INVALID_CUSTOM_VERSION_FORMAT = "INVALID_CUSTOM_VERSION_FORMAT"
    }
}

@OptIn(ExperimentalContracts::class)
fun <R> GvasHeaderParseResult.fold(
    onSuccess: (GvasHeader) -> R,
    onFailure: (GvasHeaderParseResult) -> R
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return valueOrNull?.let(onSuccess) ?: onFailure(this)
}

fun ParseGvasHeader(buf: ByteBuffer): GvasHeaderParseResult {
    val buf = buf.order(ByteOrder.LITTLE_ENDIAN)
    val magic = ByteArray(4).apply { buf.get(this) }
    if (!magic.contentEquals(GvasHeaderConstants.MAGICBYTES)) {
        return GvasHeaderParseResult().apply { invalidMagicBytes("magic bytes was $magic") }
    }


    val header = GvasHeader(
        magicBytes = magic,
        saveGameVersion = run {
            val saveGameVersion = buf.getInt()
            if (saveGameVersion != 3) {
                return GvasHeaderParseResult()
                    .apply { invalidGameVersion("saveGameVersion was $saveGameVersion") }
            }
            saveGameVersion
        },
        packageFileVersionUE4 = buf.getInt(),
        packageFileVersionUE5 = buf.getInt(),
        engineVersionMajor = buf.getShort().toUShort(),
        engineVersionMinor = buf.getShort().toUShort(),
        engineVersionPatch = buf.getShort().toUShort(),
        engineVersionChangelist = buf.getInt(),
        engineVersionBranch = readGvasHeaderStr(buf),
        customVersionFormat = run {
            val customVersionFormat = buf.getInt()
            if (customVersionFormat != 3) {
                return GvasHeaderParseResult()
                    .apply { invalidCustomVersionFormat("customVersionFormat was $customVersionFormat") }
            }
            customVersionFormat
        },
        customVersions = run {
            val count = buf.getInt()
            List(count) { i ->
                UUID(
                    buf.getLong(),
                    buf.getLong()
                ).toString() to buf.getInt()
            }
        },
        saveGameClassName = readGvasHeaderStr(buf)
    )
    return GvasHeaderParseResult(header)
}

private fun readGvasHeaderStr(
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

private fun readGvasHeaderInt(buf: ByteBuffer): Int {
    return buf.getInt()
}

private fun readGvasHeaderUShort(buf: ByteBuffer): UShort {
    return buf.getShort().toUShort()
}