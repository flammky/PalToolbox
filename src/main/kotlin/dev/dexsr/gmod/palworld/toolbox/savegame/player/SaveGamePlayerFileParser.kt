package dev.dexsr.gmod.palworld.toolbox.savegame.player

import dev.dexsr.gmod.palworld.toolbox.util.cast
import dev.dexsr.gmod.palworld.toolbox.util.checkCast
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SaveGamePlayerFileParser(
    private val coroutineScope: CoroutineScope
) {

    fun decompressPlayerFileAsync(
        input: jFile
    ): PlayerFileDecompressHandle {
        return PlayerFileDecompressHandler(coroutineScope)
            .apply { doParse(input) }
            .handle
    }

    fun parsePlayerFileHeaderAsync(
        input: ByteArray,
        decompressed: Boolean
    ): PlayerFileHeaderParseHandle {
        return PlayerFileHeaderParseHandler(coroutineScope)
            .apply { doParse(input, decompressed) }
            .handle
    }

    fun parsePlayerInventories(
        input: ByteArray,
        propertiesOffset: Int
    ): PlayerFileInventoriesParseHandle {
        return PlayerFileInventoriesParseHandler(coroutineScope)
            .apply { doParseFromPropertiesStart(input, propertiesOffset) }
            .handle
    }
}

interface SaveGamePlayerFileParseHandle <T> {

    fun cancel()

    suspend fun await(): T
}

abstract class AbstractSaveGamePlayerFileParseHandle<T> : SaveGamePlayerFileParseHandle<T> {

    val lifetime = SupervisorJob()

    protected val completion = CompletableDeferred<T>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): T {
        return completion.await()
    }

    fun complete(result: T) = completion.complete(result)
}

class PlayerFileDecompressHandle : AbstractSaveGamePlayerFileParseHandle<PlayerFileDecompressResult>() {

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                PlayerFileDecompressResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(PlayerFileDecompressResult(err = "Something Unexpected happened, decompressor finished abnormally"))
    }
}

class PlayerFileDecompressResult(
    val data: ByteArray?,
    val err: String?
) {
    constructor(data: ByteArray) : this(data, null)

    constructor(err: String) : this(null, err)
}


class PlayerFileDecompressHandler(
    private val coroutineScope: CoroutineScope
) {

    val handle = PlayerFileDecompressHandle()

    fun doParse(input: jFile) {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                input.inputStream().use { ins ->

                    SavFileTransform.open(ins)
                        .apply {
                            decodeZlibCompressed()
                        }
                        .run {
                            contentDecompressedData
                                ?.let(::PlayerFileDecompressResult)
                                ?: (userFriendlyErrMessage() ?: "Unable to decompress, no further information provided")
                                    .let(::PlayerFileDecompressResult)
                        }
                }
            }.fold(
                onSuccess = { data -> handle.complete(data) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

class PlayerFileHeaderParseHandle() : AbstractSaveGamePlayerFileParseHandle<PlayerFileHeaderParseResult>() {

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                PlayerFileHeaderParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(PlayerFileHeaderParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class PlayerFileHeaderParseHandler constructor(
    private val coroutineScope: CoroutineScope,
) {


    val handle = PlayerFileHeaderParseHandle()

    fun doParse(input: ByteArray, decompressed: Boolean) {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {

                val bytes =
                    if (decompressed) input
                    else (input.let(ByteArray::inputStream)).buffered().use { ins ->
                        val transform = SavFileTransform.open(ins)
                        transform.decodeZlibCompressed()
                        transform.contentDecompressedData
                            ?: return@runCatching PlayerFileHeaderParseResult(
                                err = transform.userFriendlyErrMessage()
                                    ?: "Unable to decompress, no further information provided"
                            )
                    }

                val buf = ByteBuffer.wrap(bytes)
                val parse = ParseGvasHeader(buf)

                parse.valueOrNull
                    ?.let { data ->
                        PlayerFileHeaderParseResult(PlayerFileHeaderParsedData(data, buf.position().toLong()))
                    }
                    ?: PlayerFileHeaderParseResult(err = "Unable to parse header, msg: ${parse.errorMsg}")
            }.fold(
                onSuccess = { v -> handle.complete(v) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

class PlayerFileHeaderParseResult private constructor(
    val data: PlayerFileHeaderParsedData?,
    val err: String?
) {

    constructor(data: PlayerFileHeaderParsedData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class PlayerFileHeaderParsedData(
    val data: GvasFileHeader,
    val pos: Long
)

class PlayerFileInventoriesParseHandle : AbstractSaveGamePlayerFileParseHandle<PlayerFileInventoriesParseResult>() {

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                PlayerFileInventoriesParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(PlayerFileInventoriesParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

class PlayerFileInventoriesParseData(
    // name to uid
    val inventories: LinkedHashMap<String, String>,
    val properties: GvasFileProperties
)

class PlayerFileInventoriesParseResult private constructor(
    val data: PlayerFileInventoriesParseData?,
    val err: String?
) {

    constructor(data: PlayerFileInventoriesParseData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class PlayerFileInventoriesParseHandler(
    private val coroutineScope: CoroutineScope
) {

    val handle = PlayerFileInventoriesParseHandle()

    fun doParseFromPropertiesStart(
        input: ByteArray,
        offset: Int
    ) {
        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                val reader = DefaultGvasReader(
                    ByteBuffer.wrap(input).position(offset).order(ByteOrder.LITTLE_ENDIAN)
                )

                val properties = ParseGvasProperties(reader)
                    .let { result ->
                        result.valueOrNull
                            ?: return@runCatching PlayerFileInventoriesParseResult(
                                err = "Unable to parse properties: ${result.errorMsg}"
                            )
                    }

                val saveData = properties["SaveData"]
                    .cast<GvasProperty>().value
                    .cast<GvasStructDict>()

                val inventoryInfo = saveData.value
                    .cast<GvasMapStruct>().v["inventoryInfo"]
                    .cast<GvasProperty>().value
                    .cast<GvasStructDict>().value
                    .cast<GvasMapStruct>().v

                val map = linkedMapOf<String, String>()
                inventoryInfo.entries.forEach { (k, v) ->
                    val name = k
                    val uid = v.value
                        .cast<GvasStructDict>().value
                        .cast<GvasMapStruct>().v["ID"]
                        .cast<GvasProperty>().value
                        .cast<GvasStructDict>().value
                        .cast<GvasGUID>().v
                    map[name] = uid
                }

                val data = PlayerFileInventoriesParseData(
                    inventories = map,
                    properties = properties
                )

                PlayerFileInventoriesParseResult(data)
            }.fold(
                onSuccess = { handle.complete(it) },
                onFailure = { ex -> ex.printStackTrace() ; handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}



private fun SavFileTransform.userFriendlyErrMessage(): String? {
    when {
        isFileEmpty -> return "File was empty"
        isFileTooSmall -> return "File was too small"
        unhandledDecompressionType -> return "Save File compression type is not handled"
        invalidFile -> return checkNotNull(invalidFileMsg).ifBlank { "Input File was invalid, no further information provided" }
    }
    return null
}