package com.mcmlr.apps

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.system.CommandModel
import com.mcmlr.system.CommandRepository
import com.mcmlr.system.PlayerEventRepository
import com.mcmlr.system.SystemInputRepository
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
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
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        commandRepository.emitCommand(CommandModel(sender, command, label, args))

        return true
    }

    @EventHandler
    fun playerClickEvent(e: PlayerInteractEvent) {
        if (inputRepository.updateStream(CursorModel(e.player.uniqueId, e.player.location, CursorEvent.CLICK)) &&
            (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.LEFT_CLICK_AIR)) {
            e.isCancelled = true
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
