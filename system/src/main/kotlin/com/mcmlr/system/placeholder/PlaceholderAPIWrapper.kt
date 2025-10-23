package com.mcmlr.system.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player


fun String.placeholders(player: Player): String {
    val text = InternalPlaceholderEngine(this)
        .setPlayerName(player)
        .build()

    return if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
        PlaceholderAPI.setPlaceholders(player, text)
    } else {
        text
    }
}

private class InternalPlaceholderEngine(private var text: String) {
    fun setPlayerName(player: Player): InternalPlaceholderEngine {
        text = text.replace("%player_name%", player.name)
        return this
    }

    fun build() = text
}