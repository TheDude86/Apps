package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.blocks.core.add
import com.mcmlr.blocks.core.remove
import com.mcmlr.system.products.cheats.CheatType
import org.bukkit.Material
import org.bukkit.entity.Player

class CondensePlugin(
    private val player: Player,
): Plugin<CheatType> {

    private val condensableItems = hashMapOf(
        Material.BAMBOO to Pair(Material.BAMBOO_BLOCK, 9),
        Material.COAL to Pair(Material.COAL_BLOCK, 9),
        Material.IRON_INGOT to Pair(Material.IRON_BLOCK, 9),
        Material.GOLD_INGOT to Pair(Material.GOLD_BLOCK, 9),
        Material.REDSTONE to Pair(Material.REDSTONE_BLOCK, 9),
        Material.EMERALD to Pair(Material.EMERALD_BLOCK, 9),
        Material.LAPIS_LAZULI to Pair(Material.LAPIS_BLOCK, 9),
        Material.DIAMOND to Pair(Material.DIAMOND_BLOCK, 9),
        Material.NETHERITE_INGOT to Pair(Material.NETHERITE_BLOCK, 9),
        Material.QUARTZ to Pair(Material.QUARTZ_BLOCK, 4),
        Material.AMETHYST_SHARD to Pair(Material.AMETHYST_BLOCK, 4),
        Material.RAW_IRON to Pair(Material.RAW_IRON_BLOCK, 9),
        Material.RAW_COPPER to Pair(Material.RAW_COPPER_BLOCK, 9),
        Material.RAW_GOLD to Pair(Material.RAW_GOLD_BLOCK, 9),
        Material.RESIN_CLUMP to Pair(Material.RESIN_BLOCK, 9),
        Material.BRICK to Pair(Material.BRICKS, 4),
        Material.NETHER_BRICK to Pair(Material.NETHER_BRICKS, 4),
        Material.RESIN_BRICK to Pair(Material.RESIN_BRICKS, 4),
        Material.DRIED_KELP to Pair(Material.DRIED_KELP_BLOCK, 9),
        Material.HONEYCOMB to Pair(Material.HONEYCOMB_BLOCK, 4),
        Material.STRING to Pair(Material.WHITE_WOOL, 4),
        Material.GLOWSTONE_DUST to Pair(Material.GLOWSTONE, 4),
        Material.BONE_MEAL to Pair(Material.BONE_BLOCK, 9),
        Material.WHEAT to Pair(Material.HAY_BLOCK, 9),
        Material.SNOWBALL to Pair(Material.SNOW_BLOCK, 4),
        Material.MELON_SLICE to Pair(Material.MELON, 9),
        Material.ICE to Pair(Material.PACKED_ICE, 9),
        Material.PACKED_ICE to Pair(Material.BLUE_ICE, 9),
        Material.SAND to Pair(Material.SANDSTONE, 4),
        Material.RED_SAND to Pair(Material.RED_SANDSTONE, 4),
    )

    override fun isApplicable(data: CheatType): Boolean = data == CheatType.CONDENSE

    override fun execute(data: CheatType) {

        condensableItems.forEach { condensableMaterial ->
            var itemCount = 0
            player.inventory.forEach playerInventory@{ playerItem ->
                if (playerItem == null) return@playerInventory
                if (playerItem.type == condensableMaterial.key) itemCount += playerItem.amount
            }

            player.inventory.remove(condensableMaterial.key, itemCount)
            player.inventory.add(
                player.location,
                condensableMaterial.value.first,
                itemCount / condensableMaterial.value.second
            )
            player.inventory.add(player.location, condensableMaterial.key, itemCount % condensableMaterial.value.second)
        }
    }
}
