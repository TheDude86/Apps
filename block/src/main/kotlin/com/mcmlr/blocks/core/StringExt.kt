package com.mcmlr.blocks.core

import org.bukkit.ChatColor
import java.util.Locale

fun String.titlecase(): String {
    val chars = mapIndexed { i, c ->
        if (i == 0 || get(i - 1) == ' ') {
            c.uppercaseChar()
        } else {
            c.lowercaseChar()
        }
    }

    return String(chars.toCharArray())
}

fun String.fromMCItem(): String = replace("_", " ").lowercase().titlecase()

fun String.colorize(): String = replace("&0", "${ChatColor.BLACK}")
    .replace("&1", "${ChatColor.DARK_BLUE}")
    .replace("&2", "${ChatColor.DARK_GREEN}")
    .replace("&3", "${ChatColor.DARK_AQUA}")
    .replace("&4", "${ChatColor.DARK_RED}")
    .replace("&5", "${ChatColor.DARK_PURPLE}")
    .replace("&6", "${ChatColor.GOLD}")
    .replace("&7", "${ChatColor.GRAY}")
    .replace("&8", "${ChatColor.DARK_GRAY}")
    .replace("&9", "${ChatColor.BLUE}")
    .replace("&a", "${ChatColor.GREEN}")
    .replace("&b", "${ChatColor.AQUA}")
    .replace("&c", "${ChatColor.RED}")
    .replace("&d", "${ChatColor.LIGHT_PURPLE}")
    .replace("&e", "${ChatColor.YELLOW}")
    .replace("&k", "${ChatColor.MAGIC}")
    .replace("&l", "${ChatColor.BOLD}")
    .replace("&m", "${ChatColor.STRIKETHROUGH}")
    .replace("&n", "${ChatColor.UNDERLINE}")
    .replace("&o", "${ChatColor.ITALIC}")
    .replace("&r", "${ChatColor.RESET}")

fun String.bolden(): String {
    val replaced = replace("${ChatColor.BLACK}", "${ChatColor.BLACK}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_BLUE}", "${ChatColor.DARK_BLUE}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_GREEN}", "${ChatColor.DARK_GREEN}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_AQUA}", "${ChatColor.DARK_AQUA}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_RED}", "${ChatColor.DARK_RED}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_PURPLE}", "${ChatColor.DARK_PURPLE}${ChatColor.BOLD}")
        .replace("${ChatColor.GOLD}", "${ChatColor.GOLD}${ChatColor.BOLD}")
        .replace("${ChatColor.GRAY}", "${ChatColor.GRAY}${ChatColor.BOLD}")
        .replace("${ChatColor.DARK_GRAY}", "${ChatColor.DARK_GRAY}${ChatColor.BOLD}")
        .replace("${ChatColor.BLUE}", "${ChatColor.BLUE}${ChatColor.BOLD}")
        .replace("${ChatColor.GREEN}", "${ChatColor.GREEN}${ChatColor.BOLD}")
        .replace("${ChatColor.AQUA}", "${ChatColor.AQUA}${ChatColor.BOLD}")
        .replace("${ChatColor.RED}", "${ChatColor.RED}${ChatColor.BOLD}")
        .replace("${ChatColor.LIGHT_PURPLE}", "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}")
        .replace("${ChatColor.YELLOW}", "${ChatColor.YELLOW}${ChatColor.BOLD}")
        .replace("${ChatColor.WHITE}", "${ChatColor.WHITE}${ChatColor.BOLD}")
        .replace("${ChatColor.RESET}", "${ChatColor.RESET}${ChatColor.BOLD}")

    return "${ChatColor.BOLD}$replaced${ChatColor.RESET}"
}

fun String.underline(): String {
    val replaced = replace("${ChatColor.BLACK}", "${ChatColor.BLACK}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_BLUE}", "${ChatColor.DARK_BLUE}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_GREEN}", "${ChatColor.DARK_GREEN}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_AQUA}", "${ChatColor.DARK_AQUA}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_RED}", "${ChatColor.DARK_RED}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_PURPLE}", "${ChatColor.DARK_PURPLE}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.GOLD}", "${ChatColor.GOLD}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.GRAY}", "${ChatColor.GRAY}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.DARK_GRAY}", "${ChatColor.DARK_GRAY}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.BLUE}", "${ChatColor.BLUE}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.GREEN}", "${ChatColor.GREEN}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.AQUA}", "${ChatColor.AQUA}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.RED}", "${ChatColor.RED}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.LIGHT_PURPLE}", "${ChatColor.LIGHT_PURPLE}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.YELLOW}", "${ChatColor.YELLOW}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.WHITE}", "${ChatColor.WHITE}${ChatColor.UNDERLINE}")
        .replace("${ChatColor.RESET}", "${ChatColor.RESET}${ChatColor.UNDERLINE}")

    return "${ChatColor.UNDERLINE}$replaced${ChatColor.RESET}"
}

fun String.priceFormat(): String {
    if (toDoubleOrNull() == null) return this
    if (!contains('.')) return "$this.00"

    val components = split('.')
    val last = components.last()
    val cents = when (last.length) {
        0 -> "00"
        1 -> "${last}0"
        2 -> last
        else -> last.substring(0..1)
    }

    val dollars = components.first().ifEmpty { "0" }.toInt()

    return "$dollars.$cents"
}

fun String.toLocale(): Locale? {
    var locale = split("_")
    if (locale.size != 2) return null

    val language = locale[0]
    val country = locale[1]

    return Locale(language, country)
}
