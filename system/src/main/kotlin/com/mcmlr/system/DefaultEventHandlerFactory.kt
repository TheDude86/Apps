package com.mcmlr.system

import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.teleport.GlobalTeleportRepository
import com.mcmlr.system.products.data.ServerEventsRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import javax.inject.Inject

@EnvironmentScope
class DefaultEventHandlerFactory @Inject constructor(
    private val serverEventsRepository: ServerEventsRepository,
    private val teleportRepository: GlobalTeleportRepository,
): Listener {

    @EventHandler
    fun playerClickEvent(e: PlayerInteractEvent) {
        serverEventsRepository.emitPlayerInteractEvent(e)
        if (serverEventsRepository.cancelBlockEvent(e.player) &&
            (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_BLOCK)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun playerTeleportEvent(e: PlayerTeleportEvent) {
        when (e.cause) {
            PlayerTeleportEvent.TeleportCause.PLUGIN,
                PlayerTeleportEvent.TeleportCause.COMMAND -> teleportRepository.setBackLocation(e.player, e.from)
            else -> {}
        }
    }

    @EventHandler
    fun playerJoinedServerEvent(e: PlayerJoinEvent) {
        teleportRepository.playerJoinedServer(e)
    }

    @EventHandler
    fun playerQuitServerEvent(e: PlayerQuitEvent) {
        teleportRepository.playerLeftServer(e)
    }

    @EventHandler
    fun playerRespawnEvent(e: PlayerRespawnEvent) {
        teleportRepository.playerRespawn(e)
    }

    @EventHandler
    fun playerDeathEvent(e: PlayerDeathEvent) {
        teleportRepository.setBackLocation(e.entity, e.entity.location)
    }

//    @EventHandler
//    fun playerMovedEvent(e: PlayerMoveEvent) = cursorRepository.updateMoveStream(e)
//
//    @EventHandler
//    fun onPlayerChangedHeldItem(event: PlayerItemHeldEvent) = cursorRepository.updateScrollStream(event)
//
//    @EventHandler
//    fun onPlayerChat(event: AsyncPlayerChatEvent) = playerChatRepository.chat(event)
//
//    @EventHandler
//    fun onPlayerQuit(event: PlayerQuitEvent) = playerEventRepository.onPlayerQuit(event.player)
//
//    @EventHandler
//    fun onPlayerJoined(event: PlayerJoinEvent) = playerEventRepository.onPlayerJoined(event.player)
}