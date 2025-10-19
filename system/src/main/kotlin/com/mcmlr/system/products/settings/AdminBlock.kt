package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AdminBlock @Inject constructor(
    player: Player,
    origin: Location,
    permissionsBlock: PermissionsBlock,
    enabledAppsBlock: EnabledAppsBlock,
    configureAppsBlock: ConfigureAppsBlock,
) : Block(player, origin) {
    private val view: AdminBlockViewController = AdminBlockViewController(player, origin)
    private val interactor: AdminInteractor = AdminInteractor(view, permissionsBlock, enabledAppsBlock, configureAppsBlock)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class AdminBlockViewController(player: Player, origin: Location): NavigationViewController(player, origin), AdminPresenter {

    private lateinit var permissionsButton: ButtonView

    private lateinit var enabledAppsButton: ButtonView

    private lateinit var configureAppsButton: ButtonView

    override fun setPermissionsListener(listener: () -> Unit) = permissionsButton.addListener(listener)

    override fun setEnabledAppsListener(listener: () -> Unit) = enabledAppsButton.addListener(listener)

    override fun setConfigurableAppsListener(listener: () -> Unit) = configureAppsButton.addListener(listener)

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

        permissionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(title)
                .alignTopToBottomOf(title)
                .margins(top = 500),
            text = "${ChatColor.GOLD}Permissions",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Permissions",
        )

        enabledAppsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(permissionsButton)
                .alignTopToBottomOf(permissionsButton)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Enabled Apps",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Enabled Apps",
        )

        configureAppsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(enabledAppsButton)
                .alignTopToBottomOf(enabledAppsButton)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Configure Apps",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Configure Apps",
        )
    }
}

interface AdminPresenter: Presenter {
    fun setPermissionsListener(listener: () -> Unit)

    fun setEnabledAppsListener(listener: () -> Unit)

    fun setConfigurableAppsListener(listener: () -> Unit)
}

class AdminInteractor(
    private val presenter: AdminPresenter,
    private val permissionsBlock: PermissionsBlock,
    private val enabledAppsBlock: EnabledAppsBlock,
    private val configureAppsBlock: ConfigureAppsBlock,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setPermissionsListener {
            routeTo(permissionsBlock)
        }

        presenter.setEnabledAppsListener {
            routeTo(enabledAppsBlock)
        }

        presenter.setConfigurableAppsListener {
            routeTo(configureAppsBlock)
        }
    }
}
