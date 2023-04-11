package net.cherrycave.minestom.lobby

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.block.Block

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val minecraftServer = MinecraftServer.init()
        val instanceManager = MinecraftServer.getInstanceManager()
        // Add custom dimension
        val instanceContainer = instanceManager.createInstanceContainer()
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