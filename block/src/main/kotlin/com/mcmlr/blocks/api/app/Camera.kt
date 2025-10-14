package com.mcmlr.blocks.api.app

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class Camera(player: Player, var camera: LivingEntity? = null) {

    val origin = player.eyeLocation.clone()

    fun origin(): Location {
        val loc = (camera?.eyeLocation ?: origin).clone()
        loc.pitch = 0f

        val direction = loc.direction.normalize()
        return loc.add(direction.clone().multiply(offset()))
    }

    fun offset(): Double = if (camera == null) 0.18 else 0.18

    fun close() {
        camera?.remove()
    }

}