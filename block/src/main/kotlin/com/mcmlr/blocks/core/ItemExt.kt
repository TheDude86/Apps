package com.mcmlr.blocks.core

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.net.URL
import java.util.*

fun getPlayerHead(url: String): ItemStack {
    val item = ItemStack(Material.PLAYER_HEAD)
    val meta = item.itemMeta as SkullMeta
    meta.ownerProfile = getProfile(url)

    item.itemMeta = meta
    return item
}

private fun getProfile(url: String): PlayerProfile {
    val profile = Bukkit.createPlayerProfile(UUID.randomUUID())
    val textures = profile.textures
    textures.skin = URL(url)
    profile.setTextures(textures)

    return profile
}