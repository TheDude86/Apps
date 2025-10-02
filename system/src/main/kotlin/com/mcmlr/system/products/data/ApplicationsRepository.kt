package com.mcmlr.system.products.data

import com.mcmlr.blocks.api.*
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationsRepository @Inject constructor(
    private val resources: Resources,
    private val permissionsRepository: PermissionsRepository,
    private val systemConfigRepository: SystemConfigRepository,
)  {

    private val environments = mutableListOf<Environment<App>>()

    fun register(environment: Environment<App>) {
        environment.configure(resources)
        environment.build()
        environments.add(environment)
    }

//    fun launch(player: Player, appName: String, deeplink: String? = null) {
//        val environment = environments.find { it.name().lowercase() == appName.lowercase() }
//        if (environment == null) {
//            player.sendMessage("An app by this name doesn't exist...")
//            return
//        } else {
//            cursorRepository.updateActivePlayer(player.uniqueId, true)
//        }
//
//        environment.launch(player, deeplink)
//    }

//    TODO: Fixed enabled apps
//    private fun enabledApps() = environments.filter { systemConfigRepository.model.enabledApps.contains(it.name().lowercase()) }
    private fun enabledApps(): List<Environment<App>> = environments

    fun getPlayerApps(player: Player): List<Environment<App>> = enabledApps().filter {
        val node = it.permission() ?: return@filter true
        permissionsRepository.checkPermission(player, node)
    }

    fun getApps(): List<Environment<App>> = enabledApps()

    fun getApp(appName: String, player: Player): Environment<App>? {
        val environment = environments.find { it.name().lowercase() == appName.lowercase() } ?: return null
        val permissionNode = environment.permission() ?: return null

        return if (permissionsRepository.checkPermission(player, permissionNode)) {
            environment
        } else {
            null
        }
    }
}
