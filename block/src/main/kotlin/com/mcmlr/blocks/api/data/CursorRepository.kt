package com.mcmlr.blocks.api.data

import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.ScrollModel
import kotlinx.coroutines.flow.Flow
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

interface CursorRepository {

    fun updateStream(data: CursorModel): Boolean

    fun updateMoveStream(event: PlayerMoveEvent)

    fun updateScrollStream(event: PlayerItemHeldEvent)

    fun cursorStream(playerId: UUID): Flow<CursorModel>

    fun playerMoveStream(playerId: UUID): Flow<PlayerMoveEvent>

    fun scrollStream(playerId: UUID): Flow<ScrollModel>

    fun updateActivePlayer(playerId: UUID, isActive: Boolean)

    fun updateUserScrollState(playerId: UUID, isScrolling: Boolean)
}
