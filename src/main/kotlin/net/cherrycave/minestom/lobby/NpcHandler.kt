package net.cherrycave.minestom.lobby

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.cherrycave.minestom.lobby.data.NpcConfigFile
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.entity.fakeplayer.FakePlayer
import net.minestom.server.timer.TaskSchedule
import java.util.*

object NpcHandler {
    val npcs = mutableListOf<FakePlayer>()

    fun reloadNpcs() {
        npcs.forEach {
            it.remove()
        }
        npcs.clear()
        val npcConfig = Main.json.decodeFromString<NpcConfigFile>(Main.npcConfigFile.readText())
        npcConfig.npcs.forEach { npc ->
            FakePlayer.initPlayer(UUID.fromString(npc.id), npc.name) { player ->
                player.option.setInTabList(false)
                player.option.isRegistered = true
                player.skin = PlayerSkin(npc.texture, npc.signature)
                player.setNoGravity(true)
                npcs.add(player)
                val meta = player.entityMeta
                meta.setNotifyAboutChanges(false)
                meta.isCapeEnabled = true
                meta.isHatEnabled = true
                meta.isJacketEnabled = true
                meta.isLeftLegEnabled = true
                meta.isLeftSleeveEnabled = true
                meta.isRightLegEnabled = true
                meta.isRightSleeveEnabled = true
                meta.setNotifyAboutChanges(true)
                MinecraftServer.getSchedulerManager().buildTask {
                    player.teleport(npc.position.toPos())
                }.delay(TaskSchedule.nextTick()).schedule()
                // TODO: Add metadata implementation
                // TODO: Add custom name
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

}