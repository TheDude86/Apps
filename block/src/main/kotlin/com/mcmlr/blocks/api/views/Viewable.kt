package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.api.ScrollEvent
import com.mcmlr.blocks.api.block.Listener
import org.bukkit.Location

interface Viewable {
    var offset: Int

    fun render()

    fun getDimensions(): Dimensions

    fun getPosition(): Coordinates

    fun getViewModifier(): Modifier

    fun getAbsolutePosition(): Coordinates

    fun start(): Int

    fun top(): Int

    fun end(): Int

    fun bottom(): Int

    fun clear()

    fun addDestroyListener(listener: Listener)

    fun addDependant(viewable: Viewable)

    fun updatePosition()

    fun updatePosition(x: Int? = null, y: Int? = null)

    fun updateDisplay()

    fun addContainerDisplay(
        view: ViewContainer
    ): TextDudeDisplay?

    fun updateContainerDisplay(
        view: ViewContainer
    )

    fun addTextDisplay(
        view: TextView
    ): TextDudeDisplay?

    fun updateTextDisplay(
        view: TextView
    )

    fun addEntityDisplay(
        view: EntityView
    ): EntityDudeDisplay?

    fun updateEntityDisplay(
        view: EntityView
    ): EntityDudeDisplay?

    fun addItemDisplay(
        view: ItemView
    ): ItemDudeDisplay?

    fun addItemDisplay(
        view: ItemButtonView
    ): ItemDudeDisplay?

    fun updateItemDisplay(
        view: ItemButtonView
    )

    fun updateItemDisplay(
        view: ItemView
    )

    fun level(): Int

    fun setScrolling(isScrolling: Boolean)

    fun setTextInput(getInput: Boolean)

    fun scroll(scrollEvent: ScrollEvent)

    fun updateLocation(location: Location)

    fun getLocation(): Location?

    fun setFocus(view: Viewable)

    fun updateFocus(view: Viewable)
}