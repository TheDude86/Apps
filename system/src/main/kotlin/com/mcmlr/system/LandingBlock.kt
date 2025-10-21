package com.mcmlr.system

import com.mcmlr.system.products.applications.ApplicationsBlock
import com.mcmlr.system.products.homes.HomesBlock
import com.mcmlr.system.products.info.SetupBlock
import com.mcmlr.system.products.info.TutorialBlock
import com.mcmlr.system.products.landing.AppsListBlock
import com.mcmlr.system.products.landing.FeedBlock
import com.mcmlr.system.products.landing.SpawnShortcutBlock
import com.mcmlr.system.products.market.MarketBlock
import com.mcmlr.system.products.pong.PongBlock
import com.mcmlr.system.products.settings.AdminBlock
import com.mcmlr.system.products.teleport.TeleportBlock
import com.mcmlr.system.products.warps.WarpsBlock
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Context
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.placeholder.placeholders
import com.mcmlr.system.products.data.*
import com.mcmlr.system.products.spawn.SpawnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class LandingBlock @Inject constructor(
    player: Player,
    origin: Location,
    homesBlock: HomesBlock,
    warpsBlock: WarpsBlock,
    teleportBlock: TeleportBlock,
    marketBlock: MarketBlock,
    pongBlock: PongBlock,
    appsListBlock: AppsListBlock,
    applicationsBlock: ApplicationsBlock,
    spawnShortcutBlock: SpawnShortcutBlock,
    feedBlock: FeedBlock,
    tutorialBlock: TutorialBlock,
    setupBlock: SetupBlock,
    permissionsRepository: PermissionsRepository,
    spawnRepository: SpawnRepository,
    systemConfigRepository: SystemConfigRepository,
    applicationsRepository: ApplicationsRepository,
) : Block(player, origin) {
    private val view: LandingViewController = LandingViewController(player, origin, systemConfigRepository)
    private val interactor: LandingInteractor = LandingInteractor(
        player,
        view,
        homesBlock,
        warpsBlock,
        teleportBlock,
        marketBlock,
        pongBlock,
        appsListBlock,
        spawnShortcutBlock,
        applicationsBlock,
        feedBlock,
        tutorialBlock,
        setupBlock,
        permissionsRepository,
        spawnRepository,
        applicationsRepository,
    )

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class LandingViewController(private val player: Player, origin: Location, private val systemConfigRepository: SystemConfigRepository): NavigationViewController(player, origin), LandingPresenter {

    private lateinit var title: TextView
    private lateinit var appsContainer: ViewContainer
    private lateinit var spawnContainer: ViewContainer
    private lateinit var homeButton: ButtonView
    private lateinit var appsButton: ButtonView
    private lateinit var feedContainer: ViewContainer
    private lateinit var profileContainer: ViewContainer

    override fun getFeedBlockContainer(): ViewContainer = feedContainer

    override fun addAppsListener(listener: Listener) = appsButton.addListener(listener)

    override fun getAppsBlockContainer(): ViewContainer = appsContainer

    override fun getSpawnBlockContainer(): ViewContainer = spawnContainer

    override fun createView() {
        super.createView()
        title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally()
                .margins(top = 250),
            text = systemConfigRepository.model.title.placeholders(player),
            size = 14,
        )

        profileContainer = addViewContainer(
            modifier = Modifier()
                .size(300, FILL_ALIGNMENT)
                .alignStartToStartOf(this)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(start = 400, top = 50, bottom = 200),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        appsContainer = addViewContainer(
            modifier = Modifier()
                .size(300, 400)
                .alignEndToEndOf(this)
                .alignTopToBottomOf(title)
                .margins(top = 50, end = 400),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        feedContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignStartToEndOf(profileContainer)
                .alignEndToStartOf(appsContainer)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(top = 50, bottom = 300),
            background = Color.fromARGB(0, 0, 0, 255)
        )

        spawnContainer = addViewContainer(
            modifier = Modifier()
                .size(300, 300)
                .alignEndToEndOf(this)
                .alignTopToBottomOf(appsContainer)
                .margins(end = 400),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        homeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(-200)
                .alignBottomToBottomOf(this)
                .margins(bottom = 200),
            text = "${ChatColor.GOLD}${ChatColor.BOLD}Home",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Home",
        )

        appsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .x(200)
                .alignBottomToBottomOf(this)
                .margins(bottom = 200),
            text = "${ChatColor.GOLD}Apps",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Apps",
        )
    }
}

interface LandingPresenter: Presenter {
    fun getAppsBlockContainer(): ViewContainer
    fun getSpawnBlockContainer(): ViewContainer
    fun addAppsListener(listener: Listener)
    fun getFeedBlockContainer(): ViewContainer
}

class LandingInteractor(
    private val player: Player,
    private val presenter: LandingPresenter,
    private val homesBlock: HomesBlock,
    private val warpsBlock: WarpsBlock,
    private val teleportBlock: TeleportBlock,
    private val marketBlock: MarketBlock,
    private val pongBlock: PongBlock,
    private val appsListBlock: AppsListBlock,
    private val spawnShortcutBlock: SpawnShortcutBlock,
    private val applicationsBlock: ApplicationsBlock,
    private val feedBlock: FeedBlock,
    private val tutorialBlock: TutorialBlock,
    private val setupBlock: SetupBlock,
    private val permissionsRepository: PermissionsRepository,
    private val spawnRepository: SpawnRepository,
    private val applicationsRepository: ApplicationsRepository,
): Interactor(presenter) {

    private var routed = false

    override fun onPause() {
        super.onPause()
    }

    override fun onResume(newOrigin: Location?) {
        super.onResume(newOrigin)
    }

    override fun onClose() {
        super.onClose()
    }

    override fun onCreate() {
        if (!routed) {
            context.deeplink()?.let {
                if (it == "setup://") {
                    routeTo(setupBlock)
                    routed = true
                    return
                }
            }
        }

        super.onCreate()

        attachChild(appsListBlock, presenter.getAppsBlockContainer())
        attachChild(feedBlock, presenter.getFeedBlockContainer())



        if (spawnRepository.model.enabled == true && permissionsRepository.checkPermission(player, PermissionNode.SPAWN) ||
            permissionsRepository.checkPermission(player, PermissionNode.BACK)) {

            attachChild(spawnShortcutBlock, presenter.getSpawnBlockContainer())
        }

        presenter.addAppsListener(object : Listener {
            override fun invoke() {
                routeTo(applicationsBlock)
            }
        })

        context.deeplink()?.let {
            val environment = applicationsRepository.getDeeplinkApp(it) ?: return@let
            launchApp(environment)
        }
    }
}