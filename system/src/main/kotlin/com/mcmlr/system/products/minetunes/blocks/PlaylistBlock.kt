package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
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
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.blocks.core.minuteTimeFormat
import com.mcmlr.system.ConfirmationBlock
import com.mcmlr.system.ConfirmationBlock.Companion.CONFIRMATION_BUNDLE_KEY
import com.mcmlr.system.ConfirmationModel
import com.mcmlr.system.ConfirmationResponse
import com.mcmlr.system.OptionRowModel
import com.mcmlr.system.OptionsBlock
import com.mcmlr.system.OptionsBlock.Companion.OPTION_BUNDLE_KEY
import com.mcmlr.system.OptionsModel
import com.mcmlr.system.products.minetunes.LibraryRepository
import com.mcmlr.system.products.minetunes.LibraryRepository.Companion.FAVORITES_UUID
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.SearchFactory
import com.mcmlr.system.products.minetunes.SearchState
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

class PlaylistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    optionsBlock: OptionsBlock,
    playlistPickerBlock: PlaylistPickerBlock,
    createPlaylistBlock: CreatePlaylistBlock,
    confirmationBlock: ConfirmationBlock,
    musicPlayerBlock: MusicPlayerBlock,
    orderPlaylistBlock: OrderPlaylistBlock,
    musicPlayerRepository: MusicPlayerRepository,
    libraryRepository: LibraryRepository,
): Block(player, origin) {
    private val view = PlaylistViewController(player, origin)
    private val interactor = PlaylistInteractor(
        player,
        view,
        optionsBlock,
        playlistPickerBlock,
        createPlaylistBlock,
        confirmationBlock,
        musicPlayerBlock,
        orderPlaylistBlock,
        musicPlayerRepository,
        libraryRepository
    )

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setPlaylist(playlist: Playlist) {
        interactor.setPlaylist(playlist)
    }
}

class PlaylistViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), PlaylistPresenter {

    private lateinit var title: TextView
    private lateinit var addButton: ButtonView
    private lateinit var optionsButton: ButtonView
    private lateinit var playButton: ButtonView
    private lateinit var musicPlayerContainer: ViewContainer
    
    private lateinit var contentFeed: ListFeedView

    private lateinit var optionsCallback: (Track) -> Unit
    private lateinit var resultCallback: (Int) -> Unit

    override fun getMusicPlayerContainer(): ViewContainer = musicPlayerContainer

    override fun setOptionsCallback(callback: (Track) -> Unit) {
        optionsCallback = callback
    }

    override fun setResultsCallback(callback: (Int) -> Unit) {
        resultCallback = callback
    }

    override fun setPlayListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setOptionsListener(listener: Listener) {
        optionsButton.addListener(listener)
    }

    override fun setBackListener(listener: Listener) {
        backButton?.addListener(listener)
    }

    override fun setAddListener(listener: Listener) {
//        addButton.addListener(listener)
    }

    override fun setPlayingState(isPlaying: Boolean) {
        val icon = if (isPlaying) S.PAUSE_BUTTON else S.PLAY_BUTTON
        playButton.update(text = R.getString(player, icon.resource()))
    }

    override fun setPlaylistTitle(title: String) {
        this.title.update(text = "${ChatColor.BOLD}${ChatColor.UNDERLINE}${ChatColor.ITALIC}$title")
    }

    override fun setPlaylist(playlist: Playlist) {
        contentFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                playlist.songs.forEachIndexed { index, track ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                resultCallback.invoke(index)
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
                                        .alignTopToBottomOf(title)
                                        .margins(top = 30),
                                    size = 4,
                                    maxLength = 600,
                                    text = "${ChatColor.GRAY}${track.length.minuteTimeFormat()}"
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
        })

    }

    override fun createView() {
        super.createView()
        title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.PLAYLIST_TITLE.resource()),
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

        optionsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignEndToEndOf(contentFeed)
                .margins(bottom = 50),
            size = 16,
            text = R.getString(player, S.OPTIONS_BUTTON.resource())
        )

//        addButton = addButtonView(
//            modifier = Modifier()
//                .size(WRAP_CONTENT, WRAP_CONTENT)
//                .alignBottomToTopOf(contentFeed)
//                .alignEndToStartOf(optionsButton)
//                .margins(bottom = 50, end = 50),
//            size = 16,
//            text = R.getString(player, S.ADD_BUTTON.resource())
//        )

        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(contentFeed)
                .alignStartToStartOf(contentFeed)
                .margins(bottom = 50),
            size = 16,
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

interface PlaylistPresenter: Presenter {
    fun setPlaylistTitle(title: String)
    fun setPlaylist(playlist: Playlist)
    fun setPlayingState(isPlaying: Boolean)

    fun setOptionsCallback(callback: (Track) -> Unit)
    fun setResultsCallback(callback: (Int) -> Unit)

    fun setAddListener(listener: Listener)
    fun setOptionsListener(listener: Listener)
    fun setPlayListener(listener: Listener)
    fun setBackListener(listener: Listener)

    fun getMusicPlayerContainer(): ViewContainer
}

class PlaylistInteractor(
    private val player: Player,
    private val presenter: PlaylistPresenter,
    private val optionsBlock: OptionsBlock,
    private val playlistPickerBlock: PlaylistPickerBlock,
    private val createPlaylistBlock: CreatePlaylistBlock,
    private val confirmationBlock: ConfirmationBlock,
    private val musicPlayerBlock: MusicPlayerBlock,
    private val orderPlaylistBlock: OrderPlaylistBlock,
    private val musicPlayerRepository: MusicPlayerRepository,
    private val libraryRepository: LibraryRepository,
): Interactor(presenter) {

    private val musicPlayer = musicPlayerRepository.getMusicPlayer(player)
    private var playlist: Playlist? = null
    private var isRouting = false
    private var playlistUpdated = false

    fun setPlaylist(playlist: Playlist) {
        this.playlist = playlist
    }

    override fun onCreate() {
        super.onCreate()

        val playlist = playlist ?: return

        presenter.setPlaylist(playlist)
        presenter.setPlaylistTitle(playlist.name ?: "Untitled Playlist")
        musicPlayer.setDefaultPlaylist(playlist)

        attachChild(musicPlayerBlock, presenter.getMusicPlayerContainer())

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

        presenter.setOptionsCallback { track ->
            val optionsList = mutableListOf<OptionRowModel>()

            if (!libraryRepository.isFavorite(track)) {
                optionsList.add(OptionRowModel("Favorite song"))
            }

            optionsList.add(OptionRowModel("Add to playlist"))
//            optionsList.add(OptionRowModel("Go to artist"))

//            if (track.album != "EP") {
//                optionsList.add(OptionRowModel("Go to album"))
//            }

            if (playlist.uuid != null) {
                optionsList.add(OptionRowModel("Remove from playlist"))
            }

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

                        "Remove from playlist" -> {
                            val uuid = playlist.uuid ?: return
                            if (libraryRepository.removeFromPlaylist(track, uuid)) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.GREEN}${ChatColor.ITALIC}Song removed from ${playlist.name?.bolden()}${ChatColor.GREEN}!"))
                                presenter.setPlaylist(playlist)
                            } else {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.RED}${ChatColor.ITALIC}Something went wrong..."))
                            }
                        }
                    }
                }
            })
        }

        presenter.setResultsCallback {
            updatePlaylist()
            musicPlayer.startSong(it)
        }

        presenter.setOptionsListener(object : Listener {
            override fun invoke() {
                val optionsList = mutableListOf(
                    OptionRowModel("Reorder Songs"),
                    OptionRowModel("Edit Playlist"),
                )

                if (playlist.uuid != FAVORITES_UUID) {
                    optionsList.add(OptionRowModel("Delete Playlist"))
                }

                val options = OptionsModel(
                    "Playlist Options",
                    optionsList,
                )

                optionsBlock.setOptions(options)
                routeTo(optionsBlock, object : RouteToCallback {
                    override fun invoke(bundle: Bundle) {
                        val option = bundle.getData<String>(OPTION_BUNDLE_KEY)

                        when (option) {
                            "Edit Playlist" -> {
                                createPlaylistBlock.setEditingPlaylist(playlist)
                                routeTo(createPlaylistBlock)
                            }

                            "Delete Playlist" -> {
                                val confirmationModel = ConfirmationModel("Are you sure you want to delete this playlist?")
                                confirmationBlock.setConfirmationModel(confirmationModel)
                                routeTo(confirmationBlock, object : RouteToCallback {
                                    override fun invoke(bundle: Bundle) {
                                        val response = bundle.getData<ConfirmationResponse>(CONFIRMATION_BUNDLE_KEY) ?: return
                                        if (response == ConfirmationResponse.ACCEPT) {
                                            val uuid = playlist.uuid ?: return
                                            libraryRepository.deletePlaylist(uuid)?.invokeOnCompletion {
                                                CoroutineScope(DudeDispatcher()).launch {
                                                    routeBack()
                                                }
                                            }
                                        }
                                    }
                                })
                            }

                            "Reorder Songs" -> {
                                orderPlaylistBlock.setPlaylist(playlist)
                                routeTo(orderPlaylistBlock)
                            }
                        }
                    }
                })
            }
        })

        presenter.setBackListener(object : Listener {
            override fun invoke() {
                playlistUpdated = false
            }
        })
    }

    private fun updatePlaylist() {
        val playlist = playlist ?: return
        if (!playlistUpdated) {
            musicPlayer.updatePlaylist(playlist)
            libraryRepository.updateLastPlayed(playlist)
            playlistUpdated = true
        }
    }
}
