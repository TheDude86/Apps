package com.mcmlr.system

import com.mcmlr.blocks.AppManager
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.FixedCursorModel
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.App
import com.mcmlr.system.products.base.AppEventHandlerFactory
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.system.dagger.SystemAppComponent
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.timeout
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
import org.bukkit.entity.Bee
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import javax.inject.Inject
import kotlin.collections.remove
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class SystemApp(player: Player): BaseApp(player), AppManager {

    lateinit var systemAppComponent: SystemAppComponent
    lateinit var inputRepository: InputRepository

    private val backgroundApps = HashMap<String, App>()
    private var foregroundApp: App? = null
    private var moveJob: Job? = null

    private var channel: Channel? = null

    @Inject
    lateinit var rootBlock: LandingBlock

    @Inject
    lateinit var eventHandler: AppEventHandlerFactory

    fun configure(environment: BaseEnvironment<BaseApp>, deeplink: String?, origin: Location, inputRepository: InputRepository) {
        this.parentEnvironment = environment
        this.deeplink = deeplink
        this.origin = origin
        this.inputRepository = inputRepository
    }

    override fun onCreate(child: Boolean) {

        player.gameMode = GameMode.SPECTATOR
        player.spectatorTarget = camera.camera
        camera.camera?.addPassenger(player)

        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        val connection = playerConnection.javaClass.getField("connection").get(playerConnection) as Connection
        channel = connection.channel

        channel?.pipeline()?.addBefore("packet_handler", "Apps", object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {

                val movePlayerPacket = msg as? ServerboundMovePlayerPacket
                if (movePlayerPacket != null) {
                    inputRepository.updateFixedStream(
                        FixedCursorModel(
                            player.uniqueId,
                            movePlayerPacket.xRot,
                            movePlayerPacket.yRot,
                            CursorEvent.MOVE,
                        )
                    )

                    log(Log.ASSERT, "Move Player = ${movePlayerPacket.xRot}, ${movePlayerPacket.yRot}")
                } else {
//                    log(Log.ASSERT, "Other Packet = $msg")
                }

                super.channelRead(ctx, msg)
            }
        })


        inputRepository.updateActivePlayer(player.uniqueId, true)
        inputRepository.cursorStream(player.uniqueId)
            .filter { it.event != CursorEvent.CLEAR }
            .collectOn(DudeDispatcher())
            .collectLatest {
                val originYaw = head.origin.yaw
                val currentYaw = it.data.yaw

                val yawDelta = if (originYaw > 90f && currentYaw < -90f) {
                    (originYaw - 180) - (180 + currentYaw)
                } else if (originYaw < -90f && currentYaw > 90f) {
                    (180 + originYaw) + (180 - currentYaw)
                } else {
                    originYaw - currentYaw
                }

                val modifier = min(60f, abs(yawDelta))

                val direction = it.data.direction.normalize()
                val cursor = it.data.add(direction.clone().multiply(camera.offset() + ((modifier / 60f) * 0.1)))
                val displays = player.world.getNearbyEntities(cursor, 0.09, 0.04, 0.09).filter { entity ->
                    entity is TextDisplay ||
                            entity is ItemDisplay ||
                            entity is BlockDisplay
                }

                val app = foregroundApp
                if (app != null) {
                    app.cursorEvent(displays, cursor, it.event)
                } else {
                    head.cursorEvent(displays, cursor, it.event)
                }

                if (it.event == CursorEvent.CLICK) inputRepository.updateStream(CursorModel(player.uniqueId, it.data, CursorEvent.CLEAR))
            }
            .disposeOn(disposer = this)

//        cursorRepository.cursorStream(player.uniqueId)
//            .filter { it.event != CursorEvent.CLEAR }
//            .collectOn(DudeDispatcher())
//            .collectLatest {
//
//                val originYaw = origin.yaw
//                val currentYaw = it.data.yaw
//
//                val yawDelta = if (originYaw > 90f && currentYaw < -90f) {
//                    (originYaw - 180) - (180 + currentYaw)
//                } else if (originYaw < -90f && currentYaw > 90f) {
//                    (180 + originYaw) + (180 - currentYaw)
//                } else {
//                    originYaw - currentYaw
//                }
//
//                val modifier = max(-58.8f, min(58.8f, yawDelta))
//                val radian = 0.01745329 * modifier
//                val finalX = (-1162.79 * tan(radian)).toInt()
//
//                val maxPitch = -(modifier / 14.026f).pow(2) + 43f
//                val rotation = 0.01745329 * max(-maxPitch, min(maxPitch, it.data.pitch))
//                val range = -1080.0 / tan(0.01745329 * maxPitch)
//                val newY = 75 + (range * tan(rotation)).toInt()
//                val finalY = min(1165, max(-1000, newY))
//
//                head.cursorEventV2(Coordinates(finalX, finalY), it.event)
//                if (it.event == CursorEvent.CLICK) cursorRepository.updateStream(CursorModel(player.uniqueId, it.data, CursorEvent.CLEAR))
//            }
//            .disposeOn(disposer = this)

        inputRepository.scrollStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                val app = foregroundApp
                if (app != null) {
                    app.scrollEvent(it)
                } else {
                    head.scrollEvent(it)
                }
            }
            .disposeOn(disposer = this)

        inputRepository.playerMoveStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                val app = foregroundApp
                if (app != null) {
                    app.minimize()
                } else {
                    head.minimize()
                }

                enableTimeoutStream()
            }
            .disposeOn(disposer = this)

        inputRepository.chatStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                val app = foregroundApp
                if (app != null) {
                    app.textInputEvent(it)
                } else {
                    head.textInputEvent(it)
                }
            }
            .disposeOn(disposer = this)

        systemAppComponent = (parentEnvironment as SystemEnvironment)
            .environmentComponent
            .subcomponent()
            .app(this)
            .build()

        systemAppComponent.inject(this)

        registerEvents(eventHandler)
    }

    @OptIn(FlowPreview::class)
    private fun enableTimeoutStream() {
        if (moveJob == null || moveJob?.isCancelled != false) {
            moveJob = inputRepository.playerMoveStream(player.uniqueId)
                .timeout(100.milliseconds)
                .catch {
                    val app = foregroundApp
                    if (app != null) {
                        app.maximize()
                    } else {
                        head.maximize()
                    }
                    currentCoroutineContext().cancel()
                }
                .collectOn(DudeDispatcher())
                .collectLatest {
                    //Do nothing, coroutines quirk I guess
                }
        }
    }

    override fun launchApp(app: Environment<App>, deeplink: String?) {
        launch(app, deeplink)
    }

    override fun setScrollState(isScrolling: Boolean) {
        inputRepository.updateUserScrollState(player.uniqueId, isScrolling)
    }

    override fun setInputState(getInput: Boolean) {
        inputRepository.updateUserInputState(player.uniqueId, getInput)
    }

    override fun root(): Block = rootBlock

    override fun launch(app: Environment<App>, deeplink: String?) {
        minimize()

        val backgroundApp = backgroundApps[app.name()]
        val newApp = if (backgroundApp == null) {
            app.launch(parentEnvironment, this, this, player, inputRepository, origin, deeplink)
        } else {
            backgroundApp.maximize()
            backgroundApp
        }

        foregroundApp?.let {
            it.minimize()
            backgroundApps[it::class.java.name] = it
        }

        foregroundApp = newApp
    }

    override fun close() {
        super.close()
        inputRepository.updateUserScrollState(player.uniqueId, false)
        inputRepository.updateUserInputState(player.uniqueId, false)
        inputRepository.updateActivePlayer(player.uniqueId, false)
    }

    override fun shutdown() {
        backgroundApps.values.forEach { it.close() }
        foregroundApp?.close()
        close()

        channel?.eventLoop()?.submit {
            channel?.pipeline()?.remove("Apps")
        }
    }

    override fun notifyShutdown() {
        backgroundApps.values.forEach { it.close() }
        close()
    }

    override fun closeApp() {
        foregroundApp = null
        maximize()
    }
}