package com.mcmlr.system.products.cheats

import com.mcmlr.system.products.cheats.children.ItemsBlock
import com.mcmlr.system.products.cheats.children.SpawnerBlock
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.*
import com.mcmlr.system.dagger.AppScope
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import javax.inject.Inject

@AppScope
class CheatsBlock @Inject constructor(
    player: Player,
    origin: Location,
    cheatsListBlock: CheatsListBlock,
    spawnerBlock: SpawnerBlock,
    itemsBlock: ItemsBlock,
    cheatRepository: SelectedCheatRepository,
): Block(player, origin) {
    private val view = CheatsViewController(player, origin)
    private val interactor = CheatsInteractor(
        player,
        view,
        cheatsListBlock,
        spawnerBlock,
        itemsBlock,
        cheatRepository,
    )

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class CheatsViewController(
    private val player: Player,
    origin: Location,
): NavigationViewController(player, origin), CheatsPresenter {

    private lateinit var cheatsListContainer: ViewContainer
    private lateinit var cheatsDetailContainer: ViewContainer

    private lateinit var selectedPluginListener: (CheatType) -> Unit

    override fun getDetailsContainer(): ViewContainer {
        cheatsDetailContainer.updateView {}
        return cheatsDetailContainer
    }

    override fun setSelectedPluginCTAListener(listener: (CheatType) -> Unit) {
        selectedPluginListener = listener
    }

    override fun listContainer(): ViewContainer = cheatsListContainer

    override fun setDetailsContainer(selectedCheat: CheatType) {
        cheatsDetailContainer.updateView {
            val animationView = addViewContainer(
                modifier = Modifier()
                    .size(200, 200)
                    .alignTopToTopOf(this)
                    .centerHorizontally()
                    .margins(top = 100),
            )

            val cheatTitle = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(animationView)
                    .alignStartToStartOf(this)
                    .margins(start = 100, top = 100),
                text = "${ChatColor.BOLD}${selectedCheat.title.titlecase()}",
                size = 12,
            )

            val cheatDescription = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignStartToStartOf(cheatTitle)
                    .alignTopToBottomOf(cheatTitle)
                    .margins(top = 50),
                text = selectedCheat.description,
                alignment = Alignment.LEFT,
                size = 6,
            )

            val cheatCTA = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToBottomOf(cheatDescription)
                    .centerHorizontally()
                    .margins(top = 50),
                text = "${ChatColor.GOLD}${selectedCheat.cta}",
                highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${selectedCheat.cta}",
                callback = object : Listener {
                    override fun invoke() {
                        selectedPluginListener.invoke(selectedCheat)
                    }
                }
            )
        }
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Cheats",
            size = 16,
        )

        cheatsListContainer = addViewContainer(
            modifier = Modifier()
                .size(250, FILL_ALIGNMENT)
                .alignStartToStartOf(title)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .margins(top = 100, bottom = 400),
            background = Color.fromARGB(0, 0, 0, 0),
        )

        cheatsDetailContainer = addViewContainer(
            modifier = Modifier()
                .size(900, FILL_ALIGNMENT)
                .alignStartToEndOf(cheatsListContainer)
                .alignTopToTopOf(cheatsListContainer)
                .alignBottomToBottomOf(cheatsListContainer),
            background = Color.fromARGB(0, 0, 0, 0),
        )
    }

}

interface CheatsPresenter: Presenter {
    fun listContainer(): ViewContainer

    fun setDetailsContainer(selectedCheat: CheatType)

    fun setSelectedPluginCTAListener(listener: (CheatType) -> Unit)

    fun getDetailsContainer(): ViewContainer
}

class CheatsInteractor(
    private val player: Player,
    private val presenter: CheatsPresenter,
    private val cheatsListBlock: CheatsListBlock,
    private val spawnerBlock: SpawnerBlock,
    private val itemsBlock: ItemsBlock,
    private val cheatRepository: SelectedCheatRepository,
): Interactor(presenter), CheatsEventListener {

    private lateinit var cheatsPlugins: CheatsPluginManager

    override fun setSpawnerMode() {
        attachChild(spawnerBlock, presenter.getDetailsContainer())
    }

    override fun setItemsMode() {
        attachChild(itemsBlock, presenter.getDetailsContainer())
    }

    override fun onCreate() {
        super.onCreate()

        cheatsPlugins = CheatsPluginManager(
            router,
            player,
            this,
        )
        registerPluginManager(cheatsPlugins)

        attachChild(cheatsListBlock, presenter.listContainer())

        presenter.setSelectedPluginCTAListener {
            cheatsPlugins.execute(it)
        }

        cheatRepository
            .getSelectedCheatStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                detachChildren()
                presenter.setDetailsContainer(it)
            }
            .disposeOn(disposer = this)
    }

    private fun detachChildren() {
        detachChild(spawnerBlock)
        detachChild(itemsBlock)
    }
}

interface CheatsEventListener {
    fun setSpawnerMode()

    fun setItemsMode()
}
