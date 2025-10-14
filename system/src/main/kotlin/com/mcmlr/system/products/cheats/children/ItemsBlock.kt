package com.mcmlr.system.products.cheats.children

import com.mcmlr.blocks.api.app.Camera
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.system.products.data.MaterialsRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class ItemsBlock @Inject constructor(
    player: Player,
    camera: Camera,
    materialsRepository: MaterialsRepository,
): Block(player, camera) {
    private val view = ItemsViewController(player, camera)
    private val interactor = ItemsInteractor(player, view, materialsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class ItemsViewController(
    player: Player,
    camera: Camera,
): ViewController(player, camera), ItemsPresenter {

    private lateinit var contentView: ViewContainer
    private lateinit var searchButton: TextInputView
    private lateinit var feedView: ListFeedView

    override fun addSearchListener(listener: (String) -> Unit) = searchButton.addTextChangedListener(listener)

    override fun setFeed(materials: List<ItemStack>, itemCallback: (ItemStack) -> Unit) {
        feedView.updateView {
            for (i in materials.indices step 6) {
                addViewContainer(
                    modifier = Modifier()
                        .size(MATCH_PARENT, 100),
                    background = Color.fromARGB(0, 0, 0, 0)
                ) {
                    for (j in 0..5) {
                        if (i + j >= materials.size) break
                        val material = materials[i + j]
                        if (material.type == Material.WATER ||
                            material.type == Material.FIRE) break

                        addItemButtonView(
                            modifier = Modifier()
                                .position(-500 + (200 * j), 0)
                                .size(73, 73),
                            item = material,
                            visible = true,
                        ) {
                            itemCallback.invoke(material)
                        }
                    }
                }
            }
        }
    }

    override fun selectItemState() {
        contentView.updateView {
            searchButton = addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally(),
                size = 8,
                text = "${ChatColor.GRAY}${ChatColor.ITALIC}\uD83D\uDD0D Search for items or blocks...",
                highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}\uD83D\uDD0D Search for items or blocks...",
            )

            feedView = addListFeedView(
                modifier = Modifier()
                    .size(800, 500)
                    .alignTopToBottomOf(searchButton)
                    .centerHorizontally()
                    .margins(top = 100, bottom = 0),
                background = Color.fromARGB(0, 0, 0, 0),
            )
        }
    }

    override fun setItemMetadataState(item: ItemStack) {
        contentView.updateView {
            val selectedItem = addItemView(
                modifier = Modifier()
                    .size(50, 50)
                    .alignTopToTopOf(this)
                    .centerHorizontally(),
                item = item,
            )

            addTextInputView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(selectedItem)
                    .x(-500)
                    .margins(top = 50),
                size = 8,
                text = "${ChatColor.GRAY}${ChatColor.ITALIC}Set custom name...",
                highlightedText = "${ChatColor.GRAY}${ChatColor.ITALIC}${ChatColor.BOLD}Set custom name...",
            )
        }
    }

    override fun createView() {
        contentView = addViewContainer(
            modifier = Modifier()
                .size(MATCH_PARENT, FILL_ALIGNMENT)
                .alignTopToTopOf(this)
                .alignBottomToBottomOf(this)
                .margins(top = 100),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }
}

interface ItemsPresenter: Presenter {
    fun addSearchListener(listener: (String) -> Unit)

    fun setFeed(materials: List<ItemStack>, itemCallback: (ItemStack) -> Unit)

    fun selectItemState()

    fun setItemMetadataState(item: ItemStack)
}

class ItemsInteractor(
    private val player: Player,
    private val presenter: ItemsPresenter,
    private val materialsRepository: MaterialsRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.selectItemState()

        materialsRepository.materialsStream().collectFirst {
            collectOn(DudeDispatcher()) { materials ->
                presenter.setFeed(materials) {
                    presenter.setItemMetadataState(it)
                }
            }
        }

        presenter.addSearchListener { searchTerm ->
            materialsRepository.searchMaterialsStream(searchTerm).collectFirst {
                collectOn(DudeDispatcher()) { materials ->
                    presenter.setFeed(materials) {

                    }
                }
            }
        }
    }
}
