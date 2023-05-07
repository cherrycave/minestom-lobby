package net.cherrycave.lobby

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.cherrycave.birgid.request.getSettings
import net.cherrycave.lobby.commands.NavigatorCommand
import net.cherrycave.lobby.commands.NpcCommand
import net.cherrycave.lobby.data.ConfigData
import net.cherrycave.lobby.data.Constants
import net.cherrycave.lobby.function.*
import net.cherrycave.lobby.handlers.BannerHandler
import net.cherrycave.lobby.handlers.SignHandler
import net.cherrycave.lobby.handlers.SkullHandler
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.InputStreamReader
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

val miniMessage by lazy {
    MiniMessage.miniMessage()
}

class CherryLobby(host: String, port: Int, production: Boolean) {

    private lateinit var config: ConfigData

    init {
        val constants =
            Json.decodeFromString<Constants>(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("constants.json")!!).readText())
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

        val navigatorManager =
            if (production) GertrudClientNavigator(gertrudClient) else FileConfigNavigator(Path("data/config.json"))

        MinecraftServer.getGlobalEventHandler().registerListeners(instanceContainer, navigatorManager)

        enableAuthentication()

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

        val npcManager = if (production) {
            coroutineScope.launch {
                config = gertrudClient.getSettings<ConfigData>("lobby-1").getOrNull() ?: error("Config not found")
            }
            GertrudClientNpcManager(gertrudClient)
        } else {
            val dataDirectory = Path("data")
            config = json.decodeFromString(dataDirectory.resolve("config.json").readText())
            FileNpcManager(dataDirectory)
        }
        MinecraftServer.getCommandManager().register(NpcCommand(npcManager, production))
        MinecraftServer.getCommandManager().register(NavigatorCommand(navigatorManager, production))

        coroutineScope.launch {
            npcManager.reloadNpcs()
        }

        minecraftServer.start(host, port)
    }

    private fun GlobalEventHandler.registerListeners(
        instanceContainer: InstanceContainer,
        navigatorManager: NavigatorManager
    ) {
        addListener(PlayerLoginEvent::class.java) { event ->
            event.setSpawningInstance(instanceContainer)
            event.player.respawnPoint = config.spawnLocation
            event.player.gameMode = GameMode.ADVENTURE
            event.player.inventory.setItemStack(4, navigatorItem)
        }.addListener(PlayerBlockInteractEvent::class.java) { event ->
            event.isCancelled = true
            event.isBlockingItemUse = true
        }.addListener(PlayerUseItemEvent::class.java) { event ->
            if (event.player.inventory.itemInMainHand.isSimilar(navigatorItem)) {
                event.player.openNavigator(config, navigatorManager)
            }
        }.addListener(ItemDropEvent::class.java) { event ->
            event.isCancelled = true
        }
    }

    private fun enableAuthentication() {
        Path("forwarding.secret").let { path ->
            if (path.exists()) {
                VelocityProxy.enable(path.readText()).run { MinecraftServer.LOGGER.info("Enable Velocity Mode") }
            } else MojangAuth.init().run { MinecraftServer.LOGGER.info("Enabling Mojang Auth") }
        }
    }

}