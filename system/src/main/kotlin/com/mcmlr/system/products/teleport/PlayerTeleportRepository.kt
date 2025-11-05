package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.dagger.AppScope
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.placeholder.placeholders
import com.mcmlr.system.products.data.CooldownRepository
import com.mcmlr.system.products.homes.HomeListRepository
import com.mcmlr.system.products.kits.KitRepository
import com.mcmlr.system.products.data.LocationModel
import com.mcmlr.system.products.data.toLocationModel
import com.mcmlr.system.products.spawn.RespawnType
import com.mcmlr.system.products.spawn.SpawnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.*
import javax.inject.Inject

@AppScope
class PlayerTeleportRepository @Inject constructor(
    resources: Resources,
    private val player: Player,
    private val cooldownRepository: CooldownRepository,
    private val teleportConfigRepository: TeleportConfigRepository,
): Repository<PlayerTeleportModel>(resources.dataFolder()) {

    var selectedPlayer: Player? = null
    var selectedRequest: TeleportRequestModel? = null

    init {
        loadModel("Spawn/Players", player.uniqueId.toString(), PlayerTeleportModel())
    }

    fun teleport(player: Player, destination: Player) {
        player.teleport(destination.location)
        cooldownRepository.addPlayerLastTeleportTime(player)
    }

    fun canTeleport(player: Player): Long {
        val lastTeleport = cooldownRepository.getPlayerLastTeleportTime(player)
        return (lastTeleport + (teleportConfigRepository.cooldown() * 1000)) - Date().time
    }
}

data class PlayerTeleportModel(
    var firstSpawn: Boolean = true,
    var backLocation: TeleportBackModel? = null,
): ConfigModel()

data class TeleportBackModel(
    val timestamp: Long,
    val location: LocationModel,
): ConfigModel()

@EnvironmentScope
class GlobalTeleportRepository @Inject constructor(
    resources: Resources,
    private val spawnRepository: SpawnRepository,
    private val kitRepository: KitRepository,
    private val homeRepository: HomeListRepository,
): Repository<PlayerTeleportModel>(resources.dataFolder()) {

    fun playerJoinedServer(e: PlayerJoinEvent) {
        if (!spawnRepository.model.enabled) return

        e.joinMessage = spawnRepository.model.joinMessage.placeholders(e.player)


        loadModel("Spawn/Players", "${e.player.uniqueId}", PlayerTeleportModel()) { model ->
            if (model.firstSpawn) {
                spawnRepository.model.spawnLocation?.toLocation()?.let {
                    CoroutineScope(DudeDispatcher(e.player)).launch {
                        e.player.teleport(it)
                    }

                    Bukkit.broadcastMessage(spawnRepository.model.welcomeMessage.placeholders(e.player))
                }

                spawnRepository.model.spawnKit?.let {
                    val kit = kitRepository.getKit(it) ?: return@let
                    kitRepository.givePlayerKit(e.player, kit, true)
                }

                save {
                    model.firstSpawn = false
                }
            }
        }
    }

    fun playerLeftServer(e: PlayerQuitEvent) {
        if (!spawnRepository.model.enabled) return

        e.quitMessage = spawnRepository.model.quitMessage.placeholders(e.player)
    }

    fun playerRespawn(e: PlayerRespawnEvent) {
        if (!spawnRepository.model.enabled) return

        spawnRepository.model.respawnLocation.forEach {
            when (it) {
                RespawnType.RESPAWN_ANCHOR -> {
                    if (e.isAnchorSpawn) {
                        return
                    }
                }
                RespawnType.BED -> {
                    if (e.isBedSpawn) {
                        return
                    }
                }
                RespawnType.HOME -> {
                    val home = homeRepository.getHomes(e.player)?.homes?.firstOrNull() ?: return@forEach
                    val location = Location(
                        Bukkit.getWorld(home.world),
                        home.x,
                        home.y,
                        home.z,
                        home.yaw,
                        home.pitch
                    )

                    e.respawnLocation = location
                    return
                }
                RespawnType.SPAWN -> {
                    val spawn = spawnRepository.model.spawnLocation?.toLocation() ?: return@forEach
                    e.respawnLocation = spawn
                }
            }
        }
    }

    fun setBackLocation(player: Player, backLocation: Location) {
        generateModel("Spawn/Players", player.uniqueId.toString(), PlayerTeleportModel()) { teleportModel ->
            val back = backLocation.toLocationModel() ?: return@generateModel
            teleportModel.backLocation = TeleportBackModel(Date().time, back)
            teleportModel.save()
        }
    }
}
