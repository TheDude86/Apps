package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.fromMCItem
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject

class MyOffersBlock @Inject constructor(
    player: Player,
    origin: Location,
    offerCreatorBlock: OfferCreatorBlock,
    offerEditorBlock: OfferEditorBlock,
    marketRepository: MarketRepository,
    orderRepository: OrderRepository,
    marketConfigRepository: MarketConfigRepository,
): Block(player, origin) {
    private val view = MyOffersViewController(player, origin)
    private val interactor = MyOffersInteractor(player, view, offerCreatorBlock, offerEditorBlock, marketRepository, orderRepository, marketConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MyOffersViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    MyOffersPresenter {
    private lateinit var myOffersButton: ButtonView
    private lateinit var feedView: FeedView
    private lateinit var messageView: TextView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}My Offers",
            size = 16,
        )

        feedView = addFeedView(
            modifier = Modifier()
                .size(800, 500)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 100, bottom = 0),
            background = Color.fromARGB(0, 0, 0, 0),
        )

        myOffersButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(feedView)
                .centerHorizontally()
                .margins(top = 50),
            text = "${ChatColor.GOLD}New Offer",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}New Offer",
        )

        messageView = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(myOffersButton)
                .centerHorizontally()
                .margins(top = 100),
            size = 4,
            text = ""
        )
    }

    override fun setFeed(orders: List<Pair<Material, Order>>, listener: (Pair<Material, Order>) -> Unit) {
        feedView.updateView {
            var row: ViewContainer? = null

            orders.forEach { model ->
                val modifier = row?.let {
                    Modifier()
                        .size(800, 100)
                        .alignTopToBottomOf(it)
                        .centerHorizontally()
                } ?: Modifier()
                    .size(800, 100)
                    .alignTopToTopOf(this)
                    .centerHorizontally()

                row = addViewContainer(
                    modifier = modifier,
                    clickable = true,
                    background = Color.fromARGB(0, 0, 255, 0),
                    backgroundHighlight = Color.fromARGB(64, 255, 255, 255),
                    listener = object : Listener {
                        override fun invoke() {
                            listener.invoke(model)
                        }
                    },
                ) {

                    val icon = addItemView(
                        modifier = Modifier()
                            .size(40, 40)
                            .alignStartToStartOf(this)
                            .centerVertically()
                            .margins(start = 100),
                        item = model.first,
                    )

                    val itemName = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(icon)
                            .alignTopToTopOf(icon)
                            .margins(start = 150, top = -20),
                        text = "${model.second.quantity} of ${model.first.name.fromMCItem()}",
                        size = 6,
                        alignment = Alignment.LEFT,
                    )

                    val head = ItemStack(Material.PLAYER_HEAD)
                    val headMeta = head.itemMeta as SkullMeta
                    headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(model.second.playerId))
                    head.itemMeta = headMeta

                    val sellerIcon = addItemView(
                        modifier = Modifier()
                            .size(32, 32)
                            .alignStartToStartOf(itemName)
                            .alignTopToBottomOf(itemName),
                        item = head,
                    )

                    val sellerName = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(sellerIcon)
                            .alignTopToTopOf(sellerIcon)
                            .alignBottomToBottomOf(sellerIcon),
                        text = "${ChatColor.GRAY}${Bukkit.getOfflinePlayer(model.second.playerId).name ?: "Dumbass don't have a name"}",
                        size = 5,
                    )

                    val price = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignStartToEndOf(sellerName)
                            .alignTopToTopOf(itemName)
                            .alignBottomToBottomOf(sellerIcon)
                            .margins(start = 500),
                        text = "${ChatColor.GRAY}$${"%.2f".format(model.second.price / 100f)}",
                        size = 10,
                    )
                }
            }
        }
    }

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }

    override fun setMyOffersListener(listener: Listener) = myOffersButton.addListener(listener)
}

interface MyOffersPresenter: Presenter {
    fun setMyOffersListener(listener: Listener)
    fun setFeed(orders: List<Pair<Material, Order>>, listener: (Pair<Material, Order>) -> Unit)
    fun setMessage(message: String)
}

class MyOffersInteractor(
    private val player: Player,
    private val presenter: MyOffersPresenter,
    private val offerCreatorBlock: OfferCreatorBlock,
    private val offerEditorBlock: OfferEditorBlock,
    private val marketRepository: MarketRepository,
    private val orderRepository: OrderRepository,
    private val marketConfigRepository: MarketConfigRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        val myOrders = marketRepository.getMyOrders(player)
        presenter.setFeed(myOrders) {
            orderRepository.selectedMaterial = it.first
            orderRepository.updatingOrder = it.second
            routeTo(offerEditorBlock)
        }

        presenter.setMyOffersListener(object : Listener {
            override fun invoke() {
                if (marketConfigRepository.model.maxOrders < 1 || myOrders.size < marketConfigRepository.model.maxOrders) {
                    routeTo(offerCreatorBlock)
                } else {
                    presenter.setMessage("${ChatColor.RED}You already have the maximum number of orders created, please delete an order before adding a new one")
                }
            }
        })
    }
}
