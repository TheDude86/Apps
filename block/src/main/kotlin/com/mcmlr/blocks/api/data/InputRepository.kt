package com.mcmlr.blocks.api.data

import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.ScrollModel
import kotlinx.coroutines.flow.Flow
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID

interface InputRepository {

    fun updateStream(data: CursorModel): Boolean

    fun updateMoveStream(event: PlayerMoveEvent)

    fun updateScrollStream(event: PlayerItemHeldEvent)

    fun cursorStream(playerId: UUID): Flow<CursorModel>

    fun playerMoveStream(playerId: UUID): Flow<PlayerMoveEvent>

    fun scrollStream(playerId: UUID): Flow<ScrollModel>

    fun updateActivePlayer(playerId: UUID, isActive: Boolean)

    fun updateUserScrollState(playerId: UUID, isScrolling: Boolean)

    fun chat(event: AsyncPlayerChatEvent)

    fun chatStream(): Flow<AsyncPlayerChatEvent>

    fun chatStream(playerId: UUID): Flow<AsyncPlayerChatEvent>

    fun updateUserInputState(playerId: UUID, getInput: Boolean)

    fun onPlayerJoined(player: Player)

    fun onPlayerQuit(player: Player)

    fun onPlayerDeath(player: Player)

    fun onlinePlayerEventStream(): Flow<PlayerOnlineEvent>
}

data class PlayerOnlineEvent(val player: Player, val eventType: PlayerOnlineEventType)

enum class PlayerOnlineEventType {
    JOINED,
    QUIT,
    DIED,
}
