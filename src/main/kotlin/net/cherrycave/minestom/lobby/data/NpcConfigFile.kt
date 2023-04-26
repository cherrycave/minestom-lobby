package net.cherrycave.minestom.lobby.data

import kotlinx.serialization.Serializable

@Serializable
data class NpcConfigFile(val npcs: List<NpcConfigEntry>) {

    @Serializable
    data class NpcConfigEntry(val id: String, val name: String, val texture: String, val signature: String, val position: ConfigFile.SerialPos)

}