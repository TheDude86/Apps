package com.mcmlr.system.products.homes

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max

class HomeConfigBlock @Inject constructor(
    player: Player,
    origin: Location,
    homesConfigRepository: HomesConfigRepository,
) : Block(player, origin) {
    private val view: HomeConfigViewController = HomeConfigViewController(player, origin)
    private val interactor: HomeConfigInteractor = HomeConfigInteractor(view, homesConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class HomeConfigViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    HomeConfigPresenter {

    private lateinit var maxHomesView: TextInputView
    private lateinit var delayView: TextInputView
    private lateinit var cooldownView: TextInputView
    private lateinit var messageView: TextView

    override fun setMaxHomesListener(listener: (String) -> Unit) = maxHomesView.addTextChangedListener(listener)

    override fun setDelayListener(listener: (String) -> Unit) = delayView.addTextChangedListener(listener)

    override fun setCooldownListener(listener: (String) -> Unit) = cooldownView.addTextChangedListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Home Settings",
            size = 16,
        )

        addViewContainer(
            modifier = Modifier()
                .size(850, 0)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 600),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
            val maxHomesTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .alignStartToStartOf(this),
                size = 6,
                text = "Max Homes",
            )

            val maxHomesMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(maxHomesTitle)
                    .alignStartToStartOf(maxHomesTitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}The maximum homes a player can set.",
            )

            maxHomesView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(maxHomesTitle)
                    .alignBottomToTopOf(maxHomesMessage),
                size = 6,
                text = "${ChatColor.GOLD}0 Home",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}0 Home",
            )

            val teleportDelayTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(maxHomesMessage)
                    .alignStartToStartOf(this)
                    .margins(top = 100),
                size = 6,
                text = "Teleport Delay",
            )

            val teleportDelayMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(teleportDelayTitle)
                    .alignStartToStartOf(teleportDelayTitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}The amount of time, in seconds, the player must wait before being teleported to a selected home.",
            )

            delayView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(teleportDelayTitle)
                    .alignBottomToTopOf(teleportDelayMessage),
                size = 6,
                text = "${ChatColor.GOLD}1 Seconds",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}1 Seconds",
            )

            val teleportCooldownTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(teleportDelayMessage)
                    .alignStartToStartOf(this)
                    .margins(top = 100),
                size = 6,
                text = "Teleport Cooldown",
            )

            val teleportCooldownMessage = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(teleportCooldownTitle)
                    .alignStartToStartOf(teleportCooldownTitle),
                alignment = Alignment.LEFT,
                lineWidth = 300,
                size = 4,
                text = "${ChatColor.GRAY}The amount of time, in seconds, the player must wait after teleporting to a home before they can teleport again.",
            )

            cooldownView = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(teleportCooldownTitle)
                    .alignBottomToTopOf(teleportCooldownMessage),
                size = 6,
                text = "${ChatColor.GOLD}1 Seconds",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}1 Seconds",
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
    fun setMaxHomesListener(listener: (String) -> Unit)
    fun setDelayListener(listener: (String) -> Unit)
    fun setCooldownListener(listener: (String) -> Unit)
    fun setMessage(message: String)
}

class HomeConfigInteractor(
    private val presenter: HomeConfigPresenter,
    private val homesConfigRepository: HomesConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        val maxHomes = homesConfigRepository.maxHomes()
        val teleportDelay = homesConfigRepository.delay()
        val cooldown = homesConfigRepository.cooldown()

        presenter.updateMaxHomesText("$maxHomes Home${if (maxHomes != 1) "s" else ""}")
        presenter.updateDelayText("$teleportDelay Second${if (teleportDelay != 1) "s" else ""}")
        presenter.updateCooldownText("$cooldown Second${if (cooldown != 1) "s" else ""}")

        presenter.setMaxHomesListener {
            val max = it.toIntOrNull()
            if (max == null) {
                val homes = homesConfigRepository.maxHomes()
                presenter.setMessage("${ChatColor.RED}Max home values must be whole numbers!")
                presenter.updateMaxHomesText("$homes Home${if (homes != 1) "s" else ""}")
                return@setMaxHomesListener
            }

            val homes = max(1, max)
            homesConfigRepository.updateMaxHomes(max(1, max))
            presenter.updateMaxHomesText("$homes Home${if (homes != 1) "s" else ""}")

            presenter.setMessage("")
        }

        presenter.setDelayListener {
            val delay = it.toIntOrNull()
            if (delay == null) {
                val defaultDelay = homesConfigRepository.delay()
                presenter.setMessage("${ChatColor.RED}Teleport delay values must be whole numbers!")
                presenter.updateDelayText("$defaultDelay Second${if (defaultDelay != 1) "s" else ""}")
                return@setDelayListener
            }

            val delaySeconds = max(0, delay)
            homesConfigRepository.updateHomesDelay(delaySeconds)
            presenter.updateDelayText("$delaySeconds Second${if (delaySeconds != 1) "s" else ""}")

            presenter.setMessage("")
        }

        presenter.setCooldownListener {
            val delay = it.toIntOrNull()
            if (delay == null) {
                val defaultCooldown = homesConfigRepository.cooldown()
                presenter.setMessage("${ChatColor.RED}Teleport cooldown values must be whole numbers!")
                presenter.updateCooldownText("$defaultCooldown Second${if (defaultCooldown != 1) "s" else ""}")
                return@setCooldownListener
            }

            val delaySeconds = max(0, delay)
            homesConfigRepository.updateHomesCooldown(delaySeconds)
            presenter.updateCooldownText("$delaySeconds Second${if (delaySeconds != 1) "s" else ""}")

            presenter.setMessage("")
        }
    }
}
