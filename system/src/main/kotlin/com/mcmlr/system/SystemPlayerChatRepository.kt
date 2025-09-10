package com.mcmlr.system

import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.UUID

class SystemPlayerChatRepository: PlayerChatRepository {
    private val playerChatFlow = MutableStateFlow<AsyncPlayerChatEvent?>(null)
    private val inputUsers = HashSet<UUID>()

    override fun chat(event: AsyncPlayerChatEvent) {
        if (inputUsers.contains(event.player.uniqueId)) {
            event.isCancelled = true
            inputUsers.remove(event.player.uniqueId)
            playerChatFlow.emitBackground(event)
        }
    }

    override fun chatStream(): Flow<AsyncPlayerChatEvent> = playerChatFlow.filterNotNull()

    override fun chatStream(playerId: UUID): Flow<AsyncPlayerChatEvent> = playerChatFlow.filterNotNull().filter { it.player.uniqueId == playerId }

    override fun updateUserInputState(playerId: UUID, getInput: Boolean) {
        if (getInput) {
            inputUsers.add(playerId)
        } else {
            inputUsers.remove(playerId)
        }
    }
}