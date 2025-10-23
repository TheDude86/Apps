package com.mcmlr.system.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player


fun String.placeholders(player: Player) = if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
    PlaceholderAPI.setPlaceholders(player, this)
} else {
    InternalPlaceholderEngine(this)
        .setPlayerName(player)
        .build()
}

private class InternalPlaceholderEngine(private var text: String) {
    fun setPlayerName(player: Player): InternalPlaceholderEngine {
        text = text.replace("%player_name%", player.name)
        return this
    }

    fun build() = text
}

class AppsDefaultPlayerExpansion : PlaceholderExpansion() {
    override fun getAuthor(): String = "The_Dude___"

    override fun getIdentifier(): String = "player"

    override fun getVersion(): String = "1.0"



    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (params.equals("name", ignoreCase = true)) {
            return player?.name
        }

        return null
    }
}