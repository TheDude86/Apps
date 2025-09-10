package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.data.ConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.AppScope
import com.mcmlr.system.products.data.CooldownRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import javax.inject.Inject

@AppScope
class WarpsRepository @Inject constructor(
    private val cooldownRepository: CooldownRepository,
    private val warpsConfigRepository: WarpsConfigRepository,
): Repository<ServerWarpsModel>() {

    private var updatingWarp: WarpModel? = null

    init {
        loadModel("Warps", "warps", ServerWarpsModel(listOf()))
    }

    fun teleport(player: Player, location: Location) {
        player.teleport(location)
        cooldownRepository.addPlayerLastWarpTime(player)
    }

    fun canTeleport(player: Player): Long {
        val lastTeleport = cooldownRepository.getPlayerLastWarpTime(player)
        return (lastTeleport + (warpsConfigRepository.cooldown() * 1000)) - Date().time
    }

    fun getWarps() = model.warps

    fun updateWarp(warpModel: WarpModel?) {
        updatingWarp = warpModel
    }

    fun getUpdateBuilder(): WarpModel.Builder? {
        val update = updatingWarp ?: return null
        return WarpModel.Builder()
            .name(update.name)
            .icon(update.icon)
            .uuid(update.uuid)
            .location(
                Location(
                    Bukkit.getWorld(update.world),
                    update.x,
                    update.y,
                    update.z,
                    update.yaw,
                    update.pitch,
                )
            )
    }

    fun saveWarp(warpModel: WarpModel) = save {
        val existingHomes = model.warps.filter { it.uuid != warpModel.uuid }
        val warpsList = mutableListOf(warpModel)
        warpsList.addAll(existingHomes)
        model.warps = warpsList
    }

    fun deleteWarp(homeModel: WarpModel) = save {
        val updatedWarps = model.warps.filter { it.uuid != homeModel.uuid }
        model.warps = updatedWarps
    }
}

data class ServerWarpsModel(
    var warps: List<WarpModel>,
): ConfigModel()

data class WarpModel(
    val uuid: UUID,
    val icon: Material,
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
) {


    class Builder {
        var icon: Material? = null
        var name: String? = null
        var location: Location? = null
        var uuid: UUID? = null

        fun icon(icon: Material?): Builder {
            this.icon = icon
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun location(location: Location): Builder {
            this.location = location
            return this
        }

        fun uuid(uuid: UUID): Builder {
            this.uuid = uuid
            return this
        }

        fun build(): WarpModel? {
            return icon?.let { icon ->
                name?.let { name ->
                    location?.let { location ->
                        return WarpModel(
                            uuid = uuid ?: UUID.randomUUID(),
                            icon = icon,
                            name = name,
                            x = location.x,
                            y = location.y,
                            z = location.z,
                            yaw = 0f,
                            pitch = 0f,
                            world = location.world?.name ?: "null",
                        )
                    }
                }
            }
        }
    }
}
