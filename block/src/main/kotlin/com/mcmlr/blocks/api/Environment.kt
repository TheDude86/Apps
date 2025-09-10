package com.mcmlr.blocks.api

import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.core.FlowDisposer
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

abstract class Environment<out T: App>: FlowDisposer() {
    private val appMap = HashMap<UUID, T>()

    private lateinit var cursorRepository: CursorRepository
    private lateinit var playerChatRepository: PlayerChatRepository
    lateinit var resources: Resources

    open fun configure(
        cursorRepository: CursorRepository,
        playerChatRepository: PlayerChatRepository,
        resources: Resources,
    ) {
        this.cursorRepository = cursorRepository
        this.playerChatRepository = playerChatRepository
        this.resources = resources
    }

    abstract fun build()

    abstract fun getInstance(player: Player): T

    abstract fun name(): String

    fun launch(player: Player, deeplink: String?) {
        val app = getInstance(player)
        app.configure(this, deeplink, origin(player))

        if (appMap.containsKey(app.player.uniqueId)) {
            appMap[app.player.uniqueId]?.close()
        }

        app.create(cursorRepository, playerChatRepository, resources)
        appMap[app.player.uniqueId] = app
    }

    private fun origin(player: Player): Location {
        val o = player.eyeLocation.clone()
        o.pitch = 0f //TODO: Fix pitch translation issue & remove

        val direction = o.direction.normalize()
        return o.add(direction.multiply(0.15))
    }

    fun close(player: Player) {
        val app = appMap.remove(player.uniqueId) ?: return
        app.close()
        playerChatRepository.updateUserInputState(player.uniqueId, false)
        cursorRepository.updateUserScrollState(player.uniqueId, false)
    }

    fun notifyClose(player: Player) {
        appMap.remove(player.uniqueId) ?: return
        playerChatRepository.updateUserInputState(player.uniqueId, false)
        cursorRepository.updateUserScrollState(player.uniqueId, false)
    }

    fun shutdown() {
        appMap.values.forEach { it.close() }
    }

    fun disable() {
        appMap.values.forEach { it.clear() }
    }
}