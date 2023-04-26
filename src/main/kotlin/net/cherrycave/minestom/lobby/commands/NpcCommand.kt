package net.cherrycave.minestom.lobby.commands

import net.cherrycave.minestom.lobby.NpcHandler
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.cherrycave.minestom.lobby.data.toSerialPos
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import java.util.UUID

class NpcCommand : Command("npc") {

    init {
        addSubcommand(NpcAddCommand())
    }

    class NpcAddCommand : Command("add") {
        init {
            val position = ArgumentType.RelativeBlockPosition("position")
            position.setCallback { sender, _ -> sender.sendMessage(Component.text("Position is invalid!")) }
            val name = ArgumentType.String("name")
            name.setCallback { sender, _ -> sender.sendMessage(Component.text("Name is invalid!")) }
            addConditionalSyntax({ sender, _ -> (sender.hasPermission("lobby.npc") && sender is Player) },
                { sender, context ->
                    val skin = PlayerSkin.fromUsername(context.get("name"))
                    NpcHandler.addNpc(NpcConfigFile.NpcConfigEntry(
                        UUID.randomUUID().toString(),
                        context.get(name),
                        skin?.textures().orEmpty(),
                        skin?.signature().orEmpty(),
                        context.get(position).from(sender as Player).asPosition().toSerialPos()
                    ))
                }, position, name)
        }
    }

}