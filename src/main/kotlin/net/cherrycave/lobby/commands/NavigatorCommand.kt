package net.cherrycave.lobby.commands

import kotlinx.coroutines.launch
import net.cherrycave.lobby.coroutineScope
import net.cherrycave.lobby.function.NavigatorManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

class NavigatorCommand(navigatorManager: NavigatorManager, production: Boolean) : Command("navigator") {
    init {
        setCondition { sender, _ -> sender.hasPermission("lobby.navigator") || !production && sender is Player }
        addSubcommand(NavigatorReloadCommand(navigatorManager))
    }

    class NavigatorReloadCommand(navigatorManager: NavigatorManager) : Command("reload") {
        init {
            addSyntax({ sender, _ ->
                coroutineScope.launch {
                    navigatorManager.reloadEntries()
                }
                sender.sendMessage(Component.text("Navigator reloaded!", NamedTextColor.GREEN))
            })
        }
    }
}