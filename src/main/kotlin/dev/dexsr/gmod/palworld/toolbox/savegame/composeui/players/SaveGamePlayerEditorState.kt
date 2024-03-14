package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.composeui.ImmutableAny
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameParser
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayersParsedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberSaveGamePlayerEditorState(
    player: ImmutableAny<SaveGamePlayersParsedData.Player>,
    editState: SaveGameEditState
): SaveGamePlayerEditorState {
    val coroutineScope = rememberCoroutineScope()

    val state = remember(player, editState) {
        SaveGamePlayerEditorState(player.value, coroutineScope, editState)
    }

    DisposableEffect(state) {
        state.onRemembered()
        onDispose { state.onDispose() }
    }

    return state
}

@Stable
class SaveGamePlayerEditorState(
    val player: SaveGamePlayersParsedData.Player,
    val coroutineScope: CoroutineScope,
    val editState: SaveGameEditState
) {

    private val lifetime = SupervisorJob()
    private val parser = SaveGameParser(CoroutineScope(lifetime))

    var initialName by mutableStateOf<String?>(player.attribute.nickName)
    var mutName by mutableStateOf<String?>(initialName)
    var mutNameCursor by mutableStateOf(TextRange(0))

    var initialUid by mutableStateOf<String?>(player.attribute.uid.filter(Char::isLetterOrDigit))
    var mutUid by mutableStateOf<String?>(initialUid)
    var mutUidCursor by mutableStateOf(TextRange(0))

    var initialLevel by mutableStateOf<String?>(player.attribute.level.toString())
    var mutLevel by mutableStateOf<String?>(initialLevel)
    var mutLevelCursor by mutableStateOf(TextRange(0))

    var initialExp by mutableStateOf<String?>(player.attribute.exp.toString())
    var mutExp by mutableStateOf<String?>(initialExp)
    var mutExpCursor by mutableStateOf(TextRange(0))

    var initialHp by mutableStateOf<String?>(player.attribute.hp.toString())
    var mutHp by mutableStateOf<String?>(initialHp)
    var mutHpCursor by mutableStateOf(TextRange(0))

    var initialMaxHp by mutableStateOf<String?>(player.attribute.maxHp.toString())
    var mutMaxHp by mutableStateOf<String?>(initialMaxHp)
    var mutMaxHpCursor by mutableStateOf(TextRange(0))

    var initialFullStomach by mutableStateOf<String?>(player.attribute.fullStomach.toString())
    var mutFullStomach by mutableStateOf<String?>(initialFullStomach)
    var mutFullStomachCursor by mutableStateOf(TextRange(0))

    var initialSupport by mutableStateOf<String?>(player.attribute.support.toString())
    var mutSupport by mutableStateOf<String?>(initialSupport)
    var mutSupportCursor by mutableStateOf(TextRange(0))

    var initialCraftSpeed by mutableStateOf<String?>(player.attribute.craftSpeed.toString())
    var mutCraftSpeed by mutableStateOf<String?>(initialCraftSpeed)
    var mutCraftSpeedCursor by mutableStateOf(TextRange(0))

    var initialMaxSp by mutableStateOf<String?>(player.attribute.maxSp.toString())
    var mutMaxSp by mutableStateOf<String?>(initialMaxSp)
    var mutMaxSpCursor by mutableStateOf(TextRange(0))

    var initialSanityValue by mutableStateOf<String?>(player.attribute.sanityValue.toString())
    var mutSanityValue by mutableStateOf<String?>(initialSanityValue)
    var mutSanityValueCursor by mutableStateOf(TextRange(0))

    var initialUnusedStatusPoint by mutableStateOf<String?>(player.attribute.unusedStatusPoint.toString())
    var mutUnusedStatusPoint by mutableStateOf<String?>(initialUnusedStatusPoint)
    var mutUnusedStatusPointCursor by mutableStateOf(TextRange(0))

    var loading by mutableStateOf(false)

    var showEditor by mutableStateOf(false)

    fun onRemembered() {
        init()
    }

    fun onDispose() {
        lifetime.cancel()
    }

    fun nickNameFieldChange(
        textFieldValue: TextFieldValue
    ) {
        if (textFieldValue.text.length > 24 && (mutName?.length ?: 0) > 23) {
            return
        }
        var n = 0
        mutName =  StringBuilder()
            .apply {
                textFieldValue.text.forEach { c ->
                    if (!c.isLetterOrDigit()) return@forEach
                    append(c)
                    if (++n == 32) return@apply
                }
            }
            .toString()
        this.mutNameCursor = textFieldValue.selection
    }

    fun uidTextFieldChange(
        textFieldValue: TextFieldValue
    ) {
        if (textFieldValue.text.length > 32 && (mutUid?.length ?: 0) > 31) {
            return
        }
        var n = 0
        mutUid =  StringBuilder()
            .apply {
                textFieldValue.text.forEach { c ->
                    if (!c.isLetterOrDigit()) return@forEach
                    append(c)
                    if (++n == 32) return@apply
                }
            }
            .toString()
        this.mutUidCursor = textFieldValue.selection

        /*var seg1Take = 0
        val seg1 = StringBuilder()
            .apply {
                uid.forEachIndexed { i, c ->
                    if (length == 8) {
                        if (c == '-') {
                            append(c)
                            seg1Take = i + 1
                        } else {
                            append('-')
                            seg1Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@forEachIndexed
                    append(c)
                }
            }
        if (seg1.length < 9) {
            mutUid = uid
            return
        }

        var seg2Take = 0
        val seg2 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - seg1Take)) { i ->
                    val c = uid[i + seg1Take]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg2Take = i + 1
                        } else {
                            append('-')
                            seg2Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        println("seg2=$seg2")
        if (seg2.length < 5) {
            mutUid = seg1.toString() + seg2.toString()
            return
        }

        var seg3Take = 0
        val seg3 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take)]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg3Take = i + 1
                        } else {
                            append('-')
                            seg3Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        if (seg3.length < 5) {
            mutUid = seg1.toString() + seg2.toString() + seg3.toString()
            return
        }

        var seg4Take = 0
        val seg4 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take + seg3Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take + seg3Take)]
                    if (length == 4) {
                        if (c == '-') {
                            append(c)
                            seg4Take = i + 1
                        } else {
                            append('-')
                            seg4Take = i
                        }
                        return@apply
                    }
                    if (c == '-') return@repeat
                    append(c)
                }
            }
        if (seg4.length < 5) {
            mutUid = seg1.toString() + seg2.toString() + seg3.toString() + seg4.toString()
            return
        }

        val seg5 = StringBuilder()
            .apply {
                repeat(maxOf(0, uid.length - (seg1Take + seg2Take + seg3Take + seg4Take))) { i ->
                    val c = uid[i + (seg1Take + seg2Take + seg3Take + seg4Take)]
                    if (c == '-') return@repeat
                    append(c)
                    if (length == 12) return@apply
                }
            }

        mutUid = seg1.toString() + seg2.toString() + seg3.toString() + seg4.toString() + seg5.toString()*/
    }

    fun levelTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 3) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toInt()
            mutLevel = num.toString()
            mutLevelCursor = textFieldValue.selection
        } else {
            mutLevel = ""
            mutLevelCursor = textFieldValue.selection
        }
    }

    fun expTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutExp = num.toString()
            mutExpCursor = textFieldValue.selection
        } else {
            mutExp = ""
            mutExpCursor = textFieldValue.selection
        }
    }

    fun hpTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutHp = num.toString()
            mutHpCursor = textFieldValue.selection
        } else {
            mutHp = ""
            mutHpCursor = textFieldValue.selection
        }
    }

    fun maxHpTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutMaxHp = num.toString()
            mutMaxHpCursor = textFieldValue.selection
        } else {
            mutMaxHp = ""
            mutMaxHpCursor = textFieldValue.selection
        }
    }

    fun fullStomachTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 9) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all { it.isDigit() || it == '.' }) return
            val num = textFieldValue.text.toFloatOrNull() ?: return
            mutFullStomach = num.toString()
            mutFullStomachCursor = textFieldValue.selection
        } else {
            mutFullStomach = ""
            mutFullStomachCursor = textFieldValue.selection
        }
    }

    fun supportTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutSupport = num.toString()
            mutSupportCursor = textFieldValue.selection
        } else {
            mutSupport = ""
            mutSupportCursor = textFieldValue.selection
        }
    }

    fun craftSpeedTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutCraftSpeed = num.toString()
            mutCraftSpeedCursor = textFieldValue.selection
        } else {
            mutCraftSpeed = ""
            mutCraftSpeedCursor = textFieldValue.selection
        }
    }

    fun maxSpTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutMaxSp = num.toString()
            mutMaxSpCursor = textFieldValue.selection
        } else {
            mutMaxSp = ""
            mutMaxSpCursor = textFieldValue.selection
        }
    }

    fun sanityValueTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 8) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all { it.isDigit() || it == '.' }) return
            val num = textFieldValue.text.toFloatOrNull() ?: return
            mutSanityValue = num.toString()
            mutSanityValueCursor = textFieldValue.selection
        } else {
            mutSanityValue = ""
            mutSanityValueCursor = textFieldValue.selection
        }
    }

    fun unusedStatusPointTextFieldChange(textFieldValue: TextFieldValue) {
        if (textFieldValue.text.length > 10) return
        if (textFieldValue.text.isNotEmpty()) {
            if (!textFieldValue.text.all(Char::isDigit)) return
            val num = textFieldValue.text.toIntOrNull() ?: return
            mutUnusedStatusPoint = num.toString()
            mutUnusedStatusPointCursor = textFieldValue.selection
        } else {
            mutUnusedStatusPoint = ""
            mutUnusedStatusPointCursor = textFieldValue.selection
        }
    }

    fun revertNickName() {
        mutName = initialName
    }

    fun revertUid() {
        mutUid = initialUid
    }

    fun revertLevel() {
        mutLevel = initialLevel
    }

    fun revertExp() {
        mutExp = initialExp
    }

    fun revertHp() {
        mutHp = initialHp
    }

    fun revertMaxHp() {
        mutMaxHp = initialMaxHp
    }

    fun revertFullStomach() {
        mutFullStomach = initialFullStomach
    }

    fun revertSupport() {
        mutSupport = initialSupport
    }

    fun revertCraftSpeed() {
        mutCraftSpeed = initialCraftSpeed
    }

    fun revertMaxSp() {
        mutMaxSp = initialMaxSp
    }

    fun revertSanityValue() {
        mutSanityValue = initialSanityValue
    }

    fun revertUnusedStatusPoint() {
        mutUnusedStatusPoint = initialUnusedStatusPoint
    }

    private fun init() {

        coroutineScope.launch(MainUIDispatcher + lifetime) {

            loading = true
            loading = false
            showEditor = true
        }
    }
}