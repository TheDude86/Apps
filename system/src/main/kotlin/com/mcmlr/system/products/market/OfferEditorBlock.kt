package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.IconSelectionBlock
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class OfferEditorBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val iconSelectionBlock: IconSelectionBlock,
    private val orderRepository: OrderRepository,
): Block(player, origin) {
    private val view = OfferEditorViewController(player, origin)
    private val interactor = OfferEditorInteractor(player, view, iconSelectionBlock, orderRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class OfferEditorViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    OfferEditorPresenter {

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
    private lateinit var update: ButtonView
    private lateinit var delete: ButtonView
    private lateinit var message: TextView

    override fun createView() {
        super.createView()
        pageTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Update Offer",
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

            update = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(-200, 0)
                    .alignTopToBottomOf(quantity)
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Update",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Update",
            )

            delete = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .position(200, 0)
                    .alignTopToBottomOf(quantity)
                    .margins(top = 50),
                text = "${ChatColor.RED}Delete",
                highlightedText = "${ChatColor.RED}${ChatColor.BOLD}Delete",
            )

            message = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(update)
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

    override fun setItem(item: Material) {
        val prettyName = item.name.fromMCItem()

        head.item = ItemStack(item)
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

    override fun setUpdateListener(listener: () -> Unit) = update.addListener(listener)

    override fun setDeleteListener(listener: () -> Unit) = delete.addListener(listener)

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

    override fun animateOrderDeleteSuccess(material: Material, order: Order, onFinish: () -> Unit) {
        var materialView: ItemView
        var titleView: TextView
        var messageView: TextView

        content.updateView {
            titleView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .centerHorizontally()
                    .alignTopToBottomOf(pageTitle),
                text = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}Order Deleted!",
                size = 24,
            )

            materialView = addItemView(
                modifier = Modifier()
                    .size(120, 120)
                    .alignTopToBottomOf(titleView)
                    .centerHorizontally()
                    .margins(top = 100),
                item = ItemStack(material)
            )

            messageView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(materialView)
                    .centerHorizontally()
                    .margins(top = 100),
                text ="${order.quantity} ${material.name.fromMCItem()}${if (order.quantity > 1) "s" else ""} has been returned to your inventory",
            )

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(messageView)
                    .centerHorizontally()
                    .margins(top = 150),
                text = "${ChatColor.GOLD}Continue",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Continue",
                callback = onFinish,
            )

        }
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
                text = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}Order Updated!",
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
                    .margins(start = 150),
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
                    .margins(start = 200),
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
    }
}

interface OfferEditorPresenter: Presenter {
    fun setItemListener(listener: () -> Unit)

    fun setZeroListener(listener: () -> Unit)

    fun setSubtractListener(listener: () -> Unit)

    fun setMaxListener(listener: () -> Unit)

    fun setAddListener(listener: () -> Unit)

    fun setUpdateListener(listener: () -> Unit)

    fun setDeleteListener(listener: () -> Unit)

    fun setItem(item: Material)

    fun setPriceListener(listener: (String) -> Unit)

    fun setQuantityListener(listener: (String) -> Unit)

    fun updatePriceText(text: String)

    fun updateQuantityText(text: String)

    fun setMessage(message: String)

    fun hideMessage()

    fun animateOrderSuccess(material: Material, order: Order, onFinish: () -> Unit)

    fun animateOrderDeleteSuccess(material: Material, order: Order, onFinish: () -> Unit)
}

class OfferEditorInteractor(
    private val player: Player,
    private val presenter: OfferEditorPresenter,
    private val iconSelectionBlock: IconSelectionBlock,
    private val orderRepository: OrderRepository,
): Interactor(presenter) {

    private var builder: Order.Builder = Order.Builder()
    private var selectedMaterial: Material? = null

    override fun onCreate() {
        super.onCreate()

        builder = Order.Builder()
        selectedMaterial = orderRepository.selectedMaterial
        val updatingOrder = orderRepository.updatingOrder ?: return
        builder.quantity(updatingOrder.quantity)
        builder.price(updatingOrder.price)
        builder.meta(updatingOrder.meta)

        presenter.updatePriceText("$${(updatingOrder.price / 100f).toString().priceFormat()}")
        presenter.updateQuantityText(updatingOrder.quantity.toString())
        selectedMaterial?.let {
            presenter.setItem(it)
        }

//        presenter.setItemListener {
//            iconSelectionBlock.setInventory(player.inventory)
//            routeTo(iconSelectionBlock) { bundle ->
//                val item = bundle.getData<ItemStack>(MATERIAL_BUNDLE_KEY) ?: return@routeTo
//                presenter.setItem(item.type)
//                selectedMaterial = item.type
//            }
//        }

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
                checkValidQuantity(updatingOrder)
            }
        }

        presenter.setZeroListener {
            presenter.updateQuantityText("0")
            builder.quantity(0)
            checkValidQuantity(updatingOrder)
        }

        presenter.setSubtractListener {
            builder.quantity?.let { quantity ->
                if (quantity > 0) {
                    builder.quantity(quantity - 1)
                    presenter.updateQuantityText(builder.quantity.toString())
                    checkValidQuantity(updatingOrder)
                }
            }

        }

        presenter.setMaxListener {
            var count = updatingOrder.quantity
            player.inventory.filterNotNull().forEach {
                if (it.type == selectedMaterial && it.itemMeta?.asComponentString == builder.meta) {
                    count += it.amount
                }
            }

            builder.quantity(count)
            presenter.updateQuantityText(builder.quantity.toString())
            checkValidQuantity(updatingOrder)
        }

        presenter.setAddListener {
            builder.quantity((builder.quantity ?: 0) + 1)
            presenter.updateQuantityText(builder.quantity.toString())
            checkValidQuantity(updatingOrder)
        }

        presenter.setDeleteListener {
            orderRepository.selectedMaterial?.let {
                orderRepository.deleteOrder(it, updatingOrder).collectFirst(DudeDispatcher()) { orderResponse ->
                    if (orderResponse == OrderStatus.ERROR) {
                        presenter.setMessage("${ChatColor.RED}Something went wrong, please try again later...")
                    } else {
//                        @Suppress("DEPRECATION") val key = if (checkVersion("1.21.5-R0.1-SNAPSHOT")) {
//                            it.keyOrNull
//                        } else {
//                            it.key
//                        }

                        @Suppress("DEPRECATION") val key = it.key

                        val item = Bukkit.getItemFactory().createItemStack("$key${updatingOrder.meta}")
                        item.amount = updatingOrder.quantity

                        player.inventory.add(player.location, item)
                        presenter.animateOrderDeleteSuccess(it, updatingOrder) {
                            routeBack()
                        }
                    }
                }
            }
        }

        presenter.setUpdateListener {
            if (selectedMaterial == null) {
                presenter.setMessage("${ChatColor.RED}You need to select an item to sell first!")
                return@setUpdateListener
            }

            if (builder.price == null) {
                presenter.setMessage("${ChatColor.RED}You need to specify the price of the ${selectedMaterial?.name?.fromMCItem()} you want to sell!")
                return@setUpdateListener
            }

            if (builder.quantity == null) {
                presenter.setMessage("${ChatColor.RED}You need to specify the amount of the ${selectedMaterial?.name?.fromMCItem()} you want to sell!")
                return@setUpdateListener
            }

            selectedMaterial?.let { material ->
                val order = builder.playerId(player.uniqueId).build() ?: return@setUpdateListener
                order.quantity -= updatingOrder.quantity
                if (checkValidQuantity(updatingOrder)) {
                    if (order.quantity > 0) {
                        orderRepository.updateOrder(material, updatingOrder, order).collectFirst(DudeDispatcher()) {
                            player.inventory.remove(material, order.meta, order.quantity)
                            presenter.animateOrderSuccess(material, order) {
                                routeBack()
                            }
                        }
                    } else if (order.quantity < 0) {
                        orderRepository.updateOrder(material, updatingOrder, order).collectFirst(DudeDispatcher()) {
//                            @Suppress("DEPRECATION") val key = if (checkVersion("1.21.5-R0.1-SNAPSHOT")) {
//                                material.keyOrNull
//                            } else {
//                                material.key
//                            }

                            @Suppress("DEPRECATION") val key = material.key

                            val item = Bukkit.getItemFactory().createItemStack("$key${updatingOrder.meta}")
                            item.amount = -order.quantity

                            player.inventory.add(player.location, item)
                            presenter.animateOrderSuccess(material, order) {
                                routeBack()
                            }
                        }
                    } else {
                        orderRepository.updateOrder(material, updatingOrder, order).collectFirst(DudeDispatcher()) {
                            presenter.animateOrderSuccess(material, order) {
                                routeBack()
                            }
                        }
                    }
                } else {
                    presenter.setMessage("${ChatColor.RED}You don't have enough ${selectedMaterial?.name?.fromMCItem()} in your inventory!")
                }
            }
        }
    }

    private fun checkValidQuantity(existingOrder: Order): Boolean {
        if (selectedMaterial != null) {
            var count = existingOrder.quantity
            player.inventory.filterNotNull().forEach {
                if (it.type == selectedMaterial && builder.meta == it.itemMeta?.asComponentString) {
                    count += it.amount
                }
            }

            if ((builder.quantity ?: 0) > count) {
                presenter.setMessage("${ChatColor.YELLOW}You don't have enough ${selectedMaterial?.name?.fromMCItem()} in your inventory!")
                return false
            } else {
                presenter.hideMessage()
                return true
            }
        }

        return true
    }
}
