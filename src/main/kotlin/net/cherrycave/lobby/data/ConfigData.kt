package net.cherrycave.lobby.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import net.cherrycave.lobby.utils.SerializablePos

@Serializable
data class ConfigData(
    val spawnLocation: SerializablePos,
    val npcs: List<NpcConfigEntry>,
    val navigator: List<NavigatorEntry> = emptyList(),
)

@Serializable
data class NpcConfigEntry(
    val id: String,
    val name: String,
    val texture: String,
    val signature: String,
    val metadata: List<NpcMetadata>,
    val position: SerializablePos
) {
    @Serializable
    enum class NpcMetadata {
        LOOK_FOLLOW_PLAYER
    }
}

@Serializable
data class NavigatorEntry(
    val slot: Int,
    val name: String,
    val lines: List<String>,
    val icon: String,
    val action: Action
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonClassDiscriminator("type")
    sealed class Action

    @Serializable
    @SerialName("send")
    data class SendServerAction(val server: String) : Action()

    @Serializable
    @SerialName("warp")
    data class TeleportAction(val position: SerializablePos) : Action()

    @Serializable
    @SerialName("spawn-teleport")
    object SpawnTeleportAction : Action()
}
