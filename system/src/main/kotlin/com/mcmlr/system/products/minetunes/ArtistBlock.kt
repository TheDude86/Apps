package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
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
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.titlecase
import com.mcmlr.system.products.minetunes.player.MusicPlayer
import com.mcmlr.system.products.minetunes.player.Playlist
import com.mcmlr.system.products.minetunes.player.Track
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class ArtistBlock @Inject constructor(
    player: Player,
    origin: Origin,
    musicPlayer: MusicPlayer,
): Block(player, origin) {
    private val view = ArtistViewController(player, origin)
    private val interactor = ArtistInteractor(player, view, musicPlayer)

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

    private lateinit var artist: TextView
    private lateinit var stats: TextView
    private lateinit var playButton: ButtonView
    private lateinit var contentFeed: ListFeedView

    override fun setPlayingState(isPlaying: Boolean) {
        playButton.update(text = R.getString(player, if (isPlaying) S.PLAY_BUTTON.resource() else S.PAUSE_BUTTON.resource()))
    }

    override fun setPlayButtonListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setArtistInfo(artist: String, stats: String) {
        this.artist.update(text = "${ChatColor.BOLD}$artist")
        this.stats.update(text = "${ChatColor.GRAY}$stats")
    }

    override fun setContent(popularTracks: List<Track>, albums: List<String>, showMoreTracks: Boolean) {
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

                    popularTracks.forEach { track ->
                        addViewContainer(
                            modifier = Modifier()
                                .size(MATCH_PARENT, 75),
                            clickable = true,
                            listener = object : Listener {
                                override fun invoke() {

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
                                        text = R.getString(player, S.ARTIST_SEE_ALL_SONGS_BUTTON.resource())
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

        artist = addTextView(
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
                .alignTopToBottomOf(artist)
                .alignStartToStartOf(artist)
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
                .margins(top = 100, bottom = 300)
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
    }

}

interface ArtistPresenter: Presenter {

    fun setArtistInfo(artist: String, stats: String)

    fun setContent(popularTracks: List<Track>, albums: List<String>, showMoreTracks: Boolean)

    fun setPlayButtonListener(listener: Listener)

    fun setPlayingState(isPlaying: Boolean)
}

class ArtistInteractor(
    private val player: Player,
    private val presenter: ArtistPresenter,
    private val musicPlayer: MusicPlayer,
): Interactor(presenter) {

    private var artist: String = ""
    private var tracks = listOf<Track>()
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        val albumsSet = mutableSetOf<String>()
        tracks.forEach { if (it.album != "EP") albumsSet.add(it.album) }

        val albumCount = albumsSet.size
        val trackCount = tracks.size

        presenter.setArtistInfo(artist, R.getString(player, S.ARTIST_STATS_PLACEHOLDER.resource(), trackCount, albumCount))

        val popularTracks = tracks.sortedBy { it.plays }
        val topFiveTracks = if (popularTracks.size > 5) popularTracks.subList(0, 5) else popularTracks

        presenter.setContent(topFiveTracks, albumsSet.toList(), popularTracks.size > 5)
        musicPlayer.updatePlaylist(Playlist(popularTracks))

        presenter.setPlayButtonListener(object : Listener {
            override fun invoke() {
                if (isPlaying) {
                    musicPlayer.pause()
                } else {
                    musicPlayer.play()
                }

                presenter.setPlayingState(isPlaying)
                isPlaying = !isPlaying
            }
        })
    }

    fun setArtist(artist: String, tracks: List<Track>) {
        this.artist = artist
        this.tracks = tracks
    }
}
