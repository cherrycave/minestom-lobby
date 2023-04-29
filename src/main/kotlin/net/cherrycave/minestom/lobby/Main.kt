package net.cherrycave.minestom.lobby

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.cherrycave.minestom.lobby.commands.NpcCommand
import net.cherrycave.minestom.lobby.data.ConfigFile
import net.cherrycave.minestom.lobby.data.Constants
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.cherrycave.minestom.lobby.data.toSerialPos
import net.cherrycave.minestom.lobby.handlers.BannerHandler
import net.cherrycave.minestom.lobby.handlers.SignHandler
import net.cherrycave.minestom.lobby.handlers.SkullHandler
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.InputStreamReader
import kotlin.io.path.*
import kotlin.system.exitProcess

object Main {
    val toml = Toml
    val json = Json {
        prettyPrint = true
        isLenient = true
    }

    val dataPath = Path("./data")

    val npcConfigFile = dataPath.resolve("npcs.json")
    val configFile = dataPath.resolve("config.toml")

    @JvmStatic
    fun main(args: Array<String>) {
        dataPath.createDirectories()
        if (!configFile.exists() || args.firstOrNull() == "--generateConfig") {
            println("Generating config...")
            configFile.writeText(toml.encodeToString(ConfigFile(Pos.ZERO.toSerialPos(), ConfigFile.ServerData())))
            npcConfigFile.writeText(json.encodeToString(NpcConfigFile(emptyList())))
            exitProcess(0)
        }
        val config = toml.decodeFromString<ConfigFile>(configFile.readText())

        val constants = Json.decodeFromString<Constants>(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("constants.json")!!).readText())
        val minecraftServer = MinecraftServer.init()
        MinecraftServer.setBrandName("Hades v${constants.version} (git-${constants.gitHash})")
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
        }.addListener(PlayerBlockInteractEvent::class.java) { event ->
            event.isCancelled = true
            event.isBlockingItemUse = true
        }
        Path("forwarding.secret").let { path ->
            if (path.exists()) {
                VelocityProxy.enable(path.readText()).run { MinecraftServer.LOGGER.info("Enable Velocity Mode") }
            } else MojangAuth.init().run { MinecraftServer.LOGGER.info("Enabling Mojang Auth") }
        }

        // Register Handlers
        Block.values().filter { it.name().endsWith("sign") }.forEach {
            MinecraftServer.getBlockManager().registerHandler(it.name()) {
                SignHandler
            }
        }

        MinecraftServer.getBlockManager().registerHandler("minecraft:sign") { SignHandler }
        MinecraftServer.getBlockManager().registerHandler("minecraft:player_head") { SkullHandler }
        MinecraftServer.getBlockManager().registerHandler("minecraft:skull") { SkullHandler }
        MinecraftServer.getBlockManager().registerHandler("minecraft:banner") { BannerHandler }

        // Init Commands
        MinecraftServer.getCommandManager().register(NpcCommand())

        // Add all NPCs
        NpcHandler.reloadNpcs()

        minecraftServer.start(config.serverData.hostname, config.serverData.port)
    }

}