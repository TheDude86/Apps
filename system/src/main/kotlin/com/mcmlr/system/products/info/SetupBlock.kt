package com.mcmlr.system.products.info

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.system.S
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock
import com.mcmlr.system.products.announcements.AnnouncementEditorBlock.Companion.ANNOUNCEMENT_POST_BUNDLE_KEY
import com.mcmlr.system.products.announcements.AnnouncementModel
import com.mcmlr.system.products.announcements.AnnouncementsRepository
import com.mcmlr.system.products.landing.FeedBlock
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.colorize
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.products.announcements.AnnouncementsEnvironment
import com.mcmlr.system.products.data.ApplicationsRepository
import com.mcmlr.system.products.homes.HomeConfigBlock
import com.mcmlr.system.products.homes.HomesEnvironment
import com.mcmlr.system.products.market.MarketConfigBlock
import com.mcmlr.system.products.market.MarketEnvironment
import com.mcmlr.system.products.preferences.PreferencesEnvironment
import com.mcmlr.system.products.settings.*
import com.mcmlr.system.products.spawn.SpawnConfigBlock
import com.mcmlr.system.products.spawn.SpawnEnvironment
import com.mcmlr.system.products.teleport.TeleportConfigBlock
import com.mcmlr.system.products.teleport.TeleportEnvironment
import com.mcmlr.system.products.warps.WarpConfigBlock
import com.mcmlr.system.products.warps.WarpsEnvironment
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject

class SetupBlock @Inject constructor(
    player: Player,
    origin: Origin,
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
    origin: Origin,
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
        appFeedContainers[app.app.name()]?.update(text = if (app.enabled) "\uD83D\uDD32" else "\uD83D\uDD33")
    }

    override fun setApps(apps: List<EnabledApplicationModel>) {
        appList = apps
        appFeedContainers.clear()
    }

    override fun setServerName(title: String) {
        serverNameView?.update(text = title)
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${R.getString(player, S.SETUP.resource())}",
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
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.FINISH.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.SETUP_FINISH_PARAGRAPH_ONE.resource()),
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
                    text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
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
                    text = "${ChatColor.GOLD}${R.getString(player, S.FINISH.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.FINISH.resource())}",
                    callback = object : Listener {
                        override fun invoke() {
                            finishCallback.invoke()
                        }
                    }
                )
            }
        })
    }

    private fun pageFive() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.CREATE_FIRST_POST.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.SETUP_ANNOUNCEMENTS_PARAGRAPH.resource()),
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.CREATE_POST.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CREATE_POST.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
                        callback = object : Listener {
                            override fun invoke() {
                                previousCallback.invoke()
                            }
                        }
                    )
                }
            }
        })

        announcementFeedContainer?.let(attachFeedCallback)
    }

    private fun pageFour() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.CUSTOMIZE_APPS.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.SETUP_CUSTOMIZE_APPS.resource()),
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
                    content = object : ContextListener<ViewContainer>() {
                        override fun ViewContainer.invoke() {
                            appsCallback.invoke().forEach {
                                addViewContainer(
                                    modifier = Modifier()
                                        .size(MATCH_PARENT, 100),
                                    clickable = true,
                                    listener = object : Listener {
                                        override fun invoke() {
                                            configureAppCallback.invoke(it)
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
                    }
                )

                appsConfigFeed?.let {
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(it)
                            .alignTopToBottomOf(it)
                            .margins(start = 100, top = 50),
                        text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
                        callback = object : Listener {
                            override fun invoke() {
                                previousCallback.invoke()
                            }
                        }
                    )
                }
            }
        })
    }

    private fun pageThree() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.CHOOSE_APPS.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.SETUP_ENABLE_APPS.resource()),
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
                    content = object : ContextListener<ViewContainer>() {
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
                                                text = R.getString(player, (if (it.enabled) S.CHECKBOX_ENABLED else S.CHECKBOX_DISABLED).resource())
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
                    }
                )

                appsFeed?.let {
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(it)
                            .alignTopToBottomOf(it)
                            .margins(start = 100, top = 50),
                        text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
                        callback = object : Listener {
                            override fun invoke() {
                                previousCallback.invoke()
                            }
                        }
                    )
                }
            }
        })
    }

    private fun pageTwo() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.SERVER_NAME.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.SETUP_SERVER_NAME_PARAGRAPH.resource()),
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
                    text = R.getString(player, S.SET_SERVER_TITLE.resource()),
                )

                serverNameView?.let {
                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(it)
                            .alignTopToBottomOf(it)
                            .margins(start = 100, top = 250),
                        text = "${ChatColor.GOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.NEXT_ARROW.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.BACK.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.BACK.resource())}",
                        callback = object : Listener {
                            override fun invoke() {
                                previousCallback.invoke()
                            }
                        }
                    )
                }
            }
        })

        serverNameView?.addTextChangedListener(serverNameCallback)
    }

    private fun pageOne() {
        contentContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val title = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToStartOf(this)
                        .alignTopToTopOf(this),
                    text = "${ChatColor.BOLD}${R.getString(player, S.GETTING_STARTED.resource())}",
                    size = 12,
                )

                val paragraphOne = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(title)
                        .alignStartToStartOf(title)
                        .margins(top = 50),
                    text = R.getString(player, S.GETTING_STARTED.resource()),
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
                    text = "${ChatColor.GOLD}${R.getString(player, S.TUTORIAL.resource())}",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.TUTORIAL.resource())}",
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.SETUP.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.SETUP.resource())}",
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
                        text = R.getString(player, S.SETUP_SUPPORT_PARAGRAPH.resource()),
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.GET_SUPPORT_LINKS.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.GET_SUPPORT_LINKS.resource())}",
                        callback = object : Listener {
                            override fun invoke() {
                                player.sendMessage("${ChatColor.DARK_BLUE}[${ChatColor.BLUE}${R.getString(player, S.DISCORD.resource())}${ChatColor.DARK_BLUE}]${ChatColor.GRAY}: https://discord.gg/tXvpdc3cZv")
                                player.sendMessage("${ChatColor.DARK_GREEN}[${ChatColor.GREEN}${R.getString(player, S.MODRINTH.resource())}${ChatColor.DARK_GREEN}]${ChatColor.GRAY}: https://modrinth.com/plugin/apps!-beta")
                                player.sendMessage("${ChatColor.GOLD}[${ChatColor.YELLOW}${R.getString(player, S.SPIGOT.resource())}${ChatColor.GOLD}]${ChatColor.GRAY}: https://www.spigotmc.org/resources/apps-beta.126555/")
                            }
                        }
                    )
                }
            }
        })
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
