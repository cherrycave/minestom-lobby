package net.cherrycave.lobby.data

import kotlinx.serialization.Serializable
import net.cherrycave.birgid.command.Settings
import net.cherrycave.lobby.utils.SerializablePos

@Serializable
data class ConfigData(
    val spawnLocation: SerializablePos,
    val npcs: List<NpcConfigEntry>
) : Settings

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
