package com.mcmlr.system.products.data

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.products.announcements.AnnouncementsBlock
import com.mcmlr.system.products.cheats.CheatsBlock
import com.mcmlr.system.products.homes.HomesBlock
import com.mcmlr.system.products.info.TutorialBlock
import com.mcmlr.system.products.kits.KitsBlock
import com.mcmlr.system.products.market.MarketBlock
import com.mcmlr.system.products.preferences.PreferencesBlock
import com.mcmlr.system.products.recipe.RecipesBlock
import com.mcmlr.system.products.settings.AdminBlock
import com.mcmlr.system.products.spawn.SpawnBlock
import com.mcmlr.system.products.teleport.TeleportBlock
import com.mcmlr.system.products.warps.WarpsBlock
import com.mcmlr.system.AppScope
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.workbenches.WorkbenchesBlock
import dagger.Lazy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.profile.PlayerProfile
import java.net.URL
import java.util.*
import javax.inject.Inject

@AppScope
class ApplicationsRepository @Inject constructor(
    private val permissionsRepository: PermissionsRepository,
    private val systemConfigRepository: SystemConfigRepository,
    private val homesBlock: Lazy<HomesBlock>,
    private val warpsBlock: Lazy<WarpsBlock>,
    private val teleportBlock: Lazy<TeleportBlock>,
    private val marketBlock: Lazy<MarketBlock>,
    private val adminBlock: Lazy<AdminBlock>,
    private val preferencesBlock: Lazy<PreferencesBlock>,
    private val workbenchesBlock: Lazy<WorkbenchesBlock>,
    private val recipesBlock: Lazy<RecipesBlock>,
    private val kitsBlock: Lazy<KitsBlock>,
    private val tutorialBlock: Lazy<TutorialBlock>,
    private val spawnBlock: Lazy<SpawnBlock>,
    private val cheatsBlock: Lazy<CheatsBlock>,
    private val announcementsBlock: Lazy<AnnouncementsBlock>,
) {

    private fun getAppIcon(url: String): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        meta.ownerProfile = getProfile(url)

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

    private val appMap = mapOf(
        "Admin" to { ApplicationModel("Admin", getAppIcon("http://textures.minecraft.net/texture/d0d5a4eba140cbb34d45e40d62dd3f7e6584b2ab0b4155a0bcc603d5d750f793"), adminBlock.get(), PermissionNode.ADMIN, "Settings only for server staff that effects Apps for all players.") },
        "Announcements" to { ApplicationModel("Announcements", getAppIcon("http://textures.minecraft.net/texture/d6b0ce673b3f28c4610cea7ce042c850e34cc988cb0d7c803979f50dd0f15731"), announcementsBlock.get(), PermissionNode.ADMIN, "Messages that server staff can write for all players to see on their home screen.") },
        "Homes" to { ApplicationModel("Homes", getAppIcon("http://textures.minecraft.net/texture/ff812112dd187e7c8ddb5c3b8e854e82f19197414a8cdb542021f1a491897e53"), homesBlock.get(), PermissionNode.HOME, "Allows players to set home locations in the world that they can then teleport to.") },
        "Warps" to { ApplicationModel("Warps", getAppIcon("http://textures.minecraft.net/texture/b0bfc2577f6e26c6c6f7365c2c4076bccee653124989382ce93bca4fc9e39b"), warpsBlock.get(), PermissionNode.WARP, "Allows server staff to set warp locations in the world that players can teleport to.") },
        "Teleport" to { ApplicationModel("Teleport", getAppIcon("http://textures.minecraft.net/texture/b1dd4fe4a429abd665dfdb3e21321d6efa6a6b5e7b956db9c5d59c9efab25"), teleportBlock.get(), PermissionNode.TELEPORT, "Allows players to teleport to other online players on the server.") },
        "Market" to { ApplicationModel("Market", getAppIcon("http://textures.minecraft.net/texture/533fc9a45be13ca57a78b21762c6e1262dae411f13048b963d972a29e07096ab"), marketBlock.get(), PermissionNode.MARKET, "A simple economy plugin where players can list in game items to sell for other players can purchase.") },
        "Preferences" to { ApplicationModel("Preferences", getAppIcon("http://textures.minecraft.net/texture/1e5edfe90156ce9b5b6b80793dc2cbfe850ddec856c99eebde31775cce956041"), preferencesBlock.get(), PermissionNode.PREFERENCES, "User settings that customize Apps for specific users.") },
        "Spawn" to { ApplicationModel("Spawn", getAppIcon("http://textures.minecraft.net/texture/9ad094dca5987a0ba2c3f98a2c8981a86f68b0f03b0ddda550d09f1defbcf207"), spawnBlock.get(), PermissionNode.SPAWN, "Changes spawning behavior allowing server staff to change the server spawn and where players are teleported to when they respawn.") },
//        "Workbenches" to { ApplicationModel("Workbenches", getAppIcon("http://textures.minecraft.net/texture/189f1e8764beed5e33a68b6190a03486b1b4b11a3a590688c75a897b9d10d95"), workbenchesBlock.get(), PermissionNode.WORKBENCH) },
        "Recipes" to { ApplicationModel("Recipes", getAppIcon("http://textures.minecraft.net/texture/c2ebbdb18d747281b5462f857ee984675a39d5a0274446a22f66264a53d2b034"), recipesBlock.get(), PermissionNode.RECIPE, "A resource for players to look up the crafting recipes for any in game item.") },
        "Kits" to { ApplicationModel("Kits", getAppIcon("http://textures.minecraft.net/texture/fe7a810d2112275cc1821dcc6e29da3d2b8fc659af7290a3cb70be536ae2040a"), kitsBlock.get(), PermissionNode.KIT, "Allows players to collect custom kits created by server staff.") },
        "Tutorial" to { ApplicationModel("Tutorial", getAppIcon("http://textures.minecraft.net/texture/fa2afa7bb063ac1ff3bbe08d2c558a7df2e2bacdf15dac2a64662dc40f8fdbad"), tutorialBlock.get(), PermissionNode.TUTORIAL, "A brief demonstration for how Apps works, a helpful resource for players who haven't used Apps before.") },
//        "Cheats" to { ApplicationModel("Cheats", getAppIcon("http://textures.minecraft.net/texture/1866ef71fb761e07e1084143cb86960674b18f0881a1190cf15a8e21d9dd9556"), cheatsBlock.get(), PermissionNode.CHEATS) },
        )

    private fun enabledApps() = appMap.filter { systemConfigRepository.model.enabledApps.contains(it.key) }

    fun getPlayerApps(player: Player): List<ApplicationModel> = enabledApps().values.filter { permissionsRepository.checkPermission(player, it.invoke().permissionNode) }.map { it.invoke() }

    fun getApps(): List<ApplicationModel> = enabledApps().values.map { it.invoke() }

    fun getApp(appName: String, player: Player): ApplicationModel? {
        val app = enabledApps()[appName]?.invoke()
        val permissionNode = app?.permissionNode ?: return null

        return if (permissionsRepository.checkPermission(player, permissionNode)) {
            enabledApps()[appName]?.invoke()
        } else {
            null
        }
    }

    fun getApp(appName: String): ApplicationModel? = enabledApps()[appName]?.invoke()
}

data class ApplicationModel(val appName: String, val appIcon: ItemStack, val headBlock: Block, val permissionNode: PermissionNode, val summary: String)
