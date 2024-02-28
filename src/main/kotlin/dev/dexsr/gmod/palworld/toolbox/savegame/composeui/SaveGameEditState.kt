package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.runtime.Stable
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.*

@Stable
class SaveGameEditState(
    private val file: jFile,
    private val gvas: GvasFile
)  {
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

    // TODO: wrap properties with easier format to work with
    val properties = SaveGameEditProperties(gvas)
}

class SaveGameEditProperties(
    private val gvas: GvasFile
) {

    val elements by lazy {
        gvas.properties.map { (k, v) -> SaveGameEditProperty(k, v) }
    }
}

class SaveGameEditProperty(
    private val name: String,
    private val property: GvasProperty
) {

    val nameString = name
}