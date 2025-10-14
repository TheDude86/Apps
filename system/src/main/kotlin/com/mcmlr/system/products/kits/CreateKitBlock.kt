package com.mcmlr.system.products.kits

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationPresenter
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.fromMCItem
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class CreateKitBlock @Inject constructor(
    player: Player,
    camera: Camera,
    iconSelectionBlock: IconSelectionBlock,
    addKitContentBlock: AddKitContentBlock,
    private val kitRepository: KitRepository,
): Block(player, camera) {
    private val view = CreateKitViewController(player, camera)
    private val interactor = CreateKitInteractor(view, iconSelectionBlock, addKitContentBlock, kitRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setSelectedKit(kit: KitModel?) {
        if (kit != null) {
            kitRepository.builder.uuid = kit.uuid
            kitRepository.builder.icon = kit.icon
            kitRepository.builder.name = kit.name
            kitRepository.builder.description = kit.description
            kitRepository.builder.kitPrice = kit.kitPrice
            kitRepository.builder.kitCooldown = kit.kitCooldown
            kitRepository.builder.items = kit.items.toMutableList()
            kitRepository.builder.commands = kit.commands.toMutableList()
        } else {
            kitRepository.builder.reset()
        }
    }
}

class CreateKitViewController(
    private val player: Player,
    camera: Camera,
): NavigationViewController(player, camera), CreateKitPresenter {

    private lateinit var iconContainer: ViewContainer
    private lateinit var kitName: TextInputView
    private lateinit var kitPrice: TextInputView
    private lateinit var kitCooldown: TextInputView
    private lateinit var errorMessage: TextView
    private lateinit var kitContents: ListFeedView
    private lateinit var kitDescription: TextInputView
    private lateinit var addItemButton: ButtonView
    private lateinit var addCommandButton: ButtonView
    private lateinit var ctaContainer: ViewContainer

    private var createKitButton: ButtonView? = null
    private var editKitButton: ButtonView? = null
    private var deleteKitButton: ButtonView? = null

    private var iconListener: () -> Unit = {}
    private var itemListener: (KitItem) -> Unit = {}
    private var commandListener: (String) -> Unit = {}

    override fun setIsEditing(isEditing: Boolean) {
        ctaContainer.updateView {
            if (isEditing) {
                editKitButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .alignStartToStartOf(this),
                    text = "${ChatColor.GOLD}Update Kit",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Update Kit",
                )

                deleteKitButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .alignEndToEndOf(this),
                    text = "${ChatColor.RED}Delete Kit",
                    highlightedText = "${ChatColor.RED}${ChatColor.BOLD}Delete Kit",
                )
            } else {
                createKitButton = addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToTopOf(this)
                        .centerHorizontally(),
                    text = "${ChatColor.GOLD}Create Kit",
                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create Kit",
                )
            }
        }
    }

    override fun setCreateKitListener(listener: () -> Unit) {
        createKitButton?.addListener(listener)
    }

    override fun setUpdateKitListener(listener: () -> Unit) {
        editKitButton?.addListener(listener)
    }

    override fun setDeleteKitListener(listener: () -> Unit) {
        deleteKitButton?.addListener(listener)
    }

    override fun setErrorMessage(message: String) = errorMessage.setTextView(message)

    override fun hideErrorMessage() = errorMessage.setTextView("")

    override fun setKitCallbacks(removeItemCallback: (KitItem) -> Unit, removeCommandCallback: (String) -> Unit) {
        itemListener = removeItemCallback
        commandListener = removeCommandCallback
    }

    override fun setKitContents(items: List<KitItem>, commands: List<String>) {

        kitContents.updateView {

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

                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(name)
                            .centerVertically()
                            .margins(start = 10),
                        text = "✖",
                        highlightedText = "${ChatColor.RED}✖",
                        size = 4,
                    ) {
                        itemListener.invoke(it)
                    }
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

                    addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(command)
                            .centerVertically()
                            .margins(start = 10),
                        text = "✖",
                        highlightedText = "${ChatColor.RED}✖",
                        size = 4,
                    ) {
                        commandListener.invoke(it)
                    }
                }
            }
        }

    }

    override fun setAddCommandListener(listener: () -> Unit) = addCommandButton.addListener(listener)

    override fun setAddItemListener(listener: () -> Unit) = addItemButton.addListener(listener)

    override fun setDescriptionListener(listener: (String) -> Unit) = kitDescription.addTextChangedListener(listener)

    override fun setDescription(description: String) {
        kitDescription.updateText(description)
    }

    override fun setCooldownListener(listener: (String) -> Unit) = kitCooldown.addTextChangedListener(listener)

    override fun setCooldown(cooldown: String) {
        kitCooldown.updateText(cooldown)
    }

    override fun setPriceListener(listener: (String) -> Unit) = kitPrice.addTextChangedListener(listener)

    override fun setPrice(price: String) {
        kitPrice.updateText(price)
    }

    override fun setIconListener(listener: () -> Unit) {
        iconListener = listener
    }

    override fun setIcon(icon: Material?) {
        iconContainer.updateView {
            if (icon == null) {
                addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .center(),
                    text = "${ChatColor.GRAY}${ChatColor.BOLD}Set Kit\nIcon",
                    size = 7,
                )
            } else {
                addItemView(
                    modifier = Modifier()
                        .size(100, 100)
                        .center(),
                    item = icon
                )
            }
        }
    }

    override fun setNameListener(listener: (String) -> Unit) = kitName.addTextChangedListener(listener)

    override fun setName(name: String) {
        kitName.updateText(name)
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Create a Kit",
            size = 16,
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(200, 200)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 50),
            clickable = true,
            listener = {
                iconListener.invoke()
            }
        ) {
            addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .center(),
                text = "${ChatColor.GRAY}${ChatColor.BOLD}Set Kit\nIcon",
                size = 7,
            )
        }

        val addKitContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(iconContainer)
                .alignBottomToBottomOf(this)
                .margins(top = 75, bottom = 400),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
            kitName = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally(),
                text = "Set Kit Name",
                teleportDuration = 0,
            )

            kitPrice = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(this)
                    .alignTopToBottomOf(kitName)
                    .margins(top = 50, start = 50),
                text = "Set Kit Price",
                teleportDuration = 0,
            )

            kitCooldown = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitPrice)
                    .alignStartToStartOf(kitPrice)
                    .margins(top = 40),
                text = "Set Kit Cooldown",
                size = 6,
                teleportDuration = 0,
            )

            val cooldownSubtitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitCooldown)
                    .alignStartToStartOf(kitCooldown),
                text = "${ChatColor.GRAY}Setting the cooldown to a negative number makes the kit single use.",
                teleportDuration = 0,
                lineWidth = 400,
                size = 3,
            )

            kitDescription = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(cooldownSubtitle)
                    .alignStartToStartOf(cooldownSubtitle)
                    .margins(top = 40),
                text = "Set Kit Description",
                teleportDuration = 0,
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

            kitContents = addListFeedView(
                modifier = Modifier()
                    .size(300, 250)
                    .alignTopToBottomOf(kitListTitle)
                    .alignStartToStartOf(kitListTitle)
                    .margins(top = 20),
            )

            addItemButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitContents)
                    .alignStartToStartOf(kitContents),
                text = "${ChatColor.GOLD}+ Add Item",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}+ Add Item",
                size = 5
            )

            addCommandButton = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(kitContents)
                    .alignStartToEndOf(addItemButton)
                    .margins(start = 50),
                text = "${ChatColor.GOLD}+ Add Command",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}+ Add Command",
                size = 5,
            )
        }

        ctaContainer = addViewContainer(
            modifier = Modifier()
                .size(450, 50)
                .alignTopToBottomOf(addKitContainer)
                .centerHorizontally(),
            background = Color.fromARGB(0, 0, 0, 0)
        )

        errorMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(ctaContainer)
                .centerHorizontally()
                .margins(top = 50),
            text = "",
            size = 6,
        )
    }
}

interface CreateKitPresenter: NavigationPresenter {
    fun setIconListener(listener: () -> Unit)

    fun setIcon(icon: Material?)

    fun setNameListener(listener: (String) -> Unit)

    fun setName(name: String)

    fun setPriceListener(listener: (String) -> Unit)

    fun setPrice(price: String)

    fun setCooldownListener(listener: (String) -> Unit)

    fun setCooldown(cooldown: String)

    fun setDescriptionListener(listener: (String) -> Unit)

    fun setDescription(description: String)

    fun setAddItemListener(listener: () -> Unit)

    fun setAddCommandListener(listener: () -> Unit)

    fun setKitContents(items: List<KitItem>, commands: List<String>)

    fun setKitCallbacks(removeItemCallback: (KitItem) -> Unit, removeCommandCallback: (String) -> Unit)

    fun setCreateKitListener(listener: () -> Unit)

    fun setUpdateKitListener(listener: () -> Unit)

    fun setDeleteKitListener(listener: () -> Unit)

    fun setErrorMessage(message: String)

    fun hideErrorMessage()

    fun setIsEditing(isEditing: Boolean)
}

class CreateKitInteractor(
    private val presenter: CreateKitPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val addKitContentBlock: AddKitContentBlock,
    private val kitRepository: KitRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.setIsEditing(kitRepository.builder.uuid != null)

        presenter.setCreateKitListener {
            val validCheckMessage = kitRepository.builder.checkValid()
            if (validCheckMessage != null) {
                presenter.setErrorMessage("${ChatColor.RED}$validCheckMessage")
                return@setCreateKitListener
            }

            val kit = kitRepository.builder.build() ?: return@setCreateKitListener
            kitRepository.addKit(kit)
            routeBack()
        }

        presenter.setUpdateKitListener {
            val validCheckMessage = kitRepository.builder.checkValid()
            if (validCheckMessage != null) {
                presenter.setErrorMessage("${ChatColor.RED}$validCheckMessage")
                return@setUpdateKitListener
            }

            val kit = kitRepository.builder.build() ?: return@setUpdateKitListener
            kitRepository.updateKit(kit)
            routeBack()
        }

        presenter.setDeleteKitListener {
            kitRepository.deleteKit()
            routeBack()
        }

        presenter.addBackListener {
            kitRepository.builder.reset()
        }

        presenter.setAddItemListener {
            addKitContentBlock.addItemContent()
            routeTo(addKitContentBlock)
        }

        presenter.setAddCommandListener {
            addKitContentBlock.addCommandContent()
            routeTo(addKitContentBlock)
        }

        if (kitRepository.builder.items.isNotEmpty() || kitRepository.builder.commands.isNotEmpty()) {
            presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
        }

        presenter.setKitCallbacks({
            kitRepository.builder.items.remove(it)
            presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
        }) {
            kitRepository.builder.commands.remove(it)
            presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
        }

        kitRepository.builder.icon?.let {
            presenter.setIcon(Material.valueOf(it))
        }

        kitRepository.builder.name?.let {
            presenter.setName(it)
        }

        presenter.setNameListener {
            kitRepository.builder.name = it
        }

        kitRepository.builder.kitPrice?.let {
            presenter.setPrice("$${"%.2f".format(it / 100f)}")
        }

        presenter.setPriceListener {
            val price = it.toDoubleOrNull()
            if (price == null) {
                presenter.setPrice("Set Kit Price")
                kitRepository.builder.kitPrice = null
                return@setPriceListener
            }

            val convertedPrice = ((price * 100) + 0.5).toInt()
            presenter.setPrice("$${"%.2f".format(convertedPrice / 100f)}")
            kitRepository.builder.kitPrice = convertedPrice
        }

        kitRepository.builder.kitCooldown?.let {
            if (it < 0) {
                presenter.setCooldown("Single Use")
            } else {
                presenter.setCooldown("$it Seconds")
            }
        }

        presenter.setCooldownListener {
            val cooldown = it.toIntOrNull()
            if (cooldown == null) {
                presenter.setCooldown("Set Kit Cooldown")
                kitRepository.builder.kitCooldown = null
                return@setCooldownListener
            }

            if (cooldown < 0) {
                presenter.setCooldown("Single Use")
            } else {
                presenter.setCooldown("$cooldown Seconds")
            }

            kitRepository.builder.kitCooldown = cooldown
        }

        kitRepository.builder.description?.let {
            presenter.setDescription(it)
        }

        presenter.setDescriptionListener {
            kitRepository.builder.description = it
        }

        presenter.setIconListener {
            iconSelectionBlock.resetInventory()
            routeTo(iconSelectionBlock) { bundle ->
                val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                kitRepository.builder.icon = icon?.type?.name
                presenter.setIcon(icon?.type)
            }
        }
    }
}
