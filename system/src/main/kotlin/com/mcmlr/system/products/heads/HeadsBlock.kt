package com.mcmlr.system.products.heads

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class HeadsBlock @Inject constructor(
    player: Player,
    origin: Location,
): Block(player, origin) {
    private val view = HeadsViewController(player, origin)
    private val interactor = HeadsInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class HeadsViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), HeadsPresenter {

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Heads",
            size = 16,
        )
    }

}

interface HeadsPresenter: Presenter {

}

class HeadsInteractor(
    private val player: Player,
    private val presenter: HeadsPresenter,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

    }
}
