package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.savegame.inventory.PlayerInventoryData
import dev.dexsr.gmod.palworld.toolbox.savegame.inventory.PlayerInventoryEntry
import dev.dexsr.gmod.palworld.toolbox.util.cast
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.*
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.MalformedInputException
import java.nio.charset.UnmappableCharacterException

class SaveGameWorldFileParser(
    private val coroutineScope: CoroutineScope
) {

    fun decompressFileAsync(file: jFile): SaveGameDecompressHandle {
        return SaveGameDecompressInstance(file, coroutineScope).apply { doCompress() }.handle
    }

    fun parseFileHeaderAsync(input: ByteArray, decompressed: Boolean): SaveGameHeaderParseHandle {
        return SaveGameHeaderParseInstance(input, coroutineScope, decompressed).apply { doParse() }.handle
    }

    fun parseFileAsync(file: jFile): SaveGameParseHandle {
        return SaveGameParseInstance(file, coroutineScope).apply { doParse() }.handle
    }

    fun parsePlayersAsync(input: ByteArray, offset: Long): SaveGamePlayersParseHandle {
        return SaveGamePlayersParseInstance(coroutineScope).apply { doParseFromPropertiesStart(input, offset.toInt()) }.handle
    }

    fun parsePlayersInventoryAsync(input: GvasFileProperties): SaveGameParsePlayersInventoriesHandle {
        return SaveGameParsePlayersInventoriesHandler(coroutineScope).apply { doParse(input) }.handle
    }
}

interface SaveGameDecompressHandle {

    fun cancel()

    suspend fun await(): SaveGameDecompressResult
}

interface SaveGameHeaderParseHandle {

    fun cancel()

    suspend fun await(): SaveGameHeaderParseResult
}

interface SaveGameParseHandle {

    fun cancel()

    suspend fun await(): SaveGameParseResult
}

private class ActualSaveGameParseHandle() : SaveGameParseHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameParseResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameParseResult {
        return completion.await()
    }

    fun complete(result: SaveGameParseResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class ActualSaveGameDecompressHandle() : SaveGameDecompressHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameDecompressResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameDecompressResult {
        return completion.await()
    }

    fun complete(result: SaveGameDecompressResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameDecompressResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameDecompressResult(err = "Something Unexpected happened, decompressor finished abnormally"))
    }
}

private class ActualSaveGameHeaderParseHandle() : SaveGameHeaderParseHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameHeaderParseResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameHeaderParseResult {
        return completion.await()
    }

    fun complete(result: SaveGameHeaderParseResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameHeaderParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameHeaderParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class SaveGameDecompressInstance(
    private val jFile: jFile,
    private val coroutineScope: CoroutineScope
) {
    val handle = ActualSaveGameDecompressHandle()

    fun doCompress() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                jFile.inputStream().use { ins ->

                    SavFileTransform.open(ins)
                        .apply {
                            decodeZlibCompressed()
                        }
                        .run {
                            contentDecompressedData
                                ?.let(::SaveGameDecompressResult)
                                ?: (userFriendlyErrMessage() ?: "Unable to decompress, no further information provided")
                                    .let(::SaveGameDecompressResult)
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

private class SaveGameHeaderParseInstance private constructor(
    private val jF: jFile?,
    private val byteArray: ByteArray?,
    private val coroutineScope: CoroutineScope,
    private val decompressed: Boolean = false
) {

    constructor(jFile: jFile, coroutineScope: CoroutineScope, decompressed: Boolean = false) : this(
        jFile, null, coroutineScope, decompressed
    )

    constructor(byteArray: ByteArray, coroutineScope: CoroutineScope, decompressed: Boolean) : this(
        null, byteArray, coroutineScope, decompressed
    )


    val handle = ActualSaveGameHeaderParseHandle()

    init {
        require(jF != null || byteArray != null) {
            "both jFile and ByteArray is null"
        }
    }

    fun doParse() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {

                val bytes =
                    if (byteArray != null && decompressed) byteArray
                    else (jF?.let(jFile::inputStream) ?: byteArray!!.let(ByteArray::inputStream)).buffered().use { ins ->

                        if (!decompressed) {
                            val transform = SavFileTransform.open(ins)
                            transform.decodeZlibCompressed()
                            transform.contentDecompressedData
                                ?: return@runCatching SaveGameHeaderParseResult(
                                    err = transform.userFriendlyErrMessage()
                                        ?: "Unable to decompress, no further information provided"
                                )
                        } else ins.readBytes()
                    }

                val buf = ByteBuffer.wrap(bytes)
                val parse = ParseGvasHeader(buf)

                parse.valueOrNull
                    ?.let { data ->
                        SaveGameHeaderParseResult(SaveGameHeaderParsedData(data, buf.position().toLong()))
                    }
                    ?: SaveGameHeaderParseResult(err = "Unable to parse header, msg: ${parse.errorMsg}")
            }.fold(
                onSuccess = { v -> handle.complete(v) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

private class SaveGameParseInstance(
    private val jFile: jFile,
    private val coroutineScope: CoroutineScope
) {

    val handle = ActualSaveGameParseHandle()

    fun doParse() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                jFile.inputStream().use { ins ->

                    val transform = SavFileTransform.open(ins)
                        .apply {
                            decodeZlibCompressed()
                        }

                    transform.contentDecompressedData
                        ?.let { data ->
                            val result  = ParseGvasFile(data)

                            result.header
                                ?.onFailure {
                                    return@use SaveGameParseResult(err = "Unable to parse header, msg: ${it.errorMsg}")
                                }
                                ?: return@use SaveGameParseResult(err = "Unable to start parsing")

                            result.data
                                ?.let { return@use SaveGameParseResult(parseParsedFile(it)) }
                                ?: SaveGameParseResult(err = "Unable to parse properties")
                        }
                        ?: SaveGameParseResult(
                            err = transform.userFriendlyErrMessage() ?: "Unable to decompress, no further information provided"
                        )
                }
            }.fold(
                onSuccess = { v -> handle.complete(v) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )

        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }

    private fun parseParsedFile(gvasFile: GvasFile): SaveGameParsedData {

        return SaveGameParsedData(
            gvasFile,
            run {
                var name: String?
                gvasFile.properties["worldSaveData"]?.value
                    .cast<GvasStructDict>().value
                    .cast<GvasMapStruct>().v["CharacterSaveParameterMap"]?.value
                    .cast<GvasMapDict>().value
                    .first().let {
                        it["value"]
                            .cast<GvasMapStruct>()
                            .v["RawData"]?.value
                            .cast<CustomByteArrayRawData>().value
                            .cast<GvasArrayDict>().value
                            .cast<GvasTransformedArrayValue>().value
                            .cast<GvasCharacterData>().`object`.get("SaveParameter")?.value
                            .cast<GvasStructDict>().value
                            .cast<GvasMapStruct>().v.get("NickName")?.value
                            .cast<GvasStrDict>().value.also { name = it }
                    }
                name ?: error("unable to parse name")
            }
        )
    }
}

class SaveGameDecompressResult private constructor(
    val data: ByteArray?,
    val err: String?
) {

    constructor(data: ByteArray) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameParseResult private constructor(
    val data: SaveGameParsedData?,
    val err: String?
) {

    constructor(data: SaveGameParsedData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameHeaderParseResult private constructor(
    val data: SaveGameHeaderParsedData?,
    val err: String?
) {

    constructor(data: SaveGameHeaderParsedData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameHeaderParsedData(
    val data: GvasFileHeader,
    val pos: Long
)

class SaveGameParsedData(
    val gvasFile: GvasFile,
    val name: String
)

private fun SavFileTransform.userFriendlyErrMessage(): String? {
    when {
        isFileEmpty -> return "File was empty"
        isFileTooSmall -> return "File was too small"
        unhandledDecompressionType -> return "Save File compression type is not handled"
        invalidFile -> return checkNotNull(invalidFileMsg).ifBlank { "Input File was invalid, no further information provided" }
    }
    return null
}

class SaveGamePlayersParseResult private constructor(
    val data: SaveGamePlayersParsedData?,
    val err: String?
) {


    constructor(data: SaveGamePlayersParsedData) : this(data, null)
    constructor(err: String) : this(null, err)
}

class SaveGamePlayersParsedData(
    // we can use HashMap but should we expect this to be big ?
    val players: ArrayList<Player>,
    val properties: GvasFileProperties
) {


    class Player(
        val attribute: PlayerAttribute
    )

    class PlayerAttribute(
        val nickName: String,
        val uid: String,
        val level: Int,
        val exp: Int,
        val hp: Long,
        val maxHp: Long,
        val fullStomach: Float,
        val support: Int,
        val craftSpeed: Int,
        val maxSp: Long?,
        val sanityValue: Float?,
        val unusedStatusPoint: Int
    )
}

interface SaveGamePlayersParseHandle {

    fun cancel()

    suspend fun await(): SaveGamePlayersParseResult
}

private class ActualSaveGamePlayersParseHandle() : SaveGamePlayersParseHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGamePlayersParseResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGamePlayersParseResult {
        return completion.await()
    }

    fun complete(result: SaveGamePlayersParseResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGamePlayersParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGamePlayersParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class SaveGamePlayersParseInstance(
    private val coroutineScope: CoroutineScope
) {

    val handle = ActualSaveGamePlayersParseHandle()

    fun doParseFromPropertiesStart(input: ByteArray, inputOffset: Int) {
        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                val reader = DefaultGvasReader(
                    ByteBuffer.wrap(input).position(inputOffset).order(ByteOrder.LITTLE_ENDIAN),
                    customProperties = CODECS
                )

                val properties = ParseGvasProperties(reader)
                    .let { result ->
                        result.valueOrNull
                            ?: return@runCatching SaveGamePlayersParseResult(
                                err = "Unable to parse properties: ${result.errorMsg}"
                            )
                    }

                val players = ArrayList<SaveGamePlayersParsedData.Player>()
                    .apply {

                        properties["worldSaveData"]
                            ?.let { wsd ->
                                val maps = wsd.value
                                    .cast<GvasStructDict>().value
                                    .cast<GvasMapStruct>().v["CharacterSaveParameterMap"]?.value
                                    .cast<GvasMapDict>().value
                                for (map in maps) {
                                    val playerStruct = map["value"]
                                        .cast<GvasMapStruct>().v["RawData"]?.value
                                        .cast<CustomByteArrayRawData>().value
                                        .cast<GvasArrayDict>().value
                                        .cast<GvasTransformedArrayValue>().value
                                        .cast<GvasCharacterData>().`object`["SaveParameter"]?.value
                                        .cast<GvasStructDict>()
                                    val playerParams = playerStruct.value.cast<GvasMapStruct>()
                                    if (
                                        playerParams.v["IsPlayer"]
                                            ?.cast<GvasProperty>()?.value
                                            ?.cast<GvasBoolDict>()?.value == true &&
                                        playerStruct.structType == "PalIndividualCharacterSaveParameter"
                                    ) {
                                        if (playerParams.v["OwnerPlayerUid"] != null) {
                                            // Corrupt
                                        } else if (playerParams.v["NickName"] != null) {
                                            try {
                                                val encoder = Charsets.UTF_8.newEncoder()
                                                    .apply {
                                                        onMalformedInput(CodingErrorAction.REPORT)
                                                    }
                                                encoder.encode(CharBuffer.wrap(playerParams.v["NickName"]!!.value.cast<GvasStrDict>().value.toCharArray()))
                                            } catch (err: Exception) {
                                                when (err) {
                                                    is MalformedInputException -> {
                                                        // this should not happen
                                                        throw err
                                                    }
                                                    is UnmappableCharacterException -> {
                                                        // contains non-valid UTF-8 encoded char
                                                    }
                                                    else -> throw err
                                                }
                                            }
                                        }

                                        val data = run {
                                            val playerStructMap = playerStruct.value
                                                .cast<GvasMapStruct>().v
                                            val name = playerParams.v["NickName"]!!.value
                                                .cast<GvasStrDict>().value
                                            val uid = map["key"]
                                                .cast<GvasMapStruct>().v["PlayerUId"]?.value
                                                .cast<GvasStructDict>().value
                                                .cast<GvasGUID>().v
                                            SaveGamePlayersParsedData.Player(
                                                attribute = SaveGamePlayersParsedData.PlayerAttribute(
                                                    nickName = name,
                                                    uid = uid,
                                                    level = playerStructMap["Level"]?.value
                                                        .cast<GvasIntDict>().value,
                                                    exp = playerStructMap["Exp"]?.value
                                                        .cast<GvasIntDict>().value,
                                                    hp = playerStructMap["HP"]?.value
                                                        .cast<GvasStructDict>().value
                                                        .cast<GvasMapStruct>().v["Value"]?.value
                                                        .cast<GvasInt64Dict>().value,
                                                    maxHp = playerStructMap["MaxHP"]?.value
                                                        .cast<GvasStructDict>().value
                                                        .cast<GvasMapStruct>().v["Value"]?.value
                                                        .cast<GvasInt64Dict>().value,
                                                    fullStomach = playerStructMap["FullStomach"]?.value
                                                        .cast<GvasFloatDict>().value,
                                                    support = playerStructMap["Support"]?.value
                                                        .cast<GvasIntDict>().value,
                                                    craftSpeed = playerStructMap["CraftSpeed"]?.value
                                                        .cast<GvasIntDict>().value,
                                                    maxSp = playerStructMap["MaxSP"]
                                                        ?.let { prop ->
                                                            prop.value
                                                                .cast<GvasStructDict>().value
                                                                .cast<GvasMapStruct>().v["Value"]?.value
                                                                .cast<GvasInt64Dict>().value
                                                        },
                                                    sanityValue = playerStructMap["SanityValue"]
                                                        ?.let { prop ->
                                                            prop.value
                                                                .cast<GvasFloatDict>().value
                                                        },
                                                    unusedStatusPoint = playerStructMap["UnusedStatusPoint"]?.value
                                                        .cast<GvasIntDict>().value
                                                )
                                            )
                                        }

                                        add(data)
                                    }
                                }
                            }
                    }

                val data = SaveGamePlayersParsedData(
                    players,
                    properties
                )

                SaveGamePlayersParseResult(data)
            }.fold(
                onSuccess = { handle.complete(it) },
                onFailure = { ex -> ex.printStackTrace() ; handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }

    private val CODECS = HashMap<String, GVAS_PROPERTY_CODEC>()
        .apply {
            putAll(PALWORLD_CUSTOM_PROPERTY_CODEC)
            put(".worldSaveData.MapObjectSaveData", Skip::decode to Skip::encode)
            put(".worldSaveData.MapObjectSaveData.MapObjectSaveData.WorldLocation", Skip::decode to Skip::encode)
            put(".worldSaveData.MapObjectSaveData.MapObjectSaveData.WorldRotation", Skip::decode to Skip::encode)
            put(".worldSaveData.MapObjectSaveData.MapObjectSaveData.Model.Value.EffectMap", Skip::decode to Skip::encode)
            put(".worldSaveData.MapObjectSaveData.MapObjectSaveData.WorldScale3D", Skip::decode to Skip::encode)
            put(".worldSaveData.FoliageGridSaveDataMap", Skip::decode to Skip::encode)
            put(".worldSaveData.MapObjectSpawnerInStageSaveData", Skip::decode to Skip::encode)
            put(".worldSaveData.DynamicItemSaveData", Skip::decode to Skip::encode)
            put(".worldSaveData.CharacterContainerSaveData", Skip::decode to Skip::encode)
            put(".worldSaveData.CharacterContainerSaveData.Value.Slots", Skip::decode to Skip::encode)
            put(".worldSaveData.CharacterContainerSaveData.Value.RawData", Skip::decode to Skip::encode)
            /*put(".worldSaveData.ItemContainerSaveData", Skip::decode to Skip::encode)
            put(".worldSaveData.ItemContainerSaveData.Value.BelongInfo", Skip::decode to Skip::encode)
            put(".worldSaveData.ItemContainerSaveData.Value.Slots", Skip::decode to Skip::encode)*/
            put(".worldSaveData.ItemContainerSaveData.Value.RawData", Skip::decode to Skip::encode)
            put(".worldSaveData.GroupSaveDataMap", Skip::decode to Skip::encode)
            put(".worldSaveData.GroupSaveDataMap.Value.RawData", Skip::decode to Skip::encode)
        }
}

private class SaveGameParsePlayersInventoriesHandler(
    private val coroutineScope: CoroutineScope
) {
    val handle = ActualSaveGameParsePlayersInventoriesHandle()

    fun doParse(
        input: GvasFileProperties
    ) {
        val properties = input
        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {

                val worldSaveData = properties["worldSaveData"]
                    .cast<GvasProperty>().value
                    .cast<GvasStructDict>().value
                    .cast<GvasMapStruct>().v

                val itemContainerSaveData = worldSaveData["ItemContainerSaveData"]
                    .cast<GvasProperty>().value
                    .cast<GvasMapDict>().value

                val inventories = mutableMapOf<String, List<PlayerInventoryEntry>>()

                for (map in itemContainerSaveData) {
                    val key = map["key"]
                        .cast<GvasMapStruct>().v["ID"]
                        .cast<GvasProperty>().value
                        .cast<GvasStructDict>().value
                        .cast<GvasGUID>().v

                    val value = map["value"]
                        .cast<GvasMapStruct>().v

                    val slots = value["Slots"]
                        .cast<GvasProperty>().value
                        .cast<GvasArrayDict>().value
                        .cast<GvasStructArrayPropertyValue>().values


                    val slotEntries = mutableListOf<PlayerInventoryEntry>()

                    slots.fastForEach { struct ->
                        val slot = struct.cast<GvasMapStruct>().v

                        val slotIndex = slot["SlotIndex"]
                            .cast<GvasProperty>().value
                            .cast<GvasIntDict>().value

                        val itemId = run {
                            slot["ItemId"]
                                .cast<GvasProperty>().value
                                .cast<GvasStructDict>().value
                                .cast<GvasMapStruct>().v["StaticId"]
                                .cast<GvasProperty>().value
                                .cast<GvasNameDict>().value
                        }

                        val stackCount = run {
                            slot["StackCount"]
                                .cast<GvasProperty>().value
                                .cast<GvasIntDict>().value
                        }

                        slotEntries.add(PlayerInventoryEntry(slotIndex, itemId, stackCount))
                    }


                    inventories[key] = slotEntries
                }

                SaveGameParsePlayersInventoriesResult(
                    SaveGameParsePlayersInventoriesData(inventories)
                )
            }.fold(
                onSuccess = { data -> handle.complete(data) },
                onFailure = { ex -> ex.printStackTrace() ; handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

interface SaveGameParsePlayersInventoriesHandle {

    fun cancel()

    suspend fun await(): SaveGameParsePlayersInventoriesResult
}

private class ActualSaveGameParsePlayersInventoriesHandle : SaveGameParsePlayersInventoriesHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameParsePlayersInventoriesResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameParsePlayersInventoriesResult {
        return completion.await()
    }

    fun complete(result: SaveGameParsePlayersInventoriesResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameParsePlayersInventoriesResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameParsePlayersInventoriesResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

class SaveGameParsePlayersInventoriesResult private constructor(
    val data: SaveGameParsePlayersInventoriesData?,
    val err: String?
) {


    constructor(data: SaveGameParsePlayersInventoriesData) : this(data, null)
    constructor(err: String) : this(null, err)
}

class SaveGameParsePlayersInventoriesData(
    val inventories: Map<String, List<PlayerInventoryEntry>>
) {

}