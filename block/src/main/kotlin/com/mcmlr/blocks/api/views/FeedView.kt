package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.ScrollModel
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.core.MutablePair
import org.bukkit.Color

open class FeedView(
    modifier: Modifier,
    background: Color,
    height: Int = 0,
    backgroundHighlight: Color = Color.fromARGB(64, 255, 255, 255),
): ViewContainer(modifier, false, background, height = height, backgroundHighlight = backgroundHighlight) {

    private val renderedChildren = mutableListOf<MutablePair<Viewable, Boolean>>()
    private val scrollListeners = mutableListOf<ScrollListener>()

    private var isFocused = false
    private var scrollingEnabled = true
    private var previousHighlightedState = false
    private var childOffset = 0
    private var bottom = 0

    override fun render() {
        renderedChildren.clear()
        dudeDisplay = parent.addContainerDisplay(this)

        bottom = children.firstOrNull()?.bottom() ?: 0
        children.forEach { childView ->
            if (childView.bottom() < bottom) bottom = childView.bottom()

            if (isViewInBounds(childView)) {
                renderedChildren.add(MutablePair(childView, true))
                renderChild(childView)
            } else {
                renderedChildren.add(MutablePair(childView, false))
            }
        }
    }

    fun addScrollListener(listener: ScrollListener) {
        scrollListeners.add(listener)
    }

    fun removeScrollListener(listener: ScrollListener) {
        scrollListeners.remove(listener)
    }

    fun enableScrolling(enabled: Boolean) {
        scrollingEnabled = enabled
    }

    override fun updateView(content: ContextListener<ViewContainer>) {
        childOffset = 0
        renderedChildren.forEach { it.first.clear() }
        renderedChildren.clear()
        super.updateView(content)
    }

    override fun scrollEvent(event: ScrollModel, isChild: Boolean) {
        if (!isFocused) return
        scrollListeners.forEach { it.invoke(event) }

        if (!scrollingEnabled) return
        if (event.event == ScrollEvent.UP && childOffset == 0) return
        if (event.event == ScrollEvent.DOWN && bottom() <= bottom + getPosition().y + (childOffset * 80)) return
        childOffset += if (event.event == ScrollEvent.DOWN) 1 else -1

        renderedChildren.forEach { childView ->
            childView.first.scroll(event.event)
            childView.first.offset = childOffset
            if (!isViewInBounds(childView.first)) {
                childView.first.clear()
                childView.second = false
            } else if (!childView.second) {
                childView.first.render()
                childView.second = true
            }
        }
    }

    private fun isViewInBounds(view: Viewable): Boolean =
        top() >= view.top() + getPosition().y &&
                bottom() <= view.bottom() + getPosition().y

    fun highlighted(highlighted: Boolean) {
        val display = dudeDisplay ?: return
        isFocused = highlighted
        if (highlighted) {
            (display as TextDudeDisplay).display.backgroundColor = backgroundHighlight
        } else {
            (display as TextDudeDisplay).display.backgroundColor = background
        }

        if (highlighted != previousHighlightedState) {
            setScrolling(highlighted)
            previousHighlightedState = highlighted
        }
    }
}

interface ScrollListener {
    fun invoke(model: ScrollModel)
}
