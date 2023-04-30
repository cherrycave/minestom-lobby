package net.cherrycave.minestom.lobby.data

import kotlinx.serialization.Serializable
import net.cherrycave.minestom.lobby.utils.SerializablePos

@Serializable
data class ConfigFile(val spawnLocation: SerializablePos, val serverData: ServerData) {

    @Serializable
    data class ServerData(val hostname: String = "0.0.0.0", val port: Int = 25565)
}