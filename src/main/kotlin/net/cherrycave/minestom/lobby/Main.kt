package net.cherrycave.minestom.lobby

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.cherrycave.minestom.lobby.data.Constants
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.InputStreamReader

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val constants = Json.decodeFromString<Constants>(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("constants.json")!!).readText())
        val minecraftServer = MinecraftServer.init()
        MinecraftServer.setBrandName("CherryCave Lobby v${constants.version} (${constants.gitHash})")
        val instanceManager = MinecraftServer.getInstanceManager()
        val dimension = DimensionType
            .builder(NamespaceID.from(Key.key("cherrycave", "lobby")))
            .fixedTime(6000)
            .ambientLight(2f)
            .build()
        MinecraftServer.getDimensionTypeManager().addDimension(dimension)
        val instanceContainer = instanceManager.createInstanceContainer(dimension)
        instanceContainer.setGenerator { unit ->
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) { event ->
            event.setSpawningInstance(instanceContainer)
            event.player.respawnPoint = Pos(0.0, 42.0, 0.0)
        }
        MojangAuth.init()
        minecraftServer.start("0.0.0.0", 25565)
    }

}