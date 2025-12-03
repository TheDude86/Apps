package com.mcmlr.system

import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerOnlineEvent
import com.mcmlr.blocks.api.data.PlayerOnlineEventType
import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import kotlin.math.abs

class SystemInputRepository: InputRepository {

    private val onlinePlayersEventStream = MutableSharedFlow<PlayerOnlineEvent>()
    private val playerChatFlow = MutableStateFlow<AsyncPlayerChatEvent?>(null)
    private val inputUsers = HashSet<UUID>()
    private val cursorFlowMap = HashMap<UUID, MutableStateFlow<CursorModel>>()
    private val playerMoveFlowMap = HashMap<UUID, MutableSharedFlow<PlayerMoveEvent>>()
    private val cursorScrollMap = HashMap<UUID, MutableSharedFlow<ScrollModel>>()
    private val scrollingUsers = HashSet<UUID>()
    private val activeUsers = HashSet<UUID>()

    override fun updateStream(data: CursorModel): Boolean {
        val mapEntry = cursorFlowMap[data.playerId]
        if (mapEntry == null) {
            cursorFlowMap[data.playerId] = MutableStateFlow(data)
        } else {
            mapEntry.emitBackground(data)
        }

        return activeUsers.contains(data.playerId)
    }

    override fun updateMoveStream(event: PlayerMoveEvent) {
        event.to?.let {
            if (abs(it.x - event.from.x) > 0.05 ||
                abs(it.y - event.from.y) > 0.05 ||
                abs(it.z - event.from.z) > 0.05) {

                val mapEntry = playerMoveFlowMap[event.player.uniqueId]
                if (mapEntry == null) {
                    val flow = MutableSharedFlow<PlayerMoveEvent>()
                    playerMoveFlowMap[event.player.uniqueId] = flow
                    flow.emitBackground(event)
                } else {
                    mapEntry.emitBackground(event)
                }
            }
        }
    }

    override fun updateScrollStream(event: PlayerItemHeldEvent) {
        val e = if (event.newSlot < 4) ScrollEvent.UP else ScrollEvent.DOWN
        cursorScrollMap[event.player.uniqueId]?.emitBackground(ScrollModel(e))
        if (scrollingUsers.contains(event.player.uniqueId)) event.isCancelled = true
    }

    override fun cursorStream(playerId: UUID): Flow<CursorModel> = cursorFlowMap[playerId] ?: flow { }

    override fun playerMoveStream(playerId: UUID): Flow<PlayerMoveEvent> {
        return if (playerMoveFlowMap.containsKey(playerId)) {
            playerMoveFlowMap[playerId] ?: flow { }
        } else {
            val player = Bukkit.getPlayer(playerId) ?: return flow { }
            playerMoveFlowMap[playerId] = MutableStateFlow(PlayerMoveEvent(player, player.location, null))
            playerMoveFlowMap[playerId] ?: flow { }
        }
    }

    override fun scrollStream(playerId: UUID): Flow<ScrollModel> {
        val flow = MutableSharedFlow<ScrollModel>()
        cursorScrollMap[playerId] = flow
        return flow
    }

    override fun updateActivePlayer(playerId: UUID, isActive: Boolean) {
        if (isActive)
            activeUsers.add(playerId)
        else
            activeUsers.remove(playerId)
    }

    override fun updateUserScrollState(playerId: UUID, isScrolling: Boolean) {
        if (isScrolling)
            scrollingUsers.add(playerId)
        else
            scrollingUsers.remove(playerId)
    }

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

    override fun onPlayerJoined(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.JOINED))
    }

    override fun onPlayerQuit(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.QUIT))
    }

    override fun onPlayerDeath(player: Player) {
        onlinePlayersEventStream.emitBackground(PlayerOnlineEvent(player, PlayerOnlineEventType.DIED))
    }

    override fun onlinePlayerEventStream(): Flow<PlayerOnlineEvent> = onlinePlayersEventStream

    override fun isActiveUser(player: Player): Boolean = activeUsers.contains(player.uniqueId)
}
