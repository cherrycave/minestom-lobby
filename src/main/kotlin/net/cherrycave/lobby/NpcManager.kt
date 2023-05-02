package net.cherrycave.lobby

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.cherrycave.birgid.GertrudClient
import net.cherrycave.birgid.command.postSettings
import net.cherrycave.birgid.request.getSettings
import net.cherrycave.lobby.data.ConfigData
import net.cherrycave.lobby.data.NpcConfigEntry
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
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val team: Team = MinecraftServer.getTeamManager().createBuilder("player_npc")
    .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
    .build()

private val atomicNpcId = AtomicInteger(0)

abstract class NpcManager {
    abstract val npcs: Map<FakePlayer, Entity>

    abstract suspend fun reloadNpcs()
    abstract suspend fun addNpc(npc: NpcConfigEntry)
    abstract suspend fun removeNpc(id: String)
    abstract suspend fun moveNpc(id: String, newLocation: Pos)
}

class GertrudClientNpcManager(val gertrudClient: GertrudClient) : NpcManager() {
    override val npcs = mutableMapOf<FakePlayer, Entity>()

    private lateinit var configData: ConfigData

    override suspend fun reloadNpcs() {
        val settings = gertrudClient.getSettings("lobby-1")

        this.configData = settings.getOrNull() as ConfigData? ?: error("Failed to get settings")

        reloadPlayers(configData, npcs)
    }

    override suspend fun addNpc(npc: NpcConfigEntry) {
        configData = configData.copy(npcs = configData.npcs.plus(npc))
        gertrudClient.postSettings("lobby-1", configData)
        reloadNpcs()
    }

    override suspend fun removeNpc(id: String) {
        configData = configData.copy(npcs = configData.npcs.filter {
            it.id != id
        })
        gertrudClient.postSettings("lobby-1", configData)
        reloadNpcs()
    }

    override suspend fun moveNpc(id: String, newLocation: Pos) {
        configData = configData.copy(npcs = configData.npcs.map {
            if (it.id != id) it
            else it.copy(position = newLocation)
        })
        gertrudClient.postSettings("lobby-1", configData)
        reloadNpcs()
    }
}

class FileNpcManager(dataDirectory: Path) : NpcManager() {
    override val npcs = mutableMapOf<FakePlayer, Entity>()

    private lateinit var configData: ConfigData

    private val configPath: Path = dataDirectory.resolve("config.json")

    override suspend fun reloadNpcs() {
        this.configData = json.decodeFromString(configPath.readText())

        reloadPlayers(configData, npcs)
    }

    override suspend fun addNpc(npc: NpcConfigEntry) {
        configData = configData.copy(npcs = configData.npcs.plus(npc))
        configPath.writeText(json.encodeToString(configData))
        reloadNpcs()
    }

    override suspend fun removeNpc(id: String) {
        configData = configData.copy(npcs = configData.npcs.filter {
            it.id != id
        })
        configPath.writeText(json.encodeToString(configData))
        reloadNpcs()
    }

    override suspend fun moveNpc(id: String, newLocation: Pos) {
        configData = configData.copy(npcs = configData.npcs.map {
            if (it.id != id) it
            else it.copy(position = newLocation)
        })
        configPath.writeText(json.encodeToString(configData))
        reloadNpcs()
    }

}

private fun reloadPlayers(configData: ConfigData, npcs: MutableMap<FakePlayer, Entity>) {
    npcs.forEach {
        it.key.remove()
        it.value.remove()
    }
    npcs.clear()
    atomicNpcId.set(0)

    configData.npcs.forEach { npc ->
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
