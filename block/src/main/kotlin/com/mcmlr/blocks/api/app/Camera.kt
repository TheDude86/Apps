package com.mcmlr.blocks.api.app

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class Camera(val player: Player, var camera: Entity? = null) {

    fun origin(): Location = camera?.location ?: player.eyeLocation

}