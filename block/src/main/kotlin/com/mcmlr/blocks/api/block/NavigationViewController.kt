package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

open class NavigationViewController(player: Player, origin: Origin): ViewController(player, origin),
    NavigationPresenter {

    protected var backButton: ButtonView? = null
    protected lateinit var closeButton: ButtonView

    override fun addBackListener(listener: Listener) {
        backButton?.addListener(listener)
    }

    override fun addCloseListener(listener: Listener) = closeButton.addListener(listener)

    override fun createView() {
        if (hasParent()) {
            backButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this)
                    .margins(top = 250, start = 500),
                text = "←",
                size = 14,
                highlightedText = "${ChatColor.RED}${ChatColor.BOLD}←",
                callback = object : Listener {
                    override fun invoke() {
                        routeBack()
                    }
                }
            )
        }

        closeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(this)
                .alignTopToTopOf(this)
                .margins(top = 250, end = 500),
            text = "✖",
            size = 14,
            highlightedText = "${ChatColor.RED}${ChatColor.BOLD}✖",
            callback = object : Listener {
                override fun invoke() {
                    close()
                }
            }
        )
    }
}

interface NavigationPresenter: Presenter {
    fun addBackListener(listener: Listener)
    fun addCloseListener(listener: Listener)
}
