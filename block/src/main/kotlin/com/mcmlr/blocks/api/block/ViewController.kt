package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

open class ViewController(
    private val player: Player,
    private val origin: Location,
    background: Color = Color.fromARGB(192, 0, 0, 0),
): ViewContainer(
    modifier = Modifier().size(MATCH_PARENT, MATCH_PARENT),
    background = background,
) {

    private var isChild: Boolean = false
    private var isScrolling: Boolean = false

    private lateinit var router: Router
    private lateinit var context: Context

    init {
        attach(RootView(player, origin))
    }

    fun updateOrigin(origin: Location) {
        val direction = origin.direction.normalize()
        val o = origin.clone().add(direction.multiply(0.15))

        this.origin.x = o.x
        this.origin.y = o.y
        this.origin.z = o.z
    }

    fun configure(isChild: Boolean, router: Router, context: Context) {
        this.isChild = isChild
        this.router = router
        this.context = context
        children.clear()
        buttonMap.clear()
    }

    override fun render() {
        if (!isChild) dudeDisplay = parent.addContainerDisplay(this)

        children.forEach {
            it.render()
            val display = (it as? View)?.dudeDisplay ?: return@forEach
            if (it is ButtonView) {
                buttonMap[display.uniqueId] = it
            } else if (it is FeedView) {
                scrollMap[display.uniqueId] = it
            }
        }
    }

    override fun setFocus(view: Viewable) {
        if (isChild) {
            super.setFocus(view)
        } else {
            updateFocus(view)
        }
    }

    override fun updateFocus(view: Viewable) {
        children.forEach { it.updateFocus(view) }
    }

    override fun getPosition(): Coordinates {
        return super.getPosition().offset(parent.getAbsolutePosition())
    }

    override fun getAbsolutePosition(): Coordinates {
        return getPosition()
    }

    override fun clear() {
        super.clear()
        if (!isChild) {
            dudeDisplay?.remove()
            parent.clear()
        }
        corners.forEach { it.remove() }
        children.forEach { it.clear() }
    }

    fun cursorEventV2(position: Coordinates, event: CursorEvent) {
        val root = parent as? RootView ?: return
        root.cursorEventV2(position, event)
    }

    fun cursorEvent(displays: List<Entity>, cursor: Location, event: CursorEvent) {
        val root = parent as? RootView ?: return
        root.cursorEvent(displays, cursor, event)
    }

    override fun scrollEvent(event: ScrollModel, isChild: Boolean) {
        if (!isScrolling && !isChild) return
        super.scrollEvent(event, isChild)
    }

    override fun setTextInput(getInput: Boolean) {
        this.context.setInputState(getInput)
    }

    override fun setScrolling(isScrolling: Boolean) {
        if (isScrolling) {
            player.inventory.heldItemSlot = 4
        }

        this.isScrolling = isScrolling
        this.context.setScrollState(isScrolling)
    }

    override fun level(): Int = parent.level() + if (isChild) 0 else 1

    protected fun routeBack() = router.routeBack()

    protected fun close() = router.close()

    protected fun hasParent() = router.hasParent()
}

class EmptyViewController(player: Player, origin: Location): ViewController(player, origin)
