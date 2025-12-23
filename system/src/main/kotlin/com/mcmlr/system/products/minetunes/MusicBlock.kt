package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class MusicBlock @Inject constructor(
    player: Player,
    origin: Origin,
): Block(player, origin) {
    private val view = MusicViewController(player, origin)
    private val interactor = MusicInteractor(player, view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MusicViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), MusicPresenter {

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.LIBRARY_TITLE.resource()),
            size = 16,
        )
    }

}

interface MusicPresenter: Presenter {

}

class MusicInteractor(
    private val player: Player,
    private val presenter: MusicPresenter,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()


    }
}
