package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.FixedCursorModel
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.views.*
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

open class ViewController(
    private val player: Player,
    private val camera: Camera,
    background: Color = Color.fromARGB(192, 0, 0, 0),
): ViewContainer(
    modifier = Modifier().size(MATCH_PARENT, MATCH_PARENT),
    background = background,
) {

    private var isChild: Boolean = false
    private var isScrolling: Boolean = false
    private var cursor: ItemView? = null

    private lateinit var router: Router
    private lateinit var context: Context

    init {
        attach(RootView(player, camera))
    }

    fun updateOrigin(origin: Location) {
        val direction = origin.direction.normalize()
        val o = origin.clone().add(direction.multiply(context.offset()))

        this.camera.origin().x = o.x
        this.camera.origin().y = o.y
        this.camera.origin().z = o.z
    }

    fun configure(isChild: Boolean, router: Router, context: Context) {
        this.isChild = isChild
        this.router = router
        this.context = context
        children.clear()
        buttonMap.clear()
    }

    override fun render() {
        if (!isChild) {
            dudeDisplay = parent.addContainerDisplay(this)

            cursor?.clear()
            cursor = addItemView(
                modifier = Modifier()
                    .size(10, 10)
                    .x(0)
                    .y(0),
                teleportDuration = 1,
                item = ItemStack(Material.SMOOTH_QUARTZ)
            )
        }

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

    fun fixedCursorEvent(model: FixedCursorModel) {
        val root = parent as? RootView ?: return
        val cursor = cursor ?: return

        val x = cursor.getViewModifier().x + model.deltaX.toInt() * 11
        val y = model.y.toInt() * 11
        cursor.setPositionView(x, y)
        root.fixedCursorEvent(x, y)
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

class EmptyViewController(player: Player, camera: Camera): ViewController(player, camera)
