package com.mcmlr.system

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.log
import com.mcmlr.system.dagger.DaggerSystemEnvironmentComponent
import com.mcmlr.system.dagger.SystemEnvironmentComponent
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.market.MarketRepository
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
import org.bukkit.entity.Bat
import org.bukkit.entity.Bee
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tadpole
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
        val app = getInstance(player)
        app.configure(this, deeplink, origin(player), inputRepository)

        if (appMap.containsKey(app.player.uniqueId)) {
            appMap[app.player.uniqueId]?.shutdown()
        }

        //TODO: Add toggle
        val location = player.eyeLocation.clone()
        location.y -= 0.3
        location.pitch = 0f
        val camera = player.world.spawnEntity(location, EntityType.BEE) as Bee
        (camera as? LivingEntity)?.setAI(false)


        app.create(resources, Camera(player, camera))
        appMap[app.player.uniqueId] = app
    }

    private fun origin(player: Player): Location {
        val o = player.eyeLocation.clone()
        o.pitch = 0f

        val direction = o.direction.normalize()
        return o.add(direction.multiply(0.15))
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