package com.mcmlr.system.products.settings.billboards

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.BillboardConfigModel
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.S
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject

class BillboardBlock @Inject constructor(
    player: Player,
    origin: Origin,
    createBillboardBlock: CreateBillboardBlock,
    editBillboardBlock: EditBillboardBlock,
    systemConfigRepository: SystemConfigRepository,
): Block(player, origin) {
    private val view = BillboardViewController(player, origin)
    private val interactor = BillboardInteractor(player, view, createBillboardBlock, editBillboardBlock, systemConfigRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class BillboardViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), BillboardPresenter {

    private lateinit var searchBar: TextInputView
    private lateinit var billboardsFeed: ListFeedView
    private lateinit var nearbyButton: ButtonView
    private lateinit var allButton: ButtonView
    private lateinit var createButton: ButtonView

    override fun setSearchTextChangeListener(listener: TextListener) = searchBar.addTextChangedListener(listener)

    override fun setNearbyButtonListener(listener: Listener) = nearbyButton.addListener(listener)

    override fun setAllButtonListener(listener: Listener) = allButton.addListener(listener)

    override fun setCreateButtonListener(listener: Listener) = createButton.addListener(listener)

    override fun setBillboards(billboardModel: BillboardConfigModel, callback: (BillboardModel) -> Unit) {
        val billboards = billboardModel.billboards
        billboardsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (billboards.isEmpty()) {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .center(),
                                    size = 6,
                                    text = "${ChatColor.GRAY}${ChatColor.ITALIC}No billboards created yet..."
                                )
                            }
                        }
                    )
                }

                billboards.forEach { billboard ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                callback.invoke(billboard)
                            }
                        },

                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val name = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .alignTopToTopOf(this)
                                        .margins(start = 50, top = 25),
                                    size = 6,
                                    lineWidth = 600,
                                    text = billboard.name.bolden(),
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .alignTopToBottomOf(name)
                                        .margins(start = 50),
                                    size = 4,
                                    lineWidth = 600,
                                    text = "${ChatColor.GRAY}${billboard.world} | X:${"%.2f".format(billboard.x)}, Y:${"%.2f".format(billboard.y)}, Z:${"%.2f".format(billboard.z)}",
                                )
                            }
                        }
                    )
                }
            }
        })
    }

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

        billboardsFeed = addListFeedView(
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
                .alignTopToBottomOf(billboardsFeed)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            text = R.getString(player, S.CREATE_BILLBOARD_BUTTON.resource()),
        )

        nearbyButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(billboardsFeed)
                .x(-200),
            size = 5,
            text = R.getString(player, S.NEARBY_BUTTON.resource()),
        )

        allButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(billboardsFeed)
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

    fun setBillboards(billboardModel: BillboardConfigModel, callback: (BillboardModel) -> Unit)
}

class BillboardInteractor(
    private val player: Player,
    private val presenter: BillboardPresenter,
    private val createBillboardBlock: CreateBillboardBlock,
    private val editBillboardBlock: EditBillboardBlock,
    private val systemConfigRepository: SystemConfigRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setCreateButtonListener(object : Listener {
            override fun invoke() {
                routeTo(createBillboardBlock)
            }
        })

        presenter.setBillboards(systemConfigRepository.model.billboards) {
            editBillboardBlock.setBillboard(it)
            routeTo(editBillboardBlock)
        }
    }
}