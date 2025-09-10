package com.mcmlr.blocks.api.views

import com.mcmlr.blocks.core.bolden
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.event.player.AsyncPlayerChatEvent

class TextInputView(
    modifier: Modifier,
    parent: Viewable,
    text: String,
    size: Int = 10,
    alignment: Alignment = Alignment.CENTER,
    maxLength: Int = 1,
    background: Color = Color.fromARGB(0x40000000),
    lineWidth: Int = 200,
    highlightedText: String?,
    highlighted: Boolean = false,
    close: Boolean = false,
    teleportDuration: Int = 3,
    height: Int = 0,
): ButtonView(
    modifier,
    parent,
    text,
    size,
    alignment,
    maxLength,
    background,
    lineWidth,
    highlightedText,
    highlighted,
    close,
    mutableListOf(),
    teleportDuration = teleportDuration,
    height = height,
) {
    private val textChangedListeners: MutableList<(String) -> Unit> = mutableListOf()

    private var waitingInput = false
    private var previousText: String? = null
    private var previousHighlightedText: String? = null

    init {
        listeners.add {
            parent.setFocus(this)
        }
    }

    override fun setTextView(text: String) {
        super.setTextView(text)
        this.highlightedText = text.bolden()
    }

    override fun updateFocus(view: Viewable) {
        if (view == this) {
            waitingInput = true
            previousText = this.text
            previousHighlightedText = this.highlightedText

            this.text = "${ChatColor.GRAY}${ChatColor.ITALIC}Type text into chat..."
            this.highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}Type text into chat..."
            setTextInput(true)
            updateDisplay()
        } else if (waitingInput) {
            waitingInput = false
            this.highlightedText = previousHighlightedText
            previousText?.let {
                this.text = it
            }
        }
    }

    fun textInputEvent(event: AsyncPlayerChatEvent) {
        if (!waitingInput) return

        waitingInput = false
        text = event.message
        highlightedText = "${ChatColor.BOLD}${event.message}"

        updateDisplay()

        textChangedListeners.forEach { it.invoke(event.message) }
    }

    fun addTextChangedListener(listener: (String) -> Unit) {
        textChangedListeners.add(listener)
    }

    fun updateText(text: String) {
        this.text = text
        highlightedText = "${ChatColor.BOLD}$text"

        updateDisplay()
        dependants.forEach { it.updatePosition() }
    }

//    private fun updateDisplay() {
//        val display = (dudeDisplay as? TextDudeDisplay) ?: return
//        display.text = text
//    }
}