package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.views.Area.*
import com.mcmlr.blocks.api.views.Axis.X
import com.mcmlr.blocks.api.views.Axis.Y
import com.mcmlr.blocks.core.FlowDisposer
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay

abstract class View(
    protected var modifier: Modifier,
    var visible: Boolean = true,
    var teleportDuration: Int = 3,
    var height: Int = 0,
    override var offset: Int = 0,
): FlowDisposer(), Viewable {
    companion object {
        const val FILL_ALIGNMENT = 0
        const val WRAP_CONTENT = -1
        const val MATCH_PARENT = -2
    }

    lateinit var parent: Viewable
    var dudeDisplay: DudeDisplay? = null

    protected var corners: List<BlockDisplay> = mutableListOf()
    protected var dependants: MutableList<Viewable> = mutableListOf()
    protected var destroyListeners: MutableList<Listener> = mutableListOf()

    open fun update(
        modifier: Modifier? = null,
        visible: Boolean? = null,
        teleportDuration: Int? = null,
        height: Int? = null,
        reconfigure: Boolean = true,
    ) {
        modifier?.let { this.modifier = it }
        visible?.let { this.visible = it }
        teleportDuration?.let { this.teleportDuration = it }
        height?.let { this.height = it }

        if (reconfigure) updateDisplay()
    }

    fun attach(parent: Viewable) {
        modifier.start?.view?.addDependant(this)
        modifier.top?.view?.addDependant(this)
        modifier.end?.view?.addDependant(this)
        modifier.bottom?.view?.addDependant(this)
        this.parent = parent
        parent.addDestroyListener(object : Listener {
            override fun invoke() {
                clear()
            }
        })
    }

    override fun addDestroyListener(listener: Listener) {
        destroyListeners.add(listener)
    }

    override fun updatePosition(x: Int?, y: Int?) {
        x?.let { modifier.x(it) }
        y?.let { modifier.y(it) }
        updateDisplay()
    }

    fun addCorners(corners: List<BlockDisplay>) {
        this.corners = corners
    }

    override fun getDimensions(): Dimensions {
        val width = when(modifier.width) {
            WRAP_CONTENT -> getWrappedDimension(WIDTH)
            MATCH_PARENT -> getParentDimension(WIDTH)
            FILL_ALIGNMENT -> getAlignedDimension(WIDTH)
            else -> modifier.width
        }

        val height = when(modifier.height) {
            WRAP_CONTENT -> getWrappedDimension(HEIGHT)
            MATCH_PARENT -> getParentDimension(HEIGHT)
            FILL_ALIGNMENT -> getAlignedDimension(HEIGHT)
            else -> modifier.height
        }

        return Dimensions(width, height)
    }

    protected open fun getWrappedDimension(dimension: Area): Int = if (dimension == WIDTH) 0 else 0

    protected open fun getParentDimension(dimension: Area): Int = if (dimension == WIDTH) {
        parent.getDimensions().width - (modifier.m.start + modifier.m.end)
    } else {
        parent.getDimensions().height - (modifier.m.top + modifier.m.bottom)
    }

    protected open fun getAlignedDimension(dimension: Area): Int = if (dimension == WIDTH) {
        val start = modifier.start?.p ?: throw Exception("TODO: add error messages")
        val end = modifier.end?.p ?: throw Exception("TODO: add error messages")

        val startMargin = modifier.m.start
        val endMargin = modifier.m.end

        ((end - endMargin) - (start + startMargin)) / 2
    } else {
        val top = modifier.top?.p ?: throw Exception("TODO: add error messages")
        val bottom = modifier.bottom?.p ?: throw Exception("TODO: add error messages")

        val topMargin = modifier.m.top
        val bottomMargin = modifier.m.bottom

        ((top - topMargin) - (bottom + bottomMargin)) / 2
    }

    override fun getAbsolutePosition(): Coordinates = getPosition().offset(parent.getAbsolutePosition())

    override fun getPosition(): Coordinates {
        val x = when(modifier.xType) {
            PositionType.ALIGNED -> getAlignedPosition(X)
            PositionType.ABSOLUTE -> getAbsolutePosition(X)
            PositionType.CENTERED -> getCenteredPosition(X)
        }

        val y = when(modifier.yType) {
            PositionType.ALIGNED -> getAlignedPosition(Y)
            PositionType.ABSOLUTE -> getAbsolutePosition(Y)
            PositionType.CENTERED -> getCenteredPosition(Y)
        }

        return Coordinates(x, y + (offset * 80))
    }

    override fun getViewModifier(): Modifier = modifier

    override fun scroll(scrollEvent: ScrollEvent) {
        dudeDisplay?.scroll(scrollEvent)
    }

    override fun updateLocation(location: Location) {
        dudeDisplay?.updateLocation(location)
    }

    override fun getLocation(): Location? = dudeDisplay?.location

    override fun start(): Int = getPosition().x - getDimensions().width

    override fun top(): Int = getPosition().y + getDimensions().height / 2

    override fun end(): Int = getPosition().x + getDimensions().width

    override fun bottom(): Int = getPosition().y - getDimensions().height / 2

    override fun clear() {
        super.clear()
        dudeDisplay?.remove()
        destroyListeners.forEach { it.invoke() }
        corners.forEach { it.remove() }
    }

    open fun clearCorners() {
        corners.forEach { it.remove() }
        corners = listOf()
    }

    override fun addDependant(viewable: Viewable) {
        dependants.add(viewable)
    }

    override fun addContainerDisplay(view: ViewContainer): TextDudeDisplay? = parent.addContainerDisplay(view)

    override fun updateContainerDisplay(view: ViewContainer) = parent.updateContainerDisplay(view)

    override fun addTextDisplay(view: TextView): TextDudeDisplay? = parent.addTextDisplay(view)

    override fun updateTextDisplay(view: TextView) = parent.updateTextDisplay(view)

    override fun addEntityDisplay(view: EntityView): EntityDudeDisplay? = parent.addEntityDisplay(view)

    override fun updateEntityDisplay(view: EntityView): EntityDudeDisplay? = parent.updateEntityDisplay(view)

    override fun addItemDisplay(view: ItemView): ItemDudeDisplay? = parent.addItemDisplay(view)

    override fun addItemDisplay(view: ItemButtonView): ItemDudeDisplay? = parent.addItemDisplay(view)

    override fun updateItemDisplay(view: ItemButtonView) = parent.updateItemDisplay(view)

    override fun updateItemDisplay(view: ItemView) = parent.updateItemDisplay(view)

    override fun level(): Int = parent.level() + 1 + height

    override fun setScrolling(isScrolling: Boolean) = parent.setScrolling(isScrolling)

    override fun setTextInput(getInput: Boolean) = parent.setTextInput(getInput)

    override fun setFocus(view: Viewable) = parent.setFocus(view)

    override fun updateFocus(view: Viewable) {}

    protected open fun getAbsolutePosition(axis: Axis) = if (axis == X) modifier.x else modifier.y

    protected open fun getCenteredPosition(axis: Axis) = if (axis == X) modifier.m.start - modifier.m.end else modifier.m.bottom - modifier.m.top

    protected open fun getAlignedPosition(axis: Axis): Int {
        return if (axis == X) {
            if (modifier.start == null && modifier.end == null) {
                throw Exception("TODO: add error messages") //TODO
            } else if (modifier.end == null) {
                val p = if (modifier.start?.view == parent) {
                    (modifier.start?.p ?: return 0) - (modifier.start?.view?.getPosition()?.x ?: 0)
                } else {
                    (modifier.start?.p ?: return 0)
                }

                p + modifier.m.start + getDimensions().width
            } else if (modifier.start == null) {
                val p = if (modifier.end?.view == parent) {
                    (modifier.end?.p ?: return 0) - (modifier.end?.view?.getPosition()?.x ?: 0)
                } else {
                    (modifier.end?.p ?: return 0)
                }

                p - modifier.m.end - getDimensions().width
            } else {
                val start = if (modifier.start?.view == parent) {
                    (modifier.start?.p ?: return 0) - (modifier.start?.view?.getPosition()?.x ?: 0)
                } else {
                    (modifier.start?.p ?: return 0)
                }

                val end = if (modifier.end?.view == parent) {
                    (modifier.end?.p ?: return 0) - (modifier.end?.view?.getPosition()?.x ?: 0)
                } else {
                    (modifier.end?.p ?: return 0)
                }

                ((start + modifier.m.start) + (end - modifier.m.end)) / 2
            }
        } else {
            if (modifier.top == null && modifier.bottom == null) {
                throw Exception("TODO: add error messages") //TODO
            } else if (modifier.top == null) {
                val p = if (modifier.bottom?.view == parent) {
                    (modifier.bottom?.p ?: return 0) - (modifier.bottom?.view?.getPosition()?.y ?: 0) + (80 * (modifier.top?.view?.offset ?: 0))
                } else {
                    (modifier.bottom?.p ?: return 0)
                }

                p + modifier.m.bottom + getDimensions().height
            } else if (modifier.bottom == null) {
                val p = if (modifier.top?.view == parent) {
                    (modifier.top?.p ?: return 0) - (modifier.top?.view?.getPosition()?.y ?: 0) + (80 * (modifier.top?.view?.offset ?: 0))
                } else {
                    (modifier.top?.p ?: return 0)
                }

                p - modifier.m.top - getDimensions().height
            } else {
                val top = if (modifier.top?.view == parent) {
                    (modifier.top?.p ?: return 0) - (modifier.top?.view?.getPosition()?.y ?: 0) + (80 * (modifier.top?.view?.offset ?: 0))
                } else {
                    (modifier.top?.p ?: return 0)
                }

                val bottom = if (modifier.bottom?.view == parent) {
                    (modifier.bottom?.p ?: return 0) - (modifier.bottom?.view?.getPosition()?.y ?: 0) + (80 * (modifier.top?.view?.offset ?: 0))
                } else {
                    (modifier.bottom?.p ?: return 0)
                }

                ((top - modifier.m.top) + (bottom + modifier.m.bottom)) / 2
            }
        }
    }

    override fun updatePosition() {
        modifier.updateAlignment()
        dependants.forEach { it.updatePosition() }
        updateDisplay()
    }
}