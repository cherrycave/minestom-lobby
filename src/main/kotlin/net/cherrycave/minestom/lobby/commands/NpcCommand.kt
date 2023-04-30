package net.cherrycave.minestom.lobby.commands

import net.cherrycave.minestom.lobby.NpcHandler
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
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
        addSubcommand(NpcMoveCommand())
    }

    class NpcAddCommand : Command("add") {
        init {
            val position = ArgumentType.RelativeBlockPosition("position")
            position.setCallback { sender, _ ->
                sender.sendMessage(
                    Component.text("Position is invalid!").color(NamedTextColor.RED)
                )
            }
            val name = ArgumentType.String("minimessage_display_name")
            name.setCallback { sender, _ ->
                sender.sendMessage(
                    Component.text("Name is invalid!").color(NamedTextColor.RED)
                )
            }
            addSyntax({ sender, context ->
                val skin = PlayerSkin.fromUsername(PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(context.get(name))))
                NpcHandler.addNpc(
                    NpcConfigFile.NpcConfigEntry(
                        UUID.randomUUID().toString(),
                        context.get(name),
                        skin?.textures().orEmpty(),
                        skin?.signature().orEmpty(),
                        emptyList(),
                        context.get(position).fromSender(sender).asPosition()
                    )
                )
            }, position, name)
        }
    }

    class NpcRemoveCommand : Command("remove") {
        init {
            val id = ArgumentType.UUID("uuid")
            addSyntax({ sender, context ->
                if (NpcHandler.npcs.any { it.key.uuid == context.get(id) }) {
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
                            npc.value.customName!!.append(Component.text(" (")).append {
                                Component.text(npc.key.uuid.toString()).color(NamedTextColor.GRAY).clickEvent(
                                    ClickEvent.copyToClipboard(
                                        npc.key.uuid.toString()
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

    class NpcMoveCommand : Command("move") {
        init {
            val id = ArgumentType.UUID("uuid")
            val location = ArgumentType.RelativeBlockPosition("location")
            addSyntax({ sender, context ->
                if (NpcHandler.npcs.any { it.key.uuid == context.get(id) }) {
                    val position = context.get(location).fromSender(sender).asPosition()
                    sender.sendMessage(Component.text("Moved NPC!").color(NamedTextColor.GREEN))
                    NpcHandler.moveNpc(context.get(id).toString(), position)
                } else sender.sendMessage(Component.text("Unknown NPC!").color(NamedTextColor.RED))
            }, id, location)
        }
    }

}