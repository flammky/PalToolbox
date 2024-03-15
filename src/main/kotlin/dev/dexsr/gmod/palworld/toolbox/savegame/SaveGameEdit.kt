package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.core.Core
import dev.dexsr.gmod.palworld.toolbox.core.MainDispatcher
import dev.dexsr.gmod.palworld.trainer.java.jFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SaveGameEdit() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Core.MainDispatcher)
    private var world: SaveGameWorldEdit? = null
    private var _jFile: jFile? = null
    private val mtx = Mutex()
    private var openable = true

    suspend fun open(jFile: jFile) {
        mtx.withLock {
            require(openable)
            openable = false
            _jFile = jFile
        }
    }

    suspend fun getOrOpenWorldEditAsync() = coroutineScope.async {
        world ?: run {
            SaveGameWorldEdit()
                .also { world = it }
                .apply { open(requireSaveGameSource()) }
        }
    }

    sealed class DecompressState {

        object Decompressing

        object Decompressed
    }

    private fun requireSaveGameSource(): jFile = requireNotNull(_jFile) {
        "SaveGameEdit wasn't opened with jFile as source"
    }
}