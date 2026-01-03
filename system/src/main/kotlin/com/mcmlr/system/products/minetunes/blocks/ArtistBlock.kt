package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.blocks.core.minuteTimeFormat
import com.mcmlr.system.OptionRowModel
import com.mcmlr.system.OptionsBlock
import com.mcmlr.system.OptionsBlock.Companion.OPTION_BUNDLE_KEY
import com.mcmlr.system.OptionsModel
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.blocks.PlaylistPickerBlock.Companion.PLAYLIST_PICKER_BUNDLE_KEY
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class ArtistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    optionsBlock: OptionsBlock,
    playlistPickerBlock: PlaylistPickerBlock,
    playlistBlock: PlaylistBlock,
    musicPlayerRepository: MusicPlayerRepository,
    libraryRepository: LibraryRepository
): Block(player, origin) {
    private val view = ArtistViewController(player, origin)
    private val interactor = ArtistInteractor(player, view, optionsBlock, playlistPickerBlock, playlistBlock, musicPlayerRepository, libraryRepository)

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
    private lateinit var startPlaylistButton: ButtonView
    private lateinit var contentFeed: ListFeedView

    private lateinit var songName: TextView
    private lateinit var artist: TextView
    private lateinit var songProgress: TextView
    private lateinit var playButton: ButtonView
    private lateinit var lastTrackButton: ButtonView
    private lateinit var shuffleButton: ButtonView
    private lateinit var nextTrackButton: ButtonView
    private lateinit var loopButton: ButtonView

    private lateinit var trackCallback: (Int) -> Unit
    private lateinit var showMoreListener: Listener
    private lateinit var albumCallback: (String) -> Unit

    override fun setIsShuffled(isShuffled: Boolean) {
        val shuffleString = R.getString(player, S.SHUFFLE_BUTTON.resource())
        val shuffleText = if (isShuffled) "${ChatColor.GOLD}$shuffleString" else shuffleString
        shuffleButton.update(text = shuffleText)
    }

    override fun setProgress(songProgress: String) {
        this.songProgress.update(text = "${ChatColor.GRAY}$songProgress")
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

    override fun setPlayingState(isPlaying: Boolean) {
        val icon = if (isPlaying) S.PAUSE_BUTTON else S.PLAY_BUTTON
        playButton.update(text = R.getString(player, icon.resource()))
        startPlaylistButton.update(text = R.getString(player, icon.resource()))
    }

    override fun setPlayingTrack(track: Track) {
        songName.update(text = track.song.bolden())
        artist.update(text = "${ChatColor.GRAY}${track.artist}")
    }

    override fun setPlayListener(listener: Listener) {
//        playButton.addListener(listener)
    }

    override fun setShuffleListener(listener: Listener) {
        shuffleButton.addListener(listener)
    }

    override fun setNextTrackListener(listener: Listener) {
        nextTrackButton.addListener(listener)
    }

    override fun setLastTrackListener(listener: Listener) {
        lastTrackButton.addListener(listener)
    }

    override fun setPlayButtonListener(listener: Listener) {
        playButton.addListener(listener)
        startPlaylistButton.addListener(listener)
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
                                    maxLength = 600,
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
                                        maxLength = 600,
                                        text = track.song.bolden(),
                                    )

                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(title)
                                            .alignTopToBottomOf(title),
                                        size = 4,
                                        maxLength = 600,
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
                                    maxLength = 600,
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
                                        maxLength = 600,
                                        text = track.song.bolden(),
                                    )

                                    addTextView(
                                        modifier = Modifier()
                                            .size(WRAP_CONTENT, WRAP_CONTENT)
                                            .alignStartToStartOf(title)
                                            .alignTopToBottomOf(title),
                                        size = 4,
                                        maxLength = 600,
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
                                        maxLength = 600,
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
                                    maxLength = 600,
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
                                        maxLength = 600,
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
                .margins(top = 250),
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

        startPlaylistButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(contentFeed)
                .alignBottomToTopOf(contentFeed)
                .margins(bottom = 50),
            size = 14,
            text = R.getString(player, S.PLAY_BUTTON.resource())
        )

        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(contentFeed)
                .alignBottomToBottomOf(this)
                .centerHorizontally(),
            size = 24,
            text = R.getString(player, S.PLAY_BUTTON.resource())
        )

        lastTrackButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(playButton)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton)
                .margins(end = 150),
            size = 18,
            text = R.getString(player, S.LAST_TRACK_BUTTON.resource())
        )

        nextTrackButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToEndOf(playButton)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton)
                .margins(start = 150),
            size = 18,
            text = R.getString(player, S.NEXT_TRACK_BUTTON.resource())
        )

        shuffleButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(contentFeed)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton),
            size = 18,
            text = R.getString(player, S.SHUFFLE_BUTTON.resource())
        )

        loopButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(contentFeed)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton),
            size = 18,
            text = R.getString(player, S.LOOP_BUTTON.resource())
        )

        artist = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(playButton)
                .alignStartToStartOf(shuffleButton),
            size = 6,
            text = ""
        )

        songName = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(artist)
                .alignStartToStartOf(artist),
            size = 8,
            text = ""
        )

        songProgress = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(playButton)
                .alignEndToEndOf(loopButton),
            size = 6,
            text = ""
        )
    }

}

interface ArtistPresenter: Presenter {

    fun setArtistInfo(artist: String, stats: String)

    fun setContent(popularTracks: List<Track>, albums: List<String>, showMoreTracks: Boolean, optionsCallback: (Track) -> Unit)
    fun setShowMoreContent(tracks: List<Track>, optionsCallback: (Track) -> Unit)

    fun setPlayButtonListener(listener: Listener)
    fun setPlayingState(isPlaying: Boolean)
    fun setPlayingTrack(track: Track)
    fun setProgress(songProgress: String)
    fun setIsShuffled(isShuffled: Boolean)

    fun setPlayListener(listener: Listener)
    fun setShuffleListener(listener: Listener)
    fun setNextTrackListener(listener: Listener)
    fun setLastTrackListener(listener: Listener)

    fun setTrackCallback(callback: (Int) -> Unit)

    fun setShowMoreListener(listener: Listener)

    fun setAlbumCallback(callback: (String) -> Unit)
}

class ArtistInteractor(
    private val player: Player,
    private val presenter: ArtistPresenter,
    private val optionsBlock: OptionsBlock,
    private val playlistPickerBlock: PlaylistPickerBlock,
    private val playlistBlock: PlaylistBlock,
    private val musicPlayerRepository: MusicPlayerRepository,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {
    companion object {
        private const val MUSIC_PLAYER_COLLECTION = "music player"
    }

    private var artist: String = ""
    private var tracks = listOf<Track>()
    private var albumsSet = mutableSetOf<String>()
    private var isPlaying = false
    private var musicPlayer = musicPlayerRepository.getMusicPlayer(player)

    override fun onCreate() {
        super.onCreate()

        presenter.setPlayButtonListener(object : Listener {
            override fun invoke() {
                isPlaying = !isPlaying
                if (isPlaying) {
                    setSongProgressSubscriber(musicPlayer.play())
                } else {
                    musicPlayer.pause()
                }

                presenter.setPlayingState(isPlaying)
            }
        })

        presenter.setTrackCallback {
            isPlaying = true
            presenter.setPlayingState(true)

            setSongProgressSubscriber(musicPlayer.startSong(it))
        }

        presenter.setAlbumCallback { album ->
            val albumTracks = tracks.filter { it.album == album }
            playlistBlock.setPlaylist(Playlist(name = album, songs = albumTracks.toMutableList()))
            routeTo(playlistBlock)
        }

        val albumCount = albumsSet.size
        val trackCount = tracks.size

        presenter.setArtistInfo(artist, R.getString(player, S.ARTIST_STATS_PLACEHOLDER.resource(), trackCount, albumCount))
        presenter.setIsShuffled(musicPlayer.isShuffled)

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

        presenter.setContent(topFiveTracks, albumsSet.toList(), popularTracks.size > 5, optionsCallback)

        presenter.setNextTrackListener(object : Listener {
            override fun invoke() {
                musicPlayer.goToNextSong()
            }
        })

        presenter.setLastTrackListener(object : Listener {
            override fun invoke() {
                musicPlayer.goToLastSong()
            }
        })

        presenter.setShuffleListener(object : Listener {
            override fun invoke() {
                musicPlayer.shuffle()
                presenter.setIsShuffled(musicPlayer.isShuffled)
            }
        })

        val playlist = Playlist(songs = popularTracks.toMutableList())
        musicPlayer.updatePlaylist(playlist)
    }

    fun setArtist(artist: String, tracks: List<Track>) {
        this.artist = artist
        this.tracks = tracks

        albumsSet = mutableSetOf<String>()
        tracks.forEach { if (it.album != "EP") albumsSet.add(it.album) }
    }

    private fun setSongProgressSubscriber(flow: Flow<Short>) {
        flow.collectOn(DudeDispatcher())
            .collectLatest {
                if (it == 0.toShort()) {
                    val track = musicPlayer.getCurrentTrack()
                    presenter.setPlayingTrack(track)
                }

                val song = musicPlayer.getCurrentSong() ?: return@collectLatest
                val songLength = song.length / song.speed
                val progress = it / song.speed
                presenter.setProgress("${progress.toInt().toShort().minuteTimeFormat()}/${songLength.toInt().toShort().minuteTimeFormat()}")
            }.disposeOn(collection = MUSIC_PLAYER_COLLECTION, disposer = this@ArtistInteractor)
    }
}
