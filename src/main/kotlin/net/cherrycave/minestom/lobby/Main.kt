package net.cherrycave.minestom.lobby

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.cherrycave.minestom.lobby.data.ConfigFile
import net.cherrycave.minestom.lobby.data.Constants
import net.cherrycave.minestom.lobby.data.toSerialPos
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

object Main {
    private val toml = Toml

    @JvmStatic
    fun main(args: Array<String>) {
        val configFile = File("config.toml")
        if (!configFile.exists() || args.firstOrNull() == "--generateConfig") {
            println("Generating config...")
            if (configFile.exists()) configFile.delete()
            configFile.createNewFile()
            configFile.writeText(toml.encodeToString(ConfigFile(Pos.ZERO.toSerialPos(), ConfigFile.ServerData())))
            exitProcess(0)
        }
        val config = toml.decodeFromString<ConfigFile>(configFile.readText())

        val constants = Json.decodeFromString<Constants>(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("constants.json")!!).readText())
        val minecraftServer = MinecraftServer.init()
        MinecraftServer.setBrandName("Hades v${constants.version} (${constants.gitHash})")
        val instanceManager = MinecraftServer.getInstanceManager()
        val dimension = DimensionType
            .builder(NamespaceID.from(Key.key("cherrycave", "fullbright")))
            .fixedTime(6000)
            .ambientLight(2f)
            .build()
        MinecraftServer.getDimensionTypeManager().addDimension(dimension)
        val instanceContainer = instanceManager.createInstanceContainer(dimension)
        instanceContainer.setGenerator(null)
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) { event ->
            event.setSpawningInstance(instanceContainer)
            event.player.respawnPoint = config.spawnLocation.toPos()
            event.player.gameMode = GameMode.ADVENTURE
        }
        File("forwarding.secret").let { file ->
            if (file.exists()) {
                VelocityProxy.enable(file.readText())
            } else MojangAuth.init()
        }
        minecraftServer.start(config.serverData.hostname, config.serverData.port)
    }

}