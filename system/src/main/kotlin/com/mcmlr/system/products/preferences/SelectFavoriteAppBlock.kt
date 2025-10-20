package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.products.data.ApplicationsRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class SelectFavoriteBlock @Inject constructor(
    player: Player,
    origin: Location,
    applicationsRepository: ApplicationsRepository,
    preferencesRepository: PreferencesRepository,
): Block(player, origin) {
    private val view = SelectFavoriteViewController(player, origin)
    private val interactor = SelectFavoriteInteractor(player, view, applicationsRepository, preferencesRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SelectFavoriteViewController(
    player: Player,
    origin: Location
): NavigationViewController(player, origin), SelectFavoritePresenter {

    private lateinit var appsFeedView: ListFeedView
    private lateinit var selectedContainer: ViewContainer
    private var clearButton: ButtonView? = null

    override fun setRemoveListener(listener: Listener) { clearButton?.addListener(listener) }

    override fun setSelectedApp(app: BaseEnvironment<BaseApp>?) {
        selectedContainer.updateView {
            val app = app ?: return@updateView
            val appIcon = addItemView(
                modifier = Modifier()
                    .size(75, 75)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 30),
                item = app.getAppIcon()
            )

            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(appIcon)
                    .centerHorizontally()
                    .margins(top = 10),
                text = "${ChatColor.GOLD}${app.name()}",
                size = 6,
            )
        }
    }

    override fun setAppsList(apps: List<BaseEnvironment<BaseApp>>, callback: (BaseEnvironment<BaseApp>) -> Unit) {
        appsFeedView.updateView {
            apps.forEach {
                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 100),
                    background = Color.fromARGB(0, 0, 0, 0)
                ) {
                    val icon = addItemView(
                        modifier = Modifier()
                            .size(60, 60)
                            .alignStartToStartOf(this)
                            .centerVertically()
                            .margins(start = 50),
                        item = it.getAppIcon()
                    )

                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(icon)
                            .alignTopToTopOf(icon)
                            .alignBottomToBottomOf(icon)
                            .margins(start = 50),
                        text = "${ChatColor.GOLD}${it.name()}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${it.name()}",
                        callback = object : Listener {
                            override fun invoke() {
                                callback.invoke(it)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Select Favorite",
            size = 16,
        )

        selectedContainer = addViewContainer(
            modifier = Modifier()
                .size(150, 150)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 100),
        )

        appsFeedView = addListFeedView(
            modifier = Modifier()
                .size(500, FILL_ALIGNMENT)
                .alignTopToBottomOf(selectedContainer)
                .alignBottomToBottomOf(this)
                .margins(top = 100, bottom = 400),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        clearButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(appsFeedView)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Remove",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Remove",
        )
    }
}

interface SelectFavoritePresenter: Presenter {
    fun setAppsList(apps: List<BaseEnvironment<BaseApp>>, callback: (BaseEnvironment<BaseApp>) -> Unit)

    fun setSelectedApp(app: BaseEnvironment<BaseApp>?)

    fun setRemoveListener(listener: Listener)
}

class SelectFavoriteInteractor(
    private val player: Player,
    private val presenter: SelectFavoritePresenter,
    private val applicationsRepository: ApplicationsRepository,
    private val preferencesRepository: PreferencesRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setSelectedApp(preferencesRepository.getSelectedFavorite(player))

        presenter.setAppsList(applicationsRepository.getPlayerApps(player)) {
            preferencesRepository.setFavorite(it)
            routeBack()
        }

        presenter.setRemoveListener(object : Listener {
            override fun invoke() {
                preferencesRepository.removeFavorite()
                presenter.setSelectedApp(preferencesRepository.getSelectedFavorite(player))
            }
        })
    }
}
