package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.core.Core
import dev.dexsr.gmod.palworld.toolbox.core.MainDispatcher
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.utilskt.LazyConstructor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

interface SaveGameEditorService {

    fun openAsync(input: jFile): Deferred<Result<SaveGameEdit>>

    companion object {

        fun get(): SaveGameEditorService = DefaultSaveGameEditorService.get()
    }
}

class DefaultSaveGameEditorService() : SaveGameEditorService {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Core.MainDispatcher)

    override fun openAsync(input: jFile): Deferred<Result<SaveGameEdit>> {
        return coroutineScope.async {
            runCatching {
                SaveGameEdit().apply { open(input) }
            }
        }
    }

    companion object {
        private val INSTANCE = LazyConstructor<DefaultSaveGameEditorService>()

        fun get() = INSTANCE.construct { DefaultSaveGameEditorService() }
    }
}