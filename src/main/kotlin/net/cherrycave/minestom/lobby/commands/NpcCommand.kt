package net.cherrycave.minestom.lobby.commands

import net.cherrycave.minestom.lobby.NpcHandler
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.cherrycave.minestom.lobby.data.toSerialPos
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import java.util.*

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
            position.setCallback { sender, _ ->
                sender.sendMessage(
                    Component.text("Position is invalid!").color(NamedTextColor.RED)
                )
            }
            val name = ArgumentType.String("name")
            name.setCallback { sender, _ ->
                sender.sendMessage(
                    Component.text("Name is invalid!").color(NamedTextColor.RED)
                )
            }
            addSyntax({ sender, context ->
                val skin = PlayerSkin.fromUsername(context.get(name))
                NpcHandler.addNpc(
                    NpcConfigFile.NpcConfigEntry(
                        UUID.randomUUID().toString(),
                        context.get(name),
                        skin?.textures().orEmpty(),
                        skin?.signature().orEmpty(),
                        emptyList(),
                        context.get(position).fromSender(sender).asPosition().toSerialPos()
                    )
                )
            }, position, name)
        }
    }

    class NpcRemoveCommand : Command("remove") {
        init {
            val id = ArgumentType.UUID("uuid")
            addSyntax({ sender, context ->
                if (NpcHandler.npcs.any { it.uuid == context.get(id) }) {
                    sender.sendMessage(Component.text("Removed NPC!").color(NamedTextColor.GREEN))
                    NpcHandler.removeNpc(context.get(id).toString())
                } else sender.sendMessage(Component.text("Unknown NPC!").color(NamedTextColor.RED))
            }, id)
        }
    }

    class NpcListCommand : Command("list") {
        init {
            setDefaultExecutor { sender, _ ->
                var component = Component.text("NPC list:").color(NamedTextColor.GREEN)
                NpcHandler.npcs.forEach { npc ->
                    component = component.append {
                        Component.text("\n ").append {
                            npc.name.append(Component.text(" (")).append {
                                Component.text(npc.uuid.toString()).color(NamedTextColor.GRAY).clickEvent(
                                    ClickEvent.copyToClipboard(
                                        npc.uuid.toString()
                                    )
                                )
                            }.append(Component.text(")"))
                        }
                    }
                }
                sender.sendMessage(component)
            }
        }
    }

    class NpcReloadCommand : Command("reload") {
        init {
            setDefaultExecutor { sender, _ ->
                sender.sendMessage(Component.text("Reloading NPCs!"))
                NpcHandler.reloadNpcs()
            }
        }
    }

}