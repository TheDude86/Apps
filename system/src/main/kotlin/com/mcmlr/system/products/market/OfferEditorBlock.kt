package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.IconSelectionBlock
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class OfferEditorBlock @Inject constructor(
    player: Player,
    origin: Origin,
    private val iconSelectionBlock: IconSelectionBlock,
    private val orderRepository: OrderRepository,
): Block(player, origin) {
    private val view = OfferEditorViewController(player, origin)
    private val interactor = OfferEditorInteractor(player, view, iconSelectionBlock, orderRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class OfferEditorViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
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
            text = R.getString(player, S.UPDATE_OFFER_TITLE.resource()),
            size = 16,
        )

        content = addViewContainer(
            modifier = Modifier()
                .size(800, 0)
                .alignTopToBottomOf(pageTitle)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
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
                        text = R.getString(player, S.SELECT_ITEM_BUTTON.resource()),
                        highlightedText = R.getString(player, S.SELECT_ITEM_BUTTON.resource()),
                    )

                    price = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(name)
                            .centerHorizontally()
                            .margins(top = 20),
                        text = R.getString(player, S.PRICE_BUTTON.resource()),
                        highlightedText = R.getString(player, S.PRICE_BUTTON.resource()),
                    )

                    quantity = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(price)
                            .centerHorizontally()
                            .margins(top = 100),
                        text = R.getString(player, S.QUANTITY_BUTTON.resource()),
                        highlightedText = R.getString(player, S.QUANTITY_BUTTON.resource()),
                    )

                    add = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(quantity)
                            .alignBottomToBottomOf(quantity)
                            .alignStartToEndOf(quantity)
                            .margins(start = 300),
                        text = R.getString(player, S.ADD_QUANTITY_BUTTON.resource()),
                        highlightedText = R.getString(player, S.ADD_QUANTITY_BUTTON.resource()).bolden(),
                    )

                    max = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(add)
                            .alignBottomToBottomOf(add)
                            .alignStartToEndOf(add)
                            .margins(start = 40),
                        text = R.getString(player, S.MAX_QUANTITY_BUTTON.resource()),
                        highlightedText = R.getString(player, S.MAX_QUANTITY_BUTTON.resource()).bolden(),
                    )

                    subtract = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(quantity)
                            .alignBottomToBottomOf(quantity)
                            .alignEndToStartOf(quantity)
                            .margins(end = 300),
                        text = R.getString(player, S.LOWER_QUANTITY_BUTTON.resource()),
                        highlightedText = R.getString(player, S.LOWER_QUANTITY_BUTTON.resource()).bolden(),
                    )

                    zero = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(subtract)
                            .alignBottomToBottomOf(subtract)
                            .alignEndToStartOf(subtract)
                            .margins(end = 40),
                        text = R.getString(player, S.ZERO_QUANTITY_BUTTON.resource()),
                        highlightedText = R.getString(player, S.ZERO_QUANTITY_BUTTON.resource()),
                    )

                    update = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(-200, 0)
                            .alignTopToBottomOf(quantity)
                            .margins(top = 50),
                        text = R.getString(player, S.UPDATE_BUTTON.resource()),
                        highlightedText = R.getString(player, S.UPDATE_BUTTON.resource()).bolden(),
                    )

                    delete = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(200, 0)
                            .alignTopToBottomOf(quantity)
                            .margins(top = 50),
                        text = R.getString(player, S.DELETE_BUTTON.resource()),
                        highlightedText = R.getString(player, S.DELETE_BUTTON.resource()).bolden(),
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
        )
    }

    override fun setItemListener(listener: Listener) = name.addListener(listener)

    override fun setItem(item: Material) {
        val prettyName = item.name.fromMCItem()

        head.item = ItemStack(item)
        name.text = prettyName
        name.highlightedText = "${ChatColor.BOLD}$prettyName"

        updateItemDisplay(head)
        updateTextDisplay(name)
    }

    override fun setPriceListener(listener: TextListener) = price.addTextChangedListener(listener)

    override fun setQuantityListener(listener: TextListener) = quantity.addTextChangedListener(listener)

    override fun setZeroListener(listener: Listener) = zero.addListener(listener)

    override fun setSubtractListener(listener: Listener) = subtract.addListener(listener)

    override fun setMaxListener(listener: Listener) = max.addListener(listener)

    override fun setAddListener(listener: Listener) = add.addListener(listener)

    override fun setUpdateListener(listener: Listener) = update.addListener(listener)

    override fun setDeleteListener(listener: Listener) = delete.addListener(listener)

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

    override fun animateOrderDeleteSuccess(material: Material, order: Order, onFinish: Listener) {
        var materialView: ItemView
        var titleView: TextView
        var messageView: TextView

        content.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                titleView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .centerHorizontally()
                        .alignTopToBottomOf(pageTitle),
                    text = R.getString(player, S.ORDER_DELETED_MESSAGE.resource()),
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
                    text = R.getString(player, S.RETURNED_ITEMS_MESSAGE.resource(), order.quantity, material.name.fromMCItem(), if (order.quantity > 1) R.getString(player, S.PLURAL.resource()) else ""),
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(messageView)
                        .centerHorizontally()
                        .margins(top = 150),
                    text = R.getString(player, S.CONTINUE_BUTTON.resource()),
                    highlightedText = R.getString(player, S.CONTINUE_BUTTON.resource()).bolden(),
                    callback = onFinish,
                )
            }
        })
    }

    override fun animateOrderSuccess(material: Material, order: Order, onFinish: Listener) {
        var materialView: ItemView
        var titleView: TextView
        var materialNameView: TextView
        var quantityView: TextView
        var priceView: TextView

        content.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                titleView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .centerHorizontally()
                        .alignTopToBottomOf(pageTitle),
                    text = R.getString(player, S.ORDER_UPDATED_MESSAGE.resource()),
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
                    text = R.getString(player, S.CREATED_ORDER_AMOUNT.resource(), order.quantity),
                    size = 8,
                )

                priceView = addTextView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignStartToEndOf(materialNameView)
                        .alignTopToBottomOf(materialNameView)
                        .alignBottomToTopOf(quantityView)
                        .margins(start = 200),
                    text = R.getString(player, S.CREATED_ORDER_PRICE.resource(), "%.2f".format(order.price / 100f)),
                    size = 14,
                )

                addButtonView(
                    modifier = Modifier()
                        .size(WRAP_CONTENT, WRAP_CONTENT)
                        .alignTopToBottomOf(priceView)
                        .centerHorizontally()
                        .margins(top = 150),
                    text = R.getString(player, S.CONTINUE_BUTTON.resource()),
                    highlightedText = R.getString(player, S.CONTINUE_BUTTON.resource()).bolden(),
                    callback = onFinish,
                )
            }
        })
    }
}

interface OfferEditorPresenter: Presenter {
    fun setItemListener(listener: Listener)

    fun setZeroListener(listener: Listener)

    fun setSubtractListener(listener: Listener)

    fun setMaxListener(listener: Listener)

    fun setAddListener(listener: Listener)

    fun setUpdateListener(listener: Listener)

    fun setDeleteListener(listener: Listener)

    fun setItem(item: Material)

    fun setPriceListener(listener: TextListener)

    fun setQuantityListener(listener: TextListener)

    fun updatePriceText(text: String)

    fun updateQuantityText(text: String)

    fun setMessage(message: String)

    fun hideMessage()

    fun animateOrderSuccess(material: Material, order: Order, onFinish: Listener)

    fun animateOrderDeleteSuccess(material: Material, order: Order, onFinish: Listener)
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

        presenter.setPriceListener(object : TextListener {
            override fun invoke(text: String) {
                if (text.toDoubleOrNull() == null) {
                    presenter.updatePriceText(R.getString(player, S.DEFAULT_PRICE.resource()))
                    presenter.setMessage(R.getString(player, S.INVALID_PRICE_ERROR_MESSAGE.resource()))
                    builder.price(0)
                } else {
                    presenter.updatePriceText("$${text.priceFormat()}")
                    val price = ((text.toDouble() * 100) + 0.5).toInt()
                    builder.price(price)
                }
            }
        })

        presenter.setQuantityListener(object : TextListener {
            override fun invoke(text: String) {
                if (text.toIntOrNull() == null) {
                    presenter.updateQuantityText(R.getString(player, S.DEFAULT_QUANTITY.resource()))
                    presenter.setMessage(R.getString(player, S.INVALID_QUANTITY_ERROR_MESSAGE.resource()))
                    builder.quantity(0)
                } else {
                    builder.quantity(text.toInt())
                    checkValidQuantity(updatingOrder)
                }
            }
        })

        presenter.setZeroListener(object : Listener {
            override fun invoke() {
                presenter.updateQuantityText(R.getString(player, S.DEFAULT_QUANTITY.resource()))
                builder.quantity(0)
                checkValidQuantity(updatingOrder)
            }
        })

        presenter.setSubtractListener(object : Listener {
            override fun invoke() {
                builder.quantity?.let { quantity ->
                    if (quantity > 0) {
                        builder.quantity(quantity - 1)
                        presenter.updateQuantityText(builder.quantity.toString())
                        checkValidQuantity(updatingOrder)
                    }
                }
            }
        })

        presenter.setMaxListener(object : Listener {
            override fun invoke() {
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
        })

        presenter.setAddListener(object : Listener {
            override fun invoke() {
                builder.quantity((builder.quantity ?: 0) + 1)
                presenter.updateQuantityText(builder.quantity.toString())
                checkValidQuantity(updatingOrder)
            }
        })

        presenter.setDeleteListener(object : Listener {
            override fun invoke() {
                orderRepository.selectedMaterial?.let {
                    orderRepository.deleteOrder(it, updatingOrder).collectFirst(DudeDispatcher()) { orderResponse ->
                        if (orderResponse == OrderStatus.ERROR) {
                            presenter.setMessage(R.getString(player, S.UNKNOWN_ERROR_MESSAGE.resource()))
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
                            presenter.animateOrderDeleteSuccess(it, updatingOrder, object : Listener {
                                override fun invoke() {
                                    routeBack()
                                }
                            })
                        }
                    }
                }
            }
        })

        presenter.setUpdateListener(object : Listener {
            override fun invoke() {
                if (selectedMaterial == null) {
                    presenter.setMessage(R.getString(player, S.MISSING_MATERIAL_ERROR_MESSAGE.resource()))
                    return
                }

                if (builder.price == null) {
                    presenter.setMessage(R.getString(player, S.MISSING_PRICE_ERROR_MESSAGE.resource(), selectedMaterial?.name?.fromMCItem() ?: ""))
                    return
                }

                if (builder.quantity == null) {
                    presenter.setMessage(R.getString(player, S.MISSING_QUANTITY_ERROR_MESSAGE.resource(), selectedMaterial?.name?.fromMCItem()))
                    return
                }

                selectedMaterial?.let { material ->
                    val order = builder.playerId(player.uniqueId).build() ?: return
                    order.quantity -= updatingOrder.quantity
                    if (checkValidQuantity(updatingOrder)) {
                        if (order.quantity > 0) {
                            orderRepository.updateOrder(material, updatingOrder, order).collectFirst(DudeDispatcher()) {
                                player.inventory.remove(material, order.meta, order.quantity)
                                presenter.animateOrderSuccess(material, order, object : Listener {
                                    override fun invoke() {
                                        routeBack()
                                    }
                                })
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
                                presenter.animateOrderSuccess(material, order, object : Listener {
                                    override fun invoke() {
                                        routeBack()
                                    }
                                })
                            }
                        } else {
                            orderRepository.updateOrder(material, updatingOrder, order).collectFirst(DudeDispatcher()) {
                                presenter.animateOrderSuccess(material, order, object : Listener {
                                    override fun invoke() {
                                        routeBack()
                                    }
                                })
                            }
                        }
                    } else {
                        presenter.setMessage(R.getString(player, S.INSUFFICIENT_QUANTITY_ERROR_MESSAGE.resource(), selectedMaterial?.name?.fromMCItem()))
                    }
                }
            }
        })
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
                presenter.setMessage(R.getString(player, S.INSUFFICIENT_QUANTITY_WARNING_MESSAGE.resource(), selectedMaterial?.name?.fromMCItem()))
                return false
            } else {
                presenter.hideMessage()
                return true
            }
        }

        return true
    }
}
