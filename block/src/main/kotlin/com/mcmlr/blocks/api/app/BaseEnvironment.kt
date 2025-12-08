package com.mcmlr.blocks.api.app

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.core.FlowDisposer
import kotlinx.coroutines.flow.flow
import org.bukkit.Bukkit
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

    var defaultLocale: Locale = Locale.US
    val appsStringMaps = mutableMapOf<String, MutableMap<String, JsonObject>>()

    private val localeMap = mapOf(
        //English
        "en_us" to "en_us",
        "en_pt" to "en_us",
        "en_au" to "en_us",
        "en_ca" to "en_us",
        "en_gb" to "en_us",
        "en_nz" to "en_us",
        "en_ud" to "en_us",
        "enp" to "en_us",
        "enws" to "en_us",
        //French
        "fr_fr" to "fr_fr",
        "fr_ca" to "fr_fr",
        //Spanish
        "es_es" to "es_es",
        "es_ar" to "es_es",
        "es_cl" to "es_es",
        "es_ec" to "es_es",
        "es_mx" to "es_es",
        "es_uy" to "es_es",
        "es_ve" to "es_es",
    )

    fun containsLocale(locale: String): Boolean = appsStringMaps.values.firstOrNull()?.containsKey(locale) == true

    fun addLanguage(locale: String, languageData: JsonObject) {
        appsStringMaps.keys.forEach {
            val app = appsStringMaps[it] ?: return@forEach
            app[locale] = languageData.getAsJsonObject(it)
        }
    }

    fun loadStringsAsync(app: String, locale: String) = flow {
        val appName = app.lowercase()
        val appStringsResource = appsStringMaps[appName]
        if (appStringsResource == null || appStringsResource[locale] == null) {
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
        val mappedLocale = getMappedLocale(locale)

        val appName = app.lowercase()
        val appStringsResource = appsStringMaps[appName]
        if (appStringsResource == null || appStringsResource[mappedLocale] == null) {
            val lines = this::class.java.classLoader.getResource("${appName}/$mappedLocale.json")?.readText()
                ?: this::class.java.classLoader.getResource("${appName}/${defaultLocale.toString().lowercase()}.json")?.readText()
                ?: return

            if (appStringsResource == null) {
                val entry = mutableMapOf<String, JsonObject>()
                entry[mappedLocale] = JsonParser.parseString(lines).asJsonObject
                appsStringMaps[appName] = entry
            } else {
                appStringsResource[mappedLocale] = JsonParser.parseString(lines).asJsonObject
            }
        }
    }

//    This method will save the lang files to Apps plugin folder so server staff can modify the strings, the issue with this method is how to handle when a plugin update adds new strings?
//    fun loadStrings(dataFolder: File, app: String, locale: String) {
//        val languageDirectory = File(dataFolder, "Lang")
//        val appLanguageDirectory = File(languageDirectory, app)
//        val localeFile = File(appLanguageDirectory, "$locale.json")
//        val appName = app.lowercase()
//
//        if (!appLanguageDirectory.exists()) {
//            appLanguageDirectory.mkdirs()
//        }
//
//        if (!localeFile.exists()) {
//            val lines = this::class.java.classLoader.getResource("${appName}/$locale.json")?.readText() ?: "{}"
//            val writer = FileWriter(localeFile)
//            writer.append(lines)
//            writer.close()
//
//            val entry = mutableMapOf<String, JsonObject>()
//            entry[locale] = JsonParser.parseString(lines).asJsonObject
//            appsStringMaps[appName] = entry
//        } else if (appsStringMaps.containsKey(appName)) {
//            val langInputStream: InputStream = localeFile.inputStream()
//            val langInputString = langInputStream.bufferedReader().use { it.readText() }
//
//            val entry = mutableMapOf<String, JsonObject>()
//            entry[locale] = JsonParser.parseString(langInputString).asJsonObject
//            appsStringMaps[appName] = entry
//        }
//
//    }

    fun getString(player: Player, resource: StringResource): String {
        val locale = getMappedLocale(player.locale)
        val appStringsResource = appsStringMaps[resource.app] ?: return "#ERROR"
        val json = appStringsResource[locale] ?: appStringsResource[defaultLocale.toString().lowercase()] ?: return "#ERROR"
        val string = json.get(resource.id.lowercase())?.asString ?: return "#ERROR"

        return string
    }

    fun getString(player: Player, resource: StringResource, vararg args: Any?): String {
        val appStringsResource = appsStringMaps[resource.app] ?: return "#ERROR"
        val json = appStringsResource[player.locale] ?: return "#ERROR"
        var string = json.get(resource.id.lowercase())?.asString ?: return "#ERROR"

        args.forEach {
            string = string.replaceFirst("%s", it.toString())
        }

        return string
    }

    private fun getMappedLocale(locale: String): String = localeMap[locale] ?: locale
}

data class StringResource(val app: String, val id: String)
