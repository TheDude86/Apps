package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.EmptyContextListener
import com.mcmlr.blocks.api.block.EmptyListener
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.max
import kotlin.math.min

open class ViewContainer(
    modifier: Modifier,
    val clickable: Boolean = false,
    val background: Color = Color.fromARGB(0x40000000),
    val backgroundHighlight: Color = Color.fromARGB(64, 64, 255, 255),
    override var listeners: MutableList<Listener> = mutableListOf(),
    override var highlighted: Boolean = false,
    teleportDuration: Int = 3,
    height: Int = 0,
): View(modifier, teleportDuration = teleportDuration, height = height), ClickableView {

    protected val children = mutableListOf<Viewable>()
    protected val buttonMap = HashMap<UUID, ButtonView>()
    protected val scrollMap = HashMap<UUID, FeedView>()

    override fun collides(position: Coordinates): Boolean = false

    override fun render() {
        dudeDisplay = parent.addContainerDisplay(this)

        children.forEach {
            renderChild(it)
        }
    }

    override fun updateDisplay() {
        parent.updateContainerDisplay(this)
    }

    override fun updateHeight(height: Int) {
        super.updateHeight(height)
        dependants.forEach { it.updatePosition() }
    }

    fun setSize(width: Int, height: Int) {
        modifier.size(width, height)
        parent.updateContainerDisplay(this)
        dependants.forEach { it.updatePosition() }
    }

    fun updateDimensions(width: Int, height: Int) {
        modifier.size(width, height)
        parent.updateDisplay()
        dependants.forEach { it.updatePosition() }
    }

    override fun updatePosition(x: Int?, y: Int?) {
        super.updatePosition(x, y)
        children.forEach { it.updatePosition() }
        dependants.forEach { it.updatePosition() }
    }

    override fun updatePosition() {
        super.updatePosition()
        parent.updateContainerDisplay(this)
        children.forEach {
            it.updatePosition()
        }
        dependants.forEach { it.updatePosition() }
    }

    protected fun renderChild(view: Viewable) {
        view.render()
        val display = (view as? View)?.dudeDisplay ?: return
        if (view is ButtonView) {
            buttonMap[display.uniqueId] = view
        } else if (view is FeedView) {
            scrollMap[display.uniqueId] = view
        }
    }

    fun textInputEvent(event: AsyncPlayerChatEvent) {
        children.forEach {
            if (it is ViewContainer) {
                it.textInputEvent(event)
            } else if (it is TextInputView) {
                it.textInputEvent(event)
            }
        }
    }

    override fun updateFocus(view: Viewable) {
        super.updateFocus(view)
        children.forEach {
            it.updateFocus(view)
        }
    }

    override fun scroll(scrollEvent: ScrollEvent) {
        dudeDisplay?.scroll(scrollEvent)
        children.forEach { it.scroll(scrollEvent) }
    }

    open fun scrollEvent(event: ScrollModel, isChild: Boolean) {
        children.forEach {
            if (it is ViewContainer) {
                it.scrollEvent(event, isChild)
            }
        }
    }

    private fun updateItemButton(itemButtonView: ItemButtonView, highlighted: Boolean) {
        if (highlighted) {
            val dimensions = itemButtonView.getDimensions()
            itemButtonView.setSize(dimensions.width * 1.2f, dimensions.height * 1.2f)
            itemButtonView.highlighted = true
        } else {
            val dimensions = itemButtonView.getDimensions()
            itemButtonView.setSize(dimensions.width.toFloat(), dimensions.height.toFloat())
            itemButtonView.highlighted = false
        }
    }

    private fun updateButton(buttonView: ButtonView, highlighted: Boolean) {
        if (highlighted) {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.highlightedText ?: "${ChatColor.BOLD}${buttonView.text}"
            buttonView.highlighted = true
        } else {
            (buttonView.dudeDisplay as? TextDudeDisplay)?.text = buttonView.text
            buttonView.highlighted = false
        }
    }

    open fun updateView(
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ) {
        dudeDisplay?.remove()
        corners.forEach { it.remove() }
        children.forEach { it.clear() }
        corners = listOf()
        children.clear()
        content.invokeContext(this)
        render()
    }

    private fun click() {
        val buttonModel = buttonMap.values.find { it.highlighted } ?: return
        if (buttonModel.visible) buttonModel.listeners.forEach { it.invoke() }

        if (buttonModel.close) {

        }
    }

    fun addPagerView(
        modifier: Modifier,
        background: Color = Color.fromARGB(0x40000000),
        height: Int = 0,
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ): PagerView {
        val view = PagerView(modifier, background, height = height)

        view.attach(this)
        children.add(view)

        content.invokeContext(view)

        return view
    }

    fun addListView(
        modifier: Modifier,
        background: Color = Color.fromARGB(0x40000000),
        height: Int = 0,
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ): ListView {
        val view = ListView(modifier, background, height = height)

        view.attach(this)
        children.add(view)

        content.invokeContext(view)

        return view
    }

    fun addListFeedView(
        modifier: Modifier,
        background: Color = Color.fromARGB(0x40000000),
        height: Int = 0,
        backgroundHighlight: Color = Color.fromARGB(64, 255, 255, 255),
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ): ListFeedView {
        val view = ListFeedView(modifier, background, height = height, backgroundHighlight = backgroundHighlight)

        view.attach(this)
        children.add(view)

        content.invokeContext(view)

        return view
    }

    open fun addFeedView(
        modifier: Modifier,
        background: Color = Color.fromARGB(0x40000000),
        height: Int = 0,
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ): FeedView {
        val view = FeedView(modifier, background, height = height)

        view.attach(this)
        children.add(view)

        content.invokeContext(view)

        return view
    }

    open fun addViewContainer(
        modifier: Modifier,
        clickable: Boolean = false,
        background: Color = Color.fromARGB(0x40000000),
        backgroundHighlight: Color = Color.fromARGB(64, 255, 255, 255),
        teleportDuration: Int = 3,
        height: Int = 0,
        listener: Listener = EmptyListener(),
        content: ContextListener<ViewContainer> = EmptyContextListener<ViewContainer>(),
    ): ViewContainer {
        val view = ViewContainer(modifier, clickable, background, backgroundHighlight, mutableListOf(listener), teleportDuration = teleportDuration, height = height)
        view.attach(this)
        children.add(view)

        content.invokeContext(view)

        return view
    }

    open fun addEntityView(
        modifier: Modifier,
        entity: EntityType,
        height: Int = 0,
    ): EntityView {
        val view = EntityView(
            modifier = modifier,
            entity = entity,
            height = height,
        )

        view.attach(this)
        children.add(view)
        return view
    }

    open fun addItemView(
        modifier: Modifier,
        item: Material,
        teleportDuration: Int = 3,
        height: Int = 0,
    ):ItemView {
        val view = ItemView(
            modifier = modifier,
            item = ItemStack(item),
            teleportDuration = teleportDuration,
            height = height,
        )

        view.attach(this)
        children.add(view)
        return view
    }

    open fun addItemView(
        modifier: Modifier,
        item: ItemStack,
        teleportDuration: Int = 3,
        height: Int = 0,
    ):ItemView {
        val view = ItemView(
            modifier = modifier,
            item = item,
            teleportDuration = teleportDuration,
            height = height,
        )

        view.attach(this)
        children.add(view)
        return view
    }

    fun spin(view: View) {
        flow {
            while (true) {
                val l = view.getLocation()?.clone() ?: continue
                l.yaw += 5
                if (l.yaw > 180f) {
                    l.yaw -= 360f
                }
                emit(l)
                delay(50)
            }
        }
            .collectOn(DudeDispatcher())
            .collectLatest {
                view.updateLocation(it)
            }
            .disposeOn(disposer = this)
    }

    open fun addTextView(
        modifier: Modifier,
        text: String,
        size: Int = 10,
        alignment: Alignment = Alignment.CENTER,
        maxLength: Int = 0,
        lineWidth: Int = 200,
        background: Color = Color.fromARGB(0x00000000),
        visible: Boolean = true,
        teleportDuration: Int = 3,
        height: Int = 0,
    ): TextView {
        val view = TextView(
            modifier = modifier,
            parent = this,
            text = text,
            size = size,
            alignment = alignment,
            maxLength = maxLength,
            lineWidth = lineWidth,
            background = background,
            visible = visible,
            teleportDuration = teleportDuration,
            height = height,
        )
        view.attach(this)

        children.add(view)
        return view
    }

    open fun addTextInputView(
        modifier: Modifier,
        text: String,
        highlightedText: String? = null,
        size: Int = 10,
        alignment: Alignment = Alignment.LEFT,
        maxLength: Int = 0,
        lineWidth: Int = 200,
        background: Color = Color.fromARGB(0x00000000),
        teleportDuration: Int = 3,
        height: Int = 0,
    ): TextInputView {
        val view = TextInputView(
            modifier = modifier,
            parent = this,
            text = text,
            highlightedText = highlightedText,
            size = size,
            alignment = alignment,
            maxLength = maxLength,
            lineWidth = lineWidth,
            background = background,
            teleportDuration = teleportDuration,
            height = height,
        )
        view.attach(this)

        children.add(view)
        return view
    }

    open fun addButtonView(
        modifier: Modifier,
        text: String,
        highlightedText: String? = null,
        size: Int = 10,
        alignment: Alignment = Alignment.LEFT,
        maxLength: Int = 0,
        lineWidth: Int = 200,
        background: Color = Color.fromARGB(0x00000000),
        teleportDuration: Int = 3,
        height: Int = 0,
        callback: Listener = EmptyListener(),
    ): ButtonView {
        val view = ButtonView(
            modifier = modifier,
            parent = this,
            text = text,
            highlightedText = highlightedText,
            size = size,
            alignment = alignment,
            maxLength = maxLength,
            lineWidth = lineWidth,
            background = background,
            listeners = mutableListOf(callback),
            teleportDuration = teleportDuration,
            height = height,
        )
        view.attach(this)

        children.add(view)
        return view
    }

    open fun addItemButtonView(
        modifier: Modifier,
        material: Material?,
        visible: Boolean = true,
        teleportDuration: Int = 3,
        height: Int = 0,
        callback: Listener = EmptyListener(),
    ): ItemButtonView {
        val view = ItemButtonView(
            modifier = modifier,
            parent = this,
            item = material?.let { ItemStack(it) },
            listeners = mutableListOf(callback),
            visible = visible,
            teleportDuration = teleportDuration,
            height = height,
        )
        view.attach(this)

        children.add(view)
        return view
    }

    fun addItemButtonView(
        modifier: Modifier,
        item: ItemStack?,
        visible: Boolean = true,
        teleportDuration: Int = 3,
        height: Int = 0,
        callback: Listener = EmptyListener(),
    ): ItemButtonView {
        val view = ItemButtonView(
            modifier = modifier,
            parent = this,
            item = item,
            listeners = mutableListOf(callback),
            visible = visible,
            teleportDuration = teleportDuration,
            height = height,
        )
        view.attach(this)

        children.add(view)
        return view
    }

    fun moveEvent(oldOrigin: Location, newOrigin: Location) {
        val x = newOrigin.x - oldOrigin.x
        val y = newOrigin.y - oldOrigin.y
        val z = newOrigin.z - oldOrigin.z

        val location = dudeDisplay?.location?.add(x, y, z)
        if (location != null && this is ViewController) {
            dudeDisplay?.updateLocation(location)
        }

        children.forEach {
            if (it is ViewContainer) {
                it.moveEvent(oldOrigin, newOrigin)
            }

            if (it is View) {
                val location = it.dudeDisplay?.location?.add(x, y, z) ?: return@forEach
                it.dudeDisplay?.updateLocation(location)
            }
        }
    }

    override fun clear() {
        super.clear()
        children.forEach { it.clear() }
    }

    fun getWrappedDimensionV2(dimension: Area): Int {
        if (children.size == 0) return 0
        if (dimension == Area.HEIGHT) {

            var alignment = 0
            var top = 0
            var bottom = 0

            var height = 100

            children.forEach {
                if (it.getViewModifier().height == MATCH_PARENT) throw Exception("You cannot have a child's height set to Match Parent of a View Container who's size is set to Wrap Content")

                when (it.getViewModifier().yType) {
                    PositionType.ALIGNED -> {

                        if (it.getViewModifier().top?.view == this) {
                            if (alignment == -1) throw Exception("You cannot have children aligned on both top and bottom of a View Container who's size is set to Wrap Content")
                            alignment = 1

                        } else if (it.getViewModifier().bottom?.view == this) {
                            if (alignment == 1) throw Exception("You cannot have children aligned on both top and bottom of a View Container who's size is set to Wrap Content")
                            alignment = -1
                        }

                        height = (it.getDimensions().height / 2) + it.getViewModifier().m.top + it.getViewModifier().m.bottom
//                        it.getViewModifier().top?.view?.let { top ->
//                            if (top != this) {
//                                height += (top.getDimensions().height / 2) + top.getViewModifier().m.top + top.getViewModifier().m.bottom
//                            }
//                        }
//
//                        it.getViewModifier().bottom?.view?.let { bottom ->
//                            if (bottom == this) {
//                                height += (bottom.getDimensions().height / 2) + bottom.getViewModifier().m.top + bottom.getViewModifier().m.bottom
//                            }
//                        }
                    }
                    PositionType.ABSOLUTE -> {
                        top = max(top, it.getPosition().y + it.getDimensions().height)
                        bottom = min(bottom, it.bottom())
                    }
                    PositionType.CENTERED -> TODO()
                }
            }

            return height
        }

        return 0
    }

    override fun getWrappedDimension(dimension: Area): Int {
        if (children.size == 0) return 0

        var min = if (dimension == Area.WIDTH) children.first().start() else children.first().bottom()
        var max = if (dimension == Area.WIDTH) children.first().end() else children.first().top()

        children.forEach {
            val s = if (dimension == Area.WIDTH) it.start() else it.bottom()
            val e = if (dimension == Area.WIDTH) it.end() else it.top()

            if (s < min) min = s
            if (e > max) max = e
        }

        return (max - min) / 2
    }


    override fun top(): Int = getPosition().y + getDimensions().height

    override fun bottom(): Int = getPosition().y - getDimensions().height

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
}