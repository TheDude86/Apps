package com.mcmlr.system.products.preferences

import com.mcmlr.blocks.api.app.BaseApp
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

class FavoritesBlock @Inject constructor(
    player: Player,
    origin: Origin,
    selectFavoriteBlock: SelectFavoriteBlock,
    preferencesRepository: PreferencesRepository,
): Block(player, origin) {
    private val view = FavoritesViewController(player, origin)
    private val interactor = FavoritesInteractor(player, view, selectFavoriteBlock, preferencesRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class FavoritesViewController(
    private val player: Player,
    origin: Origin,
): ViewController(player, origin), FavoritesPresenter {

    private lateinit var favoriteSlots: List<ViewContainer>

    private var slotListener: (Int) -> Unit = {}

    override fun setFavoriteSlotListener(listener: (Int) -> Unit) {
        slotListener = listener
    }

    override fun setFavorites(favoriteApps: List<BaseEnvironment<BaseApp>>) {
        favoriteApps.forEachIndexed { index, applicationModel ->
            if (favoriteSlots.size <= index) return@forEachIndexed
            val slot = favoriteSlots[index]
            slot.updateView(object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    val appIcon = addItemView(
                        modifier = Modifier()
                            .size(75, 75)
                            .alignTopToTopOf(this)
                            .centerHorizontally()
                            .margins(top = 30),
                        item = applicationModel.getAppIcon()
                    )

                    addTextView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .alignTopToBottomOf(appIcon)
                            .centerHorizontally()
                            .margins(top = 10),
                        text = "${ChatColor.GOLD}${applicationModel.name()}",
                        size = 6,
                    )
                }
            })
        }
    }

    override fun createView() {
        val favoriteAppsTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(this)
                .alignTopToTopOf(this),
            text = R.getString(player, S.FAVORITE_APPS_TITLE.resource()),
        )

        val favoriteAppsSubtitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(favoriteAppsTitle)
                .alignStartToStartOf(favoriteAppsTitle)
                .margins(top = 40),
            alignment = Alignment.LEFT,
            text = R.getString(player, S.FAVORITE_APPS_SUBTITLE.resource()) //TODO: Fix text line measurements
        )

        val slotSize = 150

        val favoriteAppsSlotOne = addViewContainer(
            modifier = Modifier()
                .size(slotSize, slotSize)
                .alignTopToBottomOf(favoriteAppsSubtitle)
                .alignStartToStartOf(favoriteAppsSubtitle)
                .margins(top = 60),
            height = -2,
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    slotListener.invoke(0)
                }
            }
        )

        val favoriteAppsSlotTwo = addViewContainer(
            modifier = Modifier()
                .size(slotSize, slotSize)
                .alignTopToTopOf(favoriteAppsSlotOne)
                .alignStartToEndOf(favoriteAppsSlotOne)
                .margins(start = 60),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    slotListener.invoke(1)
                }
            }
        )

        val favoriteAppsSlotThree = addViewContainer(
            modifier = Modifier()
                .size(slotSize, slotSize)
                .alignTopToTopOf(favoriteAppsSlotTwo)
                .alignStartToEndOf(favoriteAppsSlotTwo)
                .margins(start = 60),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    slotListener.invoke(2)
                }
            }
        )

        val favoriteAppsSlotFour = addViewContainer(
            modifier = Modifier()
                .size(slotSize, slotSize)
                .alignTopToTopOf(favoriteAppsSlotThree)
                .alignStartToEndOf(favoriteAppsSlotThree)
                .margins(start = 60),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    slotListener.invoke(3)
                }
            }
        )

        val favoriteAppsSlotFive = addViewContainer(
            modifier = Modifier()
                .size(slotSize, slotSize)
                .alignTopToTopOf(favoriteAppsSlotFour)
                .alignStartToEndOf(favoriteAppsSlotFour)
                .margins(start = 60),
            clickable = true,
            listener = object : Listener {
                override fun invoke() {
                    slotListener.invoke(4)
                }
            }
        )

        favoriteSlots = listOf(
            favoriteAppsSlotOne,
            favoriteAppsSlotTwo,
            favoriteAppsSlotThree,
            favoriteAppsSlotFour,
            favoriteAppsSlotFive,
        )
    }

}

interface FavoritesPresenter: Presenter {
    fun setFavoriteSlotListener(listener: (Int) -> Unit)

    fun setFavorites(favoriteApps: List<BaseEnvironment<BaseApp>>)
}

class FavoritesInteractor(
    private val player: Player,
    private val presenter: FavoritesPresenter,
    private val selectFavoriteBlock: SelectFavoriteBlock,
    private val preferencesRepository: PreferencesRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setFavoriteSlotListener {
            preferencesRepository.setEditFavoriteIndex(it)
            routeTo(selectFavoriteBlock)
        }

        presenter.setFavorites(preferencesRepository.getFavorites(player))
    }
}
