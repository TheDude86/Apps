package com.mcmlr.system.products.data

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID

data class LocationModel(
    val worldUUID: UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    fun toLocation(): Location = Location(Bukkit.getServer().getWorld(worldUUID), x, y, z, yaw, pitch)
}

fun Location.toLocationModel(): LocationModel? {
    val uuid = world?.uid ?: return null
    return LocationModel(uuid, x, y, z, yaw, pitch)
}
