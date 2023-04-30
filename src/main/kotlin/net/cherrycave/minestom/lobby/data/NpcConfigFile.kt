package net.cherrycave.minestom.lobby.data

import kotlinx.serialization.Serializable
import net.cherrycave.minestom.lobby.utils.SerializablePos

@Serializable
data class NpcConfigFile(val npcs: List<NpcConfigEntry>) {

    @Serializable
    data class NpcConfigEntry(
        val id: String,
        val name: String,
        val texture: String,
        val signature: String,
        val metadata: List<NpcMetadata>,
        val position: SerializablePos
    )

    @Serializable
    enum class NpcMetadata {
        LOOK_FOLLOW_PLAYER
    }

}