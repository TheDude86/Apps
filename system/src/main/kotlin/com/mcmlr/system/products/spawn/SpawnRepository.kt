package com.mcmlr.system.products.spawn

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.products.settings.PriorityDirection
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.kits.KitModel
import com.mcmlr.system.products.data.LocationModel
import com.mcmlr.system.products.data.toLocationModel
import org.bukkit.ChatColor
import org.bukkit.Location
import java.util.*
import javax.inject.Inject

@EnvironmentScope
class SpawnRepository @Inject constructor(
    resources: Resources,
): Repository<SpawnModel>(resources.dataFolder()) {

    init {
        loadModel("Spawn", "config", SpawnModel())
    }

    fun setEnabled(enabled: Boolean) = save {
        model.enabled = enabled
    }

    fun setSpawn(location: Location) = save {
        model.spawnLocation = location.toLocationModel()
    }

    fun setWelcomeMessage(message: String) = save {
        model.welcomeMessage = message
    }

    fun setSpawnKit(kit: KitModel) = save {
        model.spawnKit = kit.uuid
    }

    fun setRespawnPriorities(respawnLocation: MutableList<RespawnType>) = save {
        model.respawnLocation = respawnLocation
    }

    fun addRespawnLocation(respawnType: RespawnType) = save {
        model.respawnLocation.add(respawnType)
    }

    fun removeRespawnLocation(respawnType: RespawnType) = save {
        model.respawnLocation.remove(respawnType)
    }

    fun updateRespawnPriority(respawnType: RespawnType, priorityDirection: PriorityDirection): Boolean {
        val index = model.respawnLocation.indexOf(respawnType)
        val updatedIndex = index + if (priorityDirection == PriorityDirection.UP) -1 else 1
        if (updatedIndex in 0..<model.respawnLocation.size) {
            val replacedEntity = model.respawnLocation[updatedIndex]
            model.save {
                model.respawnLocation[updatedIndex] = respawnType
                model.respawnLocation[index] = replacedEntity
            }
            return true
        }

        return false
    }

    fun setSpawnOnJoin(spawnOnJoin: Boolean) = save {
        model.spawnOnJoin = spawnOnJoin
    }

    fun setPlayerJoinMessage(message: String) = save {
        model.joinMessage = message
    }

    fun setPlayerQuitMessage(message: String) = save {
        model.quitMessage = message
    }

    fun setCooldown(cooldown: Int) = save {
        model.cooldown = cooldown
    }

    fun setDelay(delay: Int) = save {
        model.delay = delay
    }
}

data class SpawnModel(
    var enabled: Boolean = false,
    var spawnLocation: LocationModel? = null,
    var welcomeMessage: String = "${ChatColor.YELLOW}Welcome %player_name% to the server!",
    var spawnKit: UUID? = null,
    var respawnLocation: MutableList<RespawnType> = mutableListOf(RespawnType.BED, RespawnType.SPAWN),
    var spawnOnJoin: Boolean = false,
    var joinMessage: String = "${ChatColor.DARK_GRAY}[${ChatColor.GREEN}+${ChatColor.DARK_GRAY}] ${ChatColor.GRAY}%player_name%",
    var quitMessage: String = "${ChatColor.DARK_GRAY}[${ChatColor.RED}-${ChatColor.DARK_GRAY}] ${ChatColor.GRAY}%player_name%",
    var cooldown: Int = 0,
    var delay: Int = 0,
): ConfigModel()

enum class RespawnType(val title: String) {
    RESPAWN_ANCHOR("Respawn Anchor"),
    BED("Bed"),
    HOME("Home"),
    SPAWN("Spawn")
}
