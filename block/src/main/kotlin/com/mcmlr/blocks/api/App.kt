package com.mcmlr.blocks.api

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Context
import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.data.CursorRepository
import com.mcmlr.blocks.api.data.PlayerChatRepository
import com.mcmlr.blocks.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.Listener
import kotlin.math.*
import kotlin.time.Duration.Companion.milliseconds

abstract class App(val player: Player): FlowDisposer(), Context {
    private lateinit var playerChatRepository: PlayerChatRepository
    private lateinit var cursorRepository: CursorRepository
    private lateinit var head: Block
    private lateinit var origin: Location

    protected lateinit var environment: Environment<App>
    protected var deeplink: String? = null

    lateinit var resources: Resources

    abstract fun root(): Block

    fun configure(environment: Environment<App>, deeplink: String?, origin: Location) {
        this.environment = environment
        this.deeplink = deeplink
        this.origin = origin
    }

    fun create(cursorRepository: CursorRepository, playerChatRepository: PlayerChatRepository, resources: Resources) {
        this.playerChatRepository = playerChatRepository
        this.cursorRepository = cursorRepository
        this.resources = resources
        onCreate()
        head = root()
        head.context = this
        head.onCreate()
    }

    override fun deeplink(): String? = deeplink

    fun registerEvents(eventHandler: Listener) {
        resources.server().pluginManager.registerEvents(eventHandler, resources.plugin())
    }

    override fun close() {
        cursorRepository.updateActivePlayer(player.uniqueId, false)
        environment.notifyClose(player)
        onClose()
        head.onClose()
        clear()
    }

    override fun minimize() {
        cursorRepository.updateActivePlayer(player.uniqueId, false)
        onClose()
        head.onPause()
    }

    override fun maximize() {
        cursorRepository.updateActivePlayer(player.uniqueId, true)
        head.onResume(player.eyeLocation.clone())
    }

    override fun onCreate(child: Boolean) {
        cursorRepository.cursorStream(player.uniqueId)
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
                val cursor = it.data.add(direction.clone().multiply(0.15 + ((modifier / 60f) * 0.1)))
                val displays = player.world.getNearbyEntities(cursor, 0.09, 0.04, 0.09).filter { entity ->
                    entity is TextDisplay ||
                            entity is ItemDisplay ||
                            entity is BlockDisplay
                }

                head.cursorEvent(displays, cursor, it.event)
                if (it.event == CursorEvent.CLICK) cursorRepository.updateStream(CursorModel(player.uniqueId, it.data, CursorEvent.CLEAR))
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

        cursorRepository.scrollStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                head.scrollEvent(it)
            }
            .disposeOn(disposer = this)

        cursorRepository.playerMoveStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                head.minimize()
                enableTimeoutStream()
            }
            .disposeOn(disposer = this)

        playerChatRepository.chatStream(player.uniqueId)
            .collectOn(DudeDispatcher())
            .collectLatest {
                head.textInputEvent(it)
            }
            .disposeOn(disposer = this)
    }

    @OptIn(FlowPreview::class)
    private fun enableTimeoutStream() {
        if (moveJob == null || moveJob?.isCancelled != false) {
            moveJob = cursorRepository.playerMoveStream(player.uniqueId)
                .timeout(100.milliseconds)
                .catch {
                    head.maximize()
                    currentCoroutineContext().cancel()
                }
                .collectOn(DudeDispatcher())
                .collectLatest {
                    //Do nothing, coroutines quirk I guess
                }
        }
    }

    private var moveJob: Job? = null



    override fun onPause() {}

    override fun onResume(newOrigin: Location?) {}

    override fun onClose() {}

    override fun setHead(head: Block) {
        this.head = head
        this.head.context = this
    }

    override fun hasParent(): Boolean = false

    override fun routeTo(block: Block, callback: ((Bundle) -> Unit)?) {
        block.parent = head
        head.onClose()
        head = block
        head.context = this

        block.setResultCallback(callback)
        block.onCreate()
    }

    override fun routeBack() {
        val parent = head.parent
        if (parent == null) {
            close()
        } else {
            parent.onCreate()
            head.onClose()
            head = parent
        }
    }

    override fun setScrollState(isScrolling: Boolean) {
        cursorRepository.updateUserScrollState(player.uniqueId, isScrolling)
    }

    override fun setInputState(getInput: Boolean) {
        playerChatRepository.updateUserInputState(player.uniqueId, getInput)
    }

    override fun getBlock(): Block = head
}
