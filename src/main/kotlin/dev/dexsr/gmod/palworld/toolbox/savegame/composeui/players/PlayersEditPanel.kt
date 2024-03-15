package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.ImmutableAny
import dev.dexsr.gmod.palworld.toolbox.composeui.wrapComposeUiImmutable
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayersParsedData
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

    BoxWithConstraints(
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
                                val snapshot = derivedStateOf { playersEditState.findPlayer(id) }
                                ;
                                {
                                    snapshot.value?.attribute?.nickName ?: ""
                                }
                            },
                            getUid = { "$id" },
                            onClick = { editState.userRequestEditPlayer(id) }
                        )
                    }
                }
            }
        }


        editState.editPlayer?.let {
            val player = remember(editState, it) {
                derivedStateOf { playersEditState.findPlayer(it)?.wrapComposeUiImmutable() }
            }.value ?: return@let
            PlayerEditor(
                player,
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
            style = MaterialTheme.typography.caption,
            maxLines = 1
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
                style = Material3Theme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PlayerEditor(
    player: ImmutableAny<SaveGamePlayersParsedData.Player>,
    editState: SaveGameEditState,
    modifier: Modifier = Modifier,
) {
    val pState = rememberSaveGamePlayerEditorState(
        player,
        editState
    )

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

        Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {

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

            HeightSpacer(12.dp)

            AttributeEditPanel(
                Modifier,
                pState
            )

            HeightSpacer(12.dp)

            InventoryEditPanel(
                Modifier,
                pState
            )

            HeightSpacer(12.dp)
            PlayerSaveEditPanel(
                Modifier
            )
        }
    }
}