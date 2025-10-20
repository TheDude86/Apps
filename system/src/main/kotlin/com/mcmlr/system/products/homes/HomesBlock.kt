package com.mcmlr.system.products.homes

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.FeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import kotlinx.coroutines.*
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class HomesBlock @Inject constructor(
    player: Player,
    origin: Location,
    addHomeBlock: AddHomeBlock,
    homesRepository: HomesRepository,
    homesConfigRepository: HomesConfigRepository,
): Block(player, origin) {
    private val view = HomesViewController(player, origin)
    private val interactor = HomesInteractor(player, view, addHomeBlock, homesRepository, homesConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class HomesViewController(
    player: Player,
    origin: Location
): NavigationViewController(player, origin), HomesPresenter {

    private lateinit var container: FeedView
    private lateinit var newHomeButton: ButtonView
    private lateinit var removeHomeButton: ButtonView
    private lateinit var messageView: TextView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Homes",
            size = 16,
        )

        container = addFeedView(
            modifier = Modifier()
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .size(700, 400)
                .margins(top = 100),
        )

        newHomeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(-600, 0)
                .alignTopToBottomOf(container)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Add new home",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Add new home"
        )

        removeHomeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(600, 0)
                .alignTopToBottomOf(container)
                .margins(top = 50),
            text = "${ChatColor.GOLD}Remove home",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Remove home"
        )

        messageView = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(newHomeButton)
                .centerHorizontally()
                .margins(top = 100),
            size = 4,
            text = ""
        )
    }

    override fun addNewHomeListener(listener: Listener) = newHomeButton.addListener(listener)

    override fun addRemoveHomeListener(listener: Listener) = removeHomeButton.addListener(listener)

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }

    override fun setHomes(homes: List<HomeModel>, deleteMode: Boolean, listener: HomeActionListener) {
        if (deleteMode) {
            removeHomeButton.text = "${ChatColor.GOLD}Cancel"
            removeHomeButton.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Cancel"
        } else {
            removeHomeButton.text = "${ChatColor.GOLD}Remove home"
            removeHomeButton.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Remove home"
        }

        container.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (homes.isEmpty()) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 10),
                        text = "${ChatColor.GRAY}${ChatColor.ITALIC}You don't have any\nhomes yet...",
                        size = 8,
                    )

                    return
                }

                var homeView: ButtonView? = null
                homes.forEach { home ->
                    val modifier = if (homeView == null) {
                        Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 10)
                    } else {
                        homeView?.let {
                            Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignTopToBottomOf(it)
                                .centerHorizontally()
                                .margins(top = 25)
                        }
                    } ?: return@forEach


                    homeView = addButtonView(
                        modifier = modifier,
                        text = home.name,
                        highlightedText = "${ChatColor.BOLD}${home.name}",
                        callback = object : Listener {
                            override fun invoke() {
                                listener.teleport(home)
                            }
                        }
                    )

                    addItemView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignEndToStartOf(homeView!!)
                            .alignTopToTopOf(homeView!!)
                            .alignBottomToBottomOf(homeView!!)
                            .margins(end = 64),
                        item = home.icon,
                    )

                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(homeView!!)
                            .alignTopToTopOf(homeView!!)
                            .alignBottomToBottomOf(homeView!!)
                            .margins(start = 64),
                        text = if (deleteMode) "${ChatColor.RED}\uD83D\uDDD1" else "✎",
                        highlightedText = if (deleteMode) "${ChatColor.RED}${ChatColor.BOLD}\uD83D\uDDD1" else "${ChatColor.BOLD}✎",
                        callback = object : Listener {
                            override fun invoke() {
                                listener.edit(home, deleteMode)
                            }
                        }
                    )
                }
            }
        })
    }
}

interface HomesPresenter: Presenter {
    fun setHomes(homes: List<HomeModel>, deleteMode: Boolean, listener: HomeActionListener)
    fun addNewHomeListener(listener: Listener)
    fun addRemoveHomeListener(listener: Listener)
    fun setMessage(message: String)
}

interface HomeActionListener {
    fun teleport(home: HomeModel)
    fun edit(home: HomeModel, delete: Boolean)
}

class HomesInteractor(
    private val player: Player,
    private val presenter: HomesPresenter,
    private val addHomeBlock: AddHomeBlock,
    private val homesRepository: HomesRepository,
    private val homesConfigRepository: HomesConfigRepository,
): Interactor(presenter), HomeActionListener {

    private var deleteMode = false

    override fun onCreate() {
        super.onCreate()

        presenter.addNewHomeListener(object : Listener {
            override fun invoke() {
                val model = homesRepository.latest(player) ?: return
                val maxHomes = homesConfigRepository.model.maxHomes
                if (model.homes.size >= maxHomes) {
                    presenter.setMessage("${ChatColor.RED}You already have the maximum number of homes set, please delete a home before adding a new one")
                    return
                }

                routeTo(addHomeBlock)
            }
        })

        presenter.addRemoveHomeListener(object : Listener {
            override fun invoke() {
                val model = homesRepository.latest(player) ?: return
                deleteMode = !deleteMode
                presenter.setHomes(model.homes, deleteMode, this@HomesInteractor)
            }
        })

        getHomes()
    }

    private fun getHomes() {
        clear()
        homesRepository.getHomes(player)
            .collectOn(DudeDispatcher())
            .collectLatest {
                presenter.setHomes(it.homes, false, this)
            }
            .disposeOn(disposer = this)
    }

    override fun edit(home: HomeModel, delete: Boolean) {
        if (delete) {
            homesRepository.deleteHome(player, home)
            getHomes()
        } else {
            homesRepository.updateHome(home)
            routeTo(addHomeBlock)
        }
        presenter.setMessage("")
    }

    override fun teleport(home: HomeModel) {
        val wait = homesRepository.canTeleport(player) / 1000
        if (wait > 0) {
            presenter.setMessage("${ChatColor.RED}You must wait $wait second${if (wait != 1L) "s" else ""} before you can teleport")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val parent = this
            var delay = homesConfigRepository.model.delay
            while (delay > 0) {
                CoroutineScope(DudeDispatcher()).launch {
                    val message = "${ChatColor.DARK_AQUA}You will be teleported in $delay second${if (delay != 1) "s" else ""}"
                    //TODO: Check spigot vs paper
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message))
                }

                delay(1.seconds)
                delay--
            }

            CoroutineScope(DudeDispatcher()).launch {
                val location = Location(
                    Bukkit.getWorld(home.world),
                    home.x,
                    home.y,
                    home.z,
                    home.yaw,
                    home.pitch
                )

                homesRepository.teleport(player, location)
                parent.cancel()
            }
        }

        close()
    }
}
