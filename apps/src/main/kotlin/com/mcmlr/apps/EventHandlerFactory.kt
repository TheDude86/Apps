package com.mcmlr.apps

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.api.log
import com.mcmlr.system.CommandModel
import com.mcmlr.system.CommandRepository
import com.mcmlr.system.PlayerEventRepository
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventHandlerFactory @Inject constructor(
    private val commandRepository: CommandRepository,
    private val cursorRepository: CursorRepository,
    private val playerChatRepository: PlayerChatRepository,
    private val playerEventRepository: PlayerEventRepository,
    private val inputRepository: InputRepository,
): Listener, CommandExecutor {
    private val sneakMap: MutableMap<UUID, Long> = mutableMapOf()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        commandRepository.emitCommand(CommandModel(sender, command, label, args))

        return true
    }

    @EventHandler
    fun playerClickEvent(e: PlayerInteractEvent) {
        if (inputRepository.updateStream(CursorModel(e.player.uniqueId, e.player.location, CursorEvent.CLICK)) &&
            (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.LEFT_CLICK_AIR || e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun playerToggleSneakEvent(e: PlayerToggleSneakEvent) {
        if (inputRepository.isActiveUser(e.player)) {
            val lastSneak = sneakMap[e.player.uniqueId]

            if (e.isSneaking) {
                if (lastSneak != null) {
                    val diff = Date().time - lastSneak

                    if (diff < 250L) {
                        inputRepository.updateStream(CursorModel(e.player.uniqueId, e.player.location, CursorEvent.CALIBRATE))
                        sneakMap[e.player.uniqueId] = 0L
                    } else {
                        sneakMap[e.player.uniqueId] = Date().time
                    }
                } else {
                    sneakMap[e.player.uniqueId] = Date().time
                }
            }
        }
    }

    @EventHandler
    fun playerMovedEvent(e: PlayerMoveEvent) = inputRepository.updateMoveStream(e)

    @EventHandler
    fun onPlayerChangedHeldItem(event: PlayerItemHeldEvent) = inputRepository.updateScrollStream(event)

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) = inputRepository.chat(event)

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) = inputRepository.onPlayerQuit(event.player)

    @EventHandler
    fun onPlayerJoined(event: PlayerJoinEvent) = inputRepository.onPlayerJoined(event.player)

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) = inputRepository.onPlayerDeath(event.entity)
}
