package com.mcmlr.blocks.api.views

import org.bukkit.entity.EntityType

class EntityView(
    modifier: Modifier,
    var entity: EntityType,
    height: Int = 0,
): View(modifier, teleportDuration = 0, height = height) {
    override fun render() {
        dudeDisplay = parent.addEntityDisplay(this)
    }

    override fun updateDisplay() {
        parent.updateEntityDisplay(this)
    }
}


