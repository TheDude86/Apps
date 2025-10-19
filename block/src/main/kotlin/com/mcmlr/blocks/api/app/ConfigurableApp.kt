package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.block.Block
import org.bukkit.entity.Player

abstract class ConfigurableApp(player: Player): App(player) {

    fun createConfig(resources: Resources) {
        this.resources = resources
        onCreate()
        head = config()
        head.context = this
        head.onCreate()
    }

    abstract fun config(): Block

}