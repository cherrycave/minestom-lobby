package net.cherrycave.minestom.lobby.commands

import net.cherrycave.minestom.lobby.NpcHandler
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.cherrycave.minestom.lobby.data.toSerialPos
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import java.util.UUID

class NpcCommand : Command("npc") {

    init {
        setCondition { sender, _ -> sender.hasPermission("lobby.npc") || true && sender is Player }
        addSubcommand(NpcAddCommand())
        addSubcommand(NpcListCommand())
        addSubcommand(NpcRemoveCommand())
        addSubcommand(NpcReloadCommand())
    }

    class NpcAddCommand : Command("add") {
        init {
            val position = ArgumentType.RelativeBlockPosition("position")
            position.setCallback { sender, _ -> sender.sendMessage(Component.text("Position is invalid!")) }
            val name = ArgumentType.String("name")
            name.setCallback { sender, _ -> sender.sendMessage(Component.text("Name is invalid!")) }
            addSyntax({ sender, context ->
                    val skin = PlayerSkin.fromUsername(context.get("name"))
                    NpcHandler.addNpc(NpcConfigFile.NpcConfigEntry(
                        UUID.randomUUID().toString(),
                        context.get(name),
                        skin?.textures().orEmpty(),
                        skin?.signature().orEmpty(),
                        emptyList(),
                        context.get(position).from(sender as Player).asPosition().toSerialPos()
                    ))
                }, position, name)
        }
    }

    class NpcListCommand : Command("list") {
        init {
            setDefaultExecutor { sender, _ ->
                val component = Component.text("NPC list: \n").color(NamedTextColor.GREEN)
                for (npc in NpcHandler.npcs) {
                    component.append(Component.text(" ${npc.name} (").append(Component.text(npc.uuid.toString()).color(NamedTextColor.GRAY)).append(Component.text(")\n")))
                }
                sender.sendMessage(component)
            }
        }
    }

    class NpcRemoveCommand : Command("remove") {
        init {

        }
    }

    class NpcReloadCommand : Command("reload") {
        init {
            setDefaultExecutor { sender, context ->
                sender.sendMessage(Component.text("Reloading NPCs!"))
                NpcHandler.reloadNpcs()
            }
        }
    }

}