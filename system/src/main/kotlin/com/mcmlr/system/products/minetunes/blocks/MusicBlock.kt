package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.app.BaseEnvironment
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.SearchFactory
import com.mcmlr.system.products.minetunes.SearchState
import com.mcmlr.system.products.minetunes.player.IconType
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import javax.inject.Inject

class MusicBlock @Inject constructor(
    player: Player,
    origin: Origin,
    createPlaylistBlock: CreatePlaylistBlock,
    playlistBlock: PlaylistBlock,
    artistBlock: ArtistBlock,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = MusicViewController(player, origin)
    private val interactor = MusicInteractor(player, view, createPlaylistBlock, playlistBlock, artistBlock, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MusicViewController(
    private val player: Player,
    origin: Origin,
): ViewController(player, origin), MusicPresenter {

    private lateinit var searchBar: TextInputView
    private lateinit var contentFeed: ListFeedView
    private lateinit var playlistsButton: ButtonView
    private lateinit var songsButton: ButtonView
    private lateinit var artistsButton: ButtonView
    private lateinit var albumsButton: ButtonView
    private lateinit var createPlaylistButton: ButtonView

    private var contentItemCallback: (LibraryListModel) -> Unit = {}

    override fun setSearchListener(listener: TextListener) {
        searchBar.addTextChangedListener(listener)
    }

    override fun setPlaylistsListener(listener: Listener) {
        playlistsButton.addListener(listener)
    }

    override fun setSongsListener(listener: Listener) {
        songsButton.addListener(listener)
    }

    override fun setAlbumsListener(listener: Listener) {
        albumsButton.addListener(listener)
    }

    override fun setArtistsListener(listener: Listener) {
        artistsButton.addListener(listener)
    }

    override fun setContentClickedCallback(callback: (LibraryListModel) -> Unit) {
        contentItemCallback = callback
    }

    override fun setCreatePlaylistListener(listener: Listener) {
        createPlaylistButton.addListener(listener)
    }

    override fun setFeedState(state: LibraryListModelType?) {
        when (state) {
            LibraryListModelType.PLAYLIST -> {
                playlistsButton.update(text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()).bolden())
                albumsButton.update(text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()))
                songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
                artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
            }

            LibraryListModelType.ALBUM -> {
                playlistsButton.update(text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()))
                albumsButton.update(text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()).bolden())
                songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
                artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
            }

            LibraryListModelType.TRACK -> {
                playlistsButton.update(text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()))
                albumsButton.update(text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()))
                songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()).bolden())
                artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
            }

            LibraryListModelType.ARTIST -> {
                playlistsButton.update(text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()))
                albumsButton.update(text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()))
                songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
                artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()).bolden())
            }

            null -> {
                playlistsButton.update(text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()))
                albumsButton.update(text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()))
                songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
                artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
            }
        }
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
                                            lineWidth = 600,
                                            text = playlist.name?.bolden() ?: "Unnamed Playlist",
                                        )

                                        addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(title)
                                                .alignTopToBottomOf(title),
                                            size = 4,
                                            lineWidth = 600,
                                            text = "${playlist.songs.size} Songs"
                                        )
                                    }
                                }
                            )
                        }

                        LibraryListModelType.ALBUM -> {
                            val album = it.album ?: return@forEach

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

                                        val title = addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(this)
                                                .alignTopToTopOf(this)
                                                .margins(start = 50, top = 30),
                                            size = 6,
                                            lineWidth = 600,
                                            text = album.bolden()
                                        )

                                        addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(title)
                                                .alignTopToBottomOf(title),
                                            size = 4,
                                            lineWidth = 600,
                                            text = "${ChatColor.GRAY}Album"
                                        )
                                    }
                                }
                            )
                        }

                        LibraryListModelType.TRACK -> {
                            val track = it.track ?: return@forEach

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

                                        val title = addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(this)
                                                .alignTopToTopOf(this)
                                                .margins(start = 50, top = 30),
                                            size = 6,
                                            lineWidth = 600,
                                            text = track.song.bolden()
                                        )

                                        addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(title)
                                                .alignTopToBottomOf(title),
                                            size = 4,
                                            lineWidth = 600,
                                            text = "${ChatColor.GRAY}Song"
                                        )
                                    }
                                }
                            )
                        }

                        LibraryListModelType.ARTIST -> {
                            val artist = it.artist ?: return@forEach

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

                                        val title = addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(this)
                                                .alignTopToTopOf(this)
                                                .margins(start = 50, top = 30),
                                            size = 6,
                                            lineWidth = 600,
                                            text = artist.bolden()
                                        )

                                        addTextView(
                                            modifier = Modifier()
                                                .size(WRAP_CONTENT, WRAP_CONTENT)
                                                .alignStartToStartOf(title)
                                                .alignTopToBottomOf(title),
                                            size = 4,
                                            lineWidth = 600,
                                            text = "${ChatColor.GRAY}Artist"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        })
    }

    override fun createView() {
        searchBar = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally(),
            text = R.getString(player, S.LIBRARY_SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.LIBRARY_SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 75)
        )

        playlistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignStartToStartOf(contentFeed)
                .margins(bottom = 25),
            size = 5,
            text = R.getString(player, S.SEARCH_PLAYLISTS_BUTTON.resource()),
        )

        songsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignStartToEndOf(playlistsButton)
                .margins(start = 50, bottom = 25),
            size = 5,
            text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()),
        )

        artistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignStartToEndOf(songsButton)
                .margins(start = 50, bottom = 25),
            size = 5,
            text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()),
        )

        albumsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignStartToEndOf(artistsButton)
                .margins(start = 50, bottom = 25),
            size = 5,
            text = R.getString(player, S.SEARCH_ALBUMS_BUTTON.resource()),
        )

        createPlaylistButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignEndToEndOf(contentFeed)
                .margins(start = 50, bottom = 25),
            size = 5,
            text = R.getString(player, S.LIBRARY_CREATE_PLAYLIST_BUTTON.resource()),
        )

    }

}

interface MusicPresenter: Presenter {
    fun setFeed(feed: List<LibraryListModel>)
    fun setFeedState(state: LibraryListModelType?)

    fun setContentClickedCallback(callback: (LibraryListModel) -> Unit)

    fun setCreatePlaylistListener(listener: Listener)
    fun setSearchListener(listener: TextListener)
    fun setPlaylistsListener(listener: Listener)
    fun setSongsListener(listener: Listener)
    fun setArtistsListener(listener: Listener)
    fun setAlbumsListener(listener: Listener)
}

class MusicInteractor(
    private val player: Player,
    private val presenter: MusicPresenter,
    private val createPlaylistBlock: CreatePlaylistBlock,
    private val playlistBlock: PlaylistBlock,
    private val artistBlock: ArtistBlock,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {

    private var contentState: LibraryListModelType? = null
    private val artists = mutableMapOf<String, LibraryListModel>()
    private val albums = mutableMapOf<String, LibraryListModel>()
    private val tracks = mutableMapOf<String, LibraryListModel>()
    private val playlists = mutableListOf<LibraryListModel>()
    private val model = mutableListOf<LibraryListModel>()

    override fun onCreate() {
        super.onCreate()

        sort()

        contentState?.let {
            presenter.setFeedState(it)
        }

        when(contentState) {
            LibraryListModelType.PLAYLIST ->  presenter.setFeed(playlists)
            LibraryListModelType.ALBUM ->  presenter.setFeed(albums.values.toList())
            LibraryListModelType.ARTIST ->  presenter.setFeed(artists.values.toList())
            LibraryListModelType.TRACK ->  presenter.setFeed(tracks.values.toList())
            null -> presenter.setFeed(model)
        }

        presenter.setSearchListener(object : TextListener {
            override fun invoke(text: String) {
                when(contentState) {
                    LibraryListModelType.PLAYLIST -> {
                        val results = playlists.filter { it.playlist?.name?.lowercase()?.contains(text.lowercase()) == true }
                        presenter.setFeed(results)
                    }
                    LibraryListModelType.ALBUM -> {
                        val results = albums.values.filter { it.album?.lowercase()?.contains(text.lowercase()) == true }
                        presenter.setFeed(results)
                    }
                    LibraryListModelType.ARTIST -> {
                        val results = artists.values.filter { it.artist?.lowercase()?.contains(text.lowercase()) == true }
                        presenter.setFeed(results)
                    }
                    LibraryListModelType.TRACK -> {
                        val results = tracks.values.filter { it.track?.song?.lowercase()?.contains(text.lowercase()) == true }
                        presenter.setFeed(results)
                    }
                    null -> {
                        val playlistResults = playlists.filter { it.playlist?.name?.lowercase()?.contains(text.lowercase()) == true }
                        val albumResults = albums.values.filter { it.album?.lowercase()?.contains(text.lowercase()) == true }
                        val artistResults = artists.values.filter { it.artist?.lowercase()?.contains(text.lowercase()) == true }
                        val trackResults = tracks.values.filter { it.track?.song?.lowercase()?.contains(text.lowercase()) == true }

                        val model = mutableListOf<LibraryListModel>()
                        model.addAll(playlistResults)
                        model.addAll(albumResults)
                        model.addAll(artistResults)
                        model.addAll(trackResults)

                        presenter.setFeed(model)
                    }
                }
            }
        })

        presenter.setPlaylistsListener(object : Listener {
            override fun invoke() {
                presenter.setFeedState(LibraryListModelType.PLAYLIST)
                if (contentState == LibraryListModelType.PLAYLIST) {
                    contentState = null
                    presenter.setFeed(model)
                } else {
                    presenter.setFeed(playlists)
                    contentState = LibraryListModelType.PLAYLIST
                }
            }
        })

        presenter.setSongsListener(object : Listener {
            override fun invoke() {
                presenter.setFeedState(LibraryListModelType.TRACK)
                if (contentState == LibraryListModelType.TRACK) {
                    contentState = null
                    presenter.setFeed(model)
                } else {
                    presenter.setFeed(tracks.values.toList())
                    contentState = LibraryListModelType.TRACK
                }
            }
        })

        presenter.setArtistsListener(object : Listener {
            override fun invoke() {
                presenter.setFeedState(LibraryListModelType.ARTIST)
                if (contentState == LibraryListModelType.ARTIST) {
                    contentState = null
                    presenter.setFeed(model)
                } else {
                    presenter.setFeed(artists.values.toList())
                    contentState = LibraryListModelType.ARTIST
                }
            }
        })

        presenter.setAlbumsListener(object : Listener {
            override fun invoke() {
                presenter.setFeedState(LibraryListModelType.ALBUM)
                if (contentState == LibraryListModelType.ALBUM) {
                    contentState = null
                    presenter.setFeed(model)
                } else {
                    presenter.setFeed(albums.values.toList())
                    contentState = LibraryListModelType.ALBUM
                }
            }
        })

        presenter.setContentClickedCallback {
            when (it.type) {
                LibraryListModelType.PLAYLIST -> {
                    val playlist = it.playlist ?: return@setContentClickedCallback
                    playlistBlock.setPlaylist(playlist)
                    routeTo(playlistBlock)
                }

                LibraryListModelType.ARTIST -> {
                    val artist = it.artist ?: return@setContentClickedCallback
                    SearchFactory.search(artist.lowercase(), SearchState.ARTIST)
                        .collectFirst(DudeDispatcher()) {
                            CoroutineScope(DudeDispatcher()).launch {
                                val artistSongs = it.filter { it.artist == artist }
                                artistBlock.setArtist(artist, artistSongs)
                                routeTo(artistBlock)
                            }
                        }
                }

                LibraryListModelType.ALBUM -> {
                    val album = it.album ?: return@setContentClickedCallback
                    val artist = it.artist ?: return@setContentClickedCallback
                    SearchFactory.search(artist.lowercase(), SearchState.ARTIST)
                        .collectFirst(DudeDispatcher()) {
                            CoroutineScope(DudeDispatcher()).launch {
                                val albumSongs = it.filter { it.artist == artist && it.album == album }

                                playlistBlock.setPlaylist(Playlist(name = album, songs = albumSongs.toMutableList()))
                                routeTo(playlistBlock)
                            }
                        }
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

    fun sort() {
        playlists.clear()
        artists.clear()
        albums.clear()
        tracks.clear()
        model.clear()

        val libraryModel = libraryRepository.getModel()

        libraryModel.playlists
            .sortedBy { if (it.lastUsedDate == 0L) Long.MIN_VALUE else -it.lastUsedDate }
            .forEach { playlist ->
                if ((Date().time - playlist.lastUsedDate < 604800000L) || (playlist.favorite == true)) {
                    model.add(LibraryListModel(type = LibraryListModelType.PLAYLIST, playlist = playlist))
                }

                playlist.songs.forEach { track ->

                    artists[track.artist] = LibraryListModel(type = LibraryListModelType.ARTIST, artist = track.artist)
                    tracks[track.song] = LibraryListModel(type = LibraryListModelType.TRACK, track = track)

                    if (track.album != "EP") {
                        albums[track.album] = LibraryListModel(type = LibraryListModelType.ALBUM, album = track.album, artist = track.artist)
                    }
                }

                playlists.add(LibraryListModel(type = LibraryListModelType.PLAYLIST, playlist = playlist))
            }

        artists.values.shuffled().forEachIndexed { index, artist ->
            if (index >= 10) return@forEachIndexed
            model.add(artist)
        }

        albums.values.shuffled().forEachIndexed { index, album ->
            if (index >= 10) return@forEachIndexed
            model.add(album)
        }

        tracks.values.shuffled().forEachIndexed { index, track ->
            if (index >= 10) return@forEachIndexed
            model.add(track)
        }
    }
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
