package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.dexsr.gmod.palworld.toolbox.commonutil.UUIDUtil
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.game.parseOrNamed
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun rememberPalsEditPanelState(
    editState: SaveGameEditState
): PalsEditPanelState {

    val state = remember(editState) {
        PalsEditPanelState(editState)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class PalsEditPanelState(
    val editState: SaveGameEditState
) {

    var pals by mutableStateOf<List<String>>(emptyList(), neverEqualPolicy())
        private set

    var filteredPals by mutableStateOf<List<String>>(pals, neverEqualPolicy())
        private set

    var palIndividualDataFlow = mutableMapOf<String, StateFlow<PalEditPanelState.PalIndividualData?>>()
        private set

    var editPal by mutableStateOf<String?>(null)
        private set

    fun stateEnter() {

    }

    fun stateExit() {

    }

    fun observePalIndividualData(uid: String): Flow<PalEditPanelState.PalIndividualData?> {
        return palIndividualDataFlow[uid] ?: flowOf()
    }

    fun cachedPalIndividualData(uid: String) = palIndividualDataFlow[uid]?.value

    fun editPal(pal: String?) {
        this.editPal = pal
    }

    inner class Mock {

        fun mockInit() {
            mockPals()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun mockPals() {
            val pals = run {
                val stream = run {
                    val contextClassLoader = Thread.currentThread().contextClassLoader!!
                    contextClassLoader.getResourceAsStream(MOCK_INDIVIDUAL_DATA)
                        ?: return
                }
                val jsonArray = stream.use { ins ->
                    Json.decodeFromStream<JsonElement>(ins)
                }.jsonArray

                val characterIdNameMap = run {
                    val stream = run {
                        val contextClassLoader = Thread.currentThread().contextClassLoader!!
                        contextClassLoader.getResourceAsStream(PAL_BREED_NAME_MAP)
                            ?: return
                    }
                    stream.use { ins ->
                        Json.decodeFromStream<JsonElement>(ins)
                    }.jsonObject
                }

                val characterIdNameMap_B = run {
                    val stream = run {
                        val contextClassLoader = Thread.currentThread().contextClassLoader!!
                        contextClassLoader.getResourceAsStream(PAL_BREED_NAME_MAP_B)
                            ?: return
                    }
                    stream.use { ins ->
                        Json.decodeFromStream<JsonElement>(ins)
                    }.jsonObject
                }

                val result = mutableListOf<PalEditPanelState.PalIndividualData>()

                println("arrSize=${jsonArray.size}")

                jsonArray.forEachIndexed forEach@ { i, e ->
                    println("rs=${result.size}, i=$i")
                    val key = e.jsonObject["key"]?.jsonObject
                        ?: return@forEach
                    val value = e.jsonObject["value"]?.jsonObject?.get("RawData")?.jsonObject?.get("value")?.jsonObject?.get("object")?.jsonObject?.get("SaveParameter")?.jsonObject?.get("value")?.jsonObject
                        ?: return@forEach
                    val isPlayer = value["IsPlayer"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toBoolean()
                    if (isPlayer == true) return@forEach
                    val data = PalEditPanelState.PalIndividualData(
                        attribute = PalEditPanelState.Attribute(
                            nickName = value["NickName"]?.jsonObject?.get("value")?.jsonPrimitive?.content,
                            uid = key["InstanceId"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.let(UUIDUtil::stripSeparator) ?: error(""),
                            characterId = value["CharacterID"]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: error(""),
                            gender = value["Gender"]?.jsonObject?.get("value")?.jsonObject?.get("value")?.jsonPrimitive?.content?.let(PalGender.Companion::parseOrNamed),
                            level = value["Level"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            exp = value["Exp"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            hp = value["HP"]?.jsonObject?.get("value")?.jsonObject?.get("Value")?.jsonObject?.get("value")?.jsonPrimitive?.content?.toLong(),
                            maxHp = value["MaxHP"]?.jsonObject?.get("value")?.jsonObject?.get("Value")?.jsonObject?.get("value")?.jsonPrimitive?.content?.toLong(),
                            fullStomach = value["FullStomach"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toFloat(),
                            maxFullStomach = value["MaxFullStomach"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toFloat(),
                            mp = value["MP"]?.jsonObject?.get("value")?.jsonObject?.get("Value")?.jsonObject?.get("value")?.jsonPrimitive?.content?.toLongOrNull() ?: error(""),
                            sanityValue = value["SanityValue"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toFloatOrNull(),
                            talentHp = value["Talent_HP"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            talentMelee = value["Talent_Melee"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            talentShot = value["Talent_Shot"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            talentDefense = value["Talent_Defense"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt(),
                            craftSpeed = value["CraftSpeed"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toIntOrNull() ?: error(""),
                            // TODO
                            craftSpeeds = value["CraftSpeeds"]?.jsonObject?.get("value")?.jsonObject?.get("values")?.jsonArray?.map {
                                PalEditPanelState.CraftSpeed(
                                    name = it.jsonObject["WorkSuitability"]?.jsonObject?.get("value")?.jsonObject?.get("value")?.jsonPrimitive?.content ?: error(""),
                                    rank = it.jsonObject["Rank"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toIntOrNull() ?: error("")
                                )
                            } ?: error(""),
                            maxSp = value["MaxSP"]?.jsonObject?.get("value")?.jsonObject?.get("Value")?.jsonObject?.get("value")?.jsonPrimitive?.content?.toLong(),
                            isRarePal = value["IsRarePal"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toBoolean(),
                            rank = value["Rank"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toInt()
                        ),
                        ownership = PalEditPanelState.Ownership(
                            ownedTime = value["OwnedTime"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toLong(),
                            ownerPlayerUid = value["OwnerPlayerUId"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.let(UUIDUtil::stripSeparator),
                            oldOwnerUIds = value["OldOwnerPlayerUIds"]?.jsonObject?.get("value")?.jsonObject?.get("values")?.jsonArray?.mapNotNull {
                                it.jsonPrimitive.content.let(UUIDUtil::stripSeparatorOrNull)
                            } ?: error(""),
                        ),
                        skills = PalEditPanelState.Skills(
                            // TODO
                            equipWaza = value["EquipWaza"]?.jsonObject?.get("value")?.jsonObject?.get("values")?.jsonArray
                                ?.map { it.jsonPrimitive.content } ?: error(""),
                            masteredWaza = value["MasteredWaza"]?.jsonObject?.get("value")?.jsonObject?.get("values")?.jsonArray
                                ?.map { it.jsonPrimitive.content } ?: error(""),
                            passiveSkills = value["PassiveSkillList"]?.jsonObject?.get("value")?.jsonObject?.get("values")?.jsonArray
                                ?.map { it.jsonPrimitive.content }
                        ),
                        inventory = PalEditPanelState.Inventory(
                            equipItemContainerId = PalEditPanelState.Inventory.EquipItemContainerId(
                                value["EquipItemContainerId"]?.jsonObject?.get("value")?.jsonObject
                                    ?.get("ID")?.jsonObject?.get("value")?.jsonPrimitive?.content
                                    ?.let(UUIDUtil::stripSeparatorOrNull) ?: error("")
                            )
                        ),
                        attributeDisplayData = run {
                            val characterId = value["CharacterID"]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: error("")
                            val isAlpha = characterId.startsWith("BOSS_")
                            val normalizedCharacterId = if (isAlpha) characterId.drop(5) else characterId
                            val breed = characterIdNameMap[normalizedCharacterId]?.jsonPrimitive?.content
                                ?: if (isAlpha)
                                    characterIdNameMap_B[normalizedCharacterId]?.jsonPrimitive?.content
                                        ?: normalizedCharacterId
                                else normalizedCharacterId

                            run {
                                val nickName = value["NickName"]?.jsonObject?.get("value")?.jsonPrimitive?.content
                                PalEditPanelState.AttributeDisplayData(
                                    displayName = nickName ?: breed,
                                    isNamed = nickName != null,
                                    dashSeparatedUid = key["InstanceId"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.let(UUIDUtil::putSeparator) ?: error(""),
                                    breed = breed,
                                    isAlpha = isAlpha,
                                    isLucky = value["IsRarePal"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.toBoolean() == true
                                )
                            }

                        }
                    )
                    result.add(data)
                }
                result
            }
            this@PalsEditPanelState.pals = pals
                .map { it.attribute.uid }
            this@PalsEditPanelState.filteredPals = pals
                .sortedBy { it.attributeDisplayData.displayName }
                .map { it.attribute.uid }
            pals.forEach { e ->
                palIndividualDataFlow[e.attribute.uid] = MutableStateFlow(e)
            }
        }


        // TODO: create suitable save-data
        private val MOCK_INDIVIDUAL_DATA = "mocks/0.1.5.1/PalsIndividualData.json"

        private val PAL_BREED_NAME_MAP = "paldata/pal_en-US.json"
        private val PAL_BREED_NAME_MAP_B = "paldata/pal_b_en-US.json"
    }
}

