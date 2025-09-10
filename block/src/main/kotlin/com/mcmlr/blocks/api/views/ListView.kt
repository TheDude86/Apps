package com.mcmlr.blocks.api.views

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class ListView(
    modifier: Modifier,
    background: Color,
    height: Int = 0,
): ViewContainer(modifier, false, background, height = height) {

    override fun addFeedView(
        modifier: Modifier,
        background: Color,
        height: Int,
        content: ViewContainer.() -> Unit
    ): FeedView {
        setListAlignment(modifier)
        return super.addFeedView(modifier, background, height, content)
    }

    override fun addViewContainer(
        modifier: Modifier,
        clickable: Boolean,
        background: Color,
        backgroundHighlight: Color,
        teleportDuration: Int,
        height: Int,
        listener: () -> Unit,
        content: ViewContainer.() -> Unit
    ): ViewContainer {
        setListAlignment(modifier)
        return super.addViewContainer(
            modifier,
            clickable,
            background,
            backgroundHighlight,
            teleportDuration,
            height,
            listener,
            content
        )
    }

    override fun addEntityView(
        modifier: Modifier,
        entity: EntityType,
        height: Int,
    ): EntityView {
        setListAlignment(modifier)
        return super.addEntityView(modifier, entity, height)
    }

    override fun addItemView(
        modifier: Modifier,
        item: Material,
        teleportDuration: Int,
        height: Int,
    ): ItemView {
        setListAlignment(modifier)
        return super.addItemView(modifier, item, teleportDuration, height)
    }

    override fun addItemView(
        modifier: Modifier,
        item: ItemStack,
        teleportDuration: Int,
        height: Int,
    ): ItemView {
        setListAlignment(modifier)
        return super.addItemView(modifier, item, teleportDuration, height)
    }

    override fun addTextView(
        modifier: Modifier,
        text: String,
        size: Int,
        alignment: Alignment,
        maxLength: Int,
        lineWidth: Int,
        background: Color,
        visible: Boolean,
        teleportDuration: Int,
        height: Int,
    ): TextView {
        setListAlignment(modifier)
        return super.addTextView(
            modifier,
            text,
            size,
            alignment,
            maxLength,
            lineWidth,
            background,
            visible,
            teleportDuration,
            height,
        )
    }

    override fun addTextInputView(
        modifier: Modifier,
        text: String,
        highlightedText: String?,
        size: Int,
        alignment: Alignment,
        maxLength: Int,
        lineWidth: Int,
        background: Color,
        teleportDuration: Int,
        height: Int,
    ): TextInputView {
        setListAlignment(modifier)
        return super.addTextInputView(
            modifier,
            text,
            highlightedText,
            size,
            alignment,
            maxLength,
            lineWidth,
            background,
            teleportDuration,
            height,
        )
    }

    override fun addButtonView(
        modifier: Modifier,
        text: String,
        highlightedText: String?,
        size: Int,
        alignment: Alignment,
        maxLength: Int,
        lineWidth: Int,
        background: Color,
        teleportDuration: Int,
        height: Int,
        callback: () -> Unit
    ): ButtonView {
        setListAlignment(modifier)
        return super.addButtonView(
            modifier,
            text,
            highlightedText,
            size,
            alignment,
            maxLength,
            lineWidth,
            background,
            teleportDuration,
            height,
            callback,
        )
    }

    override fun addItemButtonView(
        modifier: Modifier,
        material: Material?,
        visible: Boolean,
        teleportDuration: Int,
        height: Int,
        callback: () -> Unit
    ): ItemButtonView {
        setListAlignment(modifier)
        return super.addItemButtonView(modifier, material, visible, teleportDuration, height, callback)
    }

    private fun setListAlignment(modifier: Modifier) {
        if (children.isNotEmpty()) {
            modifier.alignTopToBottomOf(children.last())
        } else {
            modifier.alignTopToTopOf(this)
        }
    }
}