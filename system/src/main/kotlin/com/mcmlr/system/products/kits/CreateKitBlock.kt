package com.mcmlr.system.products.kits

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.EmptyListener
import com.mcmlr.blocks.api.block.EmptyTextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationPresenter
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.TextListener
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
    origin: Location,
    iconSelectionBlock: IconSelectionBlock,
    addKitContentBlock: AddKitContentBlock,
    private val kitRepository: KitRepository,
): Block(player, origin) {
    private val view = CreateKitViewController(player, origin)
    private val interactor = CreateKitInteractor(player, view, iconSelectionBlock, addKitContentBlock, kitRepository)

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
    origin: Location,
): NavigationViewController(player, origin), CreateKitPresenter {

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

    private var iconListener: Listener = EmptyListener()
    private var itemListener: (KitItem) -> Unit = {}
    private var commandListener: TextListener = EmptyTextListener()

    override fun setIsEditing(isEditing: Boolean) {
        ctaContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (isEditing) {
                    editKitButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .alignStartToStartOf(this),
                        text = "${ChatColor.GOLD}${R.getString(player, S.UPDATE_KIT.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.UPDATE_KIT.resource())}",
                    )

                    deleteKitButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .alignEndToEndOf(this),
                        text = "${ChatColor.RED}${R.getString(player, S.DELETE_KIT.resource())}",
                        highlightedText = "${ChatColor.RED}${ChatColor.BOLD}${R.getString(player, S.DELETE_KIT.resource())}",
                    )
                } else {
                    createKitButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally(),
                        text = "${ChatColor.GOLD}${R.getString(player, S.CREATE_KIT.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.CREATE_KIT.resource())}",
                    )
                }
            }
        })
    }

    override fun setCreateKitListener(listener: Listener) {
        createKitButton?.addListener(listener)
    }

    override fun setUpdateKitListener(listener: Listener) {
        editKitButton?.addListener(listener)
    }

    override fun setDeleteKitListener(listener: Listener) {
        deleteKitButton?.addListener(listener)
    }

    override fun setErrorMessage(message: String) = errorMessage.update(text = message)

    override fun hideErrorMessage() = errorMessage.update(text = "")

    override fun setKitCallbacks(removeItemCallback: (KitItem) -> Unit, removeCommandCallback: TextListener) {
        itemListener = removeItemCallback
        commandListener = removeCommandCallback
    }

    override fun setKitContents(items: List<KitItem>, commands: List<String>) {

        kitContents.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {            items.forEach {
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
                    content = object : ContextListener<ViewContainer>() {
                        override fun ViewContainer.invoke() {
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
                                text = R.getString(player, S.AVAILABLE_NOW.resource(), it.amount, it.material.fromMCItem()),
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
                                callback = object : Listener {
                                    override fun invoke() {
                                        itemListener.invoke(it)
                                    }
                                }
                            )
                        }
                    }
                )
            }

                commands.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 35),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
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
                                    callback = object : Listener {
                                        override fun invoke() {
                                            commandListener.invoke(it)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        })

    }

    override fun setAddCommandListener(listener: Listener) = addCommandButton.addListener(listener)

    override fun setAddItemListener(listener: Listener) = addItemButton.addListener(listener)

    override fun setDescriptionListener(listener: TextListener) = kitDescription.addTextChangedListener(listener)

    override fun setDescription(description: String) {
        kitDescription.updateInputText(description)
    }

    override fun setCooldownListener(listener: TextListener) = kitCooldown.addTextChangedListener(listener)

    override fun setCooldown(cooldown: String) {
        kitCooldown.updateInputText(cooldown)
    }

    override fun setPriceListener(listener: TextListener) = kitPrice.addTextChangedListener(listener)

    override fun setPrice(price: String) {
        kitPrice.updateInputText(price)
    }

    override fun setIconListener(listener: Listener) {
        iconListener = listener
    }

    override fun setIcon(icon: Material?) {
        iconContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (icon == null) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        text = R.getString(player, S.SET_KIT_ICON.resource()),
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
        })
    }

    override fun setNameListener(listener: TextListener) = kitName.addTextChangedListener(listener)

    override fun setName(name: String) {
        kitName.updateInputText(name)
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.CREATE_KIT_TITLE.resource()),
            size = 16,
        )

        iconContainer = addViewContainer(
            modifier = Modifier()
                .size(200, 200)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 50),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    iconListener.invoke()
                }
            },
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        text = R.getString(player, S.SET_KIT_ICON.resource()),
                        size = 7,
                    )
                }
            }
        )

        val addKitContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(iconContainer)
                .alignBottomToBottomOf(this)
                .margins(top = 75, bottom = 400),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    kitName = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .centerHorizontally(),
                        text = R.getString(player, S.SET_KIT_NAME.resource()),
                        teleportDuration = 0,
                    )

                    kitPrice = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToStartOf(this)
                            .alignTopToBottomOf(kitName)
                            .margins(top = 50, start = 50),
                        text = R.getString(player, S.SET_KIT_PRICE.resource()),
                        teleportDuration = 0,
                    )

                    kitCooldown = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(kitPrice)
                            .alignStartToStartOf(kitPrice)
                            .margins(top = 40),
                        text = R.getString(player, S.SET_KIT_COOLDOWN.resource()),
                        size = 6,
                        teleportDuration = 0,
                    )

                    val cooldownSubtitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(kitCooldown)
                            .alignStartToStartOf(kitCooldown),
                        text = R.getString(player, S.SET_KIT_COOLDOWN_SUBTITLE.resource()),
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
                        text = R.getString(player, S.SET_KIT_DESCRIPTION.resource()),
                        teleportDuration = 0,
                        size = 6,
                    )

                    val kitListTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(kitName)
                            .alignEndToEndOf(this)
                            .margins(top = 50, end = 200),
                        text = R.getString(player, S.KIT_CONTENTS_TITLE.resource()),
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
                        text = "${ChatColor.GOLD}${R.getString(player, S.ADD_ITEM_BUTTON.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.ADD_ITEM_BUTTON.resource())}",
                        size = 5
                    )

                    addCommandButton = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(kitContents)
                            .alignStartToEndOf(addItemButton)
                            .margins(start = 50),
                        text = "${ChatColor.GOLD}${R.getString(player, S.ADD_COMMAND_BUTTON.resource())}",
                        highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.ADD_COMMAND_BUTTON.resource())}",
                        size = 5,
                    )
                }
            }
        )

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
    fun setIconListener(listener: Listener)

    fun setIcon(icon: Material?)

    fun setNameListener(listener: TextListener)

    fun setName(name: String)

    fun setPriceListener(listener: TextListener)

    fun setPrice(price: String)

    fun setCooldownListener(listener: TextListener)

    fun setCooldown(cooldown: String)

    fun setDescriptionListener(listener: TextListener)

    fun setDescription(description: String)

    fun setAddItemListener(listener: Listener)

    fun setAddCommandListener(listener: Listener)

    fun setKitContents(items: List<KitItem>, commands: List<String>)

    fun setKitCallbacks(removeItemCallback: (KitItem) -> Unit, removeCommandCallback: TextListener)

    fun setCreateKitListener(listener: Listener)

    fun setUpdateKitListener(listener: Listener)

    fun setDeleteKitListener(listener: Listener)

    fun setErrorMessage(message: String)

    fun hideErrorMessage()

    fun setIsEditing(isEditing: Boolean)
}

class CreateKitInteractor(
    private val player: Player,
    private val presenter: CreateKitPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val addKitContentBlock: AddKitContentBlock,
    private val kitRepository: KitRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.setIsEditing(kitRepository.builder.uuid != null)

        presenter.setCreateKitListener(object : Listener {
            override fun invoke() {
                val validCheckMessage = kitRepository.builder.checkValid()
                if (validCheckMessage != null) {
                    presenter.setErrorMessage("${ChatColor.RED}$validCheckMessage")
                    return
                }

                val kit = kitRepository.builder.build() ?: return
                kitRepository.addKit(kit)
                routeBack()
            }
        })

        presenter.setUpdateKitListener(object : Listener {
            override fun invoke() {
                val validCheckMessage = kitRepository.builder.checkValid()
                if (validCheckMessage != null) {
                    presenter.setErrorMessage("${ChatColor.RED}$validCheckMessage")
                    return
                }

                val kit = kitRepository.builder.build() ?: return
                kitRepository.updateKit(kit)
                routeBack()
            }
        })

        presenter.setDeleteKitListener(object : Listener {
            override fun invoke() {
                kitRepository.deleteKit()
                routeBack()
            }
        })

        presenter.addBackListener(object : Listener {
            override fun invoke() {
                kitRepository.builder.reset()
            }
        })

        presenter.setAddItemListener(object : Listener {
            override fun invoke() {
                addKitContentBlock.addItemContent()
                routeTo(addKitContentBlock)
            }
        })

        presenter.setAddCommandListener(object : Listener {
            override fun invoke() {
                addKitContentBlock.addCommandContent()
                routeTo(addKitContentBlock)
            }
        })

        if (kitRepository.builder.items.isNotEmpty() || kitRepository.builder.commands.isNotEmpty()) {
            presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
        }

        presenter.setKitCallbacks({
            kitRepository.builder.items.remove(it)
            presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
        }, object : TextListener {
            override fun invoke(text: String) {
                kitRepository.builder.commands.remove(text)
                presenter.setKitContents(kitRepository.builder.items, kitRepository.builder.commands)
            }
        })

        kitRepository.builder.icon?.let {
            presenter.setIcon(Material.valueOf(it))
        }

        kitRepository.builder.name?.let {
            presenter.setName(it)
        }

        presenter.setNameListener(object : TextListener {
            override fun invoke(text: String) {
                kitRepository.builder.name = text
            }
        })

        kitRepository.builder.kitPrice?.let {
            presenter.setPrice("$${"%.2f".format(it / 100f)}")
        }

        presenter.setPriceListener(object : TextListener {
            override fun invoke(text: String) {
                val price = text.toDoubleOrNull()
                if (price == null) {
                    presenter.setPrice(R.getString(player, S.SET_KIT_PRICE.resource()))
                    kitRepository.builder.kitPrice = null
                    return
                }

                val convertedPrice = ((price * 100) + 0.5).toInt()
                presenter.setPrice("$${"%.2f".format(convertedPrice / 100f)}")
                kitRepository.builder.kitPrice = convertedPrice
            }
        })

        kitRepository.builder.kitCooldown?.let {
            if (it < 0) {
                presenter.setCooldown(R.getString(player, S.SINGLE_USE.resource()))
            } else {
                presenter.setCooldown(R.getString(player, S.SECONDS_INPUT.resource(), it))
            }
        }

        presenter.setCooldownListener(object : TextListener {
            override fun invoke(text: String) {
                val cooldown = text.toIntOrNull()
                if (cooldown == null) {
                    presenter.setCooldown(R.getString(player, S.SET_KIT_COOLDOWN.resource()))
                    kitRepository.builder.kitCooldown = null
                    return
                }

                if (cooldown < 0) {
                    presenter.setCooldown(R.getString(player, S.SINGLE_USE.resource()))
                } else {
                    presenter.setCooldown(R.getString(player, S.SECONDS_INPUT.resource(), cooldown))
                }

                kitRepository.builder.kitCooldown = cooldown
            }
        })

        kitRepository.builder.description?.let {
            presenter.setDescription(it)
        }

        presenter.setDescriptionListener(object : TextListener {
            override fun invoke(text: String) {
                kitRepository.builder.description = text
            }
        })

        presenter.setIconListener(object : Listener {
            override fun invoke() {
                iconSelectionBlock.resetInventory()
                routeTo(iconSelectionBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val icon = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY)
                        kitRepository.builder.icon = icon?.type?.name
                        presenter.setIcon(icon?.type)
                    }
                })
            }
        })
    }
}
