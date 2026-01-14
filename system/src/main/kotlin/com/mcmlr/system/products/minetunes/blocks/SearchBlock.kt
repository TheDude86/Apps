package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.app.RouteToCallback
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.TextListener
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ListFeedView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextInputView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectFirst
import com.mcmlr.blocks.core.delay
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
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import okhttp3.OkHttpClient
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class SearchBlock @Inject constructor(
    player: Player,
    origin: Origin,
    trackBlock: TrackBlock,
    artistBlock: ArtistBlock,
    optionsBlock: OptionsBlock,
    playlistPickerBlock: PlaylistPickerBlock,
    playlistBlock: PlaylistBlock,
    libraryRepository: LibraryRepository,
    musicPlayerRepository: MusicPlayerRepository,
): Block(player, origin) {
    private val view = SearchViewController(player, origin)
    private val interactor = SearchInteractor(
        player,
        view,
        trackBlock,
        artistBlock,
        optionsBlock,
        playlistPickerBlock,
        playlistBlock,
        libraryRepository,
        musicPlayerRepository
    )

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SearchViewController(
    private val player: Player,
    origin: Origin,
): ViewController(player, origin), SearchPresenter {

    private lateinit var searchBar: TextInputView
    private lateinit var resultsFeed: ListFeedView
    private lateinit var songsButton: ButtonView
    private lateinit var artistsButton: ButtonView

    override fun setSongsListener(listener: Listener) {
        songsButton.addListener(listener)
    }

    override fun setArtistsListener(listener: Listener) {
        artistsButton.addListener(listener)
    }

    override fun setSearchState(isSongSearch: Boolean) {
        resultsFeed.updateView()

        if (isSongSearch) {
            songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()).bolden())
            artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
        } else {
            songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
            artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()).bolden())
        }
    }

    override fun setArtistsSearchResults(results: List<String>, resultCallback: (String) -> Unit) {
        resultsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (results.isEmpty()) {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .center(),
                                    size = 6,
                                    text = "${ChatColor.GRAY}${ChatColor.ITALIC}No results found..."
                                )
                            }
                        }
                    )
                }

                results.forEach { artist ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                resultCallback.invoke(artist)
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
                                    text = artist.bolden(),
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setSongsSearchResults(results: List<Track>, optionsCallback: (Track) -> Unit, resultCallback: (Track) -> Unit) {
        resultsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                if (results.isEmpty()) {
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        background = Color.fromARGB(0, 0, 0, 0),
                        content = object : ContextListener<ViewContainer>() {
                            override fun ViewContainer.invoke() {

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .center(),
                                    size = 6,
                                    text = "${ChatColor.GRAY}${ChatColor.ITALIC}No results found..."
                                )
                            }
                        }
                    )
                }

                results.forEach { track ->
                    addViewContainer(
                        modifier = Modifier()
                            .size(MATCH_PARENT, 75),
                        clickable = true,
                        listener = object : Listener {
                            override fun invoke() {
                                resultCallback.invoke(track)
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
                                    lineWidth = 1200,
                                    text = track.song.bolden(),
                                )

                                addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(title)
                                        .alignTopToBottomOf(title),
                                    size = 4,
                                    lineWidth = 1200,
                                    text = "${ChatColor.GRAY}${track.artist}"
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

    override fun addSearchListener(listener: TextListener) {
        searchBar.addTextChangedListener(listener)
    }

    override fun createView() {
        searchBar = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .centerHorizontally(),
            text = R.getString(player, S.SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        resultsFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 150)
        )

        songsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(resultsFeed)
                .x(-200),
            size = 5,
            text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()),
        )

        artistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .alignBottomToTopOf(resultsFeed)
                .x(200),
            size = 5,
            text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()),
        )
    }

}

interface SearchPresenter: Presenter {
    fun addSearchListener(listener: TextListener)

    fun setSongsSearchResults(results: List<Track>, optionsCallback: (Track) -> Unit, resultCallback: (Track) -> Unit)

    fun setArtistsSearchResults(results: List<String>, resultCallback: (String) -> Unit)

    fun setSongsListener(listener: Listener)

    fun setArtistsListener(listener: Listener)

    fun setSearchState(isSongSearch: Boolean)
}

class SearchInteractor(
    private val player: Player,
    private val presenter: SearchPresenter,
    private val trackBlock: TrackBlock,
    private val artistBlock: ArtistBlock,
    private val optionsBlock: OptionsBlock,
    private val playlistPickerBlock: PlaylistPickerBlock,
    private val playlistBlock: PlaylistBlock,
    private val libraryRepository: LibraryRepository,
    musicPlayerRepository: MusicPlayerRepository,
): Interactor(presenter) {

    private val musicPlayer = musicPlayerRepository.getMusicPlayer(player)

    private var searchState = SearchState.SONG
    private var results: List<Track> = listOf()
    private var isRouting = false

    override fun onCreate() {
        super.onCreate()

        if (isRouting) {
            isRouting = false
            return
        }

        //TODO: Fix not updating synchronously
        delay(duration = 50.milliseconds, callback = object : Listener {
            override fun invoke() {
                setResults()
            }
        })

        presenter.setSearchState(true)

        presenter.setSongsListener(object : Listener {
            override fun invoke() {
                searchState = SearchState.SONG
                presenter.setSearchState(true)
            }
        })

        presenter.setArtistsListener(object : Listener {
            override fun invoke() {
                searchState = SearchState.ARTIST
                presenter.setSearchState(false)
            }
        })

        presenter.addSearchListener(object : TextListener {
            override fun invoke(text: String) {
                SearchFactory.search(text, searchState)
                    .collectFirst(DudeDispatcher()) {
                        results = it

                        CoroutineScope(DudeDispatcher()).launch {
                            setResults()
                        }
                    }
            }
        })
    }

    private fun setResults() {
        if (searchState == SearchState.SONG) {
            presenter.setSongsSearchResults(results, { track ->
                val optionsList = mutableListOf<OptionRowModel>()

                if (!libraryRepository.isFavorite(track)) {
                    optionsList.add(OptionRowModel("Favorite song"))
                }

                optionsList.add(OptionRowModel("Add to playlist"))
                optionsList.add(OptionRowModel("Go to artist"))

                if (track.album != "EP") {
                    optionsList.add(OptionRowModel("Go to album"))
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
                                            artistBlock.setArtist(track.artist, artistSongs)
                                            routeTo(artistBlock)
                                        }
                                    }
                            }

                            "Go to album" -> {
                                SearchFactory.search(track.artist.lowercase(), SearchState.ARTIST)
                                    .collectFirst(DudeDispatcher()) {
                                        CoroutineScope(DudeDispatcher()).launch {
                                            val albumSongs = it.filter { it.artist == track.artist && it.album == track.album }

                                            playlistBlock.setPlaylist(Playlist(name = track.album, songs = albumSongs.toMutableList()))
                                            routeTo(playlistBlock)
                                        }
                                    }
                            }
                        }
                    }
                })

            }) {
                musicPlayer.updatePlaylist(Playlist(songs = mutableListOf(it)))
                musicPlayer.startPlaylist()
                routeTo(trackBlock)
            }
        } else {
            val artistSet = hashSetOf<String>()
            results.forEach { artistSet.add(it.artist) }

            CoroutineScope(DudeDispatcher()).launch {
                presenter.setArtistsSearchResults(artistSet.toList()) { artist ->
                    val artistSongs = results.filter { it.artist == artist }
                    artistBlock.setArtist(artist, artistSongs)
                    routeTo(artistBlock)
                }
            }
        }
    }
}
