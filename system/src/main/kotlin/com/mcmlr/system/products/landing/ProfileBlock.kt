package com.mcmlr.system.products.landing

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class ProfileBlock @Inject constructor(
    player: Player,
    origin: Location,
): Block(player, origin) {
    private val view = ProfileViewController(player, origin)
    private val interactor = ProfileInteractor(view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class ProfileViewController(
    private val player: Player,
    origin: Location,
): ViewController(player, origin), ProfilePresenter {

    private lateinit var avatarContainer: ViewContainer

    override fun createView() {

        avatarContainer = addViewContainer(
            modifier = Modifier()
                .size(200, 200)
                .alignTopToTopOf(this)
                .centerHorizontally(),
        )

        val statsTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(avatarContainer)
                .centerHorizontally()
                .margins(top = 20),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}Stats",
            size = 5,
        )

        val ping = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(statsTitle)
                .centerHorizontally()
                .margins(top = 20),
            text = PlaceholderAPI.setPlaceholders(player, "Ping: %player_ping%ms"),
            size = 4,
        )

        val balance = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(ping)
                .centerHorizontally()
                .margins(top = 20),
            text = PlaceholderAPI.setPlaceholders(player, "$%vault_eco_balance%"),
            size = 4,
        )

    }

}

interface ProfilePresenter: Presenter {

}

class ProfileInteractor(
    private val presenter: ProfilePresenter,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()
    }
}
