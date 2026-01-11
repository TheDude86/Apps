package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.*
import com.mcmlr.blocks.core.*
import com.mcmlr.system.OptionRowModel
import com.mcmlr.system.OptionsBlock
import com.mcmlr.system.OptionsBlock.Companion.OPTION_BUNDLE_KEY
import com.mcmlr.system.OptionsModel
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.SearchFactory
import com.mcmlr.system.products.minetunes.SearchState
import com.mcmlr.system.products.minetunes.blocks.PlaylistPickerBlock.Companion.PLAYLIST_PICKER_BUNDLE_KEY
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class TrackBlock @Inject constructor(
    player: Player,
    origin: Origin,
    resources: Resources,
    optionsBlock: OptionsBlock,
    playlistPickerBlock: PlaylistPickerBlock,
    musicPlayerRepository: MusicPlayerRepository,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = TrackViewController(player, origin)
    private val interactor = TrackInteractor(player, resources, view, optionsBlock, playlistPickerBlock, musicPlayerRepository, libraryRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class TrackViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), TrackPresenter {

    private lateinit var playButton: ButtonView
    private lateinit var lastTrackButton: ButtonView
    private lateinit var shuffleButton: ButtonView
    private lateinit var nextTrackButton: ButtonView
    private lateinit var loopButton: ButtonView
    private lateinit var optionsButton: ButtonView
    private lateinit var progressBar: ViewContainer
    private lateinit var progressIndicator: ItemView
    private lateinit var progressTime: TextView
    private lateinit var songLength: TextView
    private lateinit var artist: TextView
    private lateinit var songTitle: TextView
    private lateinit var contentFeed: ListFeedView

    private lateinit var resultCallback: (Int) -> Unit

    override fun setResultsCallback(callback: (Int) -> Unit) {
        resultCallback = callback
    }

    override fun setPlayListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setLoopListener(listener: Listener) {
        loopButton.addListener(listener)
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

    override fun setOptionsListener(listener: Listener) {
        optionsButton.addListener(listener)
    }

    override fun setIsLooped(isLooped: Boolean) {
        val loopedString = R.getString(player, S.LOOP_BUTTON.resource())
        val loopedText = if (isLooped) "${ChatColor.GOLD}$loopedString" else loopedString
        loopButton.update(text = loopedText)
    }

    override fun setIsShuffled(isShuffled: Boolean) {
        val shuffleString = R.getString(player, S.SHUFFLE_BUTTON.resource())
        val shuffleText = if (isShuffled) "${ChatColor.GOLD}$shuffleString" else shuffleString
        shuffleButton.update(text = shuffleText)
    }

    override fun setPlayingState(isPlaying: Boolean) {
        val buttonText = if (isPlaying) R.getString(player, S.PAUSE_BUTTON.resource()) else R.getString(player, S.PLAY_BUTTON.resource())
        playButton.update(text = buttonText, highlightedText = "${ChatColor.BOLD}$buttonText")
    }

    override fun setPlaylist(playlist: Playlist) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                playlist.songs.forEachIndexed { index, track ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 50),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                resultCallback.invoke(index)
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
                                    size = 4,
                                    lineWidth = 600,
                                    text = track.song,
                                )
                            }
                        }
                    )
                }
            }
        })

    }

    override fun setTrack(track: Track) {
        songTitle.update(text = track.song.bolden())
        artist.update(text = "${ChatColor.GRAY}${track.artist}")
        songLength.update(text = "${ChatColor.GRAY}${track.length.minuteTimeFormat()}")
    }

    override fun setProgress(time: Short, length: Short, speed: Float) {
        val songLength = (length / speed).toInt().toShort()


        val progress = (time / speed) / songLength.toFloat()
        val position = progress * 1800

        progressIndicator.update(
            modifier = Modifier()
                .size(20, 20)
                .alignStartToStartOf(progressBar)
                .alignTopToTopOf(progressBar)
                .alignBottomToBottomOf(progressBar)
                .margins(start = position.toInt()),
            teleportDuration = 1,
            item = ItemStack(Material.SMOOTH_QUARTZ)
        )

        progressTime.update(text = (time / speed).toInt().toShort().minuteTimeFormat())
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${R.getString(player, S.PLAYER_TITLE.resource())}",
            size = 16,
        )

        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(bottom = 200),
            size = 30,
            text = R.getString(player, S.PLAY_BUTTON.resource()),
            highlightedText = R.getString(player, S.PLAY_BUTTON.resource()).bolden(),
        )

        progressBar = addViewContainer(
            modifier = Modifier()
                .size(900, 10)
                .alignBottomToTopOf(playButton)
                .centerHorizontally()
                .margins(bottom = 100),
            background = Color.fromARGB(255, 190, 190, 190)
        )

        progressIndicator = addItemView(
            modifier = Modifier()
                .size(20, 20)
                .alignStartToStartOf(progressBar)
                .alignTopToTopOf(progressBar)
                .alignBottomToBottomOf(progressBar),
            item = ItemStack(Material.SMOOTH_QUARTZ)
        )

        progressTime = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(progressBar)
                .alignTopToBottomOf(progressBar)
                .margins(top = 30),
            size = 4,
            text = "${ChatColor.GRAY}0:00"
        )

        songLength = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(progressBar)
                .alignTopToBottomOf(progressBar)
                .margins(top = 30),
            size = 4,
            text = "${ChatColor.GRAY}4:20"
        )

        artist = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(progressBar)
                .alignBottomToTopOf(progressBar)
                .margins(bottom = 100),
            size = 12,
            text = "${ChatColor.GRAY}Artist"
        )

        songTitle = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(artist)
                .alignBottomToTopOf(artist),
            size = 14,
            text = "Song Name".bolden()
        )

        lastTrackButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton)
                .alignEndToStartOf(playButton)
                .margins(end = 200),
            size = 20,
            text = R.getString(player, S.LAST_TRACK_BUTTON.resource()),
            highlightedText = R.getString(player, S.LAST_TRACK_BUTTON.resource()).bolden(),
        )

        shuffleButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(lastTrackButton)
                .alignBottomToBottomOf(lastTrackButton)
                .alignEndToStartOf(lastTrackButton)
                .margins(end = 300),
            size = 30,
            text = R.getString(player, S.SHUFFLE_BUTTON.resource()),
            highlightedText = R.getString(player, S.SHUFFLE_BUTTON.resource()).bolden(),
        )

        nextTrackButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton)
                .alignStartToEndOf(playButton)
                .margins(start = 200),
            size = 20,
            text = R.getString(player, S.NEXT_TRACK_BUTTON.resource()),
            highlightedText = R.getString(player, S.NEXT_TRACK_BUTTON.resource()).bolden(),
        )

        loopButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(nextTrackButton)
                .alignBottomToBottomOf(nextTrackButton)
                .alignStartToEndOf(nextTrackButton)
                .margins(start = 300),
            size = 30,
            text = R.getString(player, S.LOOP_BUTTON.resource()),
            highlightedText = R.getString(player, S.LOOP_BUTTON.resource()).bolden(),
        )

        optionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(songTitle)
                .alignEndToEndOf(progressBar),
            size = 20,
            text = R.getString(player, S.OPTIONS_BUTTON.resource())
        )

        contentFeed = addListFeedView(
            modifier = Modifier()
                .size(900, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 100, bottom = 850)
        )
    }

}

interface TrackPresenter: Presenter {
    fun setPlayingState(isPlaying: Boolean)
    fun setPlaylist(playlist: Playlist)
    fun setTrack(track: Track)
    fun setProgress(time: Short, length: Short, speed: Float)
    fun setIsShuffled(isShuffled: Boolean)
    fun setIsLooped(isLooped: Boolean)

    fun setPlayListener(listener: Listener)
    fun setShuffleListener(listener: Listener)
    fun setLoopListener(listener: Listener)
    fun setNextTrackListener(listener: Listener)
    fun setLastTrackListener(listener: Listener)
    fun setOptionsListener(listener: Listener)

    fun setResultsCallback(callback: (Int) -> Unit)
}

class TrackInteractor(
    private val player: Player,
    private val resources: Resources,
    private val presenter: TrackPresenter,
    private val optionsBlock: OptionsBlock,
    private val playlistPickerBlock: PlaylistPickerBlock,
    private val musicPlayerRepository: MusicPlayerRepository,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {
    companion object {
        private const val MUSIC_PLAYER_COLLECTION = "music player"
    }

    private val musicPlayer = musicPlayerRepository.getMusicPlayer(player)

    private var isRouting = false
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()

        val track = musicPlayer.getCurrentTrack()

        presenter.setTrack(track)
        presenter.setIsShuffled(musicPlayer.isShuffled)
        presenter.setIsLooped(musicPlayer.isLooped)

        if (musicPlayer.activeSong != null) {
            isPlaying = musicPlayer.isPlaying()
            presenter.setPlayingState(isPlaying)
            presenter.setTrack(musicPlayer.getCurrentTrack())
            setSongProgressSubscriber(musicPlayer.getSongProgressStream())
        }

        presenter.setShuffleListener(object : Listener {
            override fun invoke() {
                musicPlayer.shuffle()
                presenter.setIsShuffled(musicPlayer.isShuffled)
            }
        })

        presenter.setLoopListener(object : Listener {
            override fun invoke() {
                musicPlayer.loop()
                presenter.setIsLooped(musicPlayer.isLooped)
            }
        })

        presenter.setPlayListener(object : Listener {
            override fun invoke() {
                isPlaying = !isPlaying
                presenter.setPlayingState(isPlaying)
                if (isPlaying) {
                    setSongProgressSubscriber(musicPlayer.playSong())
                } else {
                    musicPlayer.pause()
                }
            }
        })

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

        presenter.setOptionsListener(object : Listener {
            override fun invoke() {


                val optionsList = mutableListOf<OptionRowModel>()

                if (!libraryRepository.isFavorite(track)) {
                    optionsList.add(OptionRowModel("Favorite song"))
                }

                optionsList.add(OptionRowModel("Add to playlist"))
//            optionsList.add(OptionRowModel("Go to artist"))

//            if (track.album != "EP") {
//                optionsList.add(OptionRowModel("Go to album"))
//            }

                val optionsModel = OptionsModel(
                    "Song Options",
                    optionsList,
                )

                optionsBlock.setOptions(optionsModel)

                isRouting = true
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

                            "Go to artist" -> {
                                SearchFactory.search(track.artist.lowercase(), SearchState.ARTIST)
                                    .collectFirst(DudeDispatcher()) {
                                        CoroutineScope(DudeDispatcher()).launch {
                                            val artistSongs = it.filter { it.artist == track.artist }
//                                        artistBlock.setArtist(track.artist, artistSongs)
//                                        routeTo(artistBlock)
                                        }
                                    }
                            }

                            "Go to album" -> {
                                SearchFactory.search(track.artist.lowercase(), SearchState.ARTIST)
                                    .collectFirst(DudeDispatcher()) {
                                        CoroutineScope(DudeDispatcher()).launch {
                                            val albumSongs = it.filter { it.artist == track.artist && it.album == track.album }
//                                        playlistBlock.setPlaylist(Playlist(name = track.album, songs = albumSongs.toMutableList()))
//                                        routeTo(playlistBlock)
                                        }
                                    }
                            }
                        }
                    }
                })



            }
        })

        presenter.setResultsCallback {
            musicPlayer.startSong(it)
        }

        presenter.setPlaylist(musicPlayer.playlist)
    }



    private fun setSongProgressSubscriber(flow: Flow<Short>) {
        clear(MUSIC_PLAYER_COLLECTION)
        flow.collectOn(DudeDispatcher())
            .collectLatest {
                if (it == 0.toShort()) {
                    val track = musicPlayer.getCurrentTrack()
                    presenter.setTrack(track)
                }

                val song = musicPlayer.getCurrentSong() ?: return@collectLatest
                val songLength = song.length / song.speed
                val progress = it / song.speed
                presenter.setProgress(it, song.length, song.speed)
            }.disposeOn(collection = MUSIC_PLAYER_COLLECTION, disposer = this@TrackInteractor)
    }
}
