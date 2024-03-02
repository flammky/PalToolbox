package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.savegame.parser.SaveGameParser
import dev.dexsr.gmod.palworld.trainer.java.jFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Stable
class SaveGameEditState(
    private val file: jFile,
    private val coroutineScope: CoroutineScope
)  {
    private val parser = SaveGameParser(coroutineScope)
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

    var headerEndPos: Long? = null
    var decompressed: ByteArray? = null

    init {
        coroutineScope.launch(MainUIDispatcher) {
            decompressing = true
            val decompress = parser.decompressFile(file).await()
            if (decompress.err != null) {
                println(decompress.err)
                decompressing = false
                return@launch
            }
            decompressing = false

            checkingHeader = true
            val header = parser.parseFileHeader(decompress.data!!, decompressed = true).await()
            if (header.err != null) {
                println(header.err)
                checkingHeader = false
                return@launch
            }
            checkingHeader = false

            decompressed = decompress.data
            headerEndPos = header.data!!.pos

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

