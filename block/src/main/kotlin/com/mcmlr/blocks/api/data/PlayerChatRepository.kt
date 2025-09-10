package com.mcmlr.blocks.api.data

import kotlinx.coroutines.flow.Flow
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.UUID

interface PlayerChatRepository {
    fun chat(event: AsyncPlayerChatEvent)

    fun chatStream(): Flow<AsyncPlayerChatEvent>

    fun chatStream(playerId: UUID): Flow<AsyncPlayerChatEvent>

    fun updateUserInputState(playerId: UUID, getInput: Boolean)
}