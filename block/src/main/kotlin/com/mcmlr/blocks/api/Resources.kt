package com.mcmlr.blocks.api

import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

interface Resources {

    fun server(): Server

    fun dataFolder(): File

    fun plugin(): JavaPlugin
}