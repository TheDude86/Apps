package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Alignment
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.NewsActionModel
import com.mcmlr.system.products.minetunes.NewsRepository
import com.mcmlr.system.products.minetunes.NewsResponse
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject

class NewsBlock @Inject constructor(
    player: Player,
    origin: Origin,
    playlistBlock: PlaylistBlock,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = NewsViewController(player, origin)
    private val interactor = NewsInteractor(player, view, playlistBlock, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class NewsViewController(
    private val player: Player,
    origin: Origin,
): ViewController(player, origin), NewsPresenter {

    private lateinit var contentFeed: ListFeedView

    private var callback: (NewsActionModel) -> Unit = {}

    override fun setCallback(callback: (NewsActionModel) -> Unit) {
        this.callback = callback
    }

    override fun setNews(news: NewsResponse) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                news.news.forEach {  model ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 150)
                            .margins(start = 50, top = 25, end = 25),
                        background = Color.fromARGB(model.backgroundColor),
                        backgroundHighlight = Color.fromARGB(model.highlightBackgroundColor),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                callback.invoke(model.action)
                            }
                        },

                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val badge = addItemView(
                                    modifier = Modifier()
                                        .size(75, 75)
                                        .alignStartToStartOf(this)
                                        .alignTopToTopOf(this)
                                        .margins(start = 50, top = 30),
                                    item = BaseEnvironment.getAppIcon(model.badge)
                                )

                                val title = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToEndOf(badge)
                                        .alignTopToTopOf(badge)
                                        .margins(start = 50),
                                    size = 6,
                                    text = model.title.bolden(),
                                )

                                val message = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(title)
                                        .alignTopToBottomOf(title),
                                    size = 4,
                                    lineWidth = 350,
                                    alignment = Alignment.LEFT,
                                    text = model.message,
                                )

                                val cta = addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignEndToEndOf(this)
                                        .alignBottomToBottomOf(this)
                                        .margins(end = 50, bottom = 20),
                                    size = 4,
                                    text = model.cta,
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(model.action)
                                        }
                                    }
                                )
                            }
                        })
                }
            }
        })
    }

    override fun createView() {
        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(800, FILL_ALIGNMENT)
                .alignTopToTopOf(this)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
        )
    }

}

interface NewsPresenter: Presenter {
    fun setNews(news: NewsResponse)

    fun setCallback(callback: (NewsActionModel) -> Unit)
}

class NewsInteractor(
    private val player: Player,
    private val presenter: NewsPresenter,
    private val playlistBlock: PlaylistBlock,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setNews(NewsRepository.news)

        presenter.setCallback { model ->
            libraryRepository.fetchPlaylist(model.data)
                .collectFirst(DudeDispatcher()) {
                    playlistBlock.setPlaylist(it)
                    routeTo(playlistBlock)
                }
        }
    }
}
