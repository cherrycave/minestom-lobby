package net.cherrycave.minestom.lobby

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.entity.fakeplayer.FakePlayer
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team
import net.minestom.server.timer.TaskSchedule
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.readText
import kotlin.io.path.writeText

object NpcHandler {
    private val team: Team = MinecraftServer.getTeamManager().createBuilder("player_npc")
        .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
        .build()

    private val atomicNpcId = AtomicInteger(0)

    val npcs = mutableMapOf<FakePlayer, Entity>()

    fun reloadNpcs() {
        npcs.forEach {
            it.key.remove()
            it.value.remove()
        }
        npcs.clear()
        atomicNpcId.set(0)
        val npcConfig = Main.json.decodeFromString<NpcConfigFile>(Main.npcConfigFile.readText())
        npcConfig.npcs.forEach { npc ->
            FakePlayer.initPlayer(UUID.fromString(npc.id), atomicNpcId.getAndIncrement().toString()) { player ->
                player.option.setInTabList(false)
                player.option.isRegistered = true
                player.skin = PlayerSkin(npc.texture, npc.signature)
                player.setNoGravity(true)
                player.team = team
                player.entityMeta.let {
                    it.setNotifyAboutChanges(false)
                    it.isCapeEnabled = true
                    it.isHatEnabled = true
                    it.isJacketEnabled = true
                    it.isLeftLegEnabled = true
                    it.isLeftSleeveEnabled = true
                    it.isRightLegEnabled = true
                    it.isRightSleeveEnabled = true
                    it.setNotifyAboutChanges(true)
                }
                MinecraftServer.getSchedulerManager().buildTask {
                    player.teleport(npc.position)
                    // Armor Stand
                    val armorStand = Entity(EntityType.ARMOR_STAND)
                    armorStand.customName = MiniMessage.miniMessage().deserialize(npc.name)
                    armorStand.isCustomNameVisible = true
                    armorStand.setNoGravity(true)
                    armorStand.isInvisible = true
                    armorStand.entityMeta.let {
                        val meta = it as ArmorStandMeta
                        meta.isMarker = true
                    }
                    armorStand.setInstance(player.instance!!, player.position.add(0.0, player.eyeHeight, 0.0))
                    npcs[player] = armorStand
                }.delay(TaskSchedule.nextTick()).schedule()
                // TODO: Add metadata implementation
            }
        }
    }

    fun addNpc(npc: NpcConfigFile.NpcConfigEntry) {
        Main.npcConfigFile.writeText(
            Main.json.encodeToString(
                NpcConfigFile(Main.json.decodeFromString<NpcConfigFile>(Main.npcConfigFile.readText()).npcs.plus(npc))
            )
        )
        reloadNpcs()
    }

    fun removeNpc(id: String) {
        Main.npcConfigFile.writeText(
            Main.json.encodeToString(
                NpcConfigFile(Main.json.decodeFromString<NpcConfigFile>(Main.npcConfigFile.readText()).npcs.filter {
                    it.id != id
                })
            )
        )
        reloadNpcs()
    }

    fun moveNpc(id: String, newLocation: Pos) {
        Main.npcConfigFile.writeText(
            Main.json.encodeToString(
                NpcConfigFile(Main.json.decodeFromString<NpcConfigFile>(Main.npcConfigFile.readText()).npcs.toMutableList().let { npcConfigEntries ->
                    val entry = npcConfigEntries.find { it.id == id }
                    if (entry == null) npcConfigEntries
                    else {
                        npcConfigEntries.remove(entry)
                        npcConfigEntries.plus(NpcConfigFile.NpcConfigEntry(entry.id, entry.name, entry.texture, entry.signature, entry.metadata, newLocation))
                    }
                })
            )
        )
        reloadNpcs()
    }

}