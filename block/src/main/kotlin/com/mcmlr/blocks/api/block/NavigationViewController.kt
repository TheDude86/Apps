package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

open class NavigationViewController(player: Player, camera: Camera,): ViewController(player, camera),
    NavigationPresenter {

    protected var backButton: ButtonView? = null
    protected lateinit var closeButton: ButtonView

    override fun addBackListener(listener: () -> Unit) {
        backButton?.addListener(listener)
    }

    override fun addCloseListener(listener: () -> Unit) = closeButton.addListener(listener)

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
                highlightedText = "${ChatColor.RED}${ChatColor.BOLD}←"
            ) {
                routeBack()
            }
        }

        closeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(this)
                .alignTopToTopOf(this)
                .margins(top = 250, end = 500),
            text = "✖",
            size = 14,
            highlightedText = "${ChatColor.RED}${ChatColor.BOLD}✖"
        ) {
            close()
        }
    }
}

interface NavigationPresenter: Presenter {
    fun addBackListener(listener: () -> Unit)
    fun addCloseListener(listener: () -> Unit)
}
