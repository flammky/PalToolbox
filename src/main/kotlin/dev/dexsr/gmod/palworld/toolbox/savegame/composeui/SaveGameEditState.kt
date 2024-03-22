package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.base.breakLoop
import dev.dexsr.gmod.palworld.toolbox.base.continueLoop
import dev.dexsr.gmod.palworld.toolbox.base.looper
import dev.dexsr.gmod.palworld.toolbox.base.strictResultingLoop
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameEdit
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameEditorService
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameWorldFileParser
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameWorldEditListener
import dev.dexsr.gmod.palworld.trainer.java.jFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Stable
class SaveGameEditState(
    private val file: jFile,
    private val coroutineScope: CoroutineScope
)  {
    private val parser = SaveGameWorldFileParser(coroutineScope)
    private val editorService = SaveGameEditorService.get()
    private var decompressedData: ByteArray? = null

    val fileName = file.name
    val nameDescription = when (file.extension) {
        "sav" -> {
            when (file.nameWithoutExtension) {
                "WorldOption" -> "World Setting"
                else -> "Unknown"
            }
        }
        else -> "Unknown"
    }

    var decompressing by mutableStateOf(false)
    var checkingHeader by mutableStateOf(false)

    val topFileOperationMsg by derivedStateOf {
        if (decompressing) "decompressing..."
        else if (checkingHeader) "checking header..."
        else null
    }

    var showEditor by mutableStateOf(false)

    var editPlayer by mutableStateOf<String?>(null)
    var saveGameEditor: SaveGameEdit? = null

    init {
        coroutineScope.launch(MainUIDispatcher) {
            val open = editorService.openAsync(file).await().getOrThrow()

            val worldEdit = open.getOrOpenWorldEditAsync().await()
                .apply {
                    addListener(
                        SaveGameWorldEditListener(
                            onDecompressing = { decompressing = true },
                            onDecompressed = { decompressing = false },
                            onCheckingHeader = { checkingHeader = true },
                            onCheckedHeader = { checkingHeader = false },
                        )
                    )
                }

            strictResultingLoop {
                runCatching {
                    worldEdit.headerCheck()
                }.fold(
                    onSuccess = { looper breakLoop Unit },
                    onFailure = { it.printStackTrace() ; looper continueLoop delay(Long.MAX_VALUE) }
                )
            }

            saveGameEditor = open
            showEditor = true
        }
    }

    fun userRequestEditPlayer(uid: String) {
        this.editPlayer = uid
    }
}

class SaveGameEditPlayerModel(
    val name: String,
    val attribute: SaveGameEditPlayerModelAttribute
)

class SaveGameEditPlayerModelAttribute(
    val level: Int
)

class SaveGameEditPlayerModelState(
    private val model: SaveGameEditPlayerModel
) {
    var name by mutableStateOf(model.name)

    val attribute = SaveGameEditModelAttributeState(model.attribute)
}

class SaveGameEditModelAttributeState(
    private val model: SaveGameEditPlayerModelAttribute
)

class SaveGameEditPalsState(

)

class SaveGameEditGuildsState(

)

class SaveGameEditPlayerState(

) {

}

