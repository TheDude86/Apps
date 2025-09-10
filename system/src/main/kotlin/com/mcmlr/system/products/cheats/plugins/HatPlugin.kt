package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.entity.Player

class HatPlugin(
    private val player: Player,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.HAT

    override fun execute(data: CheatType) {
        val item = player.inventory.getItem(player.inventory.heldItemSlot) ?: return
        val currentHelmet = player.inventory.helmet
        player.inventory.helmet = item
        player.inventory.setItem(player.inventory.heldItemSlot, currentHelmet)
    }
}