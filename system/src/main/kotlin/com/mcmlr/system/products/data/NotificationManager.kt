package com.mcmlr.system.products.data

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION") //TODO: Replace HoverEvent with whatever is current
@Singleton
class NotificationManager @Inject constructor() {

    fun sendCTAMessage(
        receiver: Player,
        message: String,
        hoverMessage: String,
        cta: String,
        command: String
    ) {
        val notification = ComponentBuilder(message)
            .append(" ${ChatColor.AQUA}[$cta]")
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverMessage).create()))
            .build()
        receiver.spigot().sendMessage(notification)
    }

    fun sendMessage(receiver: Player, message: String) {
//        receiver.sendMessage(message)
        receiver.sendRawMessage("{\"text\":\"Hover me!\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Hi!\"}}")
    }

}