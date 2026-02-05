package com.mcmlr.system.products.settings.billboards

import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.settings.billboards.SelectDefaultAppBlock.Companion.DEFAULT_APP_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.collections.forEach

class SelectDefaultAppBlock @Inject constructor(
    player: Player,
    origin: Origin,
    applicationsRepository: ApplicationsRepository,
): Block(player, origin) {
    companion object {
        const val DEFAULT_APP_BUNDLE_KEY = "app"
    }

    private val view = SelectDefaultAppViewController(player, origin)
    private val interactor = SelectDefaultAppInteractor(player, view, applicationsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SelectDefaultAppViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), SelectDefaultAppPresenter {

    private lateinit var defaultAppsFeed: ListFeedView
    private lateinit var defaultAppCallback: (Environment<*>) -> Unit

    override fun updateDefaultApps(configurableApps: List<Environment<*>>) {
        defaultAppsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                configurableApps.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 100),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                defaultAppCallback.invoke(it)
                            }
                        },
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val icon = addItemView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 150),
                                    item = it.getAppIcon()
                                )

                                val appName = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToEndOf(icon)
                                        .alignTopToTopOf(this)
                                        .margins(start = 50, top = 20),
                                    text = it.name(),
                                    size = 6,
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignTopToBottomOf(appName)
                                        .alignStartToStartOf(appName)
                                        .margins(top = 10),
                                    text = it.summary(),
                                    alignment = Alignment.LEFT,
                                    lineWidth = 250,
                                    size = 4,
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setDefaultAppsListener(listener: (Environment<*>) -> Unit) {
        defaultAppCallback = listener
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Select Default App",
            size = 16,
        )

        defaultAppsFeed = addListFeedView(
            modifier = Modifier()
                .size(600, 420)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this),
        )
    }
}

interface SelectDefaultAppPresenter: Presenter {
    fun updateDefaultApps(configurableApps: List<Environment<*>>)
    fun setDefaultAppsListener(listener: (Environment<*>) -> Unit)
}

class SelectDefaultAppInteractor(
    private val player: Player,
    private val presenter: SelectDefaultAppPresenter,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.updateDefaultApps(applicationsRepository.getApps())

        presenter.setDefaultAppsListener {
            addBundleData(DEFAULT_APP_BUNDLE_KEY, it)
            routeBack()
        }
    }
}
