package com.mcmlr.system

import com.mcmlr.system.products.data.MaterialsRepository
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.FeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.collectOn
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class IconSelectionBlock @Inject constructor(
    player: Player,
    origin: Location,
    private val materialsRepository: MaterialsRepository,
): Block(player, origin) {
    companion object {
        const val MATERIAL_BUNDLE_KEY = "material"
    }

    private val view = IconSelectionBlockViewController(player, origin)
    private val interactor = IconSelectionInteractor(player, view, materialsRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setInventory(inventory: Inventory) {
        materialsRepository.setInventory(inventory)
    }

    fun resetInventory() {
        materialsRepository.resetInventory()
    }
}

class IconSelectionBlockViewController(
    player: Player,
    origin: Location
): NavigationViewController(player, origin), IconSelectionPresenter {

    private lateinit var searchButton: TextInputView
    private lateinit var feedView: FeedView

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Select an Icon",
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
    }

    override fun addSearchListener(listener: TextListener) = searchButton.addTextChangedListener(listener)

    override fun setFeed(materials: List<ItemStack>, itemCallback: (ItemStack) -> Unit) {
        feedView.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                var row: ViewContainer? = null
                for (i in materials.indices step 6) {
                    val modifier = if (row == null) {
                        Modifier()
                            .size(MATCH_PARENT, 100)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                    } else {
                        Modifier()
                            .size(MATCH_PARENT, 100)
                            .alignTopToBottomOf(row)
                            .centerHorizontally()
                    }

                    row = addViewContainer(
                        modifier = modifier,
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                for (j in 0..5) {
                                    if (i + j >= materials.size) break
                                    val material = materials[i + j]
                                    if (material.type == Material.WATER ||
                                        material.type == Material.FIRE ||
                                        material.type == Material.ACACIA_WALL_SIGN) break

                                    addItemButtonView(
                                        modifier = Modifier()
                                            .position(-500 + (200 * j), 0)
                                            .size(73, 73),
                                        item = material,
                                        visible = true,
                                        callback = object : Listener {
                                            override fun invoke() {
                                                itemCallback.invoke(material)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        })
    }
}

interface IconSelectionPresenter: Presenter {
    fun addSearchListener(listener: TextListener)

    fun setFeed(materials: List<ItemStack>, itemCallback: (ItemStack) -> Unit)
}

class IconSelectionInteractor(
    private val player: Player,
    private val presenter: IconSelectionPresenter,
    private val materialsRepository: MaterialsRepository,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        materialsRepository.materialsStream().collectFirst {
            collectOn(DudeDispatcher(player)) { materials ->
                presenter.setFeed(materials) {
                    addBundleData(IconSelectionBlock.MATERIAL_BUNDLE_KEY, it)
                    routeBack()
                }
            }
        }

        presenter.addSearchListener(object : TextListener {
            override fun invoke(text: String) {
                materialsRepository.searchMaterialsStream(text).collectFirst {
                    collectOn(DudeDispatcher(player)) { materials ->
                        presenter.setFeed(materials) {
                            addBundleData(IconSelectionBlock.MATERIAL_BUNDLE_KEY, it)
                            routeBack()
                        }
                    }
                }
            }
        })
    }
}
