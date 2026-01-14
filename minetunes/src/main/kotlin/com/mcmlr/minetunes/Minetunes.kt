package com.mcmlr.minetunes

import com.mcmlr.pluginengine.Engine
import com.mcmlr.pluginengine.EngineModel
import com.mcmlr.system.products.minetunes.MineTunesEnvironment
import org.bukkit.plugin.java.JavaPlugin

class Minetunes : JavaPlugin() {
    private val engine = Engine(
        EngineModel(
            "mt",
            false,
            listOf(
                MineTunesEnvironment()
            ),
        )
    )

    override fun onEnable() {
        engine.onEnable(this, 27644)
    }

    override fun onDisable() {
        engine.onDisable()
    }
}