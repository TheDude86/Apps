package com.mcmlr.blocks.api.data

import com.mcmlr.blocks.api.app.BaseApp
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class Origin(private val player: Player) {
    private val eyeLocation: Location = player.eyeLocation.clone()
    private val vector: Vector

    init {
        eyeLocation.pitch = 0f
        vector = eyeLocation.direction.normalize()
    }

    fun location(): Location = eyeLocation.add(vector.multiply(BaseApp.SCREEN_DISTANCE))
}