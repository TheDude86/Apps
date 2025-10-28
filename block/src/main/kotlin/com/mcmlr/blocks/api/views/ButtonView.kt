package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.font.reduceToLength
import org.bukkit.Color

open class ButtonView(
    modifier: Modifier,
    parent: Viewable,
    text: String,
    size: Int = 10,
    alignment: Alignment = Alignment.CENTER,
    maxLength: Int = 1,
    background: Color = Color.fromARGB(0x40000000),
    lineWidth: Int = 200,
    var highlightedText: String?,
    override var highlighted: Boolean = false,
    val close: Boolean = false,
    override var listeners: MutableList<Listener>,
    visible: Boolean = true,
    teleportDuration: Int = 3,
    height: Int = 0,
): TextView(
    modifier,
    parent,
    text,
    size,
    alignment,
    maxLength,
    lineWidth,
    background,
    visible,
    teleportDuration,
    height,
), ClickableView {

    init {
        this.highlightedText = this.highlightedText?.reduceToLength(maxLength)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun updateText(text: String) {
        super.updateText(text)
        this.highlightedText = text.bolden()
    }

    override fun collides(position: Coordinates): Boolean {
        val offset = getAbsolutePosition()

        return start() + offset.x < position.x &&
                top() + offset.y > position.y &&
                end() + offset.x > position.x &&
                bottom() + offset.y < position.y
    }
}

interface ClickableView {
    var highlighted: Boolean
    var listeners: MutableList<Listener>
    var dudeDisplay: DudeDisplay?
    var visible: Boolean

    fun collides(position: Coordinates): Boolean
}
