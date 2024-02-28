package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.ParseGvasFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.SavFileTransform
import dev.dexsr.gmod.palworld.trainer.ue.gvas.decodeZlibCompressed
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher

@Stable
class SaveGameFeaturesScreenState : RememberObserver {

    private var _coroutineScope: CoroutineScope? = null
    private var currentFileParse: Job? = null

    private val coroutineScope get() = requireNotNull(_coroutineScope) {
        "state class wasn't initialized"
    }

    private val _chosenFile = mutableStateOf<jFile?>(null)
    private val _loadingFile = mutableStateOf<Boolean?>(null)
    private val _gvas = mutableStateOf<GvasFile?>(null)

    val chosenFile get() = _chosenFile.value

    val loadingFile get() = _loadingFile.value == true

    val gvas get() = _gvas.value

    fun fileDrop(file: jFile?) {
        file?.let {
            if (file.extension != "sav") return
        }
        _chosenFile.value = file
            ?: return
        loadPickedFile(file)
    }

    fun filePick(file: jFile?) {
        file?.let {
            if (file.extension != "sav") return
        }
        _chosenFile.value = file
            ?: return
        loadPickedFile(file)
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun loadPickedFile(file: jFile) {
        currentFileParse?.cancel()
        currentFileParse = coroutineScope.launch(MainUIDispatcher) {
            _loadingFile.value = true
            withContext(Dispatchers.IO) {
                file.inputStream().use { inStream ->
                    val decompress = SavFileTransform.open(inStream).apply { decodeZlibCompressed() }
                    decompress.contentDecompressedData?.let { arr ->
                        val parse = ParseGvasFile(arr)
                        ensureActive()
                        parse.data?.let(_gvas::value::set)
                    }
                    ensureActive()
                    _loadingFile.value = false
                }
            }
        }.apply {
            invokeOnCompletion(onCancelling = true, invokeImmediately = true) {
                _loadingFile.value = false
            }
        }
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