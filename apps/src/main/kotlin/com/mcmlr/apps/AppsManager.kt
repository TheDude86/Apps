package com.mcmlr.apps

import com.mcmlr.blocks.api.App
import com.mcmlr.blocks.api.Environment
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsManager @Inject constructor(
    private val cursorRepository: CursorRepository,
    private val playerChatRepository: PlayerChatRepository,
    private val resources: Resources,
) {
    private val environments = mutableListOf<Environment<App>>()

    fun register(environment: Environment<App>) {
        environment.configure(cursorRepository, playerChatRepository, resources)
        environment.build()
        environments.add(environment)
    }

    fun launch(player: Player, appName: String, deeplink: String? = null) {
        val environment = environments.find { it.name().lowercase() == appName.lowercase() }
        if (environment == null) {
            player.sendMessage("An app by this name doesn't exist...")
            return
        } else {
            cursorRepository.updateActivePlayer(player.uniqueId, true)
        }

        environment.launch(player, deeplink)
    }

    fun close(player: Player) {
        cursorRepository.updateActivePlayer(player.uniqueId, false)
        environments.forEach { it.close(player) }
    }

    fun shutdown() {
        environments.forEach { it.shutdown() }
    }

    fun disable() {
        environments.forEach {
            it.disable()
            it.clear()
        }
    }
}
