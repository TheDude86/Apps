package com.mcmlr.system.products.info

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock.Companion.ANNOUNCEMENT_POST_BUNDLE_KEY
import com.mcmlr.system.products.announcements.AnnouncementModel
import com.mcmlr.system.products.announcements.AnnouncementsRepository
import com.mcmlr.system.products.landing.FeedBlock
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.colorize
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.homes.HomeConfigBlock
import com.mcmlr.system.products.homes.HomesEnvironment
import com.mcmlr.system.products.market.MarketEnvironment
import com.mcmlr.system.products.preferences.PreferencesEnvironment
import com.mcmlr.system.products.settings.*
import com.mcmlr.system.products.spawn.SpawnEnvironment
import com.mcmlr.system.products.teleport.TeleportEnvironment
import com.mcmlr.system.products.warps.WarpsEnvironment
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class SetupBlock @Inject constructor(
    player: Player,
    origin: Location,
    tutorialBlock: TutorialBlock,
    feedBlock: FeedBlock,
    announcementEditorBlock: AnnouncementEditorBlock,
    homeConfigBlock: HomeConfigBlock,
    warpConfigBlock: WarpConfigBlock,
    teleportConfigBlock: TeleportConfigBlock,
    marketConfigBlock: MarketConfigBlock,
    spawnConfigBlock: SpawnConfigBlock,
    applicationsRepository: ApplicationsRepository,
    announcementsRepository: AnnouncementsRepository,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {
    private val view = SetupViewController(player, origin)
    private val interactor = SetupInteractor(
        view,
        tutorialBlock,
        feedBlock,
        announcementEditorBlock,
        homeConfigBlock,
        warpConfigBlock,
        teleportConfigBlock,
        marketConfigBlock,
        spawnConfigBlock,
        applicationsRepository,
        announcementsRepository,
        systemConfigRepository,
    )

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SetupViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), SetupPresenter {

    private lateinit var contentContainer: ViewContainer

    private var appsFeed: ListFeedView? = null
    private var appsConfigFeed: ListFeedView? = null
    private var announcementFeedContainer: ViewContainer? = null
    private var serverNameView: TextInputView? = null
    private var tutorialButton: ButtonView? = null
    private var setupButton: ButtonView? = null
    private var appList: List<EnabledApplicationModel> = listOf()
    private var appFeedContainers: HashMap<String, TextView> = HashMap()

    private lateinit var nextCallback: () -> Unit
    private lateinit var finishCallback: () -> Unit
    private lateinit var previousCallback: () -> Unit
    private lateinit var serverNameCallback: TextListener
    private lateinit var tutorialCallback: () -> Unit
    private lateinit var createPostCallback: () -> Unit
    private lateinit var enabledAppCallback: (EnabledApplicationModel) -> Unit
    private lateinit var configureAppCallback: (EnabledApplicationModel) -> Unit
    private lateinit var attachFeedCallback: (ViewContainer) -> Unit
    private lateinit var appsCallback: () -> List<EnabledApplicationModel>

    override fun getConfigurableApps(appsCallback: () -> List<EnabledApplicationModel>) {
        this.appsCallback = appsCallback
    }

    override fun attachFeedBlock(callback: (ViewContainer) -> Unit) {
        attachFeedCallback = callback
    }

    override fun setConfigureAppsListener(listener: (EnabledApplicationModel) -> Unit) {
        configureAppCallback = listener
    }

    override fun setEnabledAppsListener(listener: (EnabledApplicationModel) -> Unit) {
        enabledAppCallback = listener
    }

    override fun setAppEnabled(app: EnabledApplicationModel) {
        appFeedContainers[app.app.name()]?.setTextView(if (app.enabled) "\uD83D\uDD32" else "\uD83D\uDD33")
    }

    override fun setApps(apps: List<EnabledApplicationModel>) {
        appList = apps
        appFeedContainers.clear()
    }

    override fun setServerName(title: String) {
        serverNameView?.setTextView(title)
    }

    override fun setCreatePostListener(listener: () -> Unit) {
        createPostCallback = listener
    }

    override fun setServerNameListener(listener: TextListener) {
        serverNameCallback = listener
    }

    override fun setTutorialListener(listener: () -> Unit) {
        tutorialCallback = listener
    }

    override fun setFinishPageListener(listener: () -> Unit) {
        finishCallback = listener
    }

    override fun setNextPageListener(listener: () -> Unit) {
        nextCallback = listener
    }

    override fun setPreviousPageListener(listener: () -> Unit) {
        previousCallback = listener
    }

    override fun setContent(page: Int) {
        when (page) {
            1 -> pageOne()
            2 -> pageTwo()
            3 -> pageThree()
            4 -> pageFour()
            5 -> pageFive()
            6 -> pageSix()
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Setup",
            size = 16,
        )

        contentContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignStartToStartOf(this)
                .alignEndToEndOf(this)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(start = 600, top = 100, end = 600, bottom = 500),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

    private fun pageSix() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Finish",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "Congratulations! You're done configuring apps! You can still go back through the setup steps and make any more changes you like and once you press the Finish button, your changes will be saved. Now, this project is still in early development so there aren't any third party apps to download yet but they are on their way! So stay tuned and check for updates, we'll be sure to keep everybody updated on our progress.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .x(-400)
                    .alignTopToBottomOf(paragraphOne)
                    .margins(top = 150),
                text = "${ChatColor.GOLD}Back",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                callback = object : Listener {
                    override fun invoke() {
                        previousCallback.invoke()
                    }
                }
            )

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(paragraphOne)
                    .x(400)
                    .margins( top = 150),
                text = "${ChatColor.GOLD}Finish",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Finish",
                callback = object : Listener {
                    override fun invoke() {
                        finishCallback.invoke()
                    }
                }
            )
        }
    }

    private fun pageFive() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Create First Post",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "The Announcements app allows you to create announcement posts that will be shown on players home screens when the first open ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET}. We recommend you create your own post now so you can make it personal to your server and not use the generic one we set by default. Below is what the announcement feed looks like on the home screen.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            announcementFeedContainer = addViewContainer(
                modifier = Modifier()
                    .size(800, 400)
                    .alignTopToBottomOf(paragraphOne)
                    .centerHorizontally()
                    .margins(top = 50),
                background = Color.fromARGB(0, 0, 0, 255)
            )

            announcementFeedContainer?.let {
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(it)
                        .alignTopToBottomOf(it)
                        .margins(start = 100, top = 50),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCallback.invoke()
                        }
                    }
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(it)
                        .centerHorizontally()
                        .margins(top = 50),
                    text = "${ChatColor.GOLD}Create Post",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create Post",
                    callback = object : Listener {
                        override fun invoke() {
                            createPostCallback.invoke()
                        }
                    }
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToStartOf(it)
                        .alignTopToBottomOf(it)
                        .margins(top = 50, end = 100),
                    text = "${ChatColor.GOLD}Back",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                    callback = object : Listener {
                        override fun invoke() {
                            previousCallback.invoke()
                        }
                    }
                )
            }
        }

        announcementFeedContainer?.let(attachFeedCallback)
    }

    private fun pageFour() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Customize Apps",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "Some of the apps you've selected to be enabled have their own custom settings you can configure as well. You can now configure each App's settings now if you'd like or keep the default settings and make changes from the Admin settings later if you'd like.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            appsConfigFeed = addListFeedView(
                modifier = Modifier()
                    .size(600, 420)
                    .alignTopToBottomOf(paragraphOne)
                    .centerHorizontally()
                    .margins(top = 30),
            ) {
                appsCallback.invoke().forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 100),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                configureAppCallback.invoke(it)
                            }
                        }
                    ) {
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
            }

            appsConfigFeed?.let {
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(it)
                        .alignTopToBottomOf(it)
                        .margins(start = 100, top = 50),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCallback.invoke()
                        }
                    }
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToStartOf(it)
                        .alignTopToBottomOf(it)
                        .margins(top = 50, end = 100),
                    text = "${ChatColor.GOLD}Back",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                    callback = object : Listener {
                        override fun invoke() {
                            previousCallback.invoke()
                        }
                    }
                )
            }
        }
    }

    private fun pageThree() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Choose Apps",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "There are a few apps that come with ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} out of the box. You can choose which apps you'd like to enable now and you can always enable or disable them at any time from the Admin settings. A few apps can't be disabled though, like the Admin settings and player Preferences apps.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            appsFeed = addListFeedView(
                modifier = Modifier()
                    .size(600, 420)
                    .alignTopToBottomOf(paragraphOne)
                    .centerHorizontally()
                    .margins(top = 30),
            ) {
                appList.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 100),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                enabledAppCallback.invoke(it)
                            }
                        }
                    ) {
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
            }

            appsFeed?.let {
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(it)
                        .alignTopToBottomOf(it)
                        .margins(start = 100, top = 50),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCallback.invoke()
                        }
                    }
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToStartOf(it)
                        .alignTopToBottomOf(it)
                        .margins(top = 50, end = 100),
                    text = "${ChatColor.GOLD}Back",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                    callback = object : Listener {
                        override fun invoke() {
                            previousCallback.invoke()
                        }
                    }
                )
            }
        }
    }

    private fun pageTwo() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Server Name",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "First off, let's set the name of your server. This text will be displayed on the top of the player's home screen. Of course you can set this text you anything you want but since it's a small space, we recommend not going over 25 characters. This text also supports Placeholder API if you'd like a more personalized title.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            serverNameView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(paragraphOne)
                    .centerHorizontally()
                    .margins(top = 250),
                size = 16,
                text = "Set Server Title",
            )

            serverNameView?.let {
                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(it)
                        .alignTopToBottomOf(it)
                        .margins(start = 100, top = 250),
                    text = "${ChatColor.GOLD}Next ➡",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Next ➡",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCallback.invoke()
                        }
                    }
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignEndToStartOf(it)
                        .alignTopToBottomOf(it)
                        .margins(top = 250, end = 100),
                    text = "${ChatColor.GOLD}Back",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Back",
                    callback = object : Listener {
                        override fun invoke() {
                            previousCallback.invoke()
                        }
                    }
                )
            }
        }

        serverNameView?.addTextChangedListener(serverNameCallback)
    }

    private fun pageOne() {
        contentContainer.updateView {
            val title = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToTopOf(this),
                text = "${ChatColor.BOLD}Getting started",
                size = 12,
            )

            val paragraphOne = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(title)
                    .alignStartToStartOf(title)
                    .margins(top = 50),
                text = "Where would you like to start?  We have a recommended tutorial if you haven't used ${ChatColor.GOLD}${ChatColor.BOLD}Apps${ChatColor.RESET} before that'll help you get started. Or you can jump right into configuring everything now.",
                lineWidth = 500,
                alignment = Alignment.LEFT,
                size = 6,
            )

            tutorialButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(paragraphOne)
                    .alignStartToStartOf(paragraphOne)
                    .margins(top = 100),
                text = "${ChatColor.GOLD}Tutorial",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Tutorial",
                callback = object : Listener {
                    override fun invoke() {
                        tutorialCallback.invoke()
                    }
                }
            )

            tutorialButton?.let {
                setupButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(it)
                        .alignTopToBottomOf(it)
                        .margins(top = 100),
                    text = "${ChatColor.GOLD}Setup",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Setup",
                    callback = object : Listener {
                        override fun invoke() {
                            nextCallback.invoke()
                        }
                    }
                )
            }

            setupButton?.let {
                val paragraphTwo = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(it)
                        .alignStartToStartOf(title)
                        .margins(top = 100),
                    text = "If you're looking for further support, you can visit our Spigot or Modrinth pages or join our Discord and chat with the devs directly, we'll be happy to hear from you with any questions or feedback you may have! Click the button below and the links will sent to you in chat.",
                    lineWidth = 500,
                    alignment = Alignment.LEFT,
                    size = 6,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(paragraphTwo)
                        .alignStartToStartOf(paragraphTwo)
                        .margins(top = 100),
                    text = "${ChatColor.GOLD}Get Support Links",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Get Support Links",
                    callback = object : Listener {
                        override fun invoke() {
                            player.sendMessage("${ChatColor.DARK_BLUE}[${ChatColor.BLUE}Discord${ChatColor.DARK_BLUE}]${ChatColor.GRAY}: https://discord.gg/tXvpdc3cZv")
                            player.sendMessage("${ChatColor.DARK_GREEN}[${ChatColor.GREEN}Modrinth${ChatColor.DARK_GREEN}]${ChatColor.GRAY}: https://modrinth.com/plugin/apps!-beta")
                            player.sendMessage("${ChatColor.GOLD}[${ChatColor.YELLOW}Spigot${ChatColor.GOLD}]${ChatColor.GRAY}: https://www.spigotmc.org/resources/apps-beta.126555/")
                        }
                    }
                )
            }
        }
    }
}

interface SetupPresenter: Presenter {
    fun setContent(page: Int)
    fun setApps(apps: List<EnabledApplicationModel>)
    fun setAppEnabled(app: EnabledApplicationModel)
    fun setServerName(title: String)

    fun setNextPageListener(listener: () -> Unit)
    fun setFinishPageListener(listener: () -> Unit)
    fun setPreviousPageListener(listener: () -> Unit)
    fun setServerNameListener(listener: TextListener)
    fun setTutorialListener(listener: () -> Unit)
    fun setCreatePostListener(listener: () -> Unit)
    fun setEnabledAppsListener(listener: (EnabledApplicationModel) -> Unit)
    fun setConfigureAppsListener(listener: (EnabledApplicationModel) -> Unit)

    fun attachFeedBlock(callback: (ViewContainer) -> Unit)
    fun getConfigurableApps(appsCallback: () -> List<EnabledApplicationModel>)
}

class SetupInteractor(
    private val presenter: SetupPresenter,
    private val tutorialBlock: TutorialBlock,
    private val feedBlock: FeedBlock,
    private val announcementEditorBlock: AnnouncementEditorBlock,
    private val homeConfigBlock: HomeConfigBlock,
    private val warpConfigBlock: WarpConfigBlock,
    private val teleportConfigBlock: TeleportConfigBlock,
    private val marketConfigBlock: MarketConfigBlock,
    private val spawnConfigBlock: SpawnConfigBlock,
    private val applicationsRepository: ApplicationsRepository,
    private val announcementsRepository: AnnouncementsRepository,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {

    private var enabledAppsList: List<EnabledApplicationModel> = listOf()
    private var announcementPost: AnnouncementModel? = null
    private var serverTitle: String? = null
    private var page = 1

    private val configurableApps = mapOf(
        HomesEnvironment::class to homeConfigBlock,
        WarpsEnvironment::class to warpConfigBlock,
        TeleportEnvironment::class to teleportConfigBlock,
        MarketEnvironment::class to marketConfigBlock,
        SpawnEnvironment::class to spawnConfigBlock,
    )

    override fun onCreate() {
        super.onCreate()

        feedBlock.enableCTA(false)
        if (enabledAppsList.isEmpty()) {
            enabledAppsList = applicationsRepository.getSystemApps().map {
                val alwaysEnabled = it::class == AdminEnvironment::class ||
                        it::class == AnnouncementsEnvironment::class ||
                        it::class == PreferencesEnvironment::class
                EnabledApplicationModel(it, alwaysEnabled, true) //update boolean
            }
        }

        presenter.setContent(page)
        presenter.setApps(enabledAppsList)

        presenter.setNextPageListener {
            page++
            presenter.setContent(page)
            if (page == 2) {
                serverTitle?.let {
                    presenter.setServerName(it)
                }
            }
        }

        presenter.setPreviousPageListener {
            page--
            presenter.setContent(page)
            if (page == 2) {
                serverTitle?.let {
                    presenter.setServerName(it)
                }
            }
        }

        presenter.setTutorialListener {
            routeTo(tutorialBlock)
        }

        presenter.setServerNameListener(object : TextListener {
            override fun invoke(text: String) {
                serverTitle = text.colorize()
                presenter.setServerName(text.colorize())
            }
        })

        presenter.setEnabledAppsListener {
            if (!it.alwaysEnabled) it.enabled = !it.enabled
            presenter.setAppEnabled(it)
        }

        presenter.attachFeedBlock {
            attachChild(feedBlock, it)
        }

        presenter.setCreatePostListener {
            announcementEditorBlock.setSavePost(false)
            announcementEditorBlock.setSelectedAnnouncement(announcementPost)
            routeTo(announcementEditorBlock, object : RouteToCallback {
                override fun invoke(bundle: Bundle) {
                    announcementPost = bundle.getData<AnnouncementModel>(ANNOUNCEMENT_POST_BUNDLE_KEY)
                    val post = announcementPost ?: return
                    feedBlock.setCustomFeed(listOf(post))
                }
            })
        }

        presenter.getConfigurableApps {
            enabledAppsList.filter {
                it.enabled && configurableApps.containsKey(it.app::class)
            }
        }

        presenter.setConfigureAppsListener {
            val block = configurableApps[it.app::class] ?: return@setConfigureAppsListener
            routeTo(block)
        }

        presenter.setFinishPageListener {
            announcementPost?.let { announcement ->
                announcementsRepository.initAnnouncements(listOf(announcement))
            }

            val enabledAppsName = enabledAppsList.filter { it.enabled }.map { it.app.name().lowercase() }
            systemConfigRepository.completeSetup(enabledAppsName)
            serverTitle?.let {
                systemConfigRepository.updateServerTitle(it)
            }

            routeBack()
        }
    }
}

data class EnabledApplicationModel(val app: BaseEnvironment<BaseApp>, val alwaysEnabled: Boolean, var enabled: Boolean)
