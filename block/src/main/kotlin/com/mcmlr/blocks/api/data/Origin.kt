package com.mcmlr.blocks.api.data

import com.mcmlr.blocks.api.app.BaseApp
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min

class Origin(private val player: Player, var distance: Double = 0.15) {
    private var eyeLocation: Location = player.eyeLocation.clone()
    private var vector: Vector

    private var origin: Location

    init {
        eyeLocation.pitch = 0f
        vector = eyeLocation.direction.normalize()
        origin = eyeLocation.clone().add(vector.multiply(distance))
    }

    fun scrollOut() {
        distance = min(0.5, distance + 0.01)
        calibrate()
    }

    fun scrollIn() {
        distance = max(0.1, distance - 0.01)
        calibrate()
    }

    fun location(): Location = origin

    fun calibrate() {
        val location = player.eyeLocation.clone()
        location.pitch = 0f
        location.yaw = eyeLocation.yaw

        eyeLocation = location
        vector = location.direction.normalize()
        origin = eyeLocation.clone().add(vector.multiply(distance))
    }
}