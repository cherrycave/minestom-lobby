package net.cherrycave.lobby.function

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import net.cherrycave.birgid.GertrudClient
import net.cherrycave.birgid.command.sendRequest
import net.cherrycave.birgid.request.getSettings
import net.cherrycave.lobby.coroutineScope
import net.cherrycave.lobby.data.ConfigData
import net.cherrycave.lobby.data.NavigatorEntry
import net.cherrycave.lobby.gertrudClient
import net.cherrycave.lobby.json
import net.cherrycave.lobby.miniMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.nio.file.Path
import kotlin.io.path.readText

sealed class NavigatorManager {
    init {
        coroutineScope.launch {
            reloadEntries()
        }
    }

    abstract val entries: List<NavigatorEntry>

    abstract suspend fun reloadEntries()
}

class GertrudClientNavigator(private val gertrudClient: GertrudClient) : NavigatorManager() {
    private var entryCache = listOf<NavigatorEntry>()

    override val entries: List<NavigatorEntry>
        get() = entryCache

    override suspend fun reloadEntries() {
        gertrudClient.getSettings<ConfigData>("lobby-1").getOrNull()?.let { configData ->
            entryCache = configData.navigator
        }
    }
}

class FileConfigNavigator(private val configFile: Path) : NavigatorManager() {
    private var entryCache = listOf<NavigatorEntry>()

    override val entries: List<NavigatorEntry>
        get() = entryCache

    override suspend fun reloadEntries() {
        configFile.readText().let { configText ->
            entryCache = json.decodeFromString<ConfigData>(configText).navigator
        }
    }
}

fun Player.openNavigator(configData: ConfigData, navigatorManager: NavigatorManager) {
    playSound(Sound.sound(Key.key("minecraft", "block.amethyst_block.fall"), Sound.Source.AMBIENT, 5F, 2F))
    openInventory(navigatorInventory(configData, navigatorManager))
}

private val placeHolderItem = ItemStack.builder(Material.BLACK_STAINED_GLASS_PANE)
    .displayName(Component.text("")).build()

private fun navigatorInventory(configData: ConfigData, navigatorManager: NavigatorManager): Inventory {
    val inv = Inventory(InventoryType.CHEST_5_ROW, Component.text("Navigator").color(TextColor.color(0x8937db)))
    for (i in 0 until inv.size) {
        inv.setItemStack(i, placeHolderItem)
    }

    navigatorManager.entries.forEach { entry ->
        val itemStackBuilder = ItemStack.builder(Material.values().find {
            it.key().value() == entry.icon
        } ?: Material.GRASS_BLOCK)

        itemStackBuilder.displayName(miniMessage.deserialize(entry.name))

        itemStackBuilder.lore(*entry.lines.map { miniMessage.deserialize(it) }.toTypedArray())

        inv.setItemStack(entry.slot, itemStackBuilder.build())
    }

    inv.addInventoryCondition { player, slot, _, inventoryConditionResult ->
        inventoryConditionResult.isCancel = true
        navigatorManager.entries.forEach {
            if (slot == it.slot) {
                when (it.action) {
                    is NavigatorEntry.SpawnTeleportAction -> {
                        player.teleport(configData.spawnLocation)
                        player.playSound(
                            Sound.sound(
                                Key.key("minecraft", "entity.enderman.teleport"),
                                Sound.Source.AMBIENT,
                                1F,
                                1F
                            )
                        )
                        player.closeInventory()
                    }

                    is NavigatorEntry.SendServerAction -> {
                        coroutineScope.launch {
                            gertrudClient.sendRequest(listOf(player.uuid), it.action.server)
                        }
                    }

                    is NavigatorEntry.TeleportAction -> {
                        player.teleport(it.action.position)
                        player.playSound(
                            Sound.sound(
                                Key.key("minecraft", "entity.enderman.teleport"),
                                Sound.Source.AMBIENT,
                                1F,
                                1F
                            )
                        )
                        player.closeInventory()
                    }
                }
            }
        }
        when (slot) {
            22 -> {

            }
        }
    }

    return inv
}

val navigatorItem = ItemStack.builder(Material.COMPASS)
    .displayName(Component.text("Navigator").color(TextColor.color(0x8937db))).build()
