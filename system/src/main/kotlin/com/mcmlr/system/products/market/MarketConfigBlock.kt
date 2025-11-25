package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max

class MarketConfigBlock @Inject constructor(
    player: Player,
    origin: Origin,
    marketConfigRepository: MarketConfigRepository,
) : Block(player, origin) {
    private val view: MarketConfigViewController = MarketConfigViewController(player, origin)
    private val interactor: MarketConfigInteractor = MarketConfigInteractor(player, view, marketConfigRepository)

    override fun interactor(): Interactor = interactor

    override fun view() = view
}

class MarketConfigViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin),
    MarketConfigPresenter {

    private lateinit var maxOrdersView: TextInputView
    private lateinit var messageView: TextView

    override fun setMaxOrdersListener(listener: TextListener) = maxOrdersView.addTextChangedListener(listener)

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.CONFIG_MARKET_TITLE.resource()),
            size = 16,
        )

        addViewContainer(
            modifier = Modifier()
                .size(850, 0)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 600),
            background = Color.fromARGB(0, 0, 0, 0),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    val maxOrdersTitle = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToTopOf(this)
                            .alignStartToStartOf(this),
                        size = 6,
                        text = R.getString(player, S.CONFIG_MAX_ORDERS_TITLE.resource()),
                    )

                    val maxOrdersMessage = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(maxOrdersTitle)
                            .alignStartToStartOf(maxOrdersTitle),
                        alignment = Alignment.LEFT,
                        lineWidth = 300,
                        size = 4,
                        text = R.getString(player, S.CONFIG_MAX_ORDERS_MESSAGE.resource()),
                    )

                    maxOrdersView = addTextInputView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .position(600, 0)
                            .alignTopToBottomOf(maxOrdersTitle)
                            .alignBottomToTopOf(maxOrdersMessage),
                        size = 6,
                        text = R.getString(player, S.CONFIG_DEFAULT_MAX_ORDERS.resource()),
                        highlightedText = R.getString(player, S.CONFIG_DEFAULT_MAX_ORDERS.resource()),
                    )

                    messageView = addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(maxOrdersMessage)
                            .centerHorizontally()
                            .margins(top = 200),
                        size = 4,
                        text = ""
                    )
                }
            }
        )
    }

    override fun updateMaxOrdersText(text: String) {
        maxOrdersView.text = "${ChatColor.GOLD}$text"
        maxOrdersView.highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}$text"
    }

    override fun setMessage(message: String) {
        messageView.text = message
        updateTextDisplay(messageView)
    }
}

interface MarketConfigPresenter: Presenter {
    fun updateMaxOrdersText(text: String)
    fun setMaxOrdersListener(listener: TextListener)
    fun setMessage(message: String)
}

class MarketConfigInteractor(
    private val player: Player,
    private val presenter: MarketConfigPresenter,
    private val marketConfigRepository: MarketConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        val defaultOrders = marketConfigRepository.maxOrders()
        presenter.updateMaxOrdersText(R.getString(player, S.CONFIG_INPUT_ORDERS_PLACEHOLDER.resource(), defaultOrders, if (defaultOrders != 1) R.getString(player, S.PLURAL.resource()) else ""))

        presenter.setMaxOrdersListener(object : TextListener {
            override fun invoke(text: String) {
                val maxOrders = text.toIntOrNull()
                if (maxOrders == null) {
                    val defaultMaxOrders = marketConfigRepository.maxOrders()
                    presenter.setMessage(R.getString(player, S.CONFIG_MAX_ORDERS_ERROR_MESSAGE.resource()))
                    presenter.updateMaxOrdersText(R.getString(player, S.CONFIG_INPUT_ORDERS_PLACEHOLDER.resource(), defaultMaxOrders, if (defaultMaxOrders != 1) R.getString(player, S.PLURAL.resource()) else ""))
                    return
                }

                val orders = max(0, maxOrders)
                marketConfigRepository.updateMarketMaxOrders(orders)
                presenter.updateMaxOrdersText(R.getString(player, S.CONFIG_INPUT_ORDERS_PLACEHOLDER.resource(), orders, if (orders != 1) R.getString(player, S.PLURAL.resource()) else ""))

                presenter.setMessage("")
            }
        })
    }
}