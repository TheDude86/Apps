package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.View.Companion.WRAP_CONTENT
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.info.EnabledApplicationModel
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class ConfigureAppsBlock @Inject constructor(
    player: Player,
    origin: Location,
    systemConfigRepository: SystemConfigRepository,
    applicationsRepository: ApplicationsRepository,
): Block(player, origin) {

    private val view: ConfigureAppsViewController = ConfigureAppsViewController(player, origin)
    private val interactor: ConfigureAppsInteractor = ConfigureAppsInteractor(view, systemConfigRepository, applicationsRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view

}

class ConfigureAppsViewController(player: Player, origin: Location): NavigationViewController(player, origin), ConfigureAppsPresenter {

    private lateinit var appsConfigFeed: ListFeedView
    private lateinit var configureAppCallback: (ConfigurableEnvironment<*>) -> Unit
    private lateinit var appsCallback: () -> List<ConfigurableEnvironment<*>>

    override fun updateConfigurableApps(configurableApps: List<ConfigurableEnvironment<*>>) {

        log(Log.ASSERT, "Items=${configurableApps.size}")

        appsConfigFeed.updateView {
            configurableApps.forEach {
                log(Log.ASSERT, it.name())
                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 100),
                    clickable = true,
                    listener = {
                        configureAppCallback.invoke(it)
                    }
                ) {
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
        }

    }

    override fun setConfigureAppsListener(listener: (ConfigurableEnvironment<*>) -> Unit) {
        configureAppCallback = listener
    }

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Configure Apps",
            size = 16,
        )

        appsConfigFeed = addListFeedView(
            modifier = Modifier()
                .size(600, 420)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this),
        )
    }
}

interface ConfigureAppsPresenter: Presenter {
    fun updateConfigurableApps(configurableApps: List<ConfigurableEnvironment<*>>)
    fun setConfigureAppsListener(listener: (ConfigurableEnvironment<*>) -> Unit)
}

class ConfigureAppsInteractor(
    private val presenter: ConfigureAppsPresenter,
    private val systemConfigRepository: SystemConfigRepository,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.updateConfigurableApps(applicationsRepository.getConfigurableApps())


        presenter.setConfigureAppsListener {
//            val block = it.launchConfig()
//            routeTo(block)
        }
    }
}