package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.RootView.Companion.ITEM_VIEW_MULTIPLIER
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class ItemButtonView(
    modifier: Modifier,
    parent: Viewable,
    var item: ItemStack?,
    close: Boolean = false,
    listeners: MutableList<Listener>,
    visible: Boolean = true,
    teleportDuration: Int = 3,
    height: Int = 0,
): ButtonView(
    modifier,
    parent,
    "",
    10,
    Alignment.CENTER,
    1,
    Color.fromARGB(0x00000000),
    200,
    "",
    false,
    close,
    listeners,
    visible,
    teleportDuration,
    height,
) {

    override fun getWrappedDimension(dimension: Area): Int = 40

    override fun render() {
        dudeDisplay = parent.addItemDisplay(this)
    }

    fun update(
        modifier: Modifier? = null,
        item: ItemStack? = null,
        teleportDuration: Int? = null,
        visible: Boolean? = null,
        height: Int? = null,
    ) {
        update(modifier = modifier, teleportDuration = teleportDuration, visible = visible, height = height, reconfigure = false)
        item?.let { this.item = it }
        parent.updateItemDisplay(this)
    }

    override fun updateDisplay() {
        updateItemDisplay(this)
    }

    override fun top(): Int = getPosition().y + getDimensions().height

    override fun bottom(): Int = getPosition().y - getDimensions().height

    fun setSize(width: Float, height: Float) {
        val display = dudeDisplay ?: return
        dudeDisplay?.transformation = Transformation(Vector3f(0f, 0f, 0f), AxisAngle4f(0f, 0f, 0f, 1f), Vector3f(ITEM_VIEW_MULTIPLIER * width, ITEM_VIEW_MULTIPLIER * height, ITEM_VIEW_MULTIPLIER * width), AxisAngle4f(0f, 0f, 0f, 1f)
        )
    }
}
