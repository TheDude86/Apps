package com.mcmlr.system.products.applications

import com.mcmlr.system.products.data.ApplicationModel
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Coordinates
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class ApplicationsBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val applicationsRepository: ApplicationsRepository,
): Block(player, origin) {
    private val view = ApplicationsViewController(player, origin)
    private val interactor = ApplicationsInteractor(player, view, applicationsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class ApplicationsViewController(
    player: Player,
    origin: Location,
): NavigationViewController(player, origin), ApplicationsPresenter {

    private val appPositions = listOf(
        Coordinates(-1000, 500), Coordinates(-600, 500), Coordinates(-200, 500), Coordinates(200, 500), Coordinates(600, 500), Coordinates(1000, 500),
        Coordinates(-1000, 200), Coordinates(-600, 200), Coordinates(-200, 200), Coordinates(200, 200), Coordinates(600, 200), Coordinates(1000, 200),
        Coordinates(-1000, -100), Coordinates(-600, -100), Coordinates(-200, -100), Coordinates(200, -100), Coordinates(600, -100), Coordinates(1000, -100),
        Coordinates(-1000, -400), Coordinates(-600, -400), Coordinates(-200, -400), Coordinates(200, -400), Coordinates(600, -400), Coordinates(1000, -400),
        Coordinates(-1000, -700), Coordinates(-600, -700), Coordinates(-200, -700), Coordinates(200, -700), Coordinates(600, -700), Coordinates(1000, -700),
    )

    private lateinit var container: ViewContainer
    private lateinit var homeButton: ButtonView
    private lateinit var appsButton: ButtonView

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Apps",
            size = 16,
        )

        container = addViewContainer(
            modifier = Modifier()
                .size(1200, 700)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 100),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        homeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(-200)
                .alignBottomToBottomOf(this)
                .margins(bottom = 200),
            text = "${ChatColor.GOLD}Home",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Home",
        ) {
            routeBack()
        }

        appsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(200)
                .alignBottomToBottomOf(this)
                .margins(bottom = 200),
            text = "${ChatColor.GOLD}${ChatColor.BOLD}Apps",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Apps",
        )
    }

    override fun setApps(apps: List<ApplicationModel>, callback: (Block) -> Unit) {

        container.updateView {
            apps.forEachIndexed { index, applicationModel ->
                val appPosition = appPositions[index]
                val appTitle = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(appPosition.x, appPosition.y),
                    size = 7,
                    text = "${ChatColor.GOLD}${applicationModel.appName}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${applicationModel.appName}",
                ) {
                    callback.invoke(applicationModel.headBlock)
                }

                addItemButtonView(
                    modifier = Modifier()
                        .size(80, 80)
                        .alignStartToStartOf(appTitle)
                        .alignEndToEndOf(appTitle)
                        .alignBottomToTopOf(appTitle)
                        .margins(bottom = 50),
                    item = applicationModel.appIcon
                ) {
                    callback.invoke(applicationModel.headBlock)
                }
            }
        }

    }

}

interface ApplicationsPresenter: Presenter {
    fun setApps(apps: List<ApplicationModel>, callback: (Block) -> Unit)
}

class ApplicationsInteractor(
    private val player: Player,
    private val presenter: ApplicationsPresenter,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setApps(applicationsRepository.getPlayerApps(player)) {
            routeTo(it)
        }
    }
}