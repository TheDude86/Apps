package com.mcmlr.system

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.system.dagger.DaggerSystemEnvironmentComponent
import com.mcmlr.system.dagger.SystemEnvironmentComponent
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.market.MarketRepository
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import javax.inject.Inject

class SystemEnvironment(private val plugin: JavaPlugin): BaseEnvironment<SystemApp>() {
    private val appMap = HashMap<UUID, SystemApp>()

    lateinit var environmentComponent: SystemEnvironmentComponent
    lateinit var inputRepository: InputRepository

    @Inject
    lateinit var marketRepository: MarketRepository

    @Inject
    lateinit var systemConfigRepository: SystemConfigRepository

    @Inject
    lateinit var applicationsRepository: ApplicationsRepository

//    @Inject
//    lateinit var packetManager: PacketManager

    @Inject
    lateinit var defaultEventHandlerFactory: DefaultEventHandlerFactory

    fun configure(
        inputRepository: InputRepository,
        resources: Resources,
    ) {
        this.inputRepository = inputRepository
        this.resources = resources
    }

    override fun build() {

        environmentComponent = DaggerSystemEnvironmentComponent.builder()
            .environment(this)
            .build()
        environmentComponent.inject(this)

        marketRepository.loadOrders()
//        packetManager.initListeners()


        Bukkit.getServer().pluginManager.registerEvents(defaultEventHandlerFactory, plugin)
    }

    fun launch(player: Player, deeplink: String?) {
        R.loadStrings(name(), player.locale)
        val app = getInstance(player)
        app.configure(this, deeplink, Origin(player), inputRepository)

        if (appMap.containsKey(app.player.uniqueId)) {
            appMap[app.player.uniqueId]?.shutdown()
        }

        app.create(resources)
        appMap[app.player.uniqueId] = app
    }

    fun register(app: Environment<App>) {
        applicationsRepository.register(app)
    }

    fun onDisable() {
        appMap.values.forEach {
            it.shutdown()
        }
    }

    fun shutdown(player: Player) {
        appMap[player.uniqueId]?.shutdown()
    }

    override fun getInstance(player: Player): SystemApp = SystemApp(player)

    override fun name(): String = "System"

    override fun icon(): String = ""

    override fun summary(): String = "The system level application responsible for handling players' home screens and launching applications."

    override fun permission(): String? = null
}