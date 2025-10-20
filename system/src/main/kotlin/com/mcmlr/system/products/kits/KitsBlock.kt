package com.mcmlr.system.products.kits

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.fromMCItem
import com.mcmlr.system.products.data.*
import kotlinx.coroutines.*
import org.bukkit.*
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class KitsBlock @Inject constructor(
    player: Player,
    origin: Location,
    createKitBlock: CreateKitBlock,
    kitRepository: KitRepository,
    vaultRepository: VaultRepository,
    permissionsRepository: PermissionsRepository,
): Block(player, origin) {
    private val view = KitsViewController(player, origin, permissionsRepository)
    private val interactor = KitsInteractor(player, view, createKitBlock, kitRepository, vaultRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class KitsViewController(
    private val player: Player,
    origin: Location,
    private val permissionsRepository: PermissionsRepository,
): NavigationViewController(player, origin), KitsPresenter {

    private lateinit var kitPurchase: ButtonView
    private lateinit var kitsPager: PagerView
    private lateinit var kitName: TextView
    private lateinit var kitDescription: TextView
    private lateinit var kitPrice: TextView
    private lateinit var kitCooldown: TextView
    private lateinit var errorMessage: TextView
    private lateinit var kitItemList: ListFeedView

    private var kitCreate: ButtonView? = null
    private var kitEdit: ButtonView? = null

    override fun setErrorMessage(message: String) = errorMessage.setTextView(message)

    override fun clearErrorMessage() = errorMessage.setTextView("")

    override fun setPagerListener(listener: (Int) -> Unit) = kitsPager.addPagerListener(listener)

    override fun setName(name: String) = kitName.setTextView("${ChatColor.BOLD}$name")

    override fun setPrice(price: String) = kitPrice.setTextView(price)

    override fun setCooldown(cooldown: String) = kitCooldown.setTextView(cooldown)

    override fun setDescription(description: String) = kitDescription.setTextView(description)

    override fun setKitContents(items: List<KitItem>, commands: List<String>) {
        kitItemList.updateView {
            items.forEach {
//                @Suppress("DEPRECATION") val key = if (checkVersion("1.21.5-R0.1-SNAPSHOT")) {
//                    Material.valueOf(it.material).keyOrNull
//                } else {
//                    Material.valueOf(it.material).key
//                }

                @Suppress("DEPRECATION") val key = Material.valueOf(it.material).key

                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 35),
                    background = Color.fromARGB(0, 0, 0, 0),
                ) {
                    val icon = addItemView(
                        modifier = Modifier()
                            .size(30, 30)
                            .alignStartToStartOf(this)
                            .centerVertically(),
                        item = Bukkit.getItemFactory().createItemStack("$key${it.meta}"),
                    )

                    val name = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(icon)
                            .centerVertically()
                            .margins(start = 10),
                        text = "${it.amount} x ${it.material.fromMCItem()}",
                        size = 4,
                    )
                }
            }

            commands.forEach {
                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 35),
                    background = Color.fromARGB(0, 0, 0, 0),
                ) {
                    val command = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .centerVertically(),
                        text = "/$it",
                        size = 4,
                        alignment = Alignment.LEFT,
                        lineWidth = 150,
                    )
                }
            }
        }
    }

    override fun setKitAdapter(adapter: PagerViewAdapter) {
        kitsPager.attachAdapter(adapter)
    }

    override fun setGetKitListener(listener: Listener) = kitPurchase.addListener(listener)

    override fun setCreateKitListener(listener: Listener) { kitCreate?.addListener(listener) }

    override fun setEditKitListener(listener: Listener) { kitEdit?.addListener(listener) }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Kits",
            size = 16,
        )

        kitsPager = addPagerView(
            modifier = Modifier()
                .size(800, 200)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 50),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        val kitContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(kitsPager)
                .alignBottomToBottomOf(this)
                .margins(top = -250, bottom = 550),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
            kitName = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitsPager)
                    .centerHorizontally(),
                text = "${ChatColor.BOLD}Kit Name",
                teleportDuration = 0,
            )

            kitPrice = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToBottomOf(kitName)
                    .margins(start = 50, top = 50),
                text = "Kit Price",
                teleportDuration = 0,
            )

            kitCooldown = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitPrice)
                    .alignStartToStartOf(kitPrice)
                    .margins(top = 20),
                text = "Kit Cooldown",
                size = 6,
                lineWidth = 150,
                alignment = Alignment.LEFT,
                teleportDuration = 0,
            )

            kitDescription = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitCooldown)
                    .alignStartToStartOf(kitCooldown)
                    .margins(top = 20),
                text = "${ChatColor.GRAY}Kit Description",
                teleportDuration = 0,
                alignment = Alignment.LEFT,
                size = 6,
            )

            val kitListTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitName)
                    .alignEndToEndOf(this)
                    .margins(top = 50, end = 200),
                text = "${ChatColor.BOLD}Kit Contents",
                size = 6,
            )

            kitItemList = addListFeedView(
                modifier = Modifier()
                    .size(300, 250)
                    .alignTopToBottomOf(kitListTitle)
                    .alignStartToStartOf(kitListTitle)
                    .margins(top = 20),
            )
        }


        kitPurchase = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(kitContainer)
                .centerHorizontally()
                .margins(top = 20),
            text = "${ChatColor.GOLD}Get Kit",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Get Kit",
        )

        errorMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(kitPurchase)
                .centerHorizontally(),
            text = "",
            size = 4,
        )

        if (permissionsRepository.checkPermission(player, PermissionNode.ADMIN)) {
            kitCreate = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(errorMessage)
                    .alignEndToStartOf(kitPurchase)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Create Kit",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create Kit",
            )

            kitEdit = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(errorMessage)
                    .alignStartToEndOf(kitPurchase)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Edit Kit",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Edit Kit",
            )
        }
    }

}

interface KitsPresenter: Presenter {
    fun setGetKitListener(listener: Listener)

    fun setCreateKitListener(listener: Listener)

    fun setEditKitListener(listener: Listener)

    fun setKitAdapter(adapter: PagerViewAdapter)

    fun setName(name: String)

    fun setPrice(price: String)

    fun setCooldown(cooldown: String)

    fun setDescription(description: String)

    fun setKitContents(items: List<KitItem>, commands: List<String>)

    fun setPagerListener(listener: (Int) -> Unit)

    fun setErrorMessage(message: String)

    fun clearErrorMessage()
}

class KitsPagerAdapter(private val kitRepository: KitRepository): PagerViewAdapter() {
    override fun getCount(): Int = kitRepository.getKits().size

    override fun renderElement(selected: Boolean, index: Int, parent: ViewContainer) {
        parent.updateView {
            addItemView(
                modifier = Modifier()
                    .size(150, 150)
                    .center(),
                item = Material.valueOf(kitRepository.getKits()[index].icon)
            )
        }
    }

}

class KitsInteractor(
    private val player: Player,
    private val presenter: KitsPresenter,
    private val createKitBlock: CreateKitBlock,
    private val kitRepository: KitRepository,
    private val vaultRepository: VaultRepository,
): Interactor(presenter) {

    private var selectedKit = kitRepository.getKits().firstOrNull()
    private var clockJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        presenter.setCreateKitListener(object : Listener {
            override fun invoke() {
                createKitBlock.setSelectedKit(null)
                routeTo(createKitBlock)
            }
        })

        selectedKit = kitRepository.getKits().firstOrNull()
        val selectedKit = selectedKit ?: return

        presenter.setKitAdapter(KitsPagerAdapter(kitRepository))
        updateSelectedKit()

        presenter.setGetKitListener(object : Listener {
            override fun invoke() {
                kitRepository.getCooldown(player, selectedKit)
                    .collectFirst(DudeDispatcher()) {
                        val cooldown = it ?: 0
                        val balance = vaultRepository.economy?.getBalance(player) ?: 0.0

                        if (cooldown > 0) {
                            presenter.setErrorMessage("${ChatColor.RED}You need to wait for the cooldown to finish before collecting this kit again!")
                        } else if (balance < selectedKit.kitPrice / 100.0) {
                            presenter.setErrorMessage("${ChatColor.RED}You don't have enough money to buy this kit!")
                        } else {
                            kitRepository.givePlayerKit(player, selectedKit)
                            updateSelectedKit()
                        }
                    }
            }
        })

        presenter.setPagerListener {
            this.selectedKit = kitRepository.getKits()[it]
            updateSelectedKit()
        }

        presenter.setEditKitListener(object : Listener {
            override fun invoke() {
                createKitBlock.setSelectedKit(selectedKit)
                routeTo(createKitBlock)
            }
        })
    }

    private fun updateSelectedKit() {
        val selectedKit = selectedKit ?: return
        clockJob?.cancel()
        presenter.clearErrorMessage()
        presenter.setName(selectedKit.name)
        presenter.setPrice("$${"%.2f".format(selectedKit.kitPrice / 100f)}")
        presenter.setDescription(selectedKit.description)
        presenter.setKitContents(selectedKit.items, selectedKit.commands)

        kitRepository.getCooldown(player, selectedKit)
            .collectFirst(DudeDispatcher()) { cooldown ->
                if (selectedKit.kitCooldown < 0) {
                    if (cooldown != null) {
                        presenter.setCooldown("Single Use")
                    } else {
                        presenter.setCooldown("${ChatColor.GRAY}Kit Claimed")
                    }
                } else {
                    if ((cooldown ?: 0) > 0) {
                        val cooldownSeconds = (cooldown ?: 0) / 1000
                        startClockJob(cooldownSeconds)
                    } else {
                        presenter.setCooldown("${ChatColor.GREEN}Available now")
                    }
                }
            }
    }

    private fun startClockJob(time: Long) {
        clockJob = CoroutineScope(Dispatchers.IO).launch {
            var countdown = time
            while (true) {
                var timeRemainder = countdown
                val days = timeRemainder / (24 * 60 * 60)

                timeRemainder %= (24 * 60 * 60)
                val hours = timeRemainder / (60 * 60)

                timeRemainder %= (60 * 60)
                val minutes = timeRemainder / 60

                timeRemainder %= 60
                val seconds = timeRemainder

                val clockTextBuilder = StringBuilder("${ChatColor.GRAY}Available in ")
                if (days > 0) clockTextBuilder.append("$days days ")
                if (hours > 0 || days > 0) clockTextBuilder.append("$hours hours ")
                if (minutes > 0 || days > 0 || hours > 0) clockTextBuilder.append("$minutes minutes ")
                clockTextBuilder.append("$seconds seconds.")

                CoroutineScope(DudeDispatcher()).launch {
                    presenter.setCooldown(clockTextBuilder.toString())
                }

                countdown--
                delay(1.seconds)
            }
        }
    }
}
