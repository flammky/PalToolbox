package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding

@Composable
fun PlayersEditPanel(
    modifier: Modifier,
    editState: SaveGameEditState
) {
    val playersEditState = rememberSaveGameEditPlayersState(editState)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
    ) {

        run {
            val pagingData = playersEditState.pagingData
                ?: return@run

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    count = pagingData.playersCount,
                    key = { i -> pagingData.peekContent(i) ?: i }
                ) { i ->
                    val id = pagingData.getContent(i)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .fillMaxWidth()
                    ) {
                        if (id == null) return@Box
                        PlayersEditPanelPlayersLazyListItem(
                            Modifier,
                            getName = remember(editState, id) {
                                val snapshot = derivedStateOf { playersEditState.playerName(id) }
                                ; // why ?
                                {
                                    snapshot.value?.name ?: ""
                                }
                            },
                            getUid = { "$id" },
                            onClick = { editState.userRequestEditPlayer(id) }
                        )

                        val id = pagingData.getContent(i)
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .fillMaxWidth()
                        ) {
                            if (id == null) return@Box
                            PlayersEditPanelPlayersLazyListItem(
                                modifier = Modifier,
                                getName = remember(editState, id) {
                                    val snapshot = derivedStateOf { playersEditState.playerName(id) } // ; <- fixes it
                                    // I thought it will infer it as () -> String
                                    // typed on a variable work as well
                                    val a = {
                                        snapshot.value?.name ?: ""
                                    }
                                    a
                                },
                                getUid = { "$id" },
                                onClick = { editState.userRequestEditPlayer(id) }
                            )
                        }
                    }
                }
            }
        }


        editState.editPlayer?.let {
            PlayerEditor(
                it,
                remember(editState, it) { derivedStateOf { playersEditState.playerName(it) } }.value?.name ?: "",
                editState
            )
        }
    }
}

@Composable
private fun PlayersEditPanelPlayersLazyListItem(
    modifier: Modifier,
    getName: () -> String,
    getUid: () -> String,
    onClick: () -> Unit
) {
    Row(
        modifier.fillMaxSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            modifier = Modifier,
            text = getName(),
            color = Color(252, 252, 252),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.caption
        )

        WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFf5d9ff))
                .padding(vertical = 2.dp, horizontal = 6.dp)
        ) {
            Text(
                modifier = Modifier,
                text = "UID: ${getUid()}",
                color = Color(0xFF221728),
                fontWeight = FontWeight.SemiBold,
                style = Material3Theme.typography.labelMedium
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PlayerEditor(
    uid: String,
    name: String,
    editState: SaveGameEditState,
    modifier: Modifier = Modifier,
) {
    val pState = rememberSaveGamePlayerEditorState(uid, name)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
            .padding(top = 4.dp)
    ) {

        if (pState.loading) {
            Text(
                style = MaterialTheme.typography.caption,
                color = Color(252, 252, 252),
                fontWeight = FontWeight.Medium,
                text = "Parsing ..."
            )
        }

        if (!pState.showEditor) return@Box

        Column {

            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 35.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = { editState.editPlayer = null })
                        .padding(2.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("drawable/arrow_left_simple_32px.png"),
                        tint = Color(168, 140, 196),
                        contentDescription = null
                    )
                }

                WidthSpacer(12.dp)

                Row {
                    Text(
                        "Editing Player",
                        style = Material3Theme.typography.labelMedium,
                        color = Color(252, 252, 252)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFF978e98)
            )

            HeightSpacer(8.dp)

            Column(modifier = Modifier.padding(start = 4.dp)) {

                Text(
                    "Nickname: ${pState.initialName}",
                    style = Material3Theme.typography.labelMedium,
                    color = Color(252, 252, 252)
                )

                HeightSpacer(4.dp)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFf5d9ff))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "UID: ${pState.initialUid}",
                        color = Color(0xFF221728),
                        fontWeight = FontWeight.SemiBold,
                        style = Material3Theme.typography.labelMedium
                    )
                }
            }

            HeightSpacer(16.dp)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevertibleTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(pState.mutName ?: "", pState.mutNameCursor)
                    } }.value,
                    onValueChange = pState::nickNameFieldChange,
                    onRevert = pState::revertNickName,
                    labelText = "Nickname"
                )

                RevertibleUUIdTextField(
                    modifier = Modifier.padding(top = 2.dp),
                    value = remember { derivedStateOf(neverEqualPolicy()) {
                        TextFieldValue(pState.mutUid ?: "", pState.mutUidCursor)
                    } }.value,
                    onValueChange = pState::uidTextFieldChange,
                    onRevert = pState::revertUid,
                    labelText = "UID"
                )
            }
        }
    }
}


@Composable
private fun RevertibleTextField(
    modifier: Modifier,
    value: TextFieldValue,
    labelText: String,
    onValueChange: (TextFieldValue) -> Unit,
    onRevert: () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None
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
private fun RevertibleUUIdTextField(
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