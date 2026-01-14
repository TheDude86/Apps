package com.mcmlr.pluginengine

import com.mcmlr.apps.Metrics
import com.mcmlr.blocks.api.AppInjectionListener
import com.mcmlr.blocks.api.AppInjector
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.api.data.PlayerOnlineEventType.JOINED
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.FlowDisposer
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.blocks.core.pluginName
import com.mcmlr.blocks.core.toLocale
import com.mcmlr.system.CommandRepository
import com.mcmlr.system.PlayerEventRepository
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.SystemEnvironment
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.data.NotificationManager
import com.mcmlr.system.products.data.PermissionsRepository
import com.mcmlr.system.products.homes.HomesEnvironment
import com.mcmlr.system.products.info.TutorialEnvironment
import com.mcmlr.system.products.kits.KitsEnvironment
import com.mcmlr.system.products.market.MarketEnvironment
import com.mcmlr.system.products.minetunes.MineTunesEnvironment
import com.mcmlr.system.products.minetunes.NewsRepository
import com.mcmlr.system.products.pong.PongEnvironment
import com.mcmlr.system.products.preferences.PreferencesEnvironment
import com.mcmlr.system.products.recipe.RecipeEnvironment
import com.mcmlr.system.products.settings.AdminEnvironment
import com.mcmlr.system.products.spawn.SpawnEnvironment
import com.mcmlr.system.products.teleport.TeleportEnvironment
import com.mcmlr.system.products.warps.WarpsEnvironment
import com.mcmlr.system.products.yaml.YAMLEnvironment
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class Engine(private val model: EngineModel) {
    companion object {
        lateinit var instance: JavaPlugin
    }

    private val disposer = FlowDisposer()

    private lateinit var managerComponent: ManagerComponent
    private lateinit var systemEnvironment: SystemEnvironment

    @Inject
    lateinit var eventHandler: EventHandlerFactory

    @Inject
    lateinit var commandRepository: CommandRepository

    @Inject
    lateinit var playerChatRepository: PlayerChatRepository

    @Inject
    lateinit var playerEventRepository: PlayerEventRepository

    @Inject
    lateinit var inputRepository: InputRepository

    @Inject
    lateinit var playerCursorCaptureTask: PlayerCursorCaptureTask

    @Inject
    lateinit var resources: Resources

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var systemConfigRepository: SystemConfigRepository

    @Inject
    lateinit var permissionsRepository: PermissionsRepository

    fun onEnable(plugin: JavaPlugin, bstatsNumber: Int) {
        pluginName = plugin.name
        Metrics(plugin, bstatsNumber)

        instance = plugin

        NewsRepository.init()

        managerComponent = DaggerManagerComponent.builder()
            .plugin(this)
            .build()
        managerComponent.inject(this)

        systemEnvironment = SystemEnvironment(plugin, model.useSystem)
        systemEnvironment.configure(inputRepository, resources)
        systemEnvironment.build()

        AppInjector.setInjectorListener(object : AppInjectionListener {
            override fun invoke(environment: Environment<App>) {
                systemEnvironment.register(environment)
            }
        })

        systemConfigRepository.model.defaultLanguage.toLocale()?.let {
            R.defaultLocale = it
        }

        model.apps.forEach {
            AppInjector.register(it)
        }

        inputRepository
            .onlinePlayerEventStream()
            .collectOn(DudeDispatcher())
            .collectLatest { event ->
                if (event.eventType == JOINED) {

                    systemEnvironment.preloadLocale(event.player)

                    if (event.player.isOp && !systemConfigRepository.model.setupComplete && model.useSystem) {
                        notificationManager.sendCTAMessage(event.player, "${ChatColor.WHITE}${ChatColor.ITALIC}Hello, thank you for trying out ${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.ITALIC}Apps${ChatColor.WHITE}${ChatColor.ITALIC}! We've created a short setup guide to help you configure ${ChatColor.GOLD}${ChatColor.BOLD}${ChatColor.ITALIC}Apps${ChatColor.WHITE}${ChatColor.ITALIC} however you like.\n", "Start setup", "Click to start", "/. setup://")
                    }
                    return@collectLatest
                }
                systemEnvironment.shutdown(event.player)
            }
            .disposeOn(disposer = disposer)

        commandRepository
            .commandStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                val player = it.sender as? Player ?: return@collectLatest
                val command = it.command.name.lowercase()
                val arg = if (model.useSystem) {
                    it.args.firstOrNull()?.lowercase()
                } else {
                    val app = model.apps.first()
                    val permission = app.permission()
                    if (permission != null) {
                        if (permissionsRepository.checkPermission(player, permission)) {
                            "${model.apps.first().name()}://"
                        } else {
                            player.sendMessage("${ChatColor.RED}You don't have permission to use this command!")
                            return@collectLatest
                        }
                    } else {
                        "${model.apps.first().name()}://"
                    }
                }
                if (command == model.openCommand) {
                    //TODO: Handle deeplinks
                    systemEnvironment.launch(player, arg)
                } else if (command == "c") {
                    systemEnvironment.shutdown(player)
                } else if (command == "k") {
                    Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, "minecraft:kill @e[tag=mcmlr.apps]")
                }
            }
            .disposeOn(disposer = disposer)

        plugin.server.pluginManager.registerEvents(eventHandler, plugin)
        plugin.getCommand(model.openCommand)?.setExecutor(eventHandler)
        plugin.getCommand("c")?.setExecutor(eventHandler)
        plugin.getCommand("k")?.setExecutor(eventHandler)

        playerCursorCaptureTask.runTaskTimer(instance, 0, 1)
    }

    fun onDisable() {
        disposer.clear()
        systemEnvironment.onDisable()
    }

}

data class EngineModel(
    val openCommand: String,
    val useSystem: Boolean,
    val apps: List<Environment<App>>,
)
