package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.fromMCItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject

class MarketBlock @Inject constructor(
    player: Player,
    origin: Location,
    myOffersBlock: MyOffersBlock,
    purchaseBlock: PurchaseBlock,
    marketRepository: MarketRepository,
    orderRepository: OrderRepository,
): Block(player, origin) {
    private val view = MarketViewController(player, origin)
    private val interactor = MarketInteractor(view, myOffersBlock, purchaseBlock, marketRepository, orderRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MarketViewController(player: Player, origin: Location): NavigationViewController(player, origin),
    MarketPresenter {
    private lateinit var searchButton: TextInputView
    private lateinit var myOffersButton: ButtonView
    private lateinit var feedView: FeedView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Market",
            size = 16,
        )

        searchButton = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 100),
            size = 8,
            text = "${ChatColor.GRAY}${ChatColor.ITALIC}\uD83D\uDD0D Search for items or blocks...",
            highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}\uD83D\uDD0D Search for items or blocks...",
        )

        feedView = addFeedView(
            modifier = Modifier()
                .size(800, 500)
                .alignTopToBottomOf(searchButton)
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
            text = "${ChatColor.GOLD}My Offers",
            highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}My Offers",
        )
    }

    override fun setMyOffersListener(listener: () -> Unit) = myOffersButton.addListener(listener)

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
                    listener = { listener.invoke(model) },
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

    override fun setSearchListener(listener: (String) -> Unit) = searchButton.addTextChangedListener(listener)
}

interface MarketPresenter: Presenter {
    fun setMyOffersListener(listener: () -> Unit)

    fun setFeed(orders: List<Pair<Material, Order>>, listener: (Pair<Material, Order>) -> Unit)

    fun setSearchListener(listener: (String) -> Unit)
}

class MarketInteractor(
    private val presenter: MarketPresenter,
    private val myOffersBlock: MyOffersBlock,
    private val purchaseBlock: PurchaseBlock,
    private val marketRepository: MarketRepository,
    private val orderRepository: OrderRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.setFeed(marketRepository.getOrders()) {
            orderRepository.purchaseOrder = it
            routeTo(purchaseBlock)
        }

        presenter.setSearchListener { searchTerm ->
            presenter.setFeed(marketRepository.getOrders(searchTerm)) {
                orderRepository.purchaseOrder = it
                routeTo(purchaseBlock)
            }
        }

        presenter.setMyOffersListener {
            routeTo(myOffersBlock)
        }
    }
}
