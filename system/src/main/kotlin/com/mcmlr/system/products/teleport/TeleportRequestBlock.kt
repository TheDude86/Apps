package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
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

class TeleportRequestViewController(player: Player, origin: Location): NavigationViewController(player, origin),
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Send Request",
            size = 16,
        )

        content = addViewContainer(
            modifier = Modifier()
                .size(800, 0)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
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
                text = "Player name"
            )

            tpa = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(-400, 0)
                    .alignTopToBottomOf(name)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Teleport\nto them",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Teleport\nto them",
            )

            tpahere = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(400, 0)
                    .alignTopToBottomOf(name)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Teleport\nto you",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Teleport\nto you",
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

    override fun setPlayer(playerHead: ItemStack, playerName: String) {
        head.item = playerHead
        name.text = playerName
        updateItemDisplay(head)
        updateTextDisplay(name)
    }

    override fun setTpaCallback(callback: () -> Unit) {
        tpa.addListener(callback)
    }

    override fun setTpaHereCallback(callback: () -> Unit) {
        tpahere.addListener(callback)
    }

    override fun updateRequestStatus(status: TeleportStatus) {
        statusMessage.visible = true
        statusMessage.text = when (status) {
            TeleportStatus.NEW -> "${ChatColor.GREEN}Teleport request sent!"
            TeleportStatus.UPDATE -> "${ChatColor.GREEN}Teleport request updated!"
            TeleportStatus.FAILED -> "${ChatColor.RED}You've already sent this player a request!"
        }

        updateTextDisplay(statusMessage)
    }
}

interface TeleportRequestPresenter: Presenter {
    fun setPlayer(playerHead: ItemStack, playerName: String)

    fun setTpaCallback(callback: () -> Unit)

    fun setTpaHereCallback(callback: () -> Unit)

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

        presenter.setTpaCallback {
            val status = teleportRepository.sendRequest(this.player, player, TeleportRequestType.GOTO)
            presenter.updateRequestStatus(status)
            if (status != TeleportStatus.FAILED) notificationManager.sendCTAMessage(player, "${ChatColor.GRAY}${ChatColor.ITALIC}${this.player.displayName} requested to teleport to you", "Open the teleport menu", "Click to respond", "/. teleport://")
        }

        presenter.setTpaHereCallback {
            val status = teleportRepository.sendRequest(this.player, player, TeleportRequestType.COME)
            presenter.updateRequestStatus(status)
            if (status != TeleportStatus.FAILED) notificationManager.sendCTAMessage(player, "${ChatColor.GRAY}${ChatColor.ITALIC}${this.player.displayName} requested you to teleport to them", "Open the teleport menu", "Click to respond", "/. teleport://")
        }
    }
}
