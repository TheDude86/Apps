package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.Statistic
import org.bukkit.entity.Player

class RestPlugin(
    private val player: Player,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.REST

    override fun execute(data: CheatType) {
        player.setStatistic(Statistic.TIME_SINCE_REST, 0)
    }
}