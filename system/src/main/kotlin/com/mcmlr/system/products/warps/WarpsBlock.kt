package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.FeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.system.products.data.*
import kotlinx.coroutines.*
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class WarpsBlock @Inject constructor(
    player: Player,
    origin: Origin,
    private val addWarpsBlock: AddWarpsBlock,
    private val warpsRepository: WarpsRepository,
    private val permissionsRepository: PermissionsRepository,
    private val warpsConfigRepository: WarpsConfigRepository,
): Block(player, origin) {
    private val view = WarpsViewController(player, origin, permissionsRepository)
    private val interactor = WarpsInteractor(player, view, addWarpsBlock, warpsRepository, warpsConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class WarpsViewController(private val player: Player, origin: Origin, private val permissionsRepository: PermissionsRepository,): NavigationViewController(player, origin),
    WarpsPresenter {

    private lateinit var container: FeedView
    private lateinit var messageView: TextView

    private var newWarpButton: ButtonView? = null
    private var removeWarpButton: ButtonView? = null

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.WARPS_TITLE.resource()),
            size = 16,
        )


        container = addFeedView(
            modifier = Modifier()
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .size(700, 400)
                .margins(top = 100),
        )

        if (permissionsRepository.checkPermission(player, PermissionNode.ADMIN)) {
            newWarpButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(-600, 0)
                    .alignTopToBottomOf(container)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}${R.getString(player, S.ADD_NEW_WARP.resource())}",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.ADD_NEW_WARP.resource())}",
            )

            removeWarpButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(600, 0)
                    .alignTopToBottomOf(container)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}${R.getString(player, S.REMOVE_WARP.resource())}",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.REMOVE_WARP.resource())}",
            )
        }

        messageView = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(newWarpButton ?: container)
                .centerHorizontally()
                .margins(top = 100),
            size = 4,
            text = ""
        )
    }

    override fun setWarps(warps: List<WarpModel>, deleteMode: Boolean, listener: WarpActionListener) {
        if (deleteMode) {
            removeWarpButton?.text = "${ChatColor.GOLD}${R.getString(player, S.CANCEL.resource())}"
            removeWarpButton?.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CANCEL.resource())}"
        } else {
            removeWarpButton?.text = "${ChatColor.GOLD}${R.getString(player, S.REMOVE_WARP.resource())}"
            removeWarpButton?.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.REMOVE_WARP.resource())}"
        }

        container.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (warps.isEmpty()) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 10),
                        text = R.getString(player, S.EMPTY_WARPS_LIST.resource()),
                        size = 8,
                    )

                    return
                }

                var homeView: ButtonView? = null
                warps.forEach { home ->
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

                    if (permissionsRepository.checkPermission(player, PermissionNode.ADMIN)) {
                        val text = R.getString(player, (if (deleteMode) S.DELETE else S.EDIT).resource())
                        addButtonView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignStartToEndOf(homeView!!)
                                .alignTopToTopOf(homeView!!)
                                .alignBottomToBottomOf(homeView!!)
                                .margins(start = 64),
                            text = if (deleteMode) "${ChatColor.RED}$text" else text,
                            highlightedText = if (deleteMode) "${ChatColor.RED}${ChatColor.BOLD}$text" else "${ChatColor.BOLD}$text",
                            callback = object : Listener {
                                override fun invoke() {
                                    listener.edit(home, deleteMode)
                                }
                            }
                        )
                    }
                }
            }
        })
    }

    override fun addNewWarpListener(listener: Listener) {
        newWarpButton?.addListener(listener)
    }

    override fun addRemoveWarpListener(listener: Listener) {
        removeWarpButton?.addListener(listener)
    }

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }
}

interface WarpsPresenter: Presenter {
    fun setWarps(warps: List<WarpModel>, deleteMode: Boolean, listener: WarpActionListener)
    fun addNewWarpListener(listener: Listener)
    fun addRemoveWarpListener(listener: Listener)
    fun setMessage(message: String)
}

interface WarpActionListener {
    fun teleport(warp: WarpModel)
    fun edit(warp: WarpModel, delete: Boolean)
}

class WarpsInteractor(
    private val player: Player,
    private val presenter: WarpsPresenter,
    private val addWarpsBlock: AddWarpsBlock,
    private val warpsRepository: WarpsRepository,
    private val warpsConfigRepository: WarpsConfigRepository,
): Interactor(presenter), WarpActionListener {

    private var deleteMode = false

    override fun onCreate() {
        super.onCreate()

        presenter.addNewWarpListener(object : Listener {
            override fun invoke() {
                routeTo(addWarpsBlock)
            }
        })

        presenter.addRemoveWarpListener(object : Listener {
            override fun invoke() {
                val model = warpsRepository.getWarps()
                deleteMode = !deleteMode
                presenter.setWarps(model, deleteMode, this@WarpsInteractor)
            }
        })

        getWarps()
    }

    private fun getWarps() {
        clear()
        presenter.setWarps(warpsRepository.getWarps(), false, this)
    }

    override fun teleport(warp: WarpModel) {
        val wait = warpsRepository.canTeleport(player) / 1000
        if (wait > 0) {
            presenter.setMessage(R.getString(player, S.COOLDOWN_ERROR_MESSAGE.resource(), wait, if (wait != 1L) R.getString(player, S.PLURAL.resource()) else ""))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val parent = this
            var delay = warpsConfigRepository.model.delay
            while (delay > 0) {
                CoroutineScope(DudeDispatcher()).launch {
                    val message = R.getString(player, S.DELAY_MESSAGE.resource(), delay, if (delay != 1) R.getString(player, S.PLURAL.resource()) else "")
                    //TODO: Check spigot vs paper
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message))
                }

                delay(1.seconds)
                delay--
            }

            CoroutineScope(DudeDispatcher()).launch {
                val location = Location(
                    Bukkit.getWorld(warp.world),
                    warp.x,
                    warp.y,
                    warp.z,
                    warp.yaw,
                    warp.pitch
                )

                warpsRepository.teleport(player, location)
                parent.cancel()
            }
        }

        close()
    }

    override fun edit(warp: WarpModel, delete: Boolean) {
        if (delete) {
            warpsRepository.deleteWarp(warp)
            getWarps()
        } else {
            warpsRepository.updateWarp(warp)
            routeTo(addWarpsBlock)
        }
    }
}
