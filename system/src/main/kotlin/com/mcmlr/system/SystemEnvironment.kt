package com.mcmlr.system

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.system.dagger.DaggerSystemEnvironmentComponent
import com.mcmlr.system.dagger.SystemEnvironmentComponent
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.market.MarketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import javax.inject.Inject
import kotlin.collections.set

class SystemEnvironment(private val plugin: JavaPlugin, private val useSystem: Boolean = true): BaseEnvironment<SystemApp>() {
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

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val ledger = mutableListOf<Pair<Player, BillboardModel>>()

                Bukkit.getOnlinePlayers().forEach {
                    systemConfigRepository.model.billboards.billboards.forEach { billboard ->
                        if (it.location.distance(billboard.location) < 10.0) {
                            ledger.add(Pair(it, billboard))
                        }
                    }
                }

                systemConfigRepository.updateBillboardsStream(ledger)

                delay(50)
            }
        }.disposeOn(disposer = this)

        val ledger = mutableMapOf<Player, BillboardModel>()
        systemConfigRepository.getBillboardsStream()
            .collectOn(Dispatchers.IO)
            .collectLatest { list ->
                val players = ledger.keys.map { it }.toMutableList()

                list.forEach { instance ->
                    if (!ledger.containsKey(instance.first)) {
                        ledger[instance.first] = instance.second
                        launchBillboard(instance.first, "", instance.second)
                    } else {
                        players.remove(instance.first)
                    }
                }

                players.forEach {
                    ledger.remove(it)
                    shutdown(it)
                }
            }
            .disposeOn(disposer = this)
    }

    fun preloadLocale(player: Player) {
        R.loadStrings(name(), player.locale)
    }

    fun launchBillboard(player: Player, deeplink: String?, billboard: BillboardModel) {
        R.loadStrings(name(), player.locale)
        val app = getInstance(player)
        app.configure(this, deeplink, inputRepository, useSystem, billboard)

        if (appMap.containsKey(app.player.uniqueId)) {
            appMap[app.player.uniqueId]?.shutdown()
        }

        app.create(resources, useSystem)
        appMap[app.player.uniqueId] = app
    }


    fun launch(player: Player, deeplink: String?) {
        R.loadStrings(name(), player.locale)
        val app = getInstance(player)
        app.configure(this, deeplink, inputRepository, useSystem)

        if (appMap.containsKey(app.player.uniqueId)) {
            appMap[app.player.uniqueId]?.shutdown()
        }

        app.create(resources, useSystem)
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