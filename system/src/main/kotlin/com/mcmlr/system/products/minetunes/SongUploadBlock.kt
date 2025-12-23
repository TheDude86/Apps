package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.block.*
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.products.minetunes.player.MusicPlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class SongUploadBlock @Inject constructor(
    player: Player,
    origin: Origin,
    musicRepository: MusicRepository,
    musicPlayer: MusicPlayer,
): Block(player, origin) {
    private val view = SongUploadViewController(player, origin)
    private val interactor = SongUploadInteractor(player, view, musicRepository, musicPlayer)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class SongUploadViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), SongUploadPresenter {

    private lateinit var songsContainer: ViewContainer
    private lateinit var playCallback: (String) -> Unit

    override fun playSongListener(listener: (String) -> Unit) {
        playCallback = listener
    }

    override fun setSongs(songs: List<String>) {
        songsContainer.updateView(object : ContextListener<ViewContainer>() {
            override fun ViewContainer.invoke() {
                addListFeedView(
                    modifier = Modifier()
                        .size(MATCH_PARENT, MATCH_PARENT)
                        .center(),
                    content = object : ContextListener<ViewContainer>() {
                        override fun ViewContainer.invoke() {
                            songs.forEach { song ->
                                val components = song.split("-")
                                val artist = components[0].trim()
                                val name = components[1]

                                addButtonView(
                                    modifier = Modifier()
                                        .size(WRAP_CONTENT, WRAP_CONTENT)
                                        .alignStartToStartOf(this)
                                        .margins(bottom = 100),
                                    lineWidth = 500,
                                    text = "${ChatColor.GOLD}($artist) $name",
                                    highlightedText = "${ChatColor.GOLD}($artist) $name".bolden(),
                                    callback = object : Listener {
                                        override fun invoke() {
                                            playCallback.invoke(song)
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
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
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Song Upload",
            size = 16,
        )

        songsContainer = addViewContainer(
            modifier = Modifier()
                .size(1000, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 100, bottom = 200)
        )
    }

}

interface SongUploadPresenter: Presenter {
    fun setSongs(songs: List<String>)

    fun playSongListener(listener: (String) -> Unit)
}

class SongUploadInteractor(
    private val player: Player,
    private val presenter: SongUploadPresenter,
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer,
): Interactor(presenter) {

    override fun onCreate() {
        super.onCreate()

        presenter.setSongs(musicRepository.songList())

        presenter.playSongListener { song ->
            val s = musicRepository.loadSong(song) ?: return@playSongListener
            musicPlayer.playSong(s, player)
        }
    }
}
