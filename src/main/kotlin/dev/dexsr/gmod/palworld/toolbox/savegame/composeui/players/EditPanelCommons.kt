package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme

@Composable
fun RevertibleTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
) {
    val ins = remember { MutableInteractionSource() }
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color(24, 24, 24),
        unfocusedContainerColor = Color(24, 24, 24)
    )
    BasicTextField(
        modifier = modifier.defaultMinSize(150.dp, 24.dp).sizeIn(maxHeight = 30.dp),
        maxLines = 1,
        value = value,
        onValueChange = onValueChange,
        interactionSource = ins,
        textStyle = MaterialTheme.typography.caption.copy(
            color = Color(252, 252, 252),
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color(252, 252, 252)),
        visualTransformation = visualTransformation,
        decorationBox = {
            @OptIn(ExperimentalMaterial3Api::class)
            (OutlinedTextFieldDefaults.DecorationBox(
                value = " ",
                innerTextField = it,
                enabled = true,
                singleLine = true,
                visualTransformation = visualTransformation,
                interactionSource = ins,
                colors = colors,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 4.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = ins,
                        colors = colors
                    )
                },
                label = {
                    Text(
                        style = MaterialTheme.typography.caption.copy(
                            fontSize = 10.sp
                        ),
                        color = Color(252, 252, 252, (255 * 0.78f).toInt()),
                        fontWeight = FontWeight.Medium,
                        text = labelText
                    )
                },
                trailingIcon = {
                    SingleLineSimpleTooltip(
                        text = "Revert",
                        modifier = Modifier.clickable(onClick = onRevert)
                    ) {
                        Box(modifier = Modifier.size(24.dp)) {
                            Icon(
                                painter = painterResource("drawable/undo_simplefill_32px.png"),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                    }
                }
            ))
        }
    )
}

@Composable
fun RevertibleUUIdTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
) = RevertibleTextField(
    modifier, value, labelText, onValueChange, onRevert,
    visualTransformation = VisualTransformation { str ->

        var segment = 0
        val stb = StringBuilder()
            .apply {
                repeat(minOf(str.length, 8)) { append(str[it]) }


                // 00000000-0000-0000-0000-000000000001

                if (length < 8 || str.length == length) return@apply
                append('-') ; segment++
                repeat(minOf(str.length - 8, 4)) { append(str[8 + it]) }

                if (length < 13 || str.length < 13) return@apply
                append('-') ; segment++
                repeat(minOf(str.length - (8 + 4), 4)) { append(str[12 + it]) }

                if (length < 18 || str.length < 17) return@apply
                append('-') ; segment++
                repeat(minOf(str.length - (8 + 4 + 4), 4)) { append(str[16 + it]) }

                if (length < 23 || str.length < 21) return@apply
                append('-') ; segment++
                repeat(minOf(str.length - (8 + 4 + 4 + 4), 12)) { append(str[20 + it]) }
            }

        TransformedText(
            AnnotatedString(stb.toString()),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return when {
                        offset <= 8 -> offset
                        offset <= 12 -> offset + 1
                        offset <= 16 -> offset + 2
                        offset <= 20 -> offset + 3
                        else -> offset + 4
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        offset <= 8 -> offset
                        offset <= 13 -> offset - 1
                        offset <= 18 -> offset - 2
                        offset <= 23 -> offset - 3
                        else -> offset - 4
                    }
                }

            }
        )
    }
)

@Composable
fun RevertibleNumberTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
) = RevertibleTextField(
    modifier, value, labelText, onValueChange, onRevert,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleLineSimpleTooltip(
    text: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    TooltipArea(
        delayMillis = 250,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Default),
        tooltip = {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 24.dp)
                    .background(Color(230, 224, 233))
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = Color(50, 47, 53),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }
    ) {
        content()
    }
}