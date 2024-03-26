package dev.dexsr.gmod.palworld.toolbox.savegame.composeui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.libint.md3.DropdownMenu
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.StableList
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import kotlin.math.round

@Composable
fun RevertibleTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    // TODO replace with composable
    selections: StableList<String>? = null,
    onSelectionsSelected: ((String) -> Unit)? = null
) {
    val ins = remember { MutableInteractionSource() }
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color(24, 24, 24),
        unfocusedContainerColor = Color(24, 24, 24)
    )
    BasicTextField(
        modifier = modifier.defaultMinSize(150.dp, 24.dp).sizeIn(maxHeight = 30.dp),
        singleLine = true,
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
                    Row(
                        modifier = Modifier.padding(horizontal = if (selections != null) 4.dp else 0.dp)
                    ) {
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

                        if (selections != null) {
                            val state = remember {
                                DropdownMenuState()
                            }
                            SingleLineSimpleTooltip(
                                text = "Options",
                                modifier = Modifier.clickable(onClick = {
                                    state.status = DropdownMenuState.Status.Open(Offset.Zero)
                                })
                            ) {
                                Box(modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        painter = painterResource("drawable/simple_arrow_head_down_thin_32px.png"),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).align(Alignment.Center),
                                        tint = Color.White
                                    )
                                }
                            }
                            val scrollState = rememberLazyListState()
                            DropdownMenu(
                                modifier = Modifier
                                    .height(300.dp)
                                    .background(remember { Color(0xFF211F26) })
                                    .padding(horizontal = 4.dp),
                                expanded = state.status is DropdownMenuState.Status.Open,
                                onDismissRequest = {
                                    state.status = DropdownMenuState.Status.Closed
                                },
                                offset = DpOffset.Zero,
                                scrollState = rememberScrollState(),
                                properties = PopupProperties(focusable = true)
                            ) {
                                Row {
                                    LazyColumn(
                                        modifier = Modifier.height((300-16).dp).width(600.dp),
                                        scrollState
                                    ) {
                                        val size = selections.size
                                        items(size, key = { i -> selections[i] }) { i ->
                                            val e = selections[i]
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {  }
                                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = e,
                                                    style = Material3Theme.typography.labelSmall,
                                                    color = Color(252, 252, 252)
                                                )
                                            }
                                            if (i < size-1) HeightSpacer(4.dp)
                                        }
                                    }
                                    WidthSpacer(4.dp)
                                    VerticalScrollbar(
                                        modifier = Modifier.height((300-16).dp),
                                        adapter = rememberScrollbarAdapter(scrollState),
                                        style = remember {
                                            defaultScrollbarStyle().copy(
                                                unhoverColor = Color.White.copy(alpha = 0.12f),
                                                hoverColor = Color.White.copy(alpha = 0.50f)
                                            )
                                        }
                                    )
                                }
                            }
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
                    println("originalToTransformed=$offset")
                    return when {
                        offset < 8 -> offset
                        offset < 12 -> offset + 1
                        offset < 16 -> offset + 2
                        offset < 20 -> offset + 3
                        else -> offset + 4
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    println("transformedToOriginal=$offset")
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

@Composable
fun RevertibleNumberTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
    selections: StableList<String>?,
    onSelectionsSelected: ((String) -> Unit)?
) = RevertibleTextField(
    modifier, value, labelText, onValueChange, onRevert,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    selections = selections,
    onSelectionsSelected = onSelectionsSelected
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleLineSimpleTooltip(
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
                    color = Color(round(50 / 1.5f).toInt(), round(47 / 1.5f).toInt(), round(53 / 1.5f).toInt()),
                    style = Material3Theme.typography.labelMedium
                )
            }
        }
    ) {
        content()
    }
}