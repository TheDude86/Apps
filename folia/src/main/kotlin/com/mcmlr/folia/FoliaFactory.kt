package com.mcmlr.folia

import org.bukkit.Location
import org.bukkit.entity.Entity

//Don't judge the code quality here, I know this is all a bunch of hacky fixes I haven't bothered to properly refactor yet
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