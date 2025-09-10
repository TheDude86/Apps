package com.mcmlr.system.products.data

import com.mcmlr.blocks.core.emitBackground
import com.mcmlr.system.EnvironmentScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID
import javax.inject.Inject

@EnvironmentScope
class ServerEventsRepository @Inject constructor() {

    private val cancelBlockEventPlayers = HashSet<UUID>()

    private val playerInteractEventFlow = MutableSharedFlow<PlayerInteractEvent>()

    fun emitPlayerInteractEvent(event: PlayerInteractEvent) {
        playerInteractEventFlow.emitBackground(event)
    }

    fun getPlayerInteractEventStream(): Flow<PlayerInteractEvent> = playerInteractEventFlow.filterNotNull()

    fun setCancelBlock(player: Player) = cancelBlockEventPlayers.add(player.uniqueId)

    fun removeCancelBlock(player: Player) = cancelBlockEventPlayers.remove(player.uniqueId)

    fun cancelBlockEvent(player: Player): Boolean = cancelBlockEventPlayers.contains(player.uniqueId)
}