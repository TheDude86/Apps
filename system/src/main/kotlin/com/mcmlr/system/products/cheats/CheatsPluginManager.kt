package com.mcmlr.system.products.cheats

import com.mcmlr.blocks.api.block.Router
import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.blocks.api.plugin.PluginManager
import com.mcmlr.system.products.cheats.plugins.*
import org.bukkit.entity.Player

class CheatsPluginManager(
    private val router: Router,
    private val player: Player,
    private val cheatsEventListener: CheatsEventListener,
): PluginManager<CheatType>() {

    override fun setPlugins(): HashSet<Plugin<CheatType>> = hashSetOf(
        SpawnerPlugin(cheatsEventListener),
        SuicidePlugin(player, router),
        CondensePlugin(player),
        BookPlugin(player),
        HatPlugin(player),
        MorePlugin(player),
        RestPlugin(player),
        ItemsPlugin(cheatsEventListener),
    )

}