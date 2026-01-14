package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.minuteTimeFormat
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.player.Playlist
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class OrderPlaylistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = OrderPlaylistViewController(player, origin)
    private val interactor = OrderPlaylistInteractor(player, view, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setPlaylist(playlist: Playlist) {
        interactor.setPlaylist(playlist)
    }
}

class OrderPlaylistViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), OrderPlaylistPresenter {

    private lateinit var contentFeed: ListFeedView

    private val titles = mutableListOf<Pair<TextView, TextView>>()

    override fun swap(firstIndex: Int, secondIndex: Int) {
        if (firstIndex !in 0..<titles.size || secondIndex !in 0..<titles.size) return

        val swapTitle = titles[firstIndex].first.text
        titles[firstIndex].first.update(text = titles[secondIndex].first.text)
        titles[secondIndex].first.update(text = swapTitle)

        val swapArtist = titles[firstIndex].second.text
        titles[firstIndex].second.update(text = titles[secondIndex].second.text)
        titles[secondIndex].second.update(text = swapArtist)
    }

    override fun setPlaylist(playlist: Playlist, callback: (Int, OrderPlaylistAction) -> Unit) {
        titles.clear()
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                playlist.songs.forEachIndexed { index, track ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),

                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                val title = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .alignTopToTopOf(this)
                                        .margins(start = 50, top = 30),
                                    size = 6,
                                    maxLength = 600,
                                    text = track.song.bolden(),
                                )

                                val artist = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(title)
                                        .alignTopToBottomOf(title),
                                    size = 4,
                                    maxLength = 600,
                                    text = "${ChatColor.GRAY}${track.artist}"
                                )

                                titles.add(Pair(title, artist))

                                val downArrowButton = addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignEndToEndOf(this)
                                        .centerVertically()
                                        .margins(end = 50),
                                    text = R.getString(player, S.DOWN_ARROW_BUTTON.resource()),
                                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.DOWN_ARROW_BUTTON.resource())}",
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(index, OrderPlaylistAction.DOWN)
                                        }
                                    }
                                )

                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignEndToStartOf(downArrowButton)
                                        .centerVertically()
                                        .margins(end = 50),
                                    text = R.getString(player, S.UP_ARROW_BUTTON.resource()),
                                    highlightedText = "${ChatColor.GOLD}${ChatColor.BOLD}${R.getString(player, S.UP_ARROW_BUTTON.resource())}",
                                    callback = object : Listener {
                                        override fun invoke() {
                                            callback.invoke(index, OrderPlaylistAction.UP)
                                        }
                                    }
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Order Playlist",
            size = 16,
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 300, bottom = 600)
        )
    }

}

interface OrderPlaylistPresenter: Presenter {
    fun setPlaylist(playlist: Playlist, callback: (Int, OrderPlaylistAction) -> Unit)
    fun swap(firstIndex: Int, secondIndex: Int)
}

class OrderPlaylistInteractor(
    private val player: Player,
    private val presenter: OrderPlaylistPresenter,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {

    private var playlist: Playlist? = null

    fun setPlaylist(playlist: Playlist) {
        this.playlist = playlist
    }

    override fun onCreate() {
        super.onCreate()

        val playlist = playlist ?: return
        presenter.setPlaylist(playlist) { index, action ->
            val uuid = playlist.uuid ?: return@setPlaylist
            val offset = if (action == OrderPlaylistAction.UP) -1 else 1
            presenter.swap(index, index + offset)
            libraryRepository.updatePlaylistSongOrder(uuid, index, action)
        }
    }
}

enum class OrderPlaylistAction {
    UP,
    DOWN,
}
