package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.coerceIn
import androidx.compose.ui.text.input.TextFieldValue
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.ui.MainUIDispatcher
import dev.dexsr.gmod.palworld.toolbox.ui.UIFoundation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

@Composable
fun rememberAttributeEditPanelState(
    palEditPanelState: PalEditPanelState
): AttributeEditPanelState {

    val state = remember(palEditPanelState) {
        AttributeEditPanelState(palEditPanelState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class AttributeEditPanelState(
    private val palEditPanelState: PalEditPanelState
) {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope)

    var opened by mutableStateOf(false)
        private set

    var expanded by mutableStateOf(false)
        private set

    var mutAttribute by mutableStateOf<MutAttribute?>(null)
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(SupervisorJob() + UIFoundation.MainUIDispatcher)
    }
    fun stateExit() {
        coroutineScope.cancel()
    }

    fun userToggleExpand() {
        if (!opened) {
            opened = true
            expanded = true
            onInitialOpen()
            return
        }
        expanded = !expanded
    }

    private fun onInitialOpen() {

        coroutineScope.launch {
            val cache = palEditPanelState.cachedPalIndividualData()
            if (cache != null) {
                mutAttribute = MutAttribute(cache.attribute, cache.attributeDisplayData)
            }
            palEditPanelState.observePalIndividualData().collect { update ->
                if (update == null) {
                    mutAttribute = null
                    return@collect
                }
                if (update === cache) {
                    // the flow emit the cache
                    return@collect
                }
                mutAttribute?.update(update.attribute, update.attributeDisplayData)
                    ?: MutAttribute(update.attribute, update.attributeDisplayData).also { mutAttribute = it }
            }
        }
    }

    class MutAttribute(
        val attribute: PalEditPanelState.Attribute,
        val attributeDisplayData: PalEditPanelState.AttributeDisplayData
    ) {

        private var upAttribute = attribute

        var mutNickName by mutableStateOf(
            attribute.nickName?.let(::TextFieldValue) ?: TextFieldValue(""),
            neverEqualPolicy()
        )
            private set

        var mutUid by mutableStateOf(
            attribute.uid.let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutCharacterId by mutableStateOf(
            attribute.characterId.let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutGender by mutableStateOf(
            attribute.gender,
            neverEqualPolicy()
        )
            private set

        var mutLevel by mutableStateOf(
            (attribute.level?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutExp by mutableStateOf(
            (attribute.exp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutHp by mutableStateOf(
            (attribute.hp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutMaxHp by mutableStateOf(
            (attribute.maxHp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutFullStomach by mutableStateOf(
            (attribute.fullStomach?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutMaxFullStomach by mutableStateOf(
            (attribute.maxFullStomach?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutMp by mutableStateOf(
            (attribute.mp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var mutMaxSp by mutableStateOf(
            (attribute.maxSp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var sanityValue by mutableStateOf(
            (attribute.sanityValue?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var talentHp by mutableStateOf(
            (attribute.talentHp?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var talentMelee by mutableStateOf(
            (attribute.talentMelee?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var talentShot by mutableStateOf(
            (attribute.talentShot?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var talentDefense by mutableStateOf(
            (attribute.talentDefense?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var craftSpeed by mutableStateOf(
            (attribute.craftSpeed?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set

        var craftSpeeds by mutableStateOf(
            MutCraftSpeeds(CraftSpeeds(attribute.craftSpeeds.map(CraftSpeeds::Entry))),
            neverEqualPolicy()
        )
            private set

        var specialType by mutableStateOf(
            MutPalSpecialType(
                PalSpecialType(
                    isAlpha = attributeDisplayData.isAlpha,
                    isLucky = attributeDisplayData.isLucky,
                    isNormal = !attributeDisplayData.isAlpha && !attributeDisplayData.isLucky,
                    named = null
                )
            )
        )
            private set

        var rank by mutableStateOf(
            (attribute.rank?.toString() ?: "").let(::TextFieldValue),
            neverEqualPolicy()
        )
            private set


        fun mutNickNameChange(change: TextFieldValue) {
            if (change.text.length > 24) return
            mutNickName = change
        }

        fun mutNickNameRevert() {
            mutNickName = attribute.nickName?.let(::TextFieldValue) ?: TextFieldValue("")
        }

        fun uidChange(change: TextFieldValue) {
            ::mutUid.uuidTextFieldChange(change)
        }

        fun uidRevert() {
            mutUid = TextFieldValue(attribute.uid)
        }

        fun genderChange(change: PalGender?) {
            mutGender = change
        }

        fun genderRevert() {
            mutGender = upAttribute.gender
        }

        fun levelChange(change: TextFieldValue) {
            ::mutLevel.intTextFieldChange(change)
        }

        fun levelRevert() {
            mutLevel = TextFieldValue((attribute.level?.toString() ?: ""))
        }

        fun hpChange(change: TextFieldValue) {
            ::mutHp.longTextFieldChange(change)
        }

        fun hpRevert() {
            mutHp = TextFieldValue(attribute.hp.toString())
        }

        fun maxHpChange(change: TextFieldValue) {
            ::mutMaxHp.longTextFieldChange(change)
        }

        fun maxHpRevert() {
            mutMaxHp = TextFieldValue(attribute.maxHp?.toString() ?: "")
        }

        fun fullStomachChange(change: TextFieldValue) {
            ::mutFullStomach.floatTextFieldChange(change)
        }

        fun fullStomachRevert() {
            mutFullStomach = TextFieldValue(attribute.fullStomach?.toString() ?: "")
        }

        fun maxFullStomachChange(change: TextFieldValue) {
            ::mutMaxFullStomach.floatTextFieldChange(change)
        }

        fun maxFullStomachRevert() {
            mutMaxFullStomach = TextFieldValue(attribute.maxFullStomach?.toString() ?: "")
        }

        fun mpChange(change: TextFieldValue) {
            ::mutMp.longTextFieldChange(change)
        }

        fun mpRevert() {
            mutMp = TextFieldValue(attribute.mp?.toString() ?: "")
        }

        fun sanityValueChange(change: TextFieldValue) {
            ::sanityValue.floatTextFieldChange(change) ?: ""
        }

        fun sanityValueRevert() {
            sanityValue = TextFieldValue(attribute.sanityValue?.toString() ?: "")
        }

        fun talentHpChange(change: TextFieldValue) {
            ::talentHp.intTextFieldChange(change)
        }

        fun talentHpRevert() {
            talentHp = TextFieldValue(attribute.talentHp?.toString() ?: "")
        }

        fun talentMeleeChange(change: TextFieldValue) {
            ::talentMelee.intTextFieldChange(change)
        }

        fun talentMeleeRevert() {
            talentMelee = TextFieldValue(attribute.talentMelee?.toString() ?: "")
        }

        fun talentShotHpChange(change: TextFieldValue) {
            ::talentShot.intTextFieldChange(change)
        }

        fun talentShotRevert() {
            talentShot = TextFieldValue(attribute.talentShot?.toString() ?: "")
        }

        fun talentDefenseChange(change: TextFieldValue) {
            ::talentDefense.intTextFieldChange(change)
        }

        fun talentDefenseRevert() {
            talentDefense = TextFieldValue(attribute.talentDefense?.toString() ?: "")
        }

        fun craftSpeedChange(change: TextFieldValue) {
            ::craftSpeed.intTextFieldChange(change)
        }

        fun craftSpeedRevert() {
            craftSpeed = TextFieldValue(attribute.craftSpeed?.toString() ?: "")
        }

        fun maxSpChange(change: TextFieldValue) {
            ::mutMaxSp.longTextFieldChange(change)
        }

        fun maxSpRevert() {
            mutMaxSp = TextFieldValue(attribute.maxSp?.toString() ?: "")
        }

        fun rankChange(change: TextFieldValue) {
            ::rank.intTextFieldChange(change)
        }

        fun rankRevert() {
            rank = TextFieldValue(attribute.rank?.toString() ?: "")
        }

        fun update(
            attribute: PalEditPanelState.Attribute,
            displayData: PalEditPanelState.AttributeDisplayData
            // version: Comparable<Any>
        ) {
            upAttribute = attribute
            ::mutNickName.update(attribute.nickName ?: "")
            ::mutUid.update(attribute.uid)
            ::mutCharacterId.update(attribute.characterId)
            mutGender = attribute.gender
            ::mutLevel.update(attribute.level.toString())
            ::mutExp.update(attribute.exp.toString())
        }

        class CraftSpeeds(
            val entries: List<Entry>
        ) {

            class Entry(
                val craftSpeed: PalEditPanelState.CraftSpeed
            )
        }

        class MutCraftSpeeds(
            val craftSpeeds: CraftSpeeds
        ) {

            var mutEntries by mutableStateOf(
                emptyList<MutEntry>(),
                neverEqualPolicy()
            )

            var opened by mutableStateOf(
                false
            )

            var expanded by mutableStateOf(
                false
            )

            fun userToggleExpand() {
                if (!opened) {
                    opened = true
                    expanded = true
                    mutEntries = craftSpeeds.entries.map(::MutEntry)
                    return
                }
                expanded = !expanded
            }

            private fun lazyInit() {}

            class MutEntry(
                val entry: CraftSpeeds.Entry
            ) {

                var mutRank by mutableStateOf(
                    TextFieldValue(entry.craftSpeed.rank.toString()),
                    neverEqualPolicy()
                )
                    private set

                fun rankChange(change: TextFieldValue) {
                    ::mutRank.intTextFieldChange(change)
                }

                fun rankRevert() {
                    mutRank = TextFieldValue(entry.craftSpeed.rank.toString())
                }
            }

            fun add(name: String, rank: Int) {}
            fun removeAt(index: Int, name: String) {}
        }

        class PalSpecialType(
            val isNormal: Boolean,
            val isAlpha: Boolean,
            val isLucky: Boolean,
            val named: String?
        )

        class MutPalSpecialType(
            val palSpecialType: PalSpecialType
        ) {

            // TODO: Sealed Class ?

            private var mutType by mutableStateOf(
                when {
                    palSpecialType.isNormal -> "normal"
                    palSpecialType.isAlpha -> "alpha"
                    palSpecialType.isLucky -> "lucky"
                    else -> requireNotNull("NAMED::${palSpecialType.named}")
                }
            )

            val isNormal by derivedStateOf { mutType == "normal" }

            val isAlpha by derivedStateOf { mutType == "alpha" }

            val isLucky by derivedStateOf { mutType == "lucky" }

            val named by derivedStateOf {
                mutType.startsWith("NAMED::")
            }

            fun makeNormal() {
                mutType = "normal"
            }

            fun makeAlpha() {
                mutType = "alpha"
            }

            fun makeLucky() {
                mutType = "lucky"
            }

            fun makeNamed(named: String) {
                mutType = "NAMED::$named"
            }
        }

        private fun KMutableProperty0<TextFieldValue>.update(
            text: String
        ) = textFieldSync(
            text,
            ::get,
            ::set
        )

        private fun textFieldSync(
            text: String,
            getVarTextField: () -> TextFieldValue,
            update: (TextFieldValue) -> Unit
        ) {
            val current = getVarTextField()
            if (current.text != text) {
                update.invoke(
                    current.copy(
                        text = text,
                        selection = current.selection.coerceIn(0, text.length),
                        composition = null
                    )
                )
            }
        }
    }
}

private fun uuidTextFieldChange(
    textFieldValue: TextFieldValue,
    getVarTextField: () -> TextFieldValue,
    update: (TextFieldValue) -> Unit
) {
    if (textFieldValue.text.length > 36) return
    var n = 0
    var take = 0
    val filter = StringBuilder()
        .apply {
            textFieldValue.text.forEach { c ->
                if (!c.isLetterOrDigit()) {
                    if (n != 9-1 && n != 14-1 && n != 19-1 && n != 24-1) return
                    if (c != '-') return
                    n++ ; return@forEach
                }
                n++
                append(c)
                if (++take == 32) return@apply
            }
        }
        .toString()
    if (textFieldValue.text.length != 36 && textFieldValue.text.length > 32 && getVarTextField().text.length > 31) {
        return
    }
    update(textFieldValue)
}

private fun intTextFieldChange(
    textFieldValue: TextFieldValue,
    update: (TextFieldValue) -> Unit,
    range: IntRange = 0..Int.MAX_VALUE
) {
    val max = range.endInclusive
    if (textFieldValue.text.length > max.toString().length) return
    if (textFieldValue.text.isNotEmpty()) {
        if (!textFieldValue.text.all(Char::isDigit)) return
        val num = textFieldValue.text.toIntOrNull() ?: return
        if (num !in range) return
        update(
            TextFieldValue(
                textFieldValue.text,
                textFieldValue.selection
            )
        )
    } else {
        update(
            TextFieldValue()
        )
    }
}

private fun longTextFieldChange(
    textFieldValue: TextFieldValue,
    update: (TextFieldValue) -> Unit,
    range: LongRange = 0L..Long.MAX_VALUE
) {
    val max = range.endInclusive
    if (textFieldValue.text.length > max.toString().length) return
    if (textFieldValue.text.isNotEmpty()) {
        if (!textFieldValue.text.all(Char::isDigit)) return
        val num = textFieldValue.text.toLongOrNull() ?: return
        if (num !in range) return
        update(
            TextFieldValue(
                textFieldValue.text,
                textFieldValue.selection
            )
        )
    } else {
        update(
            TextFieldValue()
        )
    }
}

private fun floatTextFieldChange(
    textFieldValue: TextFieldValue,
    update: (TextFieldValue) -> Unit,
    range: ClosedFloatingPointRange<Float> = 0F..Float.MAX_VALUE
) {
    val max = range.endInclusive
    if (textFieldValue.text.length > max.toString().length) return
    if (textFieldValue.text.isNotEmpty()) {
        if (!textFieldValue.text.all { it.isDigit() || it == '.' }) return
        val num = textFieldValue.text.toFloatOrNull() ?: return
        if (num !in range) return
        update(
            TextFieldValue(
                textFieldValue.text,
                textFieldValue.selection
            )
        )
    } else {
        update(
            TextFieldValue()
        )
    }
}

private fun KMutableProperty0<TextFieldValue>.uuidTextFieldChange(
    text: TextFieldValue
) = uuidTextFieldChange(
    text,
    ::get,
    ::set
)

private fun KMutableProperty0<TextFieldValue>.intTextFieldChange(
    text: TextFieldValue,
    range: IntRange = 0..Int.MAX_VALUE
) = intTextFieldChange(
    text,
    ::set,
    range
)

private fun KMutableProperty0<TextFieldValue>.longTextFieldChange(
    text: TextFieldValue,
    range: LongRange = 0..Long.MAX_VALUE
) = longTextFieldChange(
    text,
    ::set,
    range
)

private fun KMutableProperty0<TextFieldValue>.floatTextFieldChange(
    text: TextFieldValue,
    range: ClosedFloatingPointRange<Float> = 0F..Float.MAX_VALUE
) = floatTextFieldChange(
    text,
    ::set,
    range
)