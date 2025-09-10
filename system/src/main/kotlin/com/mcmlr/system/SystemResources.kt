package com.mcmlr.system

import com.mcmlr.blocks.api.Resources
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

data class SystemResources (
    private val server: Server,
    private val dataFolder: File,
    private val plugin: JavaPlugin,
): Resources {

    override fun server(): Server = server

    override fun dataFolder(): File = dataFolder

    override fun plugin(): JavaPlugin = plugin
}