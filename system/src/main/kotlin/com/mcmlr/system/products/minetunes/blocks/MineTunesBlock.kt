package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.system.products.minetunes.NewsActionModel
import com.mcmlr.system.products.minetunes.NewsActionType
import com.mcmlr.system.products.minetunes.NewsModel
import com.mcmlr.system.products.minetunes.NewsRepository
import com.mcmlr.system.products.minetunes.S
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.Date
import javax.inject.Inject

class MineTunesBlock @Inject constructor(
    player: Player,
    origin: Origin,
    searchBlock: SearchBlock,
    musicBlock: MusicBlock,
    musicPlayerBlock: MusicPlayerBlock,
    newsBlock: NewsBlock,
): Block(player, origin) {
    private val view = MineTunesViewController(player, origin)
    private val interactor = MineTunesInteractor(view, searchBlock, musicBlock, musicPlayerBlock, newsBlock)

    override fun interactor(): Interactor = interactor

    override fun view(): ViewController = view
}

class MineTunesViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), MineTunesPresenter {

    private lateinit var contentContainer: ViewContainer
    private lateinit var musicPlayerContainer: ViewContainer

    private lateinit var title: TextView
    private lateinit var homeButton: ButtonView
    private lateinit var searchButton: ButtonView
    private lateinit var libraryButton: ButtonView

    override fun setHomeListener(listener: Listener) {
        homeButton.addListener(listener)
    }

    override fun setLibraryListener(listener: Listener) {
        libraryButton.addListener(listener)
    }

    override fun setSearchListener(listener: Listener) {
        searchButton.addListener(listener)
    }

    override fun getContentContainer(): ViewContainer = contentContainer

    override fun getMusicPlayerContainer(): ViewContainer = musicPlayerContainer

    override fun setState(state: HomeState) {
        when(state) {
            HomeState.HOME -> {
                title.update(text = R.getString(player, S.MINE_TUNES_TITLE.resource()))
                homeButton.update(text = R.getString(player, S.HOME_BUTTON.resource()).bolden())
                searchButton.update(text = R.getString(player, S.SEARCH_BUTTON.resource()))
                libraryButton.update(text = R.getString(player, S.MUSIC_BUTTON.resource()))
            }
            HomeState.SEARCH -> {
                title.update(text = R.getString(player, S.SEARCH_TITLE.resource()))
                homeButton.update(text = R.getString(player, S.HOME_BUTTON.resource()))
                searchButton.update(text = R.getString(player, S.SEARCH_BUTTON.resource()).bolden())
                libraryButton.update(text = R.getString(player, S.MUSIC_BUTTON.resource()))
            }
            HomeState.LIBRARY -> {
                title.update(text = R.getString(player, S.LIBRARY_TITLE.resource()))
                homeButton.update(text = R.getString(player, S.HOME_BUTTON.resource()))
                searchButton.update(text = R.getString(player, S.SEARCH_BUTTON.resource()))
                libraryButton.update(text = R.getString(player, S.MUSIC_BUTTON.resource()).bolden())
            }
        }
    }

    override fun createView() {
        super.createView()

        title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToStartOf(this)
                .margins(top = 250, start = 900),
            text = R.getString(player, S.MINE_TUNES_TITLE.resource()),
            size = 16,
        )

        homeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(-500)
                .margins(bottom = 150),
            text = R.getString(player, S.HOME_BUTTON.resource()),
        )

        searchButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(bottom = 150),
            text = R.getString(player, S.SEARCH_BUTTON.resource()),
            highlightedText = R.getString(player, S.SEARCH_BUTTON.resource()).bolden(),
        )

        libraryButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(500)
                .margins(bottom = 150),
            text = R.getString(player, S.MUSIC_BUTTON.resource()),
            highlightedText = R.getString(player, S.MUSIC_BUTTON.resource()).bolden(),
        )

        contentContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToTopOf(searchButton)
                .centerHorizontally()
                .margins(top = 75, bottom = 400),
            background = Color.fromARGB(0, 0, 0, 0),
        )

        musicPlayerContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignTopToBottomOf(contentContainer)
                .alignStartToStartOf(contentContainer)
                .alignEndToEndOf(contentContainer)
                .alignBottomToTopOf(searchButton),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }
}

interface MineTunesPresenter: Presenter {
    fun setState(state: HomeState)

    fun setHomeListener(listener: Listener)
    fun setSearchListener(listener: Listener)
    fun setLibraryListener(listener: Listener)

    fun getContentContainer(): ViewContainer
    fun getMusicPlayerContainer(): ViewContainer
}

class MineTunesInteractor(
    private val presenter: MineTunesPresenter,
    private val searchBlock: SearchBlock,
    private val musicBlock: MusicBlock,
    private val musicPlayerBlock: MusicPlayerBlock,
    private val newsBlock: NewsBlock,
): Interactor(presenter) {

    var state: HomeState = HomeState.HOME

    override fun onCreate() {
        super.onCreate()

//        NewsRepository.updateNews(
//            NewsModel(
//                title = "Title",
//                message = "Message",
//                cta = "CTA",
//                badge = "Badge",
//                backgroundColor = 0L,
//                textColor = 0L,
//                action = NewsActionModel(
//                    type = NewsActionType.PLAYLIST,
//                    data = "DATA"
//                ),
//                Date().time,
//            )
//        )

        attachChild(musicPlayerBlock, presenter.getMusicPlayerContainer())

        presenter.setState(state)
        when(state) {
            HomeState.HOME -> attachChild(newsBlock, presenter.getContentContainer())
            HomeState.SEARCH -> attachChild(searchBlock, presenter.getContentContainer())
            HomeState.LIBRARY -> attachChild(musicBlock, presenter.getContentContainer())
        }

        presenter.setHomeListener(object : Listener {
            override fun invoke() {
                if (state == HomeState.HOME) return

                state = HomeState.HOME
                presenter.setState(state)
                detachChild(searchBlock)
                detachChild(musicBlock)
                attachChild(newsBlock, presenter.getContentContainer())
            }
        })

        presenter.setSearchListener(object : Listener {
            override fun invoke() {
                if (state == HomeState.SEARCH) return

                state = HomeState.SEARCH
                presenter.setState(state)
                detachChild(newsBlock)
                detachChild(musicBlock)
                attachChild(searchBlock, presenter.getContentContainer())
            }
        })

        presenter.setLibraryListener(object : Listener {
            override fun invoke() {
                if (state == HomeState.LIBRARY) return

                state = HomeState.LIBRARY
                presenter.setState(state)
                detachChild(newsBlock)
                detachChild(searchBlock)
                attachChild(musicBlock, presenter.getContentContainer())
            }
        })
    }
}

enum class HomeState {
    HOME,
    SEARCH,
    LIBRARY,
}
