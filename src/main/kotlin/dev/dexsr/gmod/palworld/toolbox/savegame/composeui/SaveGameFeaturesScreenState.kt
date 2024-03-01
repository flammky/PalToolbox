package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.trainer.java.jFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Stable
class SaveGameFeaturesScreenState : RememberObserver {

    private var _coroutineScope: CoroutineScope? = null
    private var currentFileParse: Job? = null

    private val coroutineScope get() = requireNotNull(_coroutineScope) {
        "state class wasn't initialized"
    }

    private val _chosenFile = mutableStateOf<jFile?>(null, neverEqualPolicy())

    val chosenFile get() = _chosenFile.value

    fun fileDrop(file: jFile?) {
        file?.let {
            if (file.extension != "sav") return
        }
        _chosenFile.value = file
            ?: return
    }

    fun filePick(file: jFile?) {
        file?.let {
            if (file.extension != "sav") return
        }
        _chosenFile.value = file
            ?: return
    }

    override fun onAbandoned() {
    }

    override fun onForgotten() {
        currentFileParse?.cancel()
        coroutineScope.cancel()
    }

    override fun onRemembered() {
        _coroutineScope = CoroutineScope(SupervisorJob())
    }
}


@Composable
fun rememberSaveGameFeaturesScreenState(): SaveGameFeaturesScreenState {

    return remember { SaveGameFeaturesScreenState() }
}