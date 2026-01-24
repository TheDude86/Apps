package com.mcmlr.folia

import org.bukkit.Location
import org.bukkit.entity.Entity

object FoliaFactory {
    fun teleport(entity: Entity, location: Location) {
        if (isFolia()) {
            entity.teleportAsync(location)
        } else {
            entity.teleport(location)
        }
    }

    private fun isFolia(): Boolean {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
}
