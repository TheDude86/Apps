package com.mcmlr.system.products.minetunes

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.mcmlr.blocks.api.app.R
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
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class SearchBlock @Inject constructor(
    player: Player,
    origin: Origin,
    trackBlock: TrackBlock,
    artistBlock: ArtistBlock,
): Block(player, origin) {
    private val view = SearchViewController(player, origin)
    private val interactor = SearchInteractor(view, trackBlock, artistBlock)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SearchViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), SearchPresenter {

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
        if (isSongSearch) {
            songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()).bolden())
            artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()))
        } else {
            songsButton.update(text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()))
            artistsButton.update(text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()).bolden())
        }

        resultsFeed.updateView()
    }

    override fun setArtistsSearchResults(results: List<String>, resultCallback: (String) -> Unit) {
        resultsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
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
                                    maxLength = 600,
                                    text = artist.bolden(),
                                )
                            }
                        }
                    )
                }
            }
        })
    }

    override fun setSongsSearchResults(results: List<Track>, resultCallback: (Track) -> Unit) {
        resultsFeed.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
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
                                    maxLength = 600,
                                    text = track.song.bolden(),
                                )

                                val artist = addTextView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(title)
                                        .alignTopToBottomOf(title)
                                        .margins(top = 30),
                                    size = 4,
                                    maxLength = 600,
                                    text = "${ChatColor.GRAY}${track.artist}"
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
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.SEARCH_TITLE.resource()),
            size = 16,
        )

        searchBar = addTextInputView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(title)
                .centerHorizontally()
                .margins(top = 75),
            text = R.getString(player, S.SEARCH_PLACEHOLDER.resource()),
            highlightedText = R.getString(player, S.SEARCH_PLACEHOLDER.resource()).bolden(),
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(-500)
                .margins(bottom = 300),
            text = R.getString(player, S.HOME_BUTTON.resource()),
            highlightedText = R.getString(player, S.HOME_BUTTON.resource()).bolden(),
        )

        val searchButton = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(bottom = 300),
            text = R.getString(player, S.SEARCH_BUTTON.resource()).bolden(),
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(500)
                .margins(bottom = 300),
            text = R.getString(player, S.MUSIC_BUTTON.resource()),
            highlightedText = R.getString(player, S.MUSIC_BUTTON.resource()).bolden(),
        )

        songsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .x(-200)
                .margins(top = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_SONGS_BUTTON.resource()),
        )

        artistsButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToBottomOf(searchBar)
                .x(200)
                .margins(top = 50),
            size = 5,
            text = R.getString(player, S.SEARCH_ARTISTS_BUTTON.resource()),
        )

        resultsFeed = addListFeedView(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(songsButton)
                .alignBottomToTopOf(searchButton)
                .centerHorizontally()
                .margins(top = 50, bottom = 50)
        )
    }

}

interface SearchPresenter: Presenter {
    fun addSearchListener(listener: TextListener)

    fun setSongsSearchResults(results: List<Track>, resultCallback: (Track) -> Unit)

    fun setArtistsSearchResults(results: List<String>, resultCallback: (String) -> Unit)

    fun setSongsListener(listener: Listener)

    fun setArtistsListener(listener: Listener)

    fun setSearchState(isSongSearch: Boolean)
}

class SearchInteractor(
    private val presenter: SearchPresenter,
    private val trackBlock: TrackBlock,
    private val artistBlock: ArtistBlock,
): Interactor(presenter) {

    private val client = OkHttpClient()
    private var searchState = SearchState.SONG

    override fun onCreate() {
        super.onCreate()

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
                CoroutineScope(Dispatchers.IO).launch {
                    val url = if (searchState == SearchState.SONG) "https://searchsong-l5cijtvgrq-uc.a.run.app" else "https://searchartist-l5cijtvgrq-uc.a.run.app"

                    val request = Request.Builder().url("$url/?search=$text").build()
                    val response = client.newCall(request).execute()
                    val body = response.body?.string() ?: return@launch
                    val data = JsonParser.parseString(body).asJsonObject

                    val results = data.get("results").asJsonObject.keySet().map {
                        val result = data.get("results").asJsonObject.get(it).asJsonObject
                        Gson().fromJson(result, Track::class.java)
                    }

                    if (searchState == SearchState.SONG) {
                        CoroutineScope(DudeDispatcher()).launch {
                            presenter.setSongsSearchResults(results) {
                                trackBlock.setTrack(it)
                                routeTo(trackBlock)
                            }
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


                    response.close()

                }
            }
        })
    }
}

private enum class SearchState {
    SONG,
    ARTIST,
}

