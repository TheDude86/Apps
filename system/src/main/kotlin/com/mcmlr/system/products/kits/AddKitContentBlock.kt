package com.mcmlr.system.products.kits

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.EmptyListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationPresenter
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class AddKitContentBlock @Inject constructor(
    player: Player,
    origin: Location,
    iconSelectionBlock: IconSelectionBlock,
    kitRepository: KitRepository,
): Block(player, origin) {
    private val view = AddKitContentViewController(player, origin, true)
    private val interactor = AddKitContentInteractor(true, player, view, iconSelectionBlock, kitRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun addItemContent() {
        view.showItem = true
        interactor.showItem = true
    }

    fun addCommandContent() {
        view.showItem = false
        interactor.showItem = false
    }
}

class AddKitContentViewController(
    player: Player,
    origin: Location,
    var showItem: Boolean,
): NavigationViewController(player, origin), AddKitContentPresenter {

    private lateinit var itemContainer: ViewContainer
    private lateinit var addContentButton: ButtonView
    private lateinit var errorMessage: TextView

    private var quantityTextInput: TextInputView? = null
    private var commandTextInput: TextInputView? = null
    private var iconListener: Listener = EmptyListener()

    override fun setCommandListener(listener: TextListener) {
        commandTextInput?.addTextChangedListener(listener)
    }

    override fun setCommand(quantity: String) {
        commandTextInput?.updateInputText(quantity)
    }

    override fun showErrorMessage(message: String) = errorMessage.updateText(message)

    override fun hideErrorMessage() = errorMessage.updateText("")

    override fun setAddContentListener(listener: Listener) = addContentButton.addListener(listener)

    override fun setQuantityListener(listener: TextListener) {
        quantityTextInput?.addTextChangedListener(listener)
    }

    override fun setQuantity(quantity: String) {
        quantityTextInput?.updateInputText(quantity)
    }

    override fun setIconListener(listener: Listener) {
        iconListener = listener
    }

    override fun setIcon(icon: ItemStack?) {
        itemContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (icon == null) {
                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        text = "${ChatColor.GRAY}${ChatColor.BOLD}Select\nItem",
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

    override fun createView() {
        super.createView()
        val titleText = if (showItem) "Add Kit Item" else "Add Kit Command"

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}$titleText",
            size = 16,
        )

        val content: ViewContainer

        if (showItem) {
            content = addViewContainer(
                modifier = Modifier()
                    .size(1000, 400)
                    .alignTopToBottomOf(title)
                    .centerHorizontally()
                    .margins(top = 200),
                background = Color.fromARGB(0, 0, 0, 0),
                content = object : ContextListener<ViewContainer>() {
                    override fun ViewContainer.invoke() {
                        itemContainer = addViewContainer(
                            modifier = Modifier()
                                .size(200, 200)
                                .alignTopToTopOf(this),
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
                                        text = "${ChatColor.GRAY}${ChatColor.BOLD}Select\nItem",
                                        size = 7,
                                    )
                                }
                            }
                        )

                        quantityTextInput = addTextInputView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .alignTopToBottomOf(itemContainer)
                                .centerHorizontally()
                                .margins(top = 50),
                            text = "Set quantity",
                        )
                    }
                }
            )
        } else {
            content = addViewContainer(
                modifier = Modifier()
                    .size(1000, 200)
                    .alignTopToBottomOf(title)
                    .centerHorizontally()
                    .margins(top = 300),
                background = Color.fromARGB(0, 0, 0, 0),
                content = object : ContextListener<ViewContainer>() {
                    override fun ViewContainer.invoke() {
                        commandTextInput = addTextInputView(
                            modifier = Modifier()
                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                .center()
                                .margins(top = 50),
                            text = "Set command",
                        )
                    }
                }
            )
        }

        val addButtonText = if (showItem) "Add Item" else "Add Command"
        addContentButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(content)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.GOLD}$addButtonText",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$addButtonText",
        )

        errorMessage = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(addContentButton)
                .centerHorizontally()
                .margins(top = 50),
            text = "",
            size = 6,
            alignment = Alignment.LEFT,
        )
    }

}

interface AddKitContentPresenter: NavigationPresenter {
    fun setIconListener(listener: Listener)

    fun setIcon(icon: ItemStack?)

    fun setQuantityListener(listener: TextListener)

    fun setQuantity(quantity: String)

    fun setAddContentListener(listener: Listener)

    fun showErrorMessage(message: String)

    fun hideErrorMessage()

    fun setCommandListener(listener: TextListener)

    fun setCommand(quantity: String)
}

class AddKitContentInteractor(
    var showItem: Boolean,
    private val player: Player,
    private val presenter: AddKitContentPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val kitRepository: KitRepository,
): Interactor(presenter) {

    private var kitItem: KitItem? = null
    private var quantity: Int = 1

    private var command: String? = null

    override fun onCreate() {
        super.onCreate()

        presenter.setIconListener(object : Listener {
            override fun invoke() {
                iconSelectionBlock.setInventory(player.inventory)
                routeTo(iconSelectionBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val item = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY) ?: return
                        kitItem = KitItem(item.type.name, item.amount, item.itemMeta?.asComponentString)

                        presenter.hideErrorMessage()
                        presenter.setIcon(item)
                    }
                })
            }
        })

        presenter.setQuantityListener(object : TextListener {
            override fun invoke(text: String) {
                val quantity = text.toIntOrNull()
                if (quantity == null) {
                    presenter.setQuantity("Set quantity")
                    return
                }

                this@AddKitContentInteractor.quantity = quantity
            }
        })

        presenter.setCommandListener(object : TextListener {
            override fun invoke(text: String) {
                presenter.setCommand("/$text")
                presenter.hideErrorMessage()
                command = text
            }
        })

        presenter.setAddContentListener(object : Listener {
            override fun invoke() {
                if (showItem) {
                    val item = kitItem
                    if (item == null) {
                        presenter.showErrorMessage("${ChatColor.RED}You need to select an item first!")
                        return
                    }

                    kitRepository.builder.items.add(KitItem(item.material, quantity, item.meta))
                } else {
                    val command = command
                    if (command == null) {
                        presenter.showErrorMessage("${ChatColor.RED}You need to add a command!")
                        return
                    }

                    kitRepository.builder.commands.add(command)
                }

                reset()
                routeBack()
            }
        })

        presenter.addBackListener(object : Listener {
            override fun invoke() {
                reset()
            }
        })
    }

    private fun reset() {
        kitItem = null
        quantity = 1

        command = null
    }
}
