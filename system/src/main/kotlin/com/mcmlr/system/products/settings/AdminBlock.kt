package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
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
    titleBlock: TitleBlock,
) : Block(player, origin) {
    private val view: AdminBlockViewController = AdminBlockViewController(player, origin)
    private val interactor: AdminInteractor = AdminInteractor(view, permissionsBlock, enabledAppsBlock, configureAppsBlock, titleBlock)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class AdminBlockViewController(player: Player, origin: Location): NavigationViewController(player, origin), AdminPresenter {

    private lateinit var titleButton: ButtonView

    private lateinit var permissionsButton: ButtonView

    private lateinit var enabledAppsButton: ButtonView

    private lateinit var configureAppsButton: ButtonView

    override fun setTitleListener(listener: Listener) = titleButton.addListener(listener)

    override fun setPermissionsListener(listener: Listener) = permissionsButton.addListener(listener)

    override fun setEnabledAppsListener(listener: Listener) = enabledAppsButton.addListener(listener)

    override fun setConfigurableAppsListener(listener: Listener) = configureAppsButton.addListener(listener)

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

        titleButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(title)
                .alignTopToBottomOf(title)
                .margins(top = 500),
            text = "${ChatColor.GOLD}Set Title",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Set Title",
        )

        permissionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(titleButton)
                .alignTopToBottomOf(titleButton)
                .margins(top = 50),
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
    fun setTitleListener(listener: Listener)

    fun setPermissionsListener(listener: Listener)

    fun setEnabledAppsListener(listener: Listener)

    fun setConfigurableAppsListener(listener: Listener)
}

class AdminInteractor(
    private val presenter: AdminPresenter,
    private val permissionsBlock: PermissionsBlock,
    private val enabledAppsBlock: EnabledAppsBlock,
    private val configureAppsBlock: ConfigureAppsBlock,
    private val titleBlock: TitleBlock,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setTitleListener(object : Listener {
            override fun invoke() {
                routeTo(titleBlock)
            }
        })

        presenter.setPermissionsListener(object : Listener {
            override fun invoke() {
                routeTo(permissionsBlock)
            }
        })

        presenter.setEnabledAppsListener(object : Listener {
            override fun invoke() {
                routeTo(enabledAppsBlock)
            }
        })

        presenter.setConfigurableAppsListener(object : Listener {
            override fun invoke() {
                routeTo(configureAppsBlock)
            }
        })
    }
}
