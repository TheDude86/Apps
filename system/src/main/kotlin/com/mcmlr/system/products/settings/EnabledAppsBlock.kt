package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.View.Companion.WRAP_CONTENT
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.info.EnabledApplicationModel
import com.mcmlr.system.products.preferences.PreferencesEnvironment
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.collections.set

class EnabledAppsBlock @Inject constructor(
    player: Player,
    origin: Location,
    systemConfigRepository: SystemConfigRepository,
    applicationsRepository: ApplicationsRepository,
): Block(player, origin) {

    private val view: EnabledAppsViewController = EnabledAppsViewController(player, origin)
    private val interactor: EnabledAppsInteractor = EnabledAppsInteractor(view, systemConfigRepository, applicationsRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view

}

class EnabledAppsViewController(player: Player, origin: Location): NavigationViewController(player, origin), EnabledAppsPresenter {

    private lateinit var appsFeed: ListFeedView
    private lateinit var enabledAppCallback: EnabledAppsListener

    private var appList: List<EnabledApplicationModel> = listOf()
    private var appFeedContainers: HashMap<String, TextView> = HashMap()

    override fun setApps(apps: List<EnabledApplicationModel>) {
        appList = apps
        appFeedContainers.clear()
        updateAppsFeed()
    }

    override fun setAppEnabled(app: EnabledApplicationModel) {
        appFeedContainers[app.app.name()]?.setTextView(if (app.enabled) "\uD83D\uDD32" else "\uD83D\uDD33")
    }

    override fun setEnabledAppsListener(listener: EnabledAppsListener) {
        enabledAppCallback = listener
    }

    private fun updateAppsFeed() {
        appsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                appList.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 100),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                enabledAppCallback.invoke(it)
                            }
                        },
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val selected = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    text = if (it.enabled) "\uD83D\uDD32" else "\uD83D\uDD33"
                                )

                                appFeedContainers[it.app.name()] = selected

                                val icon = addItemView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 150),
                                    item = it.app.getAppIcon()
                                )

                                val appName = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToEndOf(icon)
                                        .alignTopToTopOf(this)
                                        .margins(start = 50, top = 20),
                                    text = it.app.name(),
                                    size = 6,
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignTopToBottomOf(appName)
                                        .alignStartToStartOf(appName)
                                        .margins(top = 10),
                                    text = it.app.summary(),
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

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Enabled Apps",
            size = 16,
        )

        appsFeed = addListFeedView(
            modifier = Modifier()
                .size(600, 420)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this),
        )
    }
}

interface EnabledAppsPresenter: Presenter {
    fun setApps(apps: List<EnabledApplicationModel>)
    fun setAppEnabled(app: EnabledApplicationModel)
    fun setEnabledAppsListener(listener: EnabledAppsListener)
}

interface EnabledAppsListener {
    fun invoke(model: EnabledApplicationModel)
}

class EnabledAppsInteractor(
    private val presenter: EnabledAppsPresenter,
    private val systemConfigRepository: SystemConfigRepository,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {

    private var enabledAppsList: List<EnabledApplicationModel> = listOf()

    override fun onCreate() {
        super.onCreate()
        if (enabledAppsList.isEmpty()) {

            val enabledApps = applicationsRepository.getApps()
            enabledAppsList = applicationsRepository.getSystemApps().map {
                val alwaysEnabled = it::class == AdminEnvironment::class ||
                        it::class == AnnouncementsEnvironment::class ||
                        it::class == PreferencesEnvironment::class
                EnabledApplicationModel(it, alwaysEnabled, enabledApps.contains(it)) //update boolean
            }
        }

        presenter.setApps(enabledAppsList)

        presenter.setEnabledAppsListener(object : EnabledAppsListener {
            override fun invoke(model: EnabledApplicationModel) {
                if (!model.alwaysEnabled) model.enabled = !model.enabled
                presenter.setAppEnabled(model)

                val enabledAppNames = enabledAppsList.filter { it.enabled }.map { it.app.name().lowercase() }
                systemConfigRepository.saveEnabledApps(enabledAppNames)
            }
        })
    }
}
