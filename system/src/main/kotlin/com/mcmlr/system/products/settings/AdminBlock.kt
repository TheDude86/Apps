package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.S
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class AdminBlock @Inject constructor(
    player: Player,
    origin: Origin,
    permissionsBlock: PermissionsBlock,
    enabledAppsBlock: EnabledAppsBlock,
    configureAppsBlock: ConfigureAppsBlock,
    titleBlock: TitleBlock,
    defaultLanguageBlock: DefaultLanguageBlock,
) : Block(player, origin) {
    private val view: AdminBlockViewController = AdminBlockViewController(player, origin)
    private val interactor: AdminInteractor = AdminInteractor(view, permissionsBlock, enabledAppsBlock, configureAppsBlock, titleBlock, defaultLanguageBlock)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class AdminBlockViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), AdminPresenter {

    private lateinit var titleButton: ButtonView

    private lateinit var permissionsButton: ButtonView

    private lateinit var enabledAppsButton: ButtonView

    private lateinit var configureAppsButton: ButtonView

    private lateinit var languageButton: ButtonView

    override fun setTitleListener(listener: Listener) = titleButton.addListener(listener)

    override fun setPermissionsListener(listener: Listener) = permissionsButton.addListener(listener)

    override fun setEnabledAppsListener(listener: Listener) = enabledAppsButton.addListener(listener)

    override fun setConfigurableAppsListener(listener: Listener) = configureAppsButton.addListener(listener)

    override fun setDefaultLanguageListener(listener: Listener) = languageButton.addListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.SETTINGS_TITLE.resource()),
            size = 16,
        )

        titleButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(title)
                .alignTopToBottomOf(title)
                .margins(top = 500),
            text = R.getString(player, S.SET_TITLE_BUTTON.resource()),
            highlightedText = R.getString(player, S.SET_TITLE_BUTTON.resource()).bolden(),
        )

        permissionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(titleButton)
                .alignTopToBottomOf(titleButton)
                .margins(top = 50),
            text = R.getString(player, S.PERMISSIONS_BUTTON.resource()),
            highlightedText = R.getString(player, S.SET_TITLE_BUTTON.resource()).bolden(),
        )

        enabledAppsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(permissionsButton)
                .alignTopToBottomOf(permissionsButton)
                .margins(top = 50),
            text = R.getString(player, S.ENABLED_APPS_BUTTON.resource()),
            highlightedText = R.getString(player, S.ENABLED_APPS_BUTTON.resource()).bolden(),
        )

        configureAppsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(enabledAppsButton)
                .alignTopToBottomOf(enabledAppsButton)
                .margins(top = 50),
            text = R.getString(player, S.CONFIGURE_APPS_BUTTON.resource()),
            highlightedText = R.getString(player, S.CONFIGURE_APPS_BUTTON.resource()).bolden(),
        )

        languageButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(enabledAppsButton)
                .alignTopToBottomOf(configureAppsButton)
                .margins(top = 50),
            text = R.getString(player, S.DEFAULT_LANGUAGE_BUTTON.resource()),
            highlightedText = R.getString(player, S.DEFAULT_LANGUAGE_BUTTON.resource()).bolden(),
        )
    }
}

interface AdminPresenter: Presenter {
    fun setTitleListener(listener: Listener)

    fun setPermissionsListener(listener: Listener)

    fun setEnabledAppsListener(listener: Listener)

    fun setConfigurableAppsListener(listener: Listener)

    fun setDefaultLanguageListener(listener: Listener)
}

class AdminInteractor(
    private val presenter: AdminPresenter,
    private val permissionsBlock: PermissionsBlock,
    private val enabledAppsBlock: EnabledAppsBlock,
    private val configureAppsBlock: ConfigureAppsBlock,
    private val titleBlock: TitleBlock,
    private val defaultLanguageBlock: DefaultLanguageBlock,
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

        presenter.setDefaultLanguageListener(object : Listener {
            override fun invoke() {
                routeTo(defaultLanguageBlock)
            }
        })
    }
}
