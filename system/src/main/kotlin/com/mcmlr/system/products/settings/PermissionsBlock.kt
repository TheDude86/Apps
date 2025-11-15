package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class PermissionsBlock @Inject constructor(
    player: Player,
    origin: Origin,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {

    private val view: PermissionsViewController = PermissionsViewController(player, origin)
    private val interactor: PermissionsInteractor = PermissionsInteractor(view, systemConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view

}

class PermissionsViewController(player: Player, origin: Origin): NavigationViewController(player, origin), PermissionsPresenter {

    private lateinit var permissionsButton: ButtonView

    override fun setPermissionsState(active: Boolean) {
        val status = if (active) "On" else "Off"
        permissionsButton.text = "${ChatColor.GOLD}$status"
        permissionsButton.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$status"
        updateTextDisplay(permissionsButton)
    }

    override fun setPermissionsListener(listener: Listener) = permissionsButton.addListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Permissions",
            size = 16,
        )

        val usePermissionsTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(title)
                .margins(top = 300),
            size = 6,
            text = "Use Permissions",
        )

        val usePermissionsMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(usePermissionsTitle)
                .alignStartToStartOf(usePermissionsTitle),
            alignment = Alignment.LEFT,
            lineWidth = 300,
            size = 4,
            text = "${ChatColor.GRAY}Toggle between using custom permission nodes or default permissions. Players have access to all user functionality and Opped players can access admin settings when turned off.",
        )

        permissionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(600, 0)
                .alignTopToBottomOf(usePermissionsTitle)
                .alignBottomToTopOf(usePermissionsMessage),
            size = 6,
            text = "${ChatColor.GOLD}On",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}On",
        )
    }
}

interface PermissionsPresenter: Presenter {
    fun setPermissionsState(active: Boolean)
    fun setPermissionsListener(listener: Listener)
}

class PermissionsInteractor(
    private val presenter: PermissionsPresenter,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.setPermissionsListener(object : Listener {
            override fun invoke() {
                systemConfigRepository.toggleUsePermissions()
                presenter.setPermissionsState(systemConfigRepository.model.usePermissions)
            }
        })
    }
}
