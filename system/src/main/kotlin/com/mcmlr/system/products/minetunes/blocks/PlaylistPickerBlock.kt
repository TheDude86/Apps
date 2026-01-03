package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.blocks.PlaylistPickerBlock.Companion.PLAYLIST_PICKER_BUNDLE_KEY
import com.mcmlr.system.products.minetunes.player.Playlist
import org.bukkit.entity.Player
import javax.inject.Inject

class PlaylistPickerBlock @Inject constructor(
    player: Player,
    origin: Origin,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    companion object {
        const val PLAYLIST_PICKER_BUNDLE_KEY = "playlist picker"
    }

    private val view = PlaylistPickerViewController(player, origin)
    private val interactor = PlaylistPickerInteractor(player, view, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class PlaylistPickerViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), PlaylistPickerPresenter {

    private lateinit var contentFeed: ListFeedView

    override fun setPlaylistFeed(playlists: List<Playlist>, callback: (Playlist) -> Unit) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                playlists.forEach {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                callback.invoke(it)
                            }
                        },

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
                                    text = it.name ?: "Untitled Playlist",
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(title)
                                        .alignTopToBottomOf(title),
                                    size = 4,
                                    maxLength = 600,
                                    text = "${it.songs.size} Songs",
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
            text = R.getString(player, S.PLAYLIST_PICKER_TITLE.resource()),
            size = 16,
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 300, bottom = 300)
        )
    }

}

interface PlaylistPickerPresenter: Presenter {
    fun setPlaylistFeed(playlists: List<Playlist>, callback: (Playlist) -> Unit)
}

class PlaylistPickerInteractor(
    private val player: Player,
    private val presenter: PlaylistPickerPresenter,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()
        presenter.setPlaylistFeed(libraryRepository.getPlaylists()) {
            addBundleData(PLAYLIST_PICKER_BUNDLE_KEY, it)
            routeBack()
        }
    }
}
