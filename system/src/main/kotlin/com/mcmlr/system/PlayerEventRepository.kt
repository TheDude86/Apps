package com.mcmlr.system

import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerEventRepository @Inject constructor() {

    private val onlinePlayersEventStream = MutableSharedFlow<PlayerOnlineEvent>()

    fun onPlayerJoined(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.JOINED))
    }

    fun onPlayerQuit(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.QUIT))
    }

    fun onPlayerDeath(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.DIED))
    }

    fun onlinePlayerEventStream(): Flow<PlayerOnlineEvent> = onlinePlayersEventStream
}

data class PlayerOnlineEvent(val player: Player, val eventType: PlayerOnlineEventType)

enum class PlayerOnlineEventType {
    JOINED,
    QUIT,
    DIED,
}