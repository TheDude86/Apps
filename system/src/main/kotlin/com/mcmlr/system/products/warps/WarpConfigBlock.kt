package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max

class WarpConfigBlock @Inject constructor(
    player: Player,
    origin: Origin,
    warpsConfigRepository: WarpsConfigRepository
) : Block(player, origin) {
    private val view: WarpConfigViewController = WarpConfigViewController(player, origin)
    private val interactor: WarpConfigInteractor = WarpConfigInteractor(player, view, warpsConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class WarpConfigViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
    WarpConfigPresenter {

    private lateinit var delayView: TextInputView
    private lateinit var cooldownView: TextInputView
    private lateinit var messageView: TextView

    override fun setDelayListener(listener: TextListener) = delayView.addTextChangedListener(listener)

    override fun setCooldownListener(listener: TextListener) = cooldownView.addTextChangedListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.WARP_CONFIG_TITLE.resource()),
            size = 16,
        )

        addViewContainer(
            modifier = Modifier()
                .size(850, 0)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 600),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    val teleportDelayTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .alignStartToStartOf(this),
                        size = 6,
                        text = R.getString(player, S.CONFIG_DELAY_TITLE.resource()),
                    )

                    val teleportDelayMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportDelayTitle)
                            .alignStartToStartOf(teleportDelayTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = R.getString(player, S.CONFIG_DELAY_MESSAGE.resource()),
                    )

                    delayView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(teleportDelayTitle)
                            .alignBottomToTopOf(teleportDelayMessage),
                        size = 6,
                        text = R.getString(player, S.CONFIG_DEFAULT_WAIT_VALUE.resource()),
                        highlightedText = R.getString(player, S.CONFIG_DEFAULT_WAIT_VALUE.resource()).bolden(),
                    )

                    val teleportCooldownTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportDelayMessage)
                            .alignStartToStartOf(this)
                            .margins(top = 100),
                        size = 6,
                        text = R.getString(player, S.CONFIG_COOLDOWN_TITLE.resource()),
                    )

                    val teleportCooldownMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportCooldownTitle)
                            .alignStartToStartOf(teleportCooldownTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = R.getString(player, S.CONFIG_COOLDOWN_MESSAGE.resource()),
                    )

                    cooldownView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(teleportCooldownTitle)
                            .alignBottomToTopOf(teleportCooldownMessage),
                        size = 6,
                        text = R.getString(player, S.CONFIG_DEFAULT_WAIT_VALUE.resource()),
                        highlightedText = R.getString(player, S.CONFIG_DEFAULT_WAIT_VALUE.resource()).bolden(),
                    )

                    messageView = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportCooldownMessage)
                            .centerHorizontally()
                            .margins(top = 200),
                        size = 4,
                        text = ""
                    )
                }
            }
        )
    }

    override fun updateDelayText(text: String) {
        delayView.text = "${ChatColor.GOLD}$text"
        delayView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$text"
    }

    override fun updateCooldownText(text: String) {
        cooldownView.text = "${ChatColor.GOLD}$text"
        cooldownView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$text"
    }

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }
}

interface WarpConfigPresenter: Presenter {
    fun updateDelayText(text: String)
    fun updateCooldownText(text: String)
    fun setDelayListener(listener: TextListener)
    fun setCooldownListener(listener: TextListener)
    fun setMessage(message: String)
}

class WarpConfigInteractor(
    private val player: Player,
    private val presenter: WarpConfigPresenter,
    private val warpsConfigRepository: WarpsConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        val teleportDelay = warpsConfigRepository.delay()
        val cooldown = warpsConfigRepository.cooldown()

        presenter.updateDelayText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), teleportDelay, if (teleportDelay != 1) R.getString(player, S.PLURAL.resource()) else ""))
        presenter.updateCooldownText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), cooldown, if (cooldown != 1) R.getString(player, S.PLURAL.resource()) else ""))

        presenter.setDelayListener(object : TextListener {
            override fun invoke(text: String) {
                val delay = text.toIntOrNull()
                if (delay == null) {
                    val defaultDelay = warpsConfigRepository.delay()
                    presenter.setMessage(R.getString(player, S.CONFIG_DELAY_ERROR_MESSAGE.resource()))
                    presenter.updateDelayText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), defaultDelay, if (defaultDelay != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val delaySeconds = max(0, delay)
                warpsConfigRepository.updateWarpsDelay(delaySeconds)
                presenter.updateDelayText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), delaySeconds, if (delaySeconds != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })

        presenter.setCooldownListener(object : TextListener {
            override fun invoke(text: String) {
                val delay = text.toIntOrNull()
                if (delay == null) {
                    val defaultCooldown = warpsConfigRepository.cooldown()
                    presenter.setMessage(R.getString(player, S.CONFIG_COOLDOWN_ERROR_MESSAGE.resource()))
                    presenter.updateCooldownText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), defaultCooldown, if (defaultCooldown != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val delaySeconds = max(0, delay)
                warpsConfigRepository.updateWarpsCooldown(delaySeconds)
                presenter.updateCooldownText(R.getString(player, S.CONFIG_INPUT_SECONDS_PLACEHOLDER.resource(), delaySeconds, if (delaySeconds != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })
    }
}