package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.blocks.core.minuteTimeFormat
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import com.mcmlr.system.products.minetunes.S
import com.mcmlr.system.products.minetunes.player.MusicPlayerAction
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.flow.Flow
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class MusicPlayerBlock @Inject constructor(
    player: Player,
    origin: Origin,
    trackBlock: TrackBlock,
    musicPlayerRepository: MusicPlayerRepository,
): Block(player, origin) {
    private val view = MusicPlayerViewController(player, origin)
    private val interactor = MusicPlayerInteractor(player, view, trackBlock, musicPlayerRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class MusicPlayerViewController(
    private val player: Player,
    origin: Origin,
): ViewController(player, origin), MusicPlayerPresenter {
    private lateinit var songName: TextView
    private lateinit var artist: TextView
    private lateinit var songProgress: TextView
    private lateinit var playButton: ButtonView
    private lateinit var lastTrackButton: ButtonView
    private lateinit var shuffleButton: ButtonView
    private lateinit var nextTrackButton: ButtonView
    private lateinit var loopButton: ButtonView
    private lateinit var expandButton: ButtonView

    override fun setPlayListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setExpandListener(listener: Listener) {
        expandButton.addListener(listener)
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

    override fun setProgress(songProgress: String) {
        this.songProgress.update(text = "${ChatColor.GRAY}$songProgress")
    }

    override fun setPlayingState(isPlaying: Boolean) {
        val icon = if (isPlaying) S.PAUSE_BUTTON else S.PLAY_BUTTON
        playButton.update(text = R.getString(player, icon.resource()))
    }

    override fun setPlayingTrack(track: Track) {
        songName.update(text = track.song.bolden())
        artist.update(text = "${ChatColor.GRAY}${track.artist}")
    }

    override fun createView() {
        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .center(),
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
                .alignStartToStartOf(this)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton),
            size = 18,
            text = R.getString(player, S.SHUFFLE_BUTTON.resource())
        )

        expandButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToEndOf(this)
                .alignTopToTopOf(playButton)
                .alignBottomToBottomOf(playButton),
            size = 18,
            text = R.getString(player, S.EXPAND_BUTTON.resource())
        )

        loopButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignEndToStartOf(expandButton)
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
            lineWidth = 800,
            size = 6,
            text = ""
        )

        songName = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(artist)
                .alignStartToStartOf(artist),
            lineWidth = 800,
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

interface MusicPlayerPresenter: Presenter {
    fun setPlayingState(isPlaying: Boolean)
    fun setPlayingTrack(track: Track)
    fun setProgress(songProgress: String)
    fun setIsShuffled(isShuffled: Boolean)
    fun setIsLooped(isLooped: Boolean)

    fun setPlayListener(listener: Listener)
    fun setShuffleListener(listener: Listener)
    fun setLoopListener(listener: Listener)
    fun setExpandListener(listener: Listener)
    fun setNextTrackListener(listener: Listener)
    fun setLastTrackListener(listener: Listener)
}

class MusicPlayerInteractor(
    private val player: Player,
    private val presenter: MusicPlayerPresenter,
    private val trackBlock: TrackBlock,
    private val musicPlayerRepository: MusicPlayerRepository,
): Interactor(presenter) {
    companion object {
        private const val MUSIC_PLAYER_COLLECTION = "music player"
    }

    private val musicPlayer = musicPlayerRepository.getMusicPlayer(player)
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()

        presenter.setIsShuffled(musicPlayer.isShuffled)
        presenter.setIsLooped(musicPlayer.isLooped)

        if (musicPlayer.activeSong != null) {
            isPlaying = musicPlayer.isPlaying()
            presenter.setPlayingState(isPlaying)
            presenter.setPlayingTrack(musicPlayer.getCurrentTrack())
            setSongProgressSubscriber(musicPlayer.getSongProgressStream())
        }

        musicPlayer.getActionStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                when (it) {
                    MusicPlayerAction.PLAY,
                    MusicPlayerAction.NEXT,
                    MusicPlayerAction.LAST -> {
                        isPlaying = true
                        presenter.setPlayingTrack(musicPlayer.getCurrentTrack())
                        presenter.setPlayingState(true)

                        val stream = musicPlayer.getSongProgressStream()
                        setSongProgressSubscriber(stream)
                    }
                    MusicPlayerAction.STOP -> {
                        isPlaying = false
                        presenter.setPlayingState(false)
                    }
                }

            }.disposeOn(disposer = this)

        presenter.setShuffleListener(object : Listener {
            override fun invoke() {
                if (!isPlaylistLoaded()) return

                musicPlayer.shuffle()
                presenter.setIsShuffled(musicPlayer.isShuffled)
            }
        })

        presenter.setLoopListener(object : Listener {
            override fun invoke() {
                if (!isPlaylistLoaded()) return

                musicPlayer.loop()
                presenter.setIsLooped(musicPlayer.isLooped)
            }
        })

        presenter.setPlayListener(object : Listener {
            override fun invoke() {
                if (!isPlaylistLoaded()) return

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
                if (!isPlaylistLoaded()) return

                musicPlayer.goToNextSong()
            }
        })

        presenter.setLastTrackListener(object : Listener {
            override fun invoke() {
                if (!isPlaylistLoaded()) return

                musicPlayer.goToLastSong()
            }
        })

        presenter.setExpandListener(object : Listener {
            override fun invoke() {
                if (!isPlaylistLoaded()) return

                routeTo(trackBlock)
            }
        })
    }

    private fun isPlaylistLoaded(): Boolean {
        val isLoaded = musicPlayer.isPlaylistLoaded()
        if (!isLoaded) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("${ChatColor.ITALIC}No playlist selected!"))
        }

        return isLoaded
    }

    private fun setSongProgressSubscriber(flow: Flow<Short>) {
        clear(MUSIC_PLAYER_COLLECTION)
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
            }.disposeOn(collection = MUSIC_PLAYER_COLLECTION, disposer = this@MusicPlayerInteractor)
    }
}
