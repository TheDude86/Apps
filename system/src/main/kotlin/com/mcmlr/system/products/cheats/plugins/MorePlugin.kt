package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.entity.Player

class MorePlugin(
    private val player: Player,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.MORE

    override fun execute(data: CheatType) {
        val item = player.inventory.getItem(player.inventory.heldItemSlot) ?: return
        item.amount = item.type.maxStackSize
        player.inventory.setItem(player.inventory.heldItemSlot, item)
    }
}