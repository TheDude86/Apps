package com.mcmlr.apps

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.core.*
import com.mcmlr.system.CommandRepository
import com.mcmlr.system.DefaultEnvironment
import com.mcmlr.system.PlayerEventRepository
import com.mcmlr.system.PlayerOnlineEventType.JOINED
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.data.NotificationManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class Apps : JavaPlugin() {
    companion object {
        lateinit var instance: Apps
    }

    private val disposer = FlowDisposer()

    private lateinit var managerComponent: ManagerComponent

    @Inject
    lateinit var eventHandler: EventHandlerFactory

    @Inject
    lateinit var appManager: AppsManager

    @Inject
    lateinit var commandRepository: CommandRepository

    @Inject
    lateinit var cursorRepository: CursorRepository

    @Inject
    lateinit var playerEventRepository: PlayerEventRepository

    @Inject
    lateinit var packetManager: PacketManager

    @Inject
    lateinit var playerCursorCaptureTask: PlayerCursorCaptureTask

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var systemConfigRepository: SystemConfigRepository

    override fun onEnable() {
        instance = this

        managerComponent = DaggerManagerComponent.builder()
            .plugin(this)
            .build()
        managerComponent.inject(this)

        appManager.register(DefaultEnvironment(this))

        playerEventRepository
            .onlinePlayerEventStream()
            .collectOn(DudeDispatcher())
            .collectLatest { event ->
                if (event.eventType == JOINED) {
                    if (event.player.isOp && !systemConfigRepository.model.setupComplete) {
                        notificationManager.sendCTAMessage(event.player, "${ChatColor.WHITE}${ChatColor.ITALIC}Hello, thank you for trying out ${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.ITALIC}Apps${ChatColor.WHITE}${ChatColor.ITALIC}! We've created a short setup guide to help you configure ${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.ITALIC}Apps${ChatColor.WHITE}${ChatColor.ITALIC} however you like.\n", "Start setup", "Click to start", "/. setup://")
                    }
                    return@collectLatest
                }
                appManager.close(event.player)
            }
            .disposeOn(disposer = disposer)

        commandRepository
            .commandStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                val player = it.sender as? Player ?: return@collectLatest
                val command = it.command.name.lowercase()
                val arg = it.args.firstOrNull()?.lowercase()
                if (command == ".") {
                    appManager.launch(player, "default", arg)
                } else if (command == "c") {
                    appManager.close(player)
                } else if (command == "k") {
                    Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, "kill @e[tag=mcmlr.apps]")
                }
            }
            .disposeOn(disposer = disposer)

        server.pluginManager.registerEvents(eventHandler, this)
        getCommand(".")?.setExecutor(eventHandler)
        getCommand("c")?.setExecutor(eventHandler)
        getCommand("k")?.setExecutor(eventHandler)

        playerCursorCaptureTask.runTaskTimer(instance, 0, 1)

//        packetManager.initListeners()
    }

    override fun onDisable() {
        disposer.clear()
        appManager.shutdown()
    }
}
