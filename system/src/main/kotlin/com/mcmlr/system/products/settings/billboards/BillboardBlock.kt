package com.mcmlr.system.products.settings.billboards

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.S
import org.bukkit.entity.Player
import javax.inject.Inject

class BillboardBlock @Inject constructor(
    player: Player,
    origin: Origin,
    createBillboardBlock: CreateBillboardBlock,
): Block(player, origin) {
    private val view = BillboardViewController(player, origin)
    private val interactor = BillboardInteractor(player, view, createBillboardBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class BillboardViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), BillboardPresenter {

    private lateinit var searchBar: TextInputView
    private lateinit var resultsFeed: ListFeedView
    private lateinit var nearbyButton: ButtonView
    private lateinit var allButton: ButtonView
    private lateinit var createButton: ButtonView

    override fun setSearchTextChangeListener(listener: TextListener) = searchBar.addTextChangedListener(listener)

    override fun setNearbyButtonListener(listener: Listener) = nearbyButton.addListener(listener)

    override fun setAllButtonListener(listener: Listener) = allButton.addListener(listener)

    override fun setCreateButtonListener(listener: Listener) = createButton.addListener(listener)

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.BILLBOARD_TITLE.resource()),
            size = 16,
        )

        searchBar = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 50),
            text = R.getString(player, S.SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        resultsFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 150, bottom = 300)
        )

        createButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(resultsFeed)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            text = R.getString(player, S.CREATE_BILLBOARD_BUTTON.resource()),
        )

        nearbyButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(resultsFeed)
                .x(-200),
            size = 5,
            text = R.getString(player, S.NEARBY_BUTTON.resource()),
        )

        allButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(resultsFeed)
                .x(200),
            size = 5,
            text = R.getString(player, S.ALL_BUTTON.resource()),
        )
    }

}

interface BillboardPresenter: Presenter {
    fun setSearchTextChangeListener(listener: TextListener)

    fun setNearbyButtonListener(listener: Listener)

    fun setAllButtonListener(listener: Listener)

    fun setCreateButtonListener(listener: Listener)
}

class BillboardInteractor(
    private val player: Player,
    private val presenter: BillboardPresenter,
    private val createBillboardBlock: CreateBillboardBlock,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setCreateButtonListener(object : Listener {
            override fun invoke() {
                routeTo(createBillboardBlock)
            }
        })
    }
}