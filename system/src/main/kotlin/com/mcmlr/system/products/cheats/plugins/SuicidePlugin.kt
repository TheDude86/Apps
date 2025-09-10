package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.block.Router
import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.entity.Player

class SuicidePlugin(
    private val player: Player,
    private val router: Router,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.SUICIDE

    override fun execute(data: CheatType) {
        player.health = 0.0
        router.close()
    }
}