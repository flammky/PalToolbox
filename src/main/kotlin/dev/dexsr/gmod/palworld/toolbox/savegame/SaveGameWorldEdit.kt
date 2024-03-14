package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.core.Core
import dev.dexsr.gmod.palworld.toolbox.core.MainDispatcher
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SaveGameWorldEdit {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Core.MainDispatcher)
    private var _jFile: jFile? = null
    private val mtx = Mutex()
    private val parser = SaveGameParser(coroutineScope)
    private val listeners = mutableListOf<SaveGameWorldEditListener>()
    private var decompressed: ByteArray? = null
    private var pos: Long = -1

    private val inventories = mutableMapOf<String, SaveGamePlayerInventoryEdit>()

    var players: List<SaveGamePlayersParsedData.Player>? = null

    // use atomic-fu plugin ?
    private var openable = true

    suspend fun open(jFile: jFile) {
        mtx.withLock {
            require(openable)
            openable = false
            _jFile = jFile
        }
    }

    fun addListener(listener: SaveGameWorldEditListener) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    suspend fun headerCheck() {
        coroutineScope.launch {
            val decompress =  parser.decompressFileAsync(requireNotNull(_jFile)).run {
                listeners.fastForEach { it.onDecompressing() }
                await().also {
                    it.err?.let { error(it) }
                    listeners.fastForEach { it.onDecompressed() }
                }
            }

            decompressed = decompress.data

            val header = parser.parseFileHeaderAsync(checkNotNull(decompressed), true).run {
                listeners.fastForEach { it.onCheckingHeader() }
                await().also {
                    it.err?.let { error(it) }
                    this@SaveGameWorldEdit.pos = it.data!!.pos
                    listeners.fastForEach { it.onCheckedHeader() }
                }
            }

        }.join()
    }

    suspend fun parsePlayers() {
        coroutineScope.launch {
            val parse = parser.parsePlayersAsync(input = requireNotNull(decompressed), offset = pos).run {
                await().also {
                    it.err?.let { error(it) }
                    this@SaveGameWorldEdit.players = it.data!!.players
                }
            }
        }.join()
    }
}

interface SaveGameWorldEditListener {

    fun onDecompressing() {}
    fun onDecompressed() {}
    fun onCheckingHeader() {}

    fun onCheckedHeader() {}
}

fun SaveGameWorldEditListener(
    onDecompressing: () -> Unit = {},
    onDecompressed: () -> Unit = {},
    onCheckingHeader: () -> Unit = {},
    onCheckedHeader: () -> Unit = {}
) = object : SaveGameWorldEditListener {

    override fun onDecompressing() = onDecompressing()
    override fun onDecompressed() = onDecompressed()
    override fun onCheckingHeader() = onCheckingHeader()
    override fun onCheckedHeader() {
        onCheckedHeader()
    }
}