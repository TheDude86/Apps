package com.mcmlr.blocks.api.app

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class Camera(val player: Player, var camera: LivingEntity? = null) {

    fun origin(): Location = (camera?.eyeLocation ?: player.eyeLocation).clone()

    fun offset(): Double = if (camera == null) 0.15 else 0.18

}