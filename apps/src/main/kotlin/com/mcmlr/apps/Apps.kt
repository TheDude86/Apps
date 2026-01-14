package com.mcmlr.apps

import com.mcmlr.pluginengine.Engine
import com.mcmlr.pluginengine.EngineModel
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.homes.HomesEnvironment
import com.mcmlr.system.products.info.TutorialEnvironment
import com.mcmlr.system.products.kits.KitsEnvironment
import com.mcmlr.system.products.market.MarketEnvironment
import com.mcmlr.system.products.minetunes.MineTunesEnvironment
import com.mcmlr.system.products.pong.PongEnvironment
import com.mcmlr.system.products.preferences.PreferencesEnvironment
import com.mcmlr.system.products.recipe.RecipeEnvironment
import com.mcmlr.system.products.settings.AdminEnvironment
import com.mcmlr.system.products.spawn.SpawnEnvironment
import com.mcmlr.system.products.teleport.TeleportEnvironment
import com.mcmlr.system.products.warps.WarpsEnvironment
import com.mcmlr.system.products.yaml.YAMLEnvironment
import org.bukkit.plugin.java.JavaPlugin

class Apps : JavaPlugin() {
    private val engine = Engine(
        EngineModel(
            ".",
            true,
            listOf(
                AdminEnvironment(),
                AnnouncementsEnvironment(),
                HomesEnvironment(),
                WarpsEnvironment(),
                TeleportEnvironment(),
                MarketEnvironment(),
                PreferencesEnvironment(),
                SpawnEnvironment(),
//                WorkbenchesEnvironment(),
                RecipeEnvironment(),
                KitsEnvironment(),
                YAMLEnvironment(),
                PongEnvironment(),
                MineTunesEnvironment(),
                TutorialEnvironment(),
//                CheatsEnvironment(),
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
