package com.mcmlr.blocks.api.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min

interface Origin {

    var distance: Double

    var scale: Int

    fun scrollOut()

    fun scrollIn()

    fun location(): Location

    fun distance(): Double
}

class PlayerOrigin(
    private val player: Player,
    override var distance: Double = 0.15,
    override var scale: Int = 1,
): Origin {
    private var eyeLocation: Location = player.eyeLocation.clone()
    private var vector: Vector

    private var origin: Location

    init {
        eyeLocation.pitch = 0f
        vector = eyeLocation.direction.normalize()
        origin = eyeLocation.clone().add(vector.multiply(distance))
    }

    override fun scrollOut() {
        distance = min(0.5, distance + 0.01)
        calibrate()
    }

    override fun scrollIn() {
        distance = max(0.1, distance - 0.01)
        calibrate()
    }

    override fun location(): Location = origin

    override fun distance(): Double = distance

    fun calibrate() {
        val location = player.eyeLocation.clone()
        location.pitch = 0f
        location.yaw = eyeLocation.yaw

        eyeLocation = location
        vector = location.direction.normalize()
        origin = eyeLocation.clone().add(vector.multiply(distance))
    }
}

class BillboardOrigin(private val billboard: BillboardModel, private val player: Player): Origin {
    override var distance: Double = billboard.location.distance(player.eyeLocation)
    override var scale: Int = billboard.scale

    override fun scrollOut() {}

    override fun scrollIn() {}

    override fun location(): Location {
        val l = billboard.location
        if (billboard.fixed) {
            l.yaw = billboard.rotation
        } else {
            l.direction = Vector(billboard.x - player.eyeLocation.x, 0.0, billboard.z - player.eyeLocation.z)
        }

        return l
    }

    override fun distance(): Double = billboard.location.distance(player.eyeLocation)
}
