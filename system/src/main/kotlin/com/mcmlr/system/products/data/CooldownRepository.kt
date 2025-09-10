package com.mcmlr.system.products.data

import com.mcmlr.system.EnvironmentScope
import org.bukkit.entity.Player
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class CooldownRepository @Inject constructor() {

    private val homeMap = HashMap<UUID, Long>()
    private val warpMap = HashMap<UUID, Long>()
    private val teleportMap = HashMap<UUID, Long>()

    fun addPlayerLastHomeTime(player: Player) {
        homeMap[player.uniqueId] = Date().time
    }

    fun getPlayerLastHomeTime(player: Player): Long = homeMap[player.uniqueId] ?: 0L

    fun addPlayerLastWarpTime(player: Player) {
        warpMap[player.uniqueId] = Date().time
    }

    fun getPlayerLastWarpTime(player: Player): Long = warpMap[player.uniqueId] ?: 0L

    fun addPlayerLastTeleportTime(player: Player) {
        teleportMap[player.uniqueId] = Date().time
    }

    fun getPlayerLastTeleportTime(player: Player): Long = teleportMap[player.uniqueId] ?: 0L

}