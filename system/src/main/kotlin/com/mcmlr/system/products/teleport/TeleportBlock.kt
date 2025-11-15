package com.mcmlr.system.products.teleport

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
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.underline
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject

class TeleportBlock @Inject constructor(
    player: Player,
    origin: Origin,
    teleportRequestBlock: TeleportRequestBlock,
    teleportResponseBlock: TeleportResponseBlock,
    teleportRepository: TeleportRepository,
    playerTeleportRepository: PlayerTeleportRepository,
): Block(player, origin) {
    private val view = TeleportViewController(player, origin)
    private val interactor = TeleportInteractor(player, view, teleportRequestBlock, teleportResponseBlock, teleportRepository, playerTeleportRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TeleportViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
    TeleportPresenter {
    private lateinit var feedView: FeedView
    private lateinit var players: ButtonView
    private lateinit var requests: ButtonView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.TELEPORT_TITLE.resource()),
            size = 16,
        )

        players = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(-500, 0)
                .alignTopToBottomOf(title)
                .margins(top = 200),
            text = R.getString(player, S.PLAYERS_TAB.resource()),
            highlightedText = R.getString(player, S.PLAYERS_TAB.resource()).underline(),
        )

        requests = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .position(500, 0)
                .alignTopToBottomOf(title)
                .margins(top = 200),
            text = R.getString(player, S.REQUESTS_TAB.resource()),
            highlightedText = R.getString(player, S.REQUESTS_TAB.resource()).bolden(),
        )

        feedView = addFeedView(
            modifier = Modifier()
                .size(800, 500)
                .alignTopToBottomOf(players)
                .centerHorizontally()
                .margins(top = 100),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }

    override fun setPlayersButtonCallback(callback: Listener) = players.addListener(callback)

    override fun setRequestsButtonCallback(callback: Listener) = requests.addListener(callback)

    override fun setRequestList(requests: List<TeleportRequestModel>, callback: (TeleportRequestModel) -> Unit) {
        this.players.text = R.getString(player, S.PLAYERS_BUTTON.resource())
        this.players.highlightedText = R.getString(player, S.PLAYERS_BUTTON.resource()).bolden()

        this.requests.text = R.getString(player, S.REQUESTS_BUTTON.resource()).bolden()
        this.requests.highlightedText = R.getString(player, S.REQUESTS_BUTTON.resource()).bolden().underline()

        updateTextDisplay(this.players)
        updateTextDisplay(this.requests)

        feedView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (requests.isEmpty()) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 10),
                        text = R.getString(player, S.EMPTY_REQUESTS_TEXT.resource()),
                        size = 8,
                    )

                    return
                }

                var row: ViewContainer? = null
                requests.forEach { rowModel ->
                    val r = row
                    val modifier = if (r == null) {
                        Modifier()
                            .size(MATCH_PARENT, 100)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 32)
                    } else {
                        Modifier()
                            .size(MATCH_PARENT, 100)
                            .alignTopToBottomOf(r)
                            .centerHorizontally()
                            .margins(top = 32)
                    }

                    row = addViewContainer(
                        modifier = modifier,
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val head = ItemStack(Material.PLAYER_HEAD)
                                val headMeta = head.itemMeta as SkullMeta
                                headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(rowModel.sender.uniqueId))
                                head.itemMeta = headMeta

                                val playerView = addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .center(),
                                    text = rowModel.sender.displayName,
                                    highlightedText = "${ChatColor.BOLD}${rowModel.sender.displayName}",
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(rowModel)
                                        }
                                    }
                                )

                                addItemView(
                                    modifier = Modifier()
                                        .size(80, 80)
                                        .alignEndToStartOf(playerView)
                                        .alignTopToTopOf(playerView)
                                        .alignBottomToBottomOf(playerView),
                                    item = head,
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToEndOf(playerView)
                                        .centerVertically(),
                                    text = if (rowModel.type == TeleportRequestType.GOTO) "✈" else "\uD83D\uDED6",
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setPlayerList(players: List<Pair<Player, ItemStack>>, callback: (Player) -> Unit) {
        this.players.text = R.getString(player, S.PLAYERS_BUTTON.resource()).bolden()
        this.players.highlightedText = R.getString(player, S.PLAYERS_BUTTON.resource()).bolden().underline()

        this.requests.text = R.getString(player, S.REQUESTS_BUTTON.resource())
        this.requests.highlightedText = R.getString(player, S.REQUESTS_BUTTON.resource()).bolden()

        updateTextDisplay(this.players)
        updateTextDisplay(this.requests)

        feedView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (players.isEmpty()) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 10),
                        text = R.getString(player, S.EMPTY_PLAYERS_TEXT.resource()),
                        size = 8,
                    )

                    return
                }

                var row: ViewContainer? = null
                players.forEach { rowModel ->
                    val r = row
                    val modifier = if (r == null) {
                        Modifier()
                            .size(MATCH_PARENT, 100)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 32)
                    } else {
                        Modifier()
                            .size(MATCH_PARENT, 200)
                            .alignTopToBottomOf(r)
                            .centerHorizontally()
                            .margins(top = 32)
                    }

                    row = addViewContainer(
                        modifier = modifier,
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val playerView = addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .center(),
                                    text = rowModel.first.displayName,
                                    highlightedText = "${ChatColor.BOLD}${rowModel.first.displayName}",
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(rowModel.first)
                                        }
                                    }
                                )

                                addItemView(
                                    modifier = Modifier()
                                        .size(80, 80)
                                        .alignEndToStartOf(playerView)
                                        .alignTopToTopOf(playerView)
                                        .alignBottomToBottomOf(playerView),
                                    item = rowModel.second,
                                )
                            }
                        }
                    )
                }
            }
        })
    }
}

interface TeleportPresenter: Presenter {
    fun setPlayerList(players: List<Pair<Player, ItemStack>>, callback: (Player) -> Unit)

    fun setRequestList(requests: List<TeleportRequestModel>, callback: (TeleportRequestModel) -> Unit)

    fun setPlayersButtonCallback(callback: Listener)

    fun setRequestsButtonCallback(callback: Listener)
}

class TeleportInteractor(
    private val player: Player,
    private val presenter: TeleportPresenter,
    private val teleportRequestBlock: TeleportRequestBlock,
    private val teleportResponseBlock: TeleportResponseBlock,
    private val teleportRepository: TeleportRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        gotoPlayerList()

        presenter.setPlayersButtonCallback(object : Listener {
            override fun invoke() {
                gotoPlayerList()
            }
        })

        presenter.setRequestsButtonCallback(object : Listener {
            override fun invoke() {
                gotoRequestList()
            }
        })
    }

    private fun gotoRequestList() {
        val requests = teleportRepository.getRequests(player.uniqueId)
        presenter.setRequestList(requests) {
            playerTeleportRepository.selectedRequest = it
            routeTo(teleportResponseBlock)
        }
    }

    private fun gotoPlayerList() {
        val players = teleportRepository.getOnlinePlayers(player.uniqueId).map {
            val head = ItemStack(Material.PLAYER_HEAD)
            val headMeta = head.itemMeta as SkullMeta
            headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(it.uniqueId))
            head.itemMeta = headMeta
            Pair(it, head)
        }

        presenter.setPlayerList(players) {
            playerTeleportRepository.selectedPlayer = it
            routeTo(teleportRequestBlock)
        }
    }
}

