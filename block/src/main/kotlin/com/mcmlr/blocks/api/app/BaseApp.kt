package com.mcmlr.blocks.api.app

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.FixedCursorModel
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Context
import com.mcmlr.blocks.core.FlowDisposer
import com.mcmlr.blocks.core.emitBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class BaseApp(val player: Player): FlowDisposer(), Context {

    private val cursorStream: MutableSharedFlow<FixedCursorModel> = MutableSharedFlow()

    protected lateinit var head: Block
    protected lateinit var parentEnvironment: BaseEnvironment<BaseApp>
    protected lateinit var camera: Camera

    protected var parentApp: BaseApp? = null

    protected var deeplink: String? = null

    lateinit var resources: Resources

    abstract fun root(): Block

    fun create(resources: Resources, camera: Camera) {
        this.resources = resources
        this.camera = camera
        onCreate()
        head = root()
        head.context = this
        head.onCreate()
    }

    override fun cursorEvent(cursorModel: FixedCursorModel) = cursorStream.emitBackground(cursorModel)

    override fun cursorStream(): Flow<FixedCursorModel> = cursorStream

    override fun deeplink(): String? = deeplink

    override fun offset(): Double = camera.offset()

    fun registerEvents(eventHandler: Listener) {
        resources.server().pluginManager.registerEvents(eventHandler, resources.plugin())
    }

    override fun close() {
        onClose()
        head.onClose()
        clear()
    }

    override fun minimize() {
        head.onPause()
    }

    override fun maximize() {
        head.onResume(camera.origin())
    }

    override fun onPause() {}

    override fun onResume(newOrigin: Location?) {}

    override fun onClose() {
        clear()
    }

    override fun setHeadBlock(head: Block) {
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

    override fun getBlock(): Block = head
}
