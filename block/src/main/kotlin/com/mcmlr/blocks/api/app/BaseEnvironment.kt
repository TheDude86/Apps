package com.mcmlr.blocks.api.app

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.core.FlowDisposer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.net.URL
import java.util.*

abstract class BaseEnvironment<out T: BaseApp>: FlowDisposer() {
    lateinit var resources: Resources

    fun configure(resources: Resources) {
        this.resources = resources
    }

    abstract fun build()

    abstract fun getInstance(player: Player): T

    abstract fun name(): String

    abstract fun icon(): String

    abstract fun permission(): String?

    abstract fun summary(): String

    fun getAppIcon(): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val icon = icon()
        if (icon.isNotEmpty()) {
            val meta = item.itemMeta as SkullMeta
            meta.ownerProfile = getProfile(icon())
            item.itemMeta = meta
        }

        return item
    }

    private fun getProfile(url: String): PlayerProfile {
        val profile = Bukkit.createPlayerProfile(UUID.randomUUID())
        val textures = profile.textures
        textures.skin = URL(url)
        profile.setTextures(textures)

        return profile
    }
}