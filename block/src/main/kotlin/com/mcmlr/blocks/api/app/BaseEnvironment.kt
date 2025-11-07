package com.mcmlr.blocks.api.app

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.FlowDisposer
import kotlinx.coroutines.flow.flow
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.io.File
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
        val meta = item.itemMeta as SkullMeta
        meta.ownerProfile = getProfile(icon())

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
}

object R {

    val appsStringMaps = mutableMapOf<String, MutableMap<String, JsonObject>>()

    fun loadStringsAsync(app: String, locale: String) = flow {
        val appName = app.lowercase()
        val appStringsResource = appsStringMaps[appName]
        if (appStringsResource == null || appStringsResource[locale] == null) {
            log(Log.ASSERT, "$appName${File.separatorChar}$locale.json")
            val lines = this@R::class.java.getResourceAsStream("$appName${File.separatorChar}$locale.json")?.reader()

            if (appStringsResource == null) {
                val entry = mutableMapOf<String, JsonObject>()
                entry[locale] = JsonParser.parseReader(JsonReader(lines)).asJsonObject
                appsStringMaps[appName] = entry
            } else {
                appStringsResource[locale] = JsonParser.parseReader(JsonReader(lines)).asJsonObject
            }
        }

        emit(true)
    }

    fun loadStrings(app: String, locale: String) {
        val appName = app.lowercase()
        val appStringsResource = appsStringMaps[appName]
        if (appStringsResource == null || appStringsResource[locale] == null) {
            val lines = this::class.java.classLoader.getResource("$appName/$locale.json")?.readText()
            log(Log.ASSERT, "$appName${File.separatorChar}$locale.json")

            if (appStringsResource == null) {
                val entry = mutableMapOf<String, JsonObject>()
                entry[locale] = JsonParser.parseString(lines).asJsonObject
                appsStringMaps[appName] = entry
            } else {
                appStringsResource[locale] = JsonParser.parseString(lines).asJsonObject
            }
        }
    }

    fun getString(player: Player, resource: StringResource): String {
        val appStringsResource = appsStringMaps[resource.app] ?: return "#ERROR"
        val json = appStringsResource[player.locale] ?: return "#ERROR"
        val string = json.get(resource.id.lowercase())?.asString ?: return "#ERROR"

        return string
    }
}

enum class S {
    HOME,
    APPS,
    FAVORITES,
    ;

    fun resource(): StringResource = StringResource("system", this.name)
}

data class StringResource(val app: String, val id: String)

