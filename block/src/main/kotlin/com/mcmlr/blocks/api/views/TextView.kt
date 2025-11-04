package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.views.Area.WIDTH
import com.mcmlr.blocks.api.views.Axis.Y
import com.mcmlr.blocks.font.getMCTextLines
import com.mcmlr.blocks.font.getMCTextWidth
import com.mcmlr.blocks.font.reduceToLength
import org.bukkit.Color

open class TextView(
    modifier: Modifier,
    parent: Viewable,
    var text: String,
    var size: Int = 10,
    var alignment: Alignment = Alignment.LEFT,
    var maxLength: Int = 0,
    var lineWidth: Int = 200,
    var background: Color = Color.fromARGB(0x00000000),
    visible: Boolean = true,
    teleportDuration: Int = 3,
    height: Int = 0,
): View(modifier, visible, teleportDuration = teleportDuration, height = height) {

    init {
        this.text = this.text.reduceToLength(maxLength)
    }

    open fun update(
        modifier: Modifier? = null,
        text: String? = null,
        size: Int? = null,
        alignment: Alignment? = null,
        maxLength: Int? = null,
        lineWidth: Int? = null,
        background: Color? = null,
        visible: Boolean? = null,
        teleportDuration: Int? = null,
        height: Int? = null,
    ) {
        update(modifier, visible, teleportDuration, height, false)

        maxLength?.let { this.maxLength = it }
        text?.let { this.text = it.reduceToLength(maxLength ?: this.maxLength) }
        size?.let { this.size = it }
        alignment?.let { this.alignment = it }
        lineWidth?.let { this.lineWidth = it }
        background?.let { this.background = it }

        parent.updateTextDisplay(this)
        dependants.forEach { it.updatePosition() }
    }

    override fun render() {
        dudeDisplay = parent.addTextDisplay(this)
    }

    override fun updateDisplay() {
        parent.updateTextDisplay(this)
    }

    override fun getWrappedDimension(dimension: Area): Int = if (dimension == WIDTH) {
        (text.getMCTextWidth(lineWidth) * (0.4 * size.toDouble())).toInt()
    } else {
        text.getMCTextLines(lineWidth) * (7 * (size.toDouble() + 1)).toInt()
    }

    override fun getAlignedDimension(dimension: Area): Int = if (dimension == WIDTH) {
        val start = modifier.start?.p ?: throw Exception("TODO: add error messages")
        val end = modifier.end?.p ?: throw Exception("TODO: add error messages")

        (end - start) / 2
    } else {
        val top = modifier.top?.p ?: throw Exception("TODO: add error messages")
        val bottom = modifier.bottom?.p ?: throw Exception("TODO: add error messages")

        top - bottom
    }

    override fun getAlignedPosition(axis: Axis): Int {
        if (axis == Y) {
            return if (modifier.top == null && modifier.bottom == null) {
                throw Exception("TODO: add error messages") //TODO
            } else if (modifier.top == null) {
                val p = if (modifier.bottom?.view == parent) {
                    (modifier.bottom?.p ?: return 0) - (modifier.bottom?.view?.getPosition()?.y ?: 0) + (80 * (modifier.bottom?.view?.offset ?: 0))
                } else {
                    (modifier.bottom?.p ?: return 0)
                }

                p + modifier.m.bottom + getDimensions().height / 2
            } else if (modifier.bottom == null) {
                val p = if (modifier.top?.view == parent) {
                    (modifier.top?.p ?: return 0) - (modifier.top?.view?.getPosition()?.y ?: 0) + (80 * (modifier.top?.view?.offset ?: 0))
                } else {
                    (modifier.top?.p ?: return 0)
                }

                p - modifier.m.top - getDimensions().height / 2
            } else {
                return super.getAlignedPosition(axis)
            }
        } else {
            return super.getAlignedPosition(axis)
        }
    }
}