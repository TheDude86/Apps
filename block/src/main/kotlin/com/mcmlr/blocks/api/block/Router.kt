package com.mcmlr.blocks.api.block

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.FixedCursorModel
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.views.Coordinates
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.Location
import org.bukkit.entity.Entity

open class Router {
    private val childNodes = mutableListOf<Block>()
    private lateinit var context: Context
    private var callback: ((Bundle) -> Unit)? = null

    val bundle = Bundle()

    fun configure(block: Context) {
        context = block
        childNodes.clear()
    }

    fun close() {
        context.close()
    }

    fun minimize() {
        context.minimize()
    }

    fun maximize() {
        context.maximize()
    }

    fun setCallback(callback: ((Bundle) -> Unit)? = null) {
        this.callback = callback
    }

    fun launchApp(app: Environment<App>, deeplink: String? = null) {
        context.launchApp(app, deeplink)
    }

    fun routeBack() {
        context.routeBack()
        this.callback?.invoke(bundle)
    }

    fun routeTo(block: Block, callback: ((Bundle) -> Unit)? = null) {
        context.routeTo(block, callback)
    }

    fun attachChild(child: Block, parentView: ViewContainer) {
        child.attach(context, parentView)
        child.onCreate(true)
        childNodes.add(child)
    }

    fun detachChild(child: Block) {
        childNodes.remove(child)
        child.onClose()
    }

    fun onResume(newOrigin: Location?) {
        childNodes.forEach { it.onResume(newOrigin) }
    }

    fun onPause() {
        childNodes.forEach { it.onPause() }
    }

    fun moveEvent(newOrigin: Location) {
        childNodes.forEach { it.moveEventChild(newOrigin.clone()) }
    }

    fun scrollEvent(event: ScrollModel) {
        childNodes.forEach { it.scrollEvent(event, true) }
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorEvent) {
        childNodes.forEach { it.cursorEvent(displays, cursor, event) }
    }

    fun fixedCursorEvent(model: FixedCursorModel) {
        childNodes.forEach { it.fixedCursorEvent(model) }
    }

    fun cursorEventV2(position: Coordinates, event: CursorEvent) {
        childNodes.forEach { it.cursorEventV2(position, event) }
    }

    fun clear() {
        childNodes.forEach { it.onClose() }
    }

    fun hasParent(): Boolean = context.hasParent()
}
