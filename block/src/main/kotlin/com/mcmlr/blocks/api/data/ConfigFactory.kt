package com.mcmlr.blocks.api.data

import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import java.io.File
import java.io.FileWriter

data class AppConfigModel(
    var title: String = "${ChatColor.BOLD}Welcome to ${ChatColor.GOLD}${ChatColor.BOLD}Apps!",
    var usePermissions: Boolean = true,
    var setupComplete: Boolean = false,
    var enabledApps: List<String> = listOf(
        "admin",
        "announcements",
        "homes",
        "warps",
        "teleport",
        "market",
        "preferences",
        "spawn",
        "workbenches",
        "recipes",
        "kits",
        "tutorial",
        "cheats",
    ),
): ConfigModel()

data class MarketConfigModel(
    var maxOrders: Int = 0
): ConfigModel()

data class TeleportConfigModel(
    var delay: Int = 0,
    var cooldown: Int = 0,
): ConfigModel()

data class WarpConfigModel(
    var delay: Int = 0,
    var cooldown: Int = 0,
): ConfigModel()



class HomeConfigModel(
    var maxHomes: Int = 1,
    var delay: Int = 0,
    var cooldown: Int = 0,
): ConfigModel()

open class ConfigModel() {

    @Transient var root: File? = null
    @Transient var filePath: String = "path"
    @Transient var fileName: String = "config"

    fun save(callback: ConfigModel.() -> Unit): ConfigModel {
        saveFile(filePath, fileName, this, callback)

        return this
    }

    private fun saveFile(path: String, name: String, model: Any, finishedCallback: ConfigModel.() -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(root, path)

            val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()

            val fileConfigString = gson.toJson(model)
            val fileWriter = FileWriter(File(file.path, "$name.json"))
            fileWriter.append(fileConfigString)
            fileWriter.close()

            finishedCallback()
        }
    }
}

fun <T: ConfigModel> T.save(callback: T.() -> Unit): T {
    saveFile(filePath, fileName, this, callback)

    return this
}

fun <T: ConfigModel> T.saveFile(path: String, name: String, model: Any, finishedCallback: T.() -> Unit = {}) {
    CoroutineScope(Dispatchers.IO).launch {
        val file = File(root, path)

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val fileConfigString = gson.toJson(model)
        val fileWriter = FileWriter(File(file.path, "$name.json"))
        fileWriter.append(fileConfigString)
        fileWriter.close()

        finishedCallback()
    }
}
