package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.system.OptionRowModel
import com.mcmlr.system.OptionsBlock
import com.mcmlr.system.OptionsBlock.Companion.OPTION_BUNDLE_KEY
import com.mcmlr.system.OptionsModel
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.blocks.PlaylistPickerBlock.Companion.PLAYLIST_PICKER_BUNDLE_KEY
import com.mcmlr.system.products.minetunes.player.MusicPlayerAction
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject

class ArtistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    optionsBlock: OptionsBlock,
    playlistPickerBlock: PlaylistPickerBlock,
    playlistBlock: PlaylistBlock,
    musicPlayerBlock: MusicPlayerBlock,
    trackBlock: TrackBlock,
    musicPlayerRepository: MusicPlayerRepository,
    libraryRepository: LibraryRepository
): Block(player, origin) {
    private val view = ArtistViewController(player, origin)
    private val interactor = ArtistInteractor(
        player,
        view,
        optionsBlock,
        playlistPickerBlock,
        playlistBlock,
        musicPlayerBlock,
        musicPlayerRepository,
        libraryRepository
    )

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setArtist(artist: String, tracks: List<Track>) {
        interactor.setArtist(artist, tracks)
    }
}

class ArtistViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), ArtistPresenter {

    private lateinit var artistTitle: TextView
    private lateinit var stats: TextView
    private lateinit var playButton: ButtonView
    private lateinit var contentFeed: ListFeedView

    private lateinit var musicPlayerContainer: ViewContainer

    private lateinit var trackCallback: (Int) -> Unit
    private lateinit var showMoreListener: Listener
    private lateinit var albumCallback: (String) -> Unit

    override fun getMusicPlayerContainer(): ViewContainer = musicPlayerContainer

    override fun setPlayingState(isPlaying: Boolean) {
        val icon = if (isPlaying) S.PAUSE_BUTTON else S.PLAY_BUTTON
        playButton.update(text = R.getString(player, icon.resource()))
    }

    override fun setBackListener(listener: Listener) {
        backButton?.addListener(listener)
    }

    override fun setPlayListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setAlbumCallback(callback: (String) -> Unit) {
        albumCallback = callback
    }

    override fun setShowMoreListener(listener: Listener) {
        showMoreListener = listener
    }

    override fun setTrackCallback(callback: (Int) -> Unit) {
        trackCallback = callback
    }

    override fun setArtistInfo(artist: String, stats: String) {
        this.artistTitle.update(text = "${ChatColor.BOLD}$artist")
        this.stats.update(text = "${ChatColor.GRAY}$stats")
    }

    override fun setShowMoreContent(
        tracks: List<Track>,
        optionsCallback: (Track) -> Unit
    ) {
        contentFeed.updateView(
            object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {

                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    size = 10,
                                    lineWidth = 600,
                                    text = R.getString(player, S.ARTIST_ALL_SONGS_TITLE.resource()),
                                )
                            }
                        }
                    )

                    tracks.forEachIndexed { index, track ->
                        addViewContainer(
                            modifier = Modifier()
                                .size(MATCH_PARENT, 75),
                            clickable = true,
                            listener = object : Listener {
                                override fun invoke() {
                                    trackCallback.invoke(index)
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
                                        text = track.song.bolden(),
                                    )

                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(title)
                                            .alignTopToBottomOf(title),
                                        size = 4,
                                        lineWidth = 600,
                                        text = R.getString(player, S.ARTIST_PLAYS_PLACEHOLDER.resource(), track.plays)
                                    )

                                    addButtonView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignEndToEndOf(this)
                                            .centerVertically()
                                            .margins(end = 50),
                                        text = R.getString(player, S.OPTIONS_BUTTON.resource()),
                                        callback = object : Listener {
                                            override fun invoke() {
                                                optionsCallback.invoke(track)
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    override fun setContent(popularTracks: List<Track>, albums: List<String>, showMoreTracks: Boolean, optionsCallback: (Track) -> Unit) {
        contentFeed.updateView(
            object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {

                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    size = 10,
                                    lineWidth = 600,
                                    text = R.getString(player, S.ARTIST_POPULAR_SONGS_TITLE.resource()),
                                )
                            }
                        }
                    )

                    popularTracks.forEachIndexed { index, track ->
                        addViewContainer(
                            modifier = Modifier()
                                .size(MATCH_PARENT, 75),
                            clickable = true,
                            listener = object : Listener {
                                override fun invoke() {
                                    trackCallback.invoke(index)
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
                                        text = track.song.bolden(),
                                    )

                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(title)
                                            .alignTopToBottomOf(title),
                                        size = 4,
                                        lineWidth = 600,
                                        text = R.getString(player, S.ARTIST_PLAYS_PLACEHOLDER.resource(), track.plays)
                                    )

                                    addButtonView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignEndToEndOf(this)
                                            .centerVertically()
                                            .margins(end = 50),
                                        text = R.getString(player, S.OPTIONS_BUTTON.resource()),
                                        callback = object : Listener {
                                            override fun invoke() {
                                                optionsCallback.invoke(track)
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }

                    if (showMoreTracks) {
                        addViewContainer(
                            modifier = Modifier()
                                .size(MATCH_PARENT, 75),

                            content = object : ContextListener<ViewContainer>() {
                                override fun ViewContainer.invoke() {
                                    addButtonView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .center(),
                                        size = 4,
                                        lineWidth = 600,
                                        text = R.getString(player, S.ARTIST_SEE_ALL_SONGS_BUTTON.resource()),
                                        callback = showMoreListener
                                    )
                                }
                            }
                        )
                    }

                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {
                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .centerVertically()
                                        .margins(start = 50),
                                    size = 10,
                                    lineWidth = 600,
                                    text = R.getString(player, S.ARTIST_ALBUMS_TITLE.resource()),
                                )
                            }
                        }
                    )

                    albums.forEach { album ->
                        addViewContainer(
                            modifier = Modifier()
                                .size(MATCH_PARENT, 75),
                            clickable = true,
                            listener = object : Listener {
                                override fun invoke() {
                                    albumCallback.invoke(album)
                                }
                            },

                            content = object : ContextListener<ViewContainer>() {
                                override fun ViewContainer.invoke() {
                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(this)
                                            .centerVertically()
                                            .margins(start = 50),
                                        size = 6,
                                        lineWidth = 600,
                                        text = album.bolden(),
                                    )
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.ARTIST_TITLE.resource()),
            size = 16,
        )

        artistTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .alignStartToStartOf(title)
                .margins(top = 150),
            text = "",
            size = 12,
        )

        stats = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(artistTitle)
                .alignStartToStartOf(artistTitle)
                .margins(top = 20),
            size = 6,
            text = ""
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(stats)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 100, bottom = 600)
        )

        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(contentFeed)
                .alignBottomToTopOf(contentFeed)
                .margins(bottom = 50),
            size = 14,
            text = R.getString(player, S.PLAY_BUTTON.resource())
        )

        musicPlayerContainer = addViewContainer(
            modifier = Modifier()
                .size(FILL_ALIGNMENT, FILL_ALIGNMENT)
                .alignTopToBottomOf(contentFeed)
                .alignStartToStartOf(contentFeed)
                .alignEndToEndOf(contentFeed)
                .alignBottomToBottomOf(this),
            background = Color.fromARGB(0, 0, 0, 0)
        )
    }

}

interface ArtistPresenter: Presenter {

    fun setArtistInfo(artist: String, stats: String)

    fun setContent(popularTracks: List<Track>, albums: List<String>, showMoreTracks: Boolean, optionsCallback: (Track) -> Unit)
    fun setShowMoreContent(tracks: List<Track>, optionsCallback: (Track) -> Unit)

    fun setPlayingState(isPlaying: Boolean)

    fun setPlayListener(listener: Listener)

    fun setTrackCallback(callback: (Int) -> Unit)

    fun setShowMoreListener(listener: Listener)

    fun setAlbumCallback(callback: (String) -> Unit)

    fun getMusicPlayerContainer(): ViewContainer

    fun setBackListener(listener: Listener)
}

class ArtistInteractor(
    private val player: Player,
    private val presenter: ArtistPresenter,
    private val optionsBlock: OptionsBlock,
    private val playlistPickerBlock: PlaylistPickerBlock,
    private val playlistBlock: PlaylistBlock,
    private val musicPlayerBlock: MusicPlayerBlock,
    private val musicPlayerRepository: MusicPlayerRepository,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {

    private var artist: String = ""
    private var tracks = listOf<Track>()
    private var albumsSet = mutableSetOf<String>()
    private var playlistUpdated = false
    private var musicPlayer = musicPlayerRepository.getMusicPlayer(player)

    override fun onCreate() {
        super.onCreate()

        attachChild(musicPlayerBlock, presenter.getMusicPlayerContainer())
        musicPlayer.setDefaultPlaylist(Playlist(songs = tracks.toMutableList()))

        musicPlayer.getActionStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                if (it == MusicPlayerAction.PLAY) {
                    presenter.setPlayingState(true)
                } else if (it == MusicPlayerAction.STOP) {
                    presenter.setPlayingState(false)
                }
            }.disposeOn(disposer = this)

        presenter.setPlayListener(object : Listener {
            override fun invoke() {
                val firstPress = !playlistUpdated
                updatePlaylist()
                if (firstPress) {
                    musicPlayer.startPlaylist()
                    presenter.setPlayingState(true)
                } else if (musicPlayer.isPlaying()) {
                    musicPlayer.pause()
                    presenter.setPlayingState(false)
                } else {
                    musicPlayer.playSong()
                    presenter.setPlayingState(true)
                }
            }
        })

        presenter.setTrackCallback {
            updatePlaylist()
            musicPlayer.startSong(it)
        }

        presenter.setAlbumCallback { album ->
            val albumTracks = tracks.filter { it.album == album }
            playlistBlock.setPlaylist(Playlist(name = album, songs = albumTracks.toMutableList()))
            routeTo(playlistBlock)
        }

        val albumCount = albumsSet.size
        val trackCount = tracks.size

        presenter.setArtistInfo(artist, R.getString(player, S.ARTIST_STATS_PLACEHOLDER.resource(), trackCount, albumCount))

        val popularTracks = tracks.sortedBy { it.plays }
        val topFiveTracks = if (popularTracks.size > 5) popularTracks.subList(0, 5) else popularTracks

        val optionsCallback: (Track) -> Unit = { track ->
            val optionsList = mutableListOf<OptionRowModel>()
            if (!libraryRepository.isFavorite(track)) {
                optionsList.add(OptionRowModel("Favorite song"))
            }

            optionsList.add(OptionRowModel("Add to playlist"))

            val optionsModel = OptionsModel("Options", optionsList)

            optionsBlock.setOptions(optionsModel)
            routeTo(optionsBlock, object : RouteToCallback {
                override fun invoke(bundle: Bundle) {

                    val option = bundle.getData<String>(OPTION_BUNDLE_KEY)
                    when (option) {
                        "Favorite song" -> {
                            libraryRepository.addToFavorites(track)?.invokeOnCompletion {
                                CoroutineScope(DudeDispatcher()).launch {
                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.GREEN}${ChatColor.ITALIC}Song added to Favorites!"))
                                }
                            }
                        }

                        "Add to playlist" -> {
                            routeTo(playlistPickerBlock, object : RouteToCallback {
                                override fun invoke(bundle: Bundle) {
                                    val playlist = bundle.getData<Playlist>(PLAYLIST_PICKER_BUNDLE_KEY) ?: return
                                    val uuid = playlist.uuid ?: return
                                    if (libraryRepository.addToPlaylist(track, uuid)) {
                                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.GREEN}${ChatColor.ITALIC}Song added to ${playlist.name?.bolden()}${ChatColor.GREEN}!"))
                                    } else {
                                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.RED}${ChatColor.ITALIC}Song is already in ${playlist.name?.bolden()}${ChatColor.RED}!"))
                                    }
                                }
                            })
                        }
                    }
                }
            })
        }

        presenter.setShowMoreListener(object : Listener {
            override fun invoke() {
                presenter.setShowMoreContent(tracks, optionsCallback)
            }
        })

        presenter.setBackListener(object : Listener {
            override fun invoke() {
                playlistUpdated = false
            }
        })

        presenter.setContent(topFiveTracks, albumsSet.toList(), popularTracks.size > 5, optionsCallback)
    }

    fun setArtist(artist: String, tracks: List<Track>) {
        this.artist = artist
        this.tracks = tracks

        albumsSet = mutableSetOf<String>()
        tracks.forEach { if (it.album != "EP") albumsSet.add(it.album) }
    }

    private fun updatePlaylist() {
        if (!playlistUpdated) {
            val playlist = Playlist(songs = tracks.toMutableList())
            musicPlayer.updatePlaylist(playlist)
            libraryRepository.updateLastPlayed(playlist)
            playlistUpdated = true
        }
    }
}
