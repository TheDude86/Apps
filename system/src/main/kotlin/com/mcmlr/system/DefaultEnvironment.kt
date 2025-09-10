package com.mcmlr.system

import com.mcmlr.system.products.market.MarketRepository
import com.mcmlr.blocks.api.Environment
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

class DefaultEnvironment(private val plugin: JavaPlugin): Environment<DefaultApp>() {
    lateinit var environmentComponent: DefaultEnvironmentComponent

    @Inject
    lateinit var marketRepository: MarketRepository

    @Inject
    lateinit var systemConfigRepository: SystemConfigRepository

//    @Inject
//    lateinit var packetManager: PacketManager

    @Inject
    lateinit var defaultEventHandlerFactory: DefaultEventHandlerFactory

    override fun build() {

        environmentComponent = DaggerDefaultEnvironmentComponent.builder()
            .environment(this)
            .build()
        environmentComponent.inject(this)

        marketRepository.loadOrders()
//        packetManager.initListeners()


        Bukkit.getServer().pluginManager.registerEvents(defaultEventHandlerFactory, plugin)
    }

    override fun getInstance(player: Player): DefaultApp = DefaultApp(player)

    override fun name(): String = "Default"
}