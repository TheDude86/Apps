package com.mcmlr.system.products.settings

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.colorize
import com.mcmlr.system.S
import com.mcmlr.system.SystemConfigRepository
import com.mcmlr.system.placeholder.placeholders
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class TitleBlock @Inject constructor(
    player: Player,
    origin: Origin,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {
    private val view = TitleViewController(player, origin)
    private val interactor = TitleInteractor(player, view, systemConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TitleViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), TitlePresenter {

    private lateinit var serverNameView: TextInputView
    private lateinit var saveButtonView: ButtonView
    private lateinit var titleSavedLabel: TextView

    override fun setTitle(title: String) {
        serverNameView.update(text = title)
    }

    override fun setSaveListener(listener: Listener) = saveButtonView.addListener(listener)

    override fun setTitleTextInputListener(listener: TextListener) = serverNameView.addTextChangedListener(listener)

    override fun showSavedTitleLabel() {
        titleSavedLabel.update(text = R.getString(player, S.UPDATED_TITLE_MESSAGE.resource()))
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.SET_TITLE_TITLE.resource()),
            size = 16,
        )

        val paragraphOne = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(title)
                .margins(top = 300),
            text = R.getString(player, S.SET_TITLE_MESSAGE.resource()),
            lineWidth = 500,
            alignment = Alignment.LEFT,
            size = 6,
        )

        serverNameView = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(paragraphOne)
                .centerHorizontally()
                .margins(top = 100),
            size = 16,
            text = R.getString(player, S.SET_TITLE_INPUT_PLACEHOLDER.resource()),
        )

        saveButtonView = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(serverNameView)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            text = R.getString(player, S.SAVE_BUTTON.resource()),
            highlightedText = R.getString(player, S.SAVE_BUTTON.resource()).bolden(),
        )

        titleSavedLabel = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(saveButtonView)
                .centerHorizontally()
                .margins(top = 50),
            size = 4,
            text = "",
        )
    }
}

interface TitlePresenter: Presenter {
    fun setTitle(title: String)
    fun setTitleTextInputListener(listener: TextListener)
    fun setSaveListener(listener: Listener)
    fun showSavedTitleLabel()
}

class TitleInteractor(
    private val player: Player,
    private val presenter: TitlePresenter,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {

    var newTitle: String = systemConfigRepository.model.title

    override fun onCreate() {
        super.onCreate()

        newTitle = systemConfigRepository.model.title

        presenter.setTitle(systemConfigRepository.model.title)

        presenter.setTitleTextInputListener(object : TextListener {
            override fun invoke(text: String) {
                newTitle = text
                presenter.setTitle(newTitle.colorize().placeholders(player))
            }
        })

        presenter.setSaveListener(object : Listener {
            override fun invoke() {
                systemConfigRepository.updateServerTitle(newTitle.colorize().placeholders(player))
                presenter.showSavedTitleLabel()
            }
        })
    }
}