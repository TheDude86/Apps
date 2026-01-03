package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Context
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.products.minetunes.LibraryModel
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.player.IconType
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.Date
import javax.inject.Inject

class MusicBlock @Inject constructor(
    player: Player,
    origin: Origin,
    createPlaylistBlock: CreatePlaylistBlock,
    playlistBlock: PlaylistBlock,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = MusicViewController(player, origin)
    private val interactor = MusicInteractor(player, view, createPlaylistBlock, playlistBlock, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MusicViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), MusicPresenter {

    private lateinit var searchBar: TextInputView
    private lateinit var contentFeed: ListFeedView
    private lateinit var playlistsButton: ButtonView
    private lateinit var songsButton: ButtonView
    private lateinit var artistsButton: ButtonView
    private lateinit var albumsButton: ButtonView
    private lateinit var createPlaylistButton: ButtonView

    private var contentItemCallback: (LibraryListModel) -> Unit = {}

    override fun setContentClickedCallback(callback: (LibraryListModel) -> Unit) {
        contentItemCallback = callback
    }

    override fun setFeed(feed: List<LibraryListModel>) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                feed.forEach {
                    when (it.type) {
                        LibraryListModelType.PLAYLIST -> {
                            val playlist = it.playlist ?: return@forEach

                            addViewContainer(
                                modifier = Modifier()
                                    .size(MATCH_PARENT, 75),
                                clickable = true,
                                listener = object : Listener {
                                    override fun invoke() {
                                        contentItemCallback.invoke(it)
                                    }
                                },

                                content = object : ContextListener<ViewContainer>() {
                                    override fun ViewContainer.invoke() {

                                        val icon = playlist.icon

                                        val iconItem = if (icon?.type == IconType.MATERIAL) {
                                            ItemStack(Material.valueOf(icon.data))
                                        } else if (icon?.type == IconType.HEAD) {
                                            BaseEnvironment.getAppIcon(icon.data)
                                        } else {
                                            ItemStack(Material.AIR)
                                        }

                                        val iconView = addItemView(
                                            modifier = Modifier()
                                                .size(60, 60)
                                                .alignStartToStartOf(this)
                                                .centerVertically()
                                                .margins(start = 50),
                                            item = iconItem
                                        )

                                        val title = addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToEndOf(iconView)
                                                .alignTopToTopOf(this)
                                                .margins(start = 50, top = 30),
                                            size = 6,
                                            maxLength = 600,
                                            text = playlist.name?.bolden() ?: "Unnamed Playlist",
                                        )

                                        addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(title)
                                                .alignTopToBottomOf(title),
                                            size = 4,
                                            maxLength = 600,
                                            text = "${playlist.songs.size} Songs"
                                        )
                                    }
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }
        })
    }

    override fun setCreatePlaylistListener(listener: Listener) {
        createPlaylistButton.addListener(listener)
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.LIBRARY_TITLE.resource()),
            size = 16,
        )

        searchBar = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 75),
            text = R.getString(player, S.LIBRARY_SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.LIBRARY_SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 200, bottom = 300)
        )

        playlistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignStartToStartOf(contentFeed)
                .margins(top = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()),
        )

        songsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignStartToEndOf(playlistsButton)
                .margins(top = 50, start = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()),
        )

        artistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignStartToEndOf(songsButton)
                .margins(top = 50, start = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()),
        )

        albumsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignStartToEndOf(artistsButton)
                .margins(top = 50, start = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()),
        )

        createPlaylistButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignEndToEndOf(contentFeed)
                .margins(top = 50, start = 50),
            size = 5,
            text = R.getString(player, S.LIBRARY_CREATE_PLAYLIST_BUTTON.resource()),
        )

    }

}

interface MusicPresenter: Presenter {
    fun setCreatePlaylistListener(listener: Listener)

    fun setFeed(feed: List<LibraryListModel>)

    fun setContentClickedCallback(callback: (LibraryListModel) -> Unit)
}

class MusicInteractor(
    private val player: Player,
    private val presenter: MusicPresenter,
    private val createPlaylistBlock: CreatePlaylistBlock,
    private val playlistBlock: PlaylistBlock,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {
    override fun onCreate() {
        super.onCreate()

        presenter.setFeed(libraryRepository.getModel().sort())

        presenter.setContentClickedCallback {
            when (it.type) {
                LibraryListModelType.PLAYLIST -> {
                    val playlist = it.playlist ?: return@setContentClickedCallback
                    playlistBlock.setPlaylist(playlist)
                    routeTo(playlistBlock)
                }

                else -> {}
            }
        }

        presenter.setCreatePlaylistListener(object : Listener {
            override fun invoke() {
                routeTo(createPlaylistBlock)
            }
        })
    }
}

private fun LibraryModel.sort(): List<LibraryListModel> {
    return playlists
        .filter { (Date().time - it.lastUsedDate < 604800000L) || (it.favorite == true) }
        .sortedBy { it.lastUsedDate }
        .map { LibraryListModel(type = LibraryListModelType.PLAYLIST, playlist = it) }
}

data class LibraryListModel(
    val type: LibraryListModelType,
    val playlist: Playlist? = null,
    val track: Track? = null,
    val artist: String? = null,
    val album: String? = null,
)

enum class LibraryListModelType {
    PLAYLIST,
    ALBUM,
    ARTIST,
    TRACK,
}
