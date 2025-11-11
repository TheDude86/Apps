package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.products.data.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject

class TeleportRequestBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val teleportRepository: TeleportRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
    private val notificationManager: NotificationManager,
): Block(player, origin) {
    private val view = TeleportRequestViewController(player, origin)
    private val interactor = TeleportRequestInteractor(player, view, teleportRepository, playerTeleportRepository, notificationManager)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TeleportRequestViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin),
    TeleportRequestPresenter {

    private lateinit var content: ViewContainer
    private lateinit var head: ItemView
    private lateinit var name: TextView
    private lateinit var tpa: ButtonView
    private lateinit var tpahere: ButtonView
    private lateinit var statusMessage: TextView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.SEND_REQUEST_TITLE.resource()),
            size = 16,
        )

        content = addViewContainer(
            modifier = Modifier()
                .size(800, 0)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    head = addItemView(
                        modifier = Modifier()
                            .size(280, 280)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 200),
                        item = ItemStack(Material.PLAYER_HEAD)
                    )

                    name = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .centerHorizontally()
                            .alignTopToBottomOf(head)
                            .margins(top = 300),
                        text = R.getString(player, S.PLAYER_NAME.resource()),
                    )

                    tpa = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(-400, 0)
                            .alignTopToBottomOf(name)
                            .margins(top = 50),
                        text = R.getString(player, S.TELEPORT_TO_PLAYER_BUTTON.resource()),
                        highlightedText = R.getString(player, S.TELEPORT_TO_PLAYER_BUTTON.resource()).bolden(),
                    )

                    tpahere = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(400, 0)
                            .alignTopToBottomOf(name)
                            .margins(top = 50),
                        text = R.getString(player, S.TELEPORT_TO_YOU_BUTTON.resource()),
                        highlightedText = R.getString(player, S.TELEPORT_TO_YOU_BUTTON.resource()).bolden(),
                    )

                    statusMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(tpa)
                            .centerHorizontally()
                            .margins(top = 50),
                        text = "",
                        size = 4,
                        visible = false,
                    )

                    spin(head)
                }
            }
        )
    }

    override fun setPlayer(playerHead: ItemStack, playerName: String) {
        head.item = playerHead
        name.text = playerName
        updateItemDisplay(head)
        updateTextDisplay(name)
    }

    override fun setTpaCallback(callback: Listener) {
        tpa.addListener(callback)
    }

    override fun setTpaHereCallback(callback: Listener) {
        tpahere.addListener(callback)
    }

    override fun updateRequestStatus(status: TeleportStatus) {
        statusMessage.visible = true
        statusMessage.text = when (status) {
            TeleportStatus.NEW -> R.getString(player, S.SENT_REQUEST_MESSAGE.resource())
            TeleportStatus.UPDATE -> R.getString(player, S.UPDATE_REQUEST_MESSAGE.resource())
            TeleportStatus.FAILED -> R.getString(player, S.ERROR_REQUEST_MESSAGE.resource())
        }

        updateTextDisplay(statusMessage)
    }
}

interface TeleportRequestPresenter: Presenter {
    fun setPlayer(playerHead: ItemStack, playerName: String)

    fun setTpaCallback(callback: Listener)

    fun setTpaHereCallback(callback: Listener)

    fun updateRequestStatus(status: TeleportStatus)
}

class TeleportRequestInteractor(
    private val player: Player,
    private val presenter: TeleportRequestPresenter,
    private val teleportRepository: TeleportRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
    private val notificationManager: NotificationManager,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        val player = playerTeleportRepository.selectedPlayer ?: return
        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.uniqueId))
        head.itemMeta = headMeta

        presenter.setPlayer(head, player.displayName)

        presenter.setTpaCallback(object : Listener {
            override fun invoke() {
                val status = teleportRepository.sendRequest(this@TeleportRequestInteractor.player, player, TeleportRequestType.GOTO)
                presenter.updateRequestStatus(status)
                if (status != TeleportStatus.FAILED) {
                    notificationManager.sendCTAMessage(
                        player,
                        R.getString(player, S.REQUEST_PLAYER_TELEPORT_CHAT_MESSAGE.resource(), this@TeleportRequestInteractor.player.displayName),
                        R.getString(player, S.REQUEST_TELEPORT_HOVER_MESSAGE.resource()),
                        R.getString(player, S.REQUEST_CHAT_CTA.resource()),
                        "/. teleport://"
                    )
                }
            }
        })

        presenter.setTpaHereCallback(object : Listener {
            override fun invoke() {
                val status = teleportRepository.sendRequest(this@TeleportRequestInteractor.player, player, TeleportRequestType.COME)
                presenter.updateRequestStatus(status)
                if (status != TeleportStatus.FAILED) {
                    notificationManager.sendCTAMessage(
                        player,
                        R.getString(player, S.REQUEST_YOU_TELEPORT_CHAT_MESSAGE.resource(), this@TeleportRequestInteractor.player.displayName),
                        R.getString(player, S.REQUEST_TELEPORT_HOVER_MESSAGE.resource()),
                        R.getString(player, S.REQUEST_CHAT_CTA.resource()),
                        "/. teleport://"
                    )
                }
            }
        })
    }
}
