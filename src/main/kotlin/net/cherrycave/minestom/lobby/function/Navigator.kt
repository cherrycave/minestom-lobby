package net.cherrycave.minestom.lobby.function

import net.cherrycave.minestom.lobby.Main
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

fun Player.openNavigator() {
    playSound(Sound.sound(Key.key("minecraft", "block.amethyst_block.fall"), Sound.Source.AMBIENT, 5F, 2F))
    openInventory(navigatorInventory)
}

private val placeHolderItem = ItemStack.builder(Material.BLACK_STAINED_GLASS_PANE)
    .displayName(Component.text("")).build()

private val navigatorInventory: Inventory by lazy {
    val inv = Inventory(InventoryType.CHEST_5_ROW, Component.text("Navigator").color(TextColor.color(0x8937db)))
    for (i in 0 until inv.size) {
        inv.setItemStack(i, placeHolderItem)
    }

    val spawnItem = ItemStack.builder(Material.ENDER_PEARL)
        .displayName(Component.text("Teleport to spawn").color(TextColor.color(0x34eb86))).build()

    inv.setItemStack(22, spawnItem)

    inv.addInventoryCondition { player, slot, _, inventoryConditionResult ->
        inventoryConditionResult.isCancel = true
        when (slot) {
            22 -> {
                inventoryConditionResult.isCancel = false
                player.playSound(Sound.sound(Key.key("minecraft", "entity.enderman.teleport"), Sound.Source.AMBIENT, 1F, 1F))
                player.teleport(Main.config.spawnLocation)
                player.closeInventory()
            }
        }
    }

    inv
}

val navigatorItem = ItemStack.builder(Material.COMPASS)
    .displayName(Component.text("Navigator").color(TextColor.color(0x8937db))).build()
