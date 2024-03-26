package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.ui.text.input.TextFieldValue
import kotlin.reflect.KMutableProperty0

fun uuidTextFieldChange(
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

fun intTextFieldChange(
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

fun longTextFieldChange(
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

fun floatTextFieldChange(
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

fun KMutableProperty0<TextFieldValue>.uuidTextFieldChange(
    text: TextFieldValue
) = uuidTextFieldChange(
    text,
    ::get,
    ::set
)

fun KMutableProperty0<TextFieldValue>.intTextFieldChange(
    text: TextFieldValue,
    range: IntRange = 0..Int.MAX_VALUE
) = intTextFieldChange(
    text,
    ::set,
    range
)

fun KMutableProperty0<TextFieldValue>.longTextFieldChange(
    text: TextFieldValue,
    range: LongRange = 0..Long.MAX_VALUE
) = longTextFieldChange(
    text,
    ::set,
    range
)

fun KMutableProperty0<TextFieldValue>.floatTextFieldChange(
    text: TextFieldValue,
    range: ClosedFloatingPointRange<Float> = 0F..Float.MAX_VALUE
) = floatTextFieldChange(
    text,
    ::set,
    range
)