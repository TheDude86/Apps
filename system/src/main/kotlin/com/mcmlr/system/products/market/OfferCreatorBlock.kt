package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.IconSelectionBlock
import com.mcmlr.system.IconSelectionBlock.Companion.MATERIAL_BUNDLE_KEY
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class OfferCreatorBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val iconSelectionBlock: IconSelectionBlock,
    private val orderRepository: OrderRepository,
): Block(player, origin) {
    private val view = OfferCreatorViewController(player, origin)
    private val interactor = OfferCreatorInteractor(player, view, iconSelectionBlock, orderRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class OfferCreatorViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    OfferCreatorPresenter {

    private lateinit var pageTitle: TextView
    private lateinit var content: ViewContainer
    private lateinit var head: ItemView
    private lateinit var name: ButtonView
    private lateinit var price: TextInputView
    private lateinit var quantity: TextInputView
    private lateinit var add: ButtonView
    private lateinit var subtract: ButtonView
    private lateinit var max: ButtonView
    private lateinit var zero: ButtonView
    private lateinit var create: ButtonView
    private lateinit var message: TextView

    override fun createView() {
        super.createView()
        pageTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Create Offer",
            size = 16,
        )

        content = addViewContainer(
            modifier = Modifier()
                .size(800, 0)
                .alignTopToBottomOf(pageTitle)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            background = Color.fromARGB(0, 0, 0, 0)
        ) {
            head = addItemView(
                modifier = Modifier()
                    .size(120, 120)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 350),
                item = ItemStack(Material.AIR)
            )

            name = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .centerHorizontally()
                    .alignTopToBottomOf(head)
                    .margins(top = 150),
                text = "Select Item",
                highlightedText = "${ChatColor.BOLD}Select item",
            )

            price = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(name)
                    .centerHorizontally()
                    .margins(top = 20),
                text = "${ChatColor.GRAY}Price",
                highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Price",
            )

            quantity = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(price)
                    .centerHorizontally()
                    .margins(top = 100),
                text = "${ChatColor.GRAY}Quantity",
                highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Quantity",
            )

            add = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(quantity)
                    .alignBottomToBottomOf(quantity)
                    .alignStartToEndOf(quantity)
                    .margins(start = 300),
                text = "${ChatColor.GOLD}+",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}+",
            )

            max = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(add)
                    .alignBottomToBottomOf(add)
                    .alignStartToEndOf(add)
                    .margins(start = 40),
                text = "${ChatColor.GOLD}Max",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Max",
            )

            subtract = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(quantity)
                    .alignBottomToBottomOf(quantity)
                    .alignEndToStartOf(quantity)
                    .margins(end = 300),
                text = "${ChatColor.GOLD}-",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}-",
            )

            zero = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(subtract)
                    .alignBottomToBottomOf(subtract)
                    .alignEndToStartOf(subtract)
                    .margins(end = 40),
                text = "${ChatColor.GOLD}Zero",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Zero",
            )

             create = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(quantity)
                    .centerHorizontally()
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Create",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Create",
            )

            message = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(create)
                    .centerHorizontally()
                    .margins(top = 25),
                size = 4,
                alignment = Alignment.LEFT,
                visible = false,
                text = "",
            )

            spin(head)
        }
    }

    override fun setItemListener(listener: () -> Unit) = name.addListener(listener)

    override fun setItem(item: ItemStack) {
        val prettyName = item.type.name.fromMCItem()

        head.item = item
        name.text = prettyName
        name.highlightedText = "${ChatColor.BOLD}$prettyName"

        updateItemDisplay(head)
        updateTextDisplay(name)
    }

    override fun setPriceListener(listener: (String) -> Unit) = price.addTextChangedListener(listener)

    override fun setQuantityListener(listener: (String) -> Unit) = quantity.addTextChangedListener(listener)

    override fun setZeroListener(listener: () -> Unit) = zero.addListener(listener)

    override fun setSubtractListener(listener: () -> Unit) = subtract.addListener(listener)

    override fun setMaxListener(listener: () -> Unit) = max.addListener(listener)

    override fun setAddListener(listener: () -> Unit) = add.addListener(listener)

    override fun setCreateListener(listener: () -> Unit) = create.addListener(listener)

    override fun updatePriceText(text: String) {
        price.text = text
        price.highlightedText = "${ChatColor.BOLD}$text"
        updateTextDisplay(price)
    }

    override fun updateQuantityText(text: String) {
        quantity.text = text
        quantity.highlightedText = "${ChatColor.BOLD}$text"
        updateTextDisplay(quantity)
    }

    override fun setMessage(message: String) {
        this.message.text = message
        this.message.visible = true
        updateTextDisplay(this.message)
    }

    override fun hideMessage() {
        this.message.visible = false
        updateTextDisplay(this.message)
    }

    override fun animateOrderSuccess(material: Material, order: Order, onFinish: () -> Unit) {
        var materialView: ItemView
        var titleView: TextView
        var materialNameView: TextView
        var quantityView: TextView
        var priceView: TextView

        content.updateView {
            titleView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .centerHorizontally()
                    .alignTopToBottomOf(pageTitle),
                text = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}Order Created!",
                size = 24,
            )

            materialView = addItemView(
                modifier = Modifier()
                    .size(120, 120)
                    .alignStartToStartOf(titleView)
                    .alignTopToBottomOf(titleView)
                    .margins(top = 300),
                item = ItemStack(material)
            )

            materialNameView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToEndOf(materialView)
                    .alignTopToTopOf(materialView)
                    .margins(start = 200),
                text = material.name.fromMCItem(),
            )

            quantityView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToEndOf(materialView)
                    .alignTopToBottomOf(materialNameView)
                    .margins(start = 150),
                text = "${ChatColor.GRAY}Amount: ${order.quantity}",
                size = 8,
            )

            priceView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToEndOf(materialNameView)
                    .alignTopToBottomOf(materialNameView)
                    .alignBottomToTopOf(quantityView)
                    .margins(start = 150),
                text = "$${"%.2f".format(order.price / 100f)}\nper Item",
                size = 14,
            )

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(priceView)
                    .centerHorizontally()
                    .margins(top = 150),
                text = "${ChatColor.GOLD}Continue",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Continue",
                callback = onFinish,
            )

        }

//        val flow = flow {
//            var step = 0
//            while (step < 100) {
//                emit(step)
//
//                step++
//                delay(50)
//            }
//        }
//
//        flow
//            .collectOn(DudeDispatcher())
//            .collectLatest {
//                if (it < 20) {
//                    titleModifier.margins(top = 1000 - (it * 50))
//
//                    content.updateTextDisplay(titleView!!)
//                    content.updateItemDisplay(materialView!!)
//                    content.updateTextDisplay(materialNameView!!)
//                    content.updateTextDisplay(quantityView!!)
//                    content.updateTextDisplay(priceView!!)
//                }
//            }
//            .disposeOn(disposer = this)
    }
}

interface OfferCreatorPresenter: Presenter {
    fun setItemListener(listener: () -> Unit)

    fun setZeroListener(listener: () -> Unit)

    fun setSubtractListener(listener: () -> Unit)

    fun setMaxListener(listener: () -> Unit)

    fun setAddListener(listener: () -> Unit)

    fun setCreateListener(listener: () -> Unit)

    fun setItem(item: ItemStack)

    fun setPriceListener(listener: (String) -> Unit)

    fun setQuantityListener(listener: (String) -> Unit)

    fun updatePriceText(text: String)

    fun updateQuantityText(text: String)

    fun setMessage(message: String)

    fun hideMessage()

    fun animateOrderSuccess(material: Material, order: Order, onFinish: () -> Unit)
}

class OfferCreatorInteractor(
    private val player: Player,
    private val presenter: OfferCreatorPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val orderRepository: OrderRepository,
): Interactor(presenter) {

    private var builder: Order.Builder = Order.Builder()
    private var selectedMaterial: ItemStack? = null

    override fun onCreate() {
        super.onCreate()

        builder = Order.Builder()
        selectedMaterial = null

        presenter.setItemListener {
            iconSelectionBlock.setInventory(player.inventory)
            routeTo(iconSelectionBlock) { bundle ->
                val item = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY) ?: return@routeTo
                presenter.setItem(item)
                selectedMaterial = item
                builder.meta(item.itemMeta?.asComponentString)
            }
        }

        presenter.setPriceListener {
            if (it.toDoubleOrNull() == null) {
                presenter.updatePriceText("$0")
                presenter.setMessage("${ChatColor.RED}Prices must be a valid number!")
                builder.price(0)
            } else {
                presenter.updatePriceText("$${it.priceFormat()}")
                val price = ((it.toDouble() * 100) + 0.5).toInt()
                builder.price(price)
            }
        }

        presenter.setQuantityListener {
            if (it.toIntOrNull() == null) {
                presenter.updateQuantityText("0")
                presenter.setMessage("${ChatColor.RED}Quantities must be a valid, whole number!")
                builder.quantity(0)
            } else {
                builder.quantity(it.toInt())
                checkValidQuantity()
            }
        }

        presenter.setZeroListener {
            presenter.updateQuantityText("0")
            builder.quantity(0)
            checkValidQuantity()
        }

        presenter.setSubtractListener {
            builder.quantity?.let { quantity ->
                if (quantity > 0) {
                    builder.quantity(quantity - 1)
                    presenter.updateQuantityText(builder.quantity.toString())
                    checkValidQuantity()
                }
            }

        }

        presenter.setMaxListener {
            var count = 0
            player.inventory.filterNotNull().forEach {
                if (it.type == selectedMaterial?.type && it.itemMeta == selectedMaterial?.itemMeta) {
                    count += it.amount
                }
            }

            builder.quantity(count)
            presenter.updateQuantityText(builder.quantity.toString())
            checkValidQuantity()
        }

        presenter.setAddListener {
            builder.quantity((builder.quantity ?: 0) + 1)
            presenter.updateQuantityText(builder.quantity.toString())
            checkValidQuantity()
        }

        presenter.setCreateListener {
            if (selectedMaterial == null) {
                presenter.setMessage("${ChatColor.RED}You need to select an item to sell first!")
                return@setCreateListener
            }

            if (builder.price == null) {
                presenter.setMessage("${ChatColor.RED}You need to specify the price of the ${selectedMaterial?.type?.name?.fromMCItem()} you want to sell!")
                return@setCreateListener
            }

            if (builder.quantity == null) {
                presenter.setMessage("${ChatColor.RED}You need to specify the amount of the ${selectedMaterial?.type?.name?.fromMCItem()} you want to sell!")
                return@setCreateListener
            }

            selectedMaterial?.let { material ->
                val order = builder.playerId(player.uniqueId).build() ?: return@setCreateListener
                if (checkValidQuantity()) {
                    orderRepository.setOrder(material, order).collectFirst(DudeDispatcher()) {
                        val type = material.type
                        player.inventory.remove(type, material.itemMeta?.asComponentString, order.quantity)
                        presenter.animateOrderSuccess(type, order) {
                            routeBack()
                        }
                    }
                } else {
                    presenter.setMessage("${ChatColor.RED}You don't have enough ${selectedMaterial?.type?.name?.fromMCItem()} in your inventory!")
                }
            }
        }
    }

    private fun checkValidQuantity(): Boolean {
        if (selectedMaterial != null) {
            var count = 0
            player.inventory.filterNotNull().forEach {
                if (it.type == selectedMaterial?.type && it.itemMeta == selectedMaterial?.itemMeta) {
                    count += it.amount
                }
            }

            if ((builder.quantity ?: 0) > count) {
                presenter.setMessage("${ChatColor.YELLOW}You don't have enough ${selectedMaterial?.type?.name?.fromMCItem()} in your inventory!")
                return false
            } else {
                presenter.hideMessage()
                return true
            }
        }

        return true
    }
}
