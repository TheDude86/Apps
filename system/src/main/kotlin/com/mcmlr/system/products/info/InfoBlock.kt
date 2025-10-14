package com.mcmlr.system.products.info

import com.mcmlr.blocks.api.app.Camera
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

class InfoBlock @Inject constructor(
    player: Player,
    camera: Camera,
): Block(player, camera) {
    private val view = InfoViewController(player, camera)
    private val interactor = InfoInteractor(view)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class InfoViewController(
    private val player: Player,
    camera: Camera,
): NavigationViewController(player, camera), InfoPresenter {

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Server Info",
            size = 16,
        )
    }

}

interface InfoPresenter: Presenter {

}

class InfoInteractor(
    private val presenter: InfoPresenter,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()
    }
}
