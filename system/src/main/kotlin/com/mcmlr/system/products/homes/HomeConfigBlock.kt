package com.mcmlr.system.products.homes

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
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max

class HomeConfigBlock @Inject constructor(
    player: Player,
    origin: Origin,
    homesConfigRepository: HomesConfigRepository,
) : Block(player, origin) {
    private val view: HomeConfigViewController = HomeConfigViewController(player, origin)
    private val interactor: HomeConfigInteractor = HomeConfigInteractor(player, view, homesConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class HomeConfigViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
    HomeConfigPresenter {

    private lateinit var maxHomesView: TextInputView
    private lateinit var delayView: TextInputView
    private lateinit var cooldownView: TextInputView
    private lateinit var messageView: TextView

    override fun setMaxHomesListener(listener: TextListener) = maxHomesView.addTextChangedListener(listener)

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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${R.getString(player, S.HOMES_CONFIG_TITLE.resource())}",
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
                    val maxHomesTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .alignStartToStartOf(this),
                        size = 6,
                        text = R.getString(player, S.MAX_HOMES_TITLE.resource()),
                    )

                    val maxHomesMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(maxHomesTitle)
                            .alignStartToStartOf(maxHomesTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = "${ChatColor.GRAY}${R.getString(player, S.MAX_HOMES_MESSAGE.resource())}",
                    )

                    maxHomesView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(maxHomesTitle)
                            .alignBottomToTopOf(maxHomesMessage),
                        size = 6,
                        text = "${ChatColor.GOLD}${R.getString(player, S.DEFAULT_MAX_HOMES.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.DEFAULT_MAX_HOMES.resource())}",
                    )

                    val teleportDelayTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(maxHomesMessage)
                            .alignStartToStartOf(this)
                            .margins(top = 100),
                        size = 6,
                        text = R.getString(player, S.CONFIG_TELEPORT_DELAY_TITLE.resource()),
                    )

                    val teleportDelayMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportDelayTitle)
                            .alignStartToStartOf(teleportDelayTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = "${ChatColor.GRAY}${R.getString(player, S.CONFIG_TELEPORT_DELAY_MESSAGE.resource())}",
                    )

                    delayView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(teleportDelayTitle)
                            .alignBottomToTopOf(teleportDelayMessage),
                        size = 6,
                        text = "${ChatColor.GOLD}${R.getString(player, S.CONFIG_TELEPORT_DEFAULT.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CONFIG_TELEPORT_DEFAULT.resource())}",
                    )

                    val teleportCooldownTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportDelayMessage)
                            .alignStartToStartOf(this)
                            .margins(top = 100),
                        size = 6,
                        text = R.getString(player, S.CONFIG_TELEPORT_COOLDOWN_TITLE.resource()),
                    )

                    val teleportCooldownMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(teleportCooldownTitle)
                            .alignStartToStartOf(teleportCooldownTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = "${ChatColor.GRAY}${R.getString(player, S.CONFIG_TELEPORT_COOLDOWN_MESSAGE.resource())}",
                    )

                    cooldownView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(teleportCooldownTitle)
                            .alignBottomToTopOf(teleportCooldownMessage),
                        size = 6,
                        text = "${ChatColor.GOLD}${R.getString(player, S.CONFIG_TELEPORT_DEFAULT.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CONFIG_TELEPORT_DEFAULT.resource())}",
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

    override fun updateMaxHomesText(text: String) {
        maxHomesView.text = "${ChatColor.GOLD}$text"
        maxHomesView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$text"
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

interface HomeConfigPresenter: Presenter {
    fun updateMaxHomesText(text: String)
    fun updateDelayText(text: String)
    fun updateCooldownText(text: String)
    fun setMaxHomesListener(listener: TextListener)
    fun setDelayListener(listener: TextListener)
    fun setCooldownListener(listener: TextListener)
    fun setMessage(message: String)
}

class HomeConfigInteractor(
    private val player: Player,
    private val presenter: HomeConfigPresenter,
    private val homesConfigRepository: HomesConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        val maxHomes = homesConfigRepository.maxHomes()
        val teleportDelay = homesConfigRepository.delay()
        val cooldown = homesConfigRepository.cooldown()

        R.getString(player, S.SECONDS_TEXT.resource(), cooldown, if (cooldown != 1) R.getString(player, S.PLURAL.resource()) else "")

        presenter.updateMaxHomesText(R.getString(player, S.MAX_HOMES_TEXT.resource(), maxHomes, if (maxHomes != 1) R.getString(player, S.PLURAL.resource()) else ""))
        presenter.updateDelayText(R.getString(player, S.SECONDS_TEXT.resource(), teleportDelay, if (teleportDelay != 1) R.getString(player, S.PLURAL.resource()) else ""))
        presenter.updateCooldownText(R.getString(player, S.SECONDS_TEXT.resource(), cooldown, if (cooldown != 1) R.getString(player, S.PLURAL.resource()) else ""))

        presenter.setMaxHomesListener(object : TextListener {
            override fun invoke(text: String) {
                val max = text.toIntOrNull()
                if (max == null) {
                    val homes = homesConfigRepository.maxHomes()
                    presenter.setMessage("${ChatColor.RED}${R.getString(player, S.MAX_HOMES_WHOLE_NUMBERS_ERROR.resource())}")
                    presenter.updateMaxHomesText(R.getString(player, S.MAX_HOMES_TEXT.resource(), homes, if (homes != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val homes = max(1, max)
                homesConfigRepository.updateMaxHomes(max(1, max))
                presenter.updateMaxHomesText(R.getString(player, S.MAX_HOMES_TEXT.resource(), homes, if (homes != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })

        presenter.setDelayListener(object : TextListener {
            override fun invoke(text: String) {
                val delay = text.toIntOrNull()
                if (delay == null) {
                    val defaultDelay = homesConfigRepository.delay()
                    presenter.setMessage("${ChatColor.RED}${R.getString(player, S.DELAY_WHOLE_NUMBERS_ERROR.resource())}")
                    presenter.updateDelayText(R.getString(player, S.SECONDS_TEXT.resource(), defaultDelay, if (defaultDelay != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val delaySeconds = max(0, delay)
                homesConfigRepository.updateHomesDelay(delaySeconds)
                presenter.updateDelayText(R.getString(player, S.SECONDS_TEXT.resource(), delaySeconds, if (delaySeconds != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })

        presenter.setCooldownListener(object : TextListener {
            override fun invoke(text: String) {
                val delay = text.toIntOrNull()
                if (delay == null) {
                    val defaultCooldown = homesConfigRepository.cooldown()
                    presenter.setMessage("${ChatColor.RED}${R.getString(player, S.COOLDOWN_WHOLE_NUMBERS_ERROR.resource())}")
                    presenter.updateCooldownText(R.getString(player, S.SECONDS_TEXT.resource(), defaultCooldown, if (defaultCooldown != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val delaySeconds = max(0, delay)
                homesConfigRepository.updateHomesCooldown(delaySeconds)
                presenter.updateCooldownText(R.getString(player, S.SECONDS_TEXT.resource(), delaySeconds, if (delaySeconds != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })
    }
}
