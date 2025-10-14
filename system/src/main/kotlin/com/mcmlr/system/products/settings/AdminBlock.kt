package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AdminBlock @Inject constructor(
    player: Player,
    camera: Camera,
    homeConfigBlock: HomeConfigBlock,
    warpConfigBlock: WarpConfigBlock,
    teleportConfigBlock: TeleportConfigBlock,
    marketConfigBlock: MarketConfigBlock,
    spawnConfigBlock: SpawnConfigBlock,
    systemConfigRepository: SystemConfigRepository,
) : Block(player, camera) {
    private val view: AdminBlockViewController = AdminBlockViewController(player, camera)
    private val interactor: AdminInteractor = AdminInteractor(view, homeConfigBlock, warpConfigBlock, teleportConfigBlock, marketConfigBlock, spawnConfigBlock, systemConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class AdminBlockViewController(player: Player, camera: Camera,): NavigationViewController(player, camera),
    AdminPresenter {

    private lateinit var permissionsButton: ButtonView
    private lateinit var homesButton: ButtonView
    private lateinit var warpsButton: ButtonView
    private lateinit var teleportButton: ButtonView
    private lateinit var marketButton: ButtonView
    private lateinit var spawnButton: ButtonView

    override fun setPermissionsListener(listener: () -> Unit) = permissionsButton.addListener(listener)

    override fun setHomeListener(listener: () -> Unit) = homesButton.addListener(listener)

    override fun setWarpListener(listener: () -> Unit) = warpsButton.addListener(listener)

    override fun setTeleportListener(listener: () -> Unit) = teleportButton.addListener(listener)

    override fun setMarketListener(listener: () -> Unit) = marketButton.addListener(listener)

    override fun setSpawnListener(listener: () -> Unit) = spawnButton.addListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Settings",
            size = 16,
        )

        val usePermissionsTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(title)
                .margins(top = 300),
            size = 6,
            text = "Use Permissions",
        )

        val usePermissionsMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(usePermissionsTitle)
                .alignStartToStartOf(usePermissionsTitle),
            alignment = Alignment.LEFT,
            lineWidth = 300,
            size = 4,
            text = "${ChatColor.GRAY}Toggle between using custom permission nodes or default permissions. Players have access to all user functionality and Opped players can access admin settings when turned off.",
        )

        permissionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(600, 0)
                .alignTopToBottomOf(usePermissionsTitle)
                .alignBottomToTopOf(usePermissionsMessage),
            size = 6,
            text = "${ChatColor.GOLD}On",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}On",
        )

        val appsConfigTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(usePermissionsTitle)
                .centerHorizontally()
                .margins(top = 400),
            size = 6,
            text = "${ChatColor.BOLD}App Configs",
        )

        homesButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(-500, 0)
                .alignTopToBottomOf(appsConfigTitle)
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GOLD}Homes",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Homes",
        )

        warpsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(0, 0)
                .alignTopToBottomOf(appsConfigTitle)
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GOLD}Warps",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Warps",
        )

        teleportButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(-500, 0)
                .alignTopToBottomOf(homesButton)
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GOLD}Teleports",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Teleports",
        )

        marketButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(0, 0)
                .alignTopToBottomOf(warpsButton)
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GOLD}Market",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Market",
        )

        spawnButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(500, 0)
                .alignTopToBottomOf(appsConfigTitle)
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GOLD}Spawn",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Spawn",
        )
    }

    override fun setPermissionsState(active: Boolean) {
        val status = if (active) "On" else "Off"
        permissionsButton.text = "${ChatColor.GOLD}$status"
        permissionsButton.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$status"
        updateTextDisplay(permissionsButton)
    }
}

interface AdminPresenter: Presenter {
    fun setPermissionsState(active: Boolean)
    fun setPermissionsListener(listener: () -> Unit)
    fun setHomeListener(listener: () -> Unit)
    fun setWarpListener(listener: () -> Unit)
    fun setTeleportListener(listener: () -> Unit)
    fun setMarketListener(listener: () -> Unit)
    fun setSpawnListener(listener: () -> Unit)
}

class AdminInteractor(
    private val presenter: AdminPresenter,
    private val homeConfigBlock: HomeConfigBlock,
    private val warpConfigBlock: WarpConfigBlock,
    private val teleportConfigBlock: TeleportConfigBlock,
    private val marketConfigBlock: MarketConfigBlock,
    private val spawnConfigBlock: SpawnConfigBlock,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setPermissionsState(systemConfigRepository.model.usePermissions)

        presenter.setPermissionsListener {
            systemConfigRepository.toggleUsePermissions()
            presenter.setPermissionsState(systemConfigRepository.model.usePermissions)
        }

        presenter.setSpawnListener {
            routeTo(spawnConfigBlock)
        }

        presenter.setHomeListener {
            routeTo(homeConfigBlock)
        }

        presenter.setWarpListener {
            routeTo(warpConfigBlock)
        }

        presenter.setTeleportListener {
            routeTo(teleportConfigBlock)
        }

        presenter.setMarketListener {
            routeTo(marketConfigBlock)
        }
    }
}
