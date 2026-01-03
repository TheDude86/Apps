package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.WritableBookMeta

class BookPlugin(
    private val player: Player,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.BOOK

    override fun execute(data: CheatType) {
        val item = player.inventory.getItem(player.inventory.heldItemSlot) ?: return

        val meta = item.itemMeta as? WritableBookMeta ?: return

        val newBook = ItemStack(Material.WRITABLE_BOOK)
        (newBook.itemMeta as? WritableBookMeta)?.pages = meta.pages

        player.inventory.setItem(player.inventory.heldItemSlot, newBook)
    }
}