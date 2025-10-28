package com.mcmlr.blocks.api.views

import org.bukkit.inventory.ItemStack

class ItemView(
    modifier: Modifier,
    var item: ItemStack,
    teleportDuration: Int = 3,
    height: Int = 0,
): View(modifier, teleportDuration = teleportDuration, height = height) {

    override fun render() {
        dudeDisplay = parent.addItemDisplay(this)
    }

    //TODO: Add override to ItemButton, Container
    override fun updateVisibility(visible: Boolean) {
        super.updateVisibility(visible)
        parent.updateItemDisplay(this)
    }

    override fun updateDisplay() {
        parent.updateItemDisplay(this)
    }

    override fun getWrappedDimension(dimension: Area): Int = 40

    override fun top(): Int = getPosition().y + getDimensions().height

    override fun bottom(): Int = getPosition().y - getDimensions().height
}
