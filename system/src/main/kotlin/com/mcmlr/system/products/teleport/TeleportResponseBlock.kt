package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TeleportResponseBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val teleportRepository: TeleportRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
    teleportConfigRepository: TeleportConfigRepository
): Block(player, origin) {
    private val view = TeleportResponseViewController(player, origin)
    private val interactor = TeleportResponseInteractor(player, view, teleportRepository, playerTeleportRepository, teleportConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TeleportResponseViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin),
    TeleportResponsePresenter {

    private lateinit var content: ViewContainer
    private lateinit var head: ItemView
    private lateinit var name: TextView
    private lateinit var accept: ButtonView
    private lateinit var reject: ButtonView
    private lateinit var messageView: TextView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.TELEPORT_RESPOND_TITLE.resource()),
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
                        text = R.getString(player, S.PLAYER_NAME.resource())
                    )

                    accept = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(-400, 0)
                            .alignTopToBottomOf(name)
                            .margins(top = 50),
                        text = R.getString(player, S.ACCEPT.resource()),
                        highlightedText = R.getString(player, S.ACCEPT.resource()).bolden(),
                    )

                    reject = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(400, 0)
                            .alignTopToBottomOf(name)
                            .margins(top = 50),
                        text = R.getString(player, S.REJECT.resource()),
                        highlightedText = R.getString(player, S.REJECT.resource()).bolden(),
                    )

                    messageView = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(accept)
                            .centerHorizontally()
                            .margins(top = 100),
                        size = 4,
                        text = ""
                    )

                    spin(head)
                }
            }
        )
    }

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }

    override fun setPlayer(playerHead: ItemStack, playerName: String) {
        head.item = playerHead
        name.text = playerName
        updateItemDisplay(head)
        updateTextDisplay(name)
    }

    override fun setAcceptCallback(callback: Listener) {
        accept.addListener(callback)
    }

    override fun setRejectCallback(callback: Listener) {
        reject.addListener(callback)
    }
}

interface TeleportResponsePresenter: Presenter {
    fun setPlayer(playerHead: ItemStack, playerName: String)

    fun setAcceptCallback(callback: Listener)

    fun setRejectCallback(callback: Listener)

    fun setMessage(message: String)
}

class TeleportResponseInteractor(
    private val player: Player,
    private val presenter: TeleportResponsePresenter,
    private val teleportRepository: TeleportRepository,
    private val playerTeleportRepository: PlayerTeleportRepository,
    private val teleportConfigRepository: TeleportConfigRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        val request = playerTeleportRepository.selectedRequest ?: return
        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(request.sender.uniqueId))
        head.itemMeta = headMeta

        presenter.setPlayer(head, request.sender.displayName)

        presenter.setAcceptCallback(object : Listener {
            override fun invoke() {
                val wait = playerTeleportRepository.canTeleport(player) / 1000
                if (wait > 0) {
                    presenter.setMessage(R.getString(player, S.COOLDOWN_ERROR_MESSAGE.resource(), wait, if (wait != 1L) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                teleportRepository.deleteRequest(player.uniqueId, request)

                CoroutineScope(Dispatchers.IO).launch {
                    var delay = teleportConfigRepository.model.delay
                    while (delay > 0) {
                        CoroutineScope(DudeDispatcher()).launch {
                            val passenger = if (request.type == TeleportRequestType.GOTO) request.sender else player
                            val destination = if (request.type == TeleportRequestType.GOTO) player else request.sender
                            val passengerMessage = R.getString(player, S.PASSENGER_MESSAGE.resource(), delay, if (delay != 1) R.getString(player, S.PLURAL.resource()) else "")
                            val destinationMessage = R.getString(player, S.DESTINATION_MESSAGE.resource(), delay, if (delay != 1) R.getString(player, S.PLURAL.resource()) else "")

                            //TODO: Check spigot vs paper
                            passenger.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(passengerMessage))
                            destination.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(destinationMessage))
                        }

                        delay(1.seconds)
                        delay--
                    }

                    CoroutineScope(DudeDispatcher()).launch {
                        if (request.type == TeleportRequestType.GOTO) {
                            request.sender.teleport(player)
                        } else {
                            player.teleport(request.sender)
                        }
                    }
                }

                close()
            }
        })

        presenter.setRejectCallback(object : Listener {
            override fun invoke() {
                teleportRepository.deleteRequest(player.uniqueId, request)
                routeBack()
            }
        })
    }
}
