package com.mcmlr.system.products.market

import com.mcmlr.system.products.data.VaultRepository
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.fromMCItem
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject
import kotlin.math.max

class PurchaseBlock @Inject constructor(
    player: Player,
    origin: Location,
    orderRepository: OrderRepository,
    marketRepository: MarketRepository,
    vaultRepository: VaultRepository,
): Block(player, origin) {
    private val view: PurchaseViewController = PurchaseViewController(player, origin)
    private val interactor: PurchaseInteractor = PurchaseInteractor(player, view, orderRepository, marketRepository, vaultRepository)

    override fun interactor(): Interactor = interactor

    override fun view(): ViewController = view
}

class PurchaseViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    PurchasePresenter {

    private lateinit var pageTitle: TextView
    private lateinit var content: ViewContainer
    private lateinit var head: ItemView
    private lateinit var sellerHead: ItemView
    private lateinit var name: TextView
    private lateinit var sellerName: TextView
    private lateinit var price: TextView
    private lateinit var quantity: TextView
    private lateinit var quantityInput: TextInputView
    private lateinit var add: ButtonView
    private lateinit var subtract: ButtonView
    private lateinit var max: ButtonView
    private lateinit var zero: ButtonView
    private lateinit var purchase: ButtonView
    private lateinit var message: TextView

    override fun createView() {
        super.createView()
        pageTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Purchase",
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
                    .size(80, 80)
                    .alignTopToTopOf(this)
                    .alignStartToStartOf(this)
                    .margins(start = 250, top = 350),
                item = Material.AIR
            )

            name = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(head)
                    .alignStartToEndOf(head)
                    .margins(start = 100, top = -50),
                text = "",
                size = 6,
            )

            sellerHead = addItemView(
                modifier = Modifier()
                    .size(40, 40)
                    .alignStartToEndOf(name)
                    .alignTopToBottomOf(name),
                item = Material.AIR,
            )

            sellerName = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToEndOf(sellerHead)
                    .alignTopToBottomOf(name)
                    .margins(top = 10),
                text = "",
                size = 5,
            )

            quantity = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToEndOf(head)
                    .alignBottomToBottomOf(name)
                    .margins(start = 800, top = 150),
                text = "",
                size = 7,
            )

            price = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(quantity)
                    .alignStartToStartOf(quantity)
                    .margins(top = 0),
                text = "",
                size = 7,
            )


            quantityInput = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(price)
                    .centerHorizontally()
                    .margins(top = 400),
                text = "${ChatColor.GRAY}Quantity",
                highlightedText = "${ChatColor.GRAY}${ChatColor.BOLD}Quantity",
            )

            add = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(quantityInput)
                    .alignBottomToBottomOf(quantityInput)
                    .alignStartToEndOf(quantityInput)
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
                    .alignTopToTopOf(quantityInput)
                    .alignBottomToBottomOf(quantityInput)
                    .alignEndToStartOf(quantityInput)
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

            purchase = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(quantityInput)
                    .centerHorizontally()
                    .margins(top = 50),
                text = "${ChatColor.GOLD}Purchase",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Purchase",
            )

            message = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(purchase)
                    .centerHorizontally()
                    .margins(top = 25),
                size = 4,
                alignment = Alignment.LEFT,
                text = "",
            )
        }
    }

    override fun setPurchaseResult(success: Boolean, material: Material, order: Order, onFinish: Listener) {
        if (success) {
            showPurchaseSuccess(material, order, onFinish)
        } else {
            showPurchaseFailure(onFinish)
        }
    }


    private fun showPurchaseFailure(onFinish: Listener) {
        var titleView: TextView
        var errorMessageView: TextView

        content.updateView {
            titleView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .centerHorizontally()
                    .alignTopToBottomOf(pageTitle),
                text = "${ChatColor.DARK_RED}${ChatColor.BOLD}Purchase failed...",
                size = 24,
            )

            errorMessageView = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .centerHorizontally()
                    .alignTopToBottomOf(titleView)
                    .margins(top = 100),
                text = "Something happened to the listing and is no longer available for purchase.",
                alignment = Alignment.LEFT,
            )

            addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(errorMessageView)
                    .centerHorizontally()
                    .margins(top = 350),
                text = "${ChatColor.GOLD}Continue",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}Continue",
                callback = onFinish,
            )

        }
    }

    private fun showPurchaseSuccess(material: Material, order: Order, onFinish: Listener) {
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
                text = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}Purchase Complete!",
                size = 24,
            )

            materialView = addItemView(
                modifier = Modifier()
                    .size(120, 120)
                    .alignStartToStartOf(titleView)
                    .alignTopToBottomOf(titleView)
                    .margins(top = 300, start = 600),
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
                    .alignStartToEndOf(quantityView)
                    .alignTopToBottomOf(materialNameView)
                    .alignBottomToTopOf(quantityView)
                    .margins(start = 500),
                text = "$${"%.2f".format((order.quantity * order.price) / 100f)}",
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

    override fun setOrder(material: Material, order: Order) {
        head.item = ItemStack(material)
        name.text = "${ChatColor.BOLD}${material.name.fromMCItem()}"

        sellerName.text = "${ChatColor.GRAY}${Bukkit.getOfflinePlayer(order.playerId).name ?: "Dumbass don't have a name"}"
        quantity.text = "${order.quantity} in stock"
        price.text = "$${"%.2f".format(order.price / 100f)} per item"

        val playerHead = ItemStack(Material.PLAYER_HEAD)
        val headMeta = playerHead.itemMeta as SkullMeta
        headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(order.playerId))
        playerHead.itemMeta = headMeta

        sellerHead.item = playerHead


        updateItemDisplay(head)
        updateItemDisplay(sellerHead)
        updateTextDisplay(name)
        updateTextDisplay(sellerName)
        updateTextDisplay(quantity)
        updateTextDisplay(price)
    }

    override fun setZeroListener(listener: Listener) = zero.addListener(listener)

    override fun setSubtractListener(listener: Listener) = subtract.addListener(listener)

    override fun setMaxListener(listener: Listener) = max.addListener(listener)

    override fun setAddListener(listener: Listener) = add.addListener(listener)

    override fun setQuantityListener(listener: TextListener) = quantityInput.addTextChangedListener(listener)

    override fun setPurchaseListener(listener: Listener) = purchase.addListener(listener)

    override fun setMessage(message: String) {
        this.message.text = message
        updateTextDisplay(this.message)
    }

    override fun hideMessage() {
        this.message.text = ""
        updateTextDisplay(this.message)
    }

    override fun updateQuantityText(text: String) {
        quantityInput.text = text
        quantityInput.highlightedText = "${ChatColor.BOLD}$text"
        updateTextDisplay(quantity)
    }
}

interface PurchasePresenter: Presenter {
    fun setOrder(material: Material, order: Order)

    fun setZeroListener(listener: Listener)

    fun setSubtractListener(listener: Listener)

    fun setMaxListener(listener: Listener)

    fun setAddListener(listener: Listener)

    fun setQuantityListener(listener: TextListener)

    fun setPurchaseListener(listener: Listener)

    fun setMessage(message: String)

    fun hideMessage()

    fun updateQuantityText(text: String)

    fun setPurchaseResult(success: Boolean, material: Material, order: Order, onFinish: Listener)
}

class PurchaseInteractor(
    private val player: Player,
    private val presenter: PurchasePresenter,
    private val orderRepository: OrderRepository,
    private val marketRepository: MarketRepository,
    private val vaultRepository: VaultRepository,
): Interactor(presenter) {

    private var quantity = 0

    override fun onCreate() {
        super.onCreate()

        val order = orderRepository.purchaseOrder
        quantity = 0

        presenter.setQuantityListener(object : TextListener {
            override fun invoke(text: String) {
                val quantityInput = text.toIntOrNull()
                if (quantityInput == null) {
                    presenter.updateQuantityText("0")
                    presenter.setMessage("${ChatColor.RED}Quantities must be a valid, whole number!")
                } else {
                    quantity = quantityInput
                    updateOrder()
                }
            }
        })

        presenter.setZeroListener(object : Listener {
            override fun invoke() {
                quantity = 0
                updateOrder()
            }
        })

        presenter.setSubtractListener(object : Listener {
            override fun invoke() {
                quantity = max(quantity - 1, 0)
                updateOrder()
            }
        })

        presenter.setAddListener(object : Listener {
            override fun invoke() {
                quantity++
                updateOrder()
            }
        })

        presenter.setMaxListener(object : Listener {
            override fun invoke() {
                quantity = order?.second?.quantity ?: 0
                updateOrder()
            }
        })

        presenter.setPurchaseListener(object : Listener {
            override fun invoke() {
                if (checkValidQuantity() && checkValidBalance()) {
                    if (quantity < 1) {
                        presenter.setMessage("${ChatColor.RED}Quantities must be above zero!")
                    } else {
                        val material = order?.first ?: return
                        val playerId = order.second.playerId
                        val price = order.second.price
                        val meta = order.second.meta
                        val purchaseOrder = Order(playerId, quantity, price, meta)
                        marketRepository.queuePurchase(player, material, purchaseOrder) {
                            presenter.setPurchaseResult(it, material, purchaseOrder, object : Listener {
                                override fun invoke() {
                                    routeBack()
                                }
                            })
                        }
                    }
                }
            }
        })

        if (order != null) {
            presenter.setOrder(order.first, order.second)
        } else {
            routeBack()
        }
    }

    private fun updateOrder() {
        updateQuantityText()
        if (checkValidBalance() && checkValidQuantity()) {
            presenter.hideMessage()
        }
    }

    private fun updateQuantityText() {
        val cost = (orderRepository.purchaseOrder?.second?.price ?: 0) * quantity
        val quantityString = if (quantity == 0) "0" else "$quantity ($${"%.2f".format(cost / 100f)})"

        presenter.updateQuantityText(quantityString)

    }

    private fun checkValidBalance(): Boolean {
        val cost = (orderRepository.purchaseOrder?.second?.price ?: 0) * quantity
        val playerBalance = vaultRepository.economy?.getBalance(player) ?: 0.0
        if (playerBalance < (cost / 100.0)) {
            presenter.setMessage("${ChatColor.YELLOW}You don't have enough money to purchase this much ${orderRepository.purchaseOrder?.first?.name?.fromMCItem()}")
        }

        return playerBalance >= (cost / 100.0)
    }

    private fun checkValidQuantity(): Boolean {
        val quantity = orderRepository.purchaseOrder?.second?.quantity ?: return false
        if (this.quantity > quantity) {
            presenter.setMessage("${ChatColor.YELLOW}There's not enough ${orderRepository.purchaseOrder?.first?.name?.fromMCItem()} in stock!")
        }

        return this.quantity <= quantity
    }
}
