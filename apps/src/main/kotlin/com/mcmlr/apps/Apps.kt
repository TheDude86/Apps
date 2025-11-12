package com.mcmlr.apps

import com.mcmlr.blocks.api.AppInjectionListener
import com.mcmlr.blocks.api.AppInjector
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.api.data.PlayerOnlineEventType.JOINED
import com.mcmlr.blocks.core.*
import com.mcmlr.system.CommandRepository
import com.mcmlr.system.PlayerEventRepository
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.SystemEnvironment
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.data.NotificationManager
import com.mcmlr.system.products.homes.HomesEnvironment
import com.mcmlr.system.products.info.TutorialEnvironment
import com.mcmlr.system.products.kits.KitsEnvironment
import com.mcmlr.system.products.market.MarketEnvironment
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

class Apps : JavaPlugin() {
    companion object {
        lateinit var instance: Apps
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
        Metrics(this, 27644)

        instance = this

        managerComponent = DaggerManagerComponent.builder()
            .plugin(this)
            .build()
        managerComponent.inject(this)

        systemEnvironment = SystemEnvironment(this)
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

        AppInjector.register(AdminEnvironment())
        AppInjector.register(AnnouncementsEnvironment())
        AppInjector.register(HomesEnvironment())
        AppInjector.register(WarpsEnvironment())
        AppInjector.register(TeleportEnvironment())
        AppInjector.register(MarketEnvironment())
        AppInjector.register(PreferencesEnvironment())
        AppInjector.register(SpawnEnvironment())
//        AppInjector.register(WorkbenchesEnvironment())
        AppInjector.register(RecipeEnvironment())
        AppInjector.register(KitsEnvironment())
        AppInjector.register(YAMLEnvironment())
        AppInjector.register(PongEnvironment())
        AppInjector.register(TutorialEnvironment())
//        AppInjector.register(CheatsEnvironment())

        inputRepository
            .onlinePlayerEventStream()
            .collectOn(DudeDispatcher())
            .collectLatest { event ->
                if (event.eventType == JOINED) {
                    if (event.player.isOp && !systemConfigRepository.model.setupComplete) {
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
                val arg = it.args.firstOrNull()?.lowercase()
                if (command == ".") {
                    //TODO: Handle deeplinks
                    systemEnvironment.launch(player, arg)
                } else if (command == "c") {
                    systemEnvironment.shutdown(player)
                } else if (command == "k") {
                    Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, "minecraft:kill @e[tag=mcmlr.apps]")
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
        systemEnvironment.onDisable()
    }
}
