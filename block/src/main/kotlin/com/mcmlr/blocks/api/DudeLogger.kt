package com.mcmlr.blocks.api

import org.bukkit.Bukkit
import org.bukkit.ChatColor

fun log(level: Log, message: String) {
    val output = when (level) {
        Log.VERBOSE -> "${ChatColor.GRAY}[Apps] $message"
        Log.INFO -> "${ChatColor.DARK_AQUA}[Apps] $message"
        Log.DEBUG -> "${ChatColor.GREEN}[Apps] $message"
        Log.ERROR -> "${ChatColor.YELLOW}[Apps] $message"
        Log.ASSERT -> "${ChatColor.RED}${ChatColor.BOLD}[Apps] $message"
    }

    Bukkit.getServer().consoleSender.sendMessage(output)
}

fun serverVersion(): Versions = Versions.getByName(Bukkit.getBukkitVersion())

fun isSpigotServer(): Boolean = Bukkit.getVersion().lowercase().contains("spigot")

fun checkVersion(version: Versions): Boolean {
    val currentValue = serverVersion()
    if (currentValue.versionNumber == -1 || version.versionNumber == -1) return false
    return currentValue.versionNumber >= version.versionNumber
}

enum class Versions(val versionName: String, val versionNumber: Int) {
    UNKNOWN("", -1),
    V1_19_4("1.19.4-R0.1-SNAPSHOT", 0),
    V1_20("1.20-R0.1-SNAPSHOT", 1),
    V1_20_1("1.20.1-R0.1-SNAPSHOT", 2),
    V1_20_2("1.20.2-R0.1-SNAPSHOT", 3),
    V1_20_4("1.20.4-R0.1-SNAPSHOT", 4),
    V1_20_5("1.20.5-R0.1-SNAPSHOT", 5),
    V1_20_6("1.20.6-R0.1-SNAPSHOT", 6),
    V1_21("1.21-R0.1-SNAPSHOT", 7),
    V1_21_1("1.21.1-R0.1-SNAPSHOT", 8),
    V1_21_3("1.21.3-R0.1-SNAPSHOT", 9),
    V1_21_4("1.21.4-R0.1-SNAPSHOT", 10),
    V1_21_5("1.21.5-R0.1-SNAPSHOT", 11),
    V1_21_6("1.21.6-R0.1-SNAPSHOT", 12),
    V1_21_7("1.21.7-R0.1-SNAPSHOT", 13),
    V1_21_8("1.21.8-R0.1-SNAPSHOT", 14);

    companion object {
        fun getByName(name: String): Versions = entries.find { it.versionName == name } ?: UNKNOWN

        fun getByVersionNumber(versionNumber: Int): Versions = entries.find { it.versionNumber == versionNumber } ?: UNKNOWN
    }
}

enum class Log {
    VERBOSE,
    INFO,
    DEBUG,
    ERROR,
    ASSERT,
}