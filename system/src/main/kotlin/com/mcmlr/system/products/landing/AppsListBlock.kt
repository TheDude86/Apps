package com.mcmlr.system.products.landing

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.S
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.products.preferences.PreferencesRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AppsListBlock @Inject constructor(
    player: Player,
    origin: Location,
    preferencesRepository: PreferencesRepository,
): Block(player, origin) {
    private val view = AppsListViewController(player, origin)
    private val interactor = AppsListInteractor(player, view, preferencesRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class AppsListViewController(
    private val player: Player,
    origin: Location,
): ViewController(player, origin), AppsListPresenter {

    private lateinit var listView: ListView

    override fun setFavorites(favoriteApps: List<Environment<App>>, callback: (Environment<App>) -> Unit) {
        if (favoriteApps.isEmpty()) return

        listView.updateDimensions(MATCH_PARENT, 50 * favoriteApps.size)

        listView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                favoriteApps.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val appIcon = addItemView(
                                    modifier = Modifier()
                                        .size(50, 50)
                                        .alignStartToStartOf(this)
                                        .centerVertically(),
                                    item = it.getAppIcon()
                                )

                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToEndOf(appIcon)
                                        .alignTopToTopOf(appIcon)
                                        .alignBottomToBottomOf(appIcon),
                                    text = "${ChatColor.GOLD}${it.name()}",
                                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${it.name()}",
                                    size = 8,
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(it)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun createView() {
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${R.getString(player, S.APPS.resource())}",
            size = 6,
        )

        val favorites = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(this)
                .margins(top = 50, start = 50),
            text = "${ChatColor.GRAY}${ChatColor.BOLD}${R.getString(player, S.FAVORITES.resource())}",
            size = 5,
        )

        listView = addListView(
            modifier = Modifier()
                .size(MATCH_PARENT, 250)
                .alignTopToBottomOf(favorites)
                .margins(start = 20, top = 20, end = 20),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }

}

interface AppsListPresenter: Presenter {
    fun setFavorites(favoriteApps: List<Environment<App>>, callback: (Environment<App>) -> Unit)
}

class AppsListInteractor(
    private val player: Player,
    private val presenter: AppsListPresenter,
    private val preferencesRepository: PreferencesRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setFavorites(preferencesRepository.getFavorites(player)) {
            launchApp(it)
        }
    }
}
