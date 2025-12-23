package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.S
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.Locale
import javax.inject.Inject

class DefaultLanguageBlock @Inject constructor(
    player: Player,
    origin: Origin,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {
    private val view = DefaultLanguageViewController(player, origin)
    private val interactor = DefaultLanguageInteractor(player, view, systemConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class DefaultLanguageViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), DefaultLanguagePresenter {

    private lateinit var languageButton: ButtonView
    private lateinit var contentView: ViewContainer

    private lateinit var defaultLanguageListener: Listener
    private lateinit var selectedLanguageListener: (Locale) -> Unit

    override fun setSelectedLanguageListener(callback: (Locale) -> Unit) {
        selectedLanguageListener = callback
    }

    override fun setDefaultLanguageListener(listener: Listener) {
        defaultLanguageListener = listener
    }

    override fun showLanguageList(locales: List<Locale>) {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                addListFeedView(
                    modifier = Modifier()
                        .size(MATCH_PARENT, MATCH_PARENT)
                        .center(),
                    content = object : ContextListener<ViewContainer>() {
                        override fun ViewContainer.invoke() {

                            locales.forEach { locale ->
                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .centerHorizontally()
                                        .margins(top = 50),
                                    text = "${ChatColor.GOLD}${locale.displayLanguage} (${locale.displayCountry})",
                                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${locale.displayLanguage} (${locale.displayCountry})",
                                    callback = object : Listener {
                                        override fun invoke() {
                                            selectedLanguageListener.invoke(locale)
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        })
    }

    override fun showSelectedLanguage(locale: Locale) {
        contentView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                val currentLanguageTitle = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .alignStartToStartOf(this)
                        .margins(top = 200),
                    size = 6,
                    text = R.getString(player, S.CURRENT_LANGUAGE_TITLE.resource()),
                )

                val currentLanguageMessage = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(currentLanguageTitle)
                        .alignStartToStartOf(currentLanguageTitle),
                    alignment = Alignment.LEFT,
                    lineWidth = 300,
                    size = 4,
                    text = R.getString(player, S.CURRENT_LANGUAGE_MESSAGE.resource()),
                )

                val languageText = "${locale.displayLanguage} (${locale.displayCountry})"
                languageButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .position(600, 0)
                        .alignTopToBottomOf(currentLanguageTitle)
                        .alignBottomToTopOf(currentLanguageMessage),
                    size = 6,
                    text = "${ChatColor.GOLD}$languageText",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$languageText",
                    callback = defaultLanguageListener,
                )
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
            text = R.getString(player, S.SETTINGS_SET_LANGUAGE_TITLE.resource()),
            size = 16,
        )

        contentView = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(this)
                .alignEndToEndOf(this)
                .alignBottomToBottomOf(this)
                .margins(start = 800, top = 100, end = 800, bottom = 100),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

}

interface DefaultLanguagePresenter: Presenter {
    fun showSelectedLanguage(locale: Locale)

    fun showLanguageList(locales: List<Locale>)

    fun setDefaultLanguageListener(listener: Listener)

    fun setSelectedLanguageListener(callback: (Locale) -> Unit)
}

class DefaultLanguageInteractor(
    private val player: Player,
    private val presenter: DefaultLanguagePresenter,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setDefaultLanguageListener(object : Listener {
            override fun invoke() {
                presenter.showLanguageList(SystemConfigRepository.supportedLocals)
            }
        })

        presenter.setSelectedLanguageListener { locale ->
            showSelectedLanguage(locale.toString())
            systemConfigRepository.updateDefaultLanguage(locale)
            R.updateDefaultLocale(locale)
        }

        showSelectedLanguage(systemConfigRepository.model.defaultLanguage)
    }

    private fun showSelectedLanguage(localeString: String) {
        var locale = localeString.split("_")
        if (locale.size != 2) {
            //TODO: Show error message
            systemConfigRepository.updateDefaultLanguage(Locale.US)
            locale = listOf("en", "us")
        }

        val language = locale[0]
        val country = locale[1]

        presenter.showSelectedLanguage(Locale(language, country))
    }
}
