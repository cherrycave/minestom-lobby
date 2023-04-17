package net.cherrycave.minestom.lobby.data

import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.Pos

@Serializable
data class ConfigFile(val spawnLocation: SerialPos, val serverData: ServerData) {

    @Serializable
    data class ServerData(val hostname: String = "0.0.0.0", val port: Int = 25565)

    @Serializable
    data class SerialPos(val x: Double,val y: Double, val z: Double) {
        fun toPos() : Pos {
            return Pos(x, y, z)
        }
    }

}

fun Pos.toSerialPos() : ConfigFile.SerialPos {
    return ConfigFile.SerialPos(x, y, z)
}