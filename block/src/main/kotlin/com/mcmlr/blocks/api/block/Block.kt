package com.mcmlr.blocks.api.block

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.views.Coordinates
import com.mcmlr.blocks.api.views.ViewContainer
import kotlinx.coroutines.flow.Flow
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

abstract class Block(protected val player: Player, origin: Location): Context {

    val origin: Location = origin.clone()
    lateinit var context: Context
    var parent: Block? = null

    private val router = Router()
    private var isChild = false

    override fun onCreate(child: Boolean) {
        isChild = child
        router().configure(this)
        interactor().configure(this, router(), isChild)
        view().configure(isChild, router(), this)
        interactor().onCreate()
    }

    override fun onPause() {
        interactor().onPause()
        router().onPause()
        view().clear()
    }

    override fun onResume(newOrigin: Location?) {
        if (newOrigin != null) {
            newOrigin.yaw = origin.yaw
            newOrigin.pitch = 0f

            val direction = newOrigin.direction.normalize()
            val o = newOrigin.clone().add(direction.multiply(0.15))

            this.origin.x = o.x
            this.origin.y = o.y
            this.origin.z = o.z
        }

        router().onResume(newOrigin)
        interactor().onResume(newOrigin)
    }

    override fun onClose() {
        interactor().onClose()
        view().clear()
        router().clear()
    }

    override fun cursorEvent(cursorModel: CursorModel) = context.cursorEvent(cursorModel)

    override fun cursorStream(): Flow<CursorModel> = context.cursorStream()

    override fun deeplink(): String? = context.deeplink()

    fun setResultCallback(callback: ((Bundle) -> Unit)?) {
        router().setCallback(callback)
    }

    open fun interactor(): Interactor = EmptyInteractor()

    open fun router(): Router = router

    open fun view(): ViewController = EmptyViewController(player, origin)

    override fun setHeadBlock(head: Block) {
        context.setHeadBlock(head)
    }

    override fun hasParent(): Boolean = parent != null || context.hasParent()

    override fun launchApp(app: Environment<App>, deeplink: String?) = context.launchApp(app, deeplink)

    override fun routeTo(block: Block, callback: ((Bundle) -> Unit)?) = context.routeTo(block, callback)

    override fun routeBack() {
        context.routeBack()
    }

    override fun close() {
        context.close()
    }

    override fun minimize() {
        context.minimize()
    }

    override fun maximize() {
        context.maximize()
    }

    override fun getBlock(): Block = this

    override fun setScrollState(isScrolling: Boolean) = context.setScrollState(isScrolling)

    override fun setInputState(getInput: Boolean) = context.setInputState(getInput)

    fun textInputEvent(event: AsyncPlayerChatEvent) {
        view().textInputEvent(event)
    }

    open fun scrollEvent(event: ScrollModel, isChild: Boolean = false) {
        view().scrollEvent(event, isChild)
        router().scrollEvent(event)
    }

    fun cursorEventV2(position: Coordinates, event: CursorEvent) {
        view().cursorEventV2(position, event)
        router().cursorEventV2(position, event)
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorModel) {
        context.cursorEvent(event)
        view().cursorEvent(displays, cursor, event)
        router().cursorEvent(displays, cursor, event)
    }

    fun moveEventChild(newOrigin: Location) {
        newOrigin.yaw = origin.yaw
        newOrigin.pitch = 0f

        val o = newOrigin.clone()

        view().moveEvent(origin, o)
        router().moveEvent(o)
        this.origin.x = o.x
        this.origin.y = o.y
        this.origin.z = o.z
    }

    fun attach(context: Context, parentView: ViewContainer) {
        this.parent = context.getBlock()
        this.context = context
        view().attach(parentView)
    }
}

interface Context {

    fun cursorEvent(cursorModel: CursorModel)

    fun cursorStream(): Flow<CursorModel>

    fun onCreate(child: Boolean = false)

    fun onPause()

    fun onResume(newOrigin: Location? = null)

    fun onClose()

    fun launchApp(app: Environment<App>, deeplink: String? = null)

    fun routeTo(block: Block, callback: ((Bundle) -> Unit)? = null)

    fun routeBack()

    fun getBlock(): Block

    fun close()

    fun minimize()

    fun maximize()

    fun setHeadBlock(head: Block)

    fun hasParent(): Boolean

    fun setScrollState(isScrolling: Boolean)

    fun setInputState(getInput: Boolean)

    fun deeplink(): String?
}
