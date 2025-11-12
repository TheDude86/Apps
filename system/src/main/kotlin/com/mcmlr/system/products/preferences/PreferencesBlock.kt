package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class PreferencesBlock @Inject constructor(
    player: Player,
    origin: Location,
    favoritesBlock: FavoritesBlock,
): Block(player, origin) {
    private val view = PreferencesViewController(player, origin)
    private val interactor = PreferencesInteractor(view, favoritesBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class PreferencesViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), PreferencesPresenter {

    private lateinit var favoritesBlock: ViewContainer

    override fun favorites(): ViewContainer = favoritesBlock

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.PREFERENCES_TITLE.resource()),
            size = 16,
        )

        addListFeedView(
            modifier = Modifier()
                .size(MATCH_PARENT, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(start = 350, top = 150, end = 350, bottom = 200),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    favoritesBlock = addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 320)
                            .centerHorizontally(),
                        background = Color.fromARGB(0, 255, 255, 0)
                    )
                }
            }
        )


    }
}

interface PreferencesPresenter: Presenter {
    fun favorites(): ViewContainer
}

class PreferencesInteractor(
    private val presenter: PreferencesPresenter,
    private val favoritesBlock: FavoritesBlock,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        attachChild(favoritesBlock, presenter.favorites())

    }
}
