package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.bolden
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import com.mcmlr.blocks.core.minuteTimeFormat
import com.mcmlr.system.products.minetunes.nbs.data.Song
import com.mcmlr.system.products.minetunes.player.MusicPlayer
import com.mcmlr.system.products.minetunes.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class TrackBlock @Inject constructor(
    player: Player,
    origin: Origin,
    resources: Resources,
    musicRepository: MusicRepository,
    musicPlayer: MusicPlayer,
): Block(player, origin) {
    private val view = TrackViewController(player, origin)
    private val interactor = TrackInteractor(player, resources, view, musicRepository, musicPlayer)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor

    fun setTrack(track: Track) {
        view.setTitle(track.song)
        interactor.setTrack(track)
    }
}

class TrackViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), TrackPresenter {

    private var title: String = "Song"

    private lateinit var playButton: ButtonView
    private lateinit var lastTrackButton: ButtonView
    private lateinit var shuffleButton: ButtonView
    private lateinit var nextTrackButton: ButtonView
    private lateinit var loopButton: ButtonView
    private lateinit var progressBar: ViewContainer
    private lateinit var progressIndicator: ItemView
    private lateinit var progressTime: TextView
    private lateinit var songLength: TextView
    private lateinit var artist: TextView
    private lateinit var songTitle: TextView

    fun setTitle(title: String) {
        this.title = title
    }

    override fun addPlayListener(listener: Listener) {
        playButton.addListener(listener)
    }

    override fun setPlayingState(isPlaying: Boolean) {
        val buttonText = if (isPlaying) R.getString(player, S.PAUSE_BUTTON.resource()) else R.getString(player, S.PLAY_BUTTON.resource())
        playButton.update(text = buttonText, highlightedText = "${ChatColor.BOLD}$buttonText")
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
//        log(Log.ERROR, "progress=$progress position=$position toInt=${position.toInt()}")

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

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .x(-500)
                .margins(bottom = 300),
            text = R.getString(player, S.HOME_BUTTON.resource()),
            highlightedText = R.getString(player, S.HOME_BUTTON.resource()).bolden(),
        )

        val searchButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(bottom = 300),
            text = R.getString(player, S.SEARCH_BUTTON.resource()),
            highlightedText = R.getString(player, S.SEARCH_BUTTON.resource()).bolden(),
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

        playButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(searchButton)
                .centerHorizontally()
                .margins(bottom = 100),
            size = 30,
            text = R.getString(player, S.PLAY_BUTTON.resource()),
            highlightedText = R.getString(player, S.PLAY_BUTTON.resource()).bolden(),
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
    }

}

interface TrackPresenter: Presenter {
    fun addPlayListener(listener: Listener)

    fun setPlayingState(isPlaying: Boolean)

    fun setTrack(track: Track)

    fun setProgress(time: Short, length: Short, speed: Float)
}

class TrackInteractor(
    private val player: Player,
    private val resources: Resources,
    private val presenter: TrackPresenter,
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer,
): Interactor(presenter) {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://firebasestorage.googleapis.com/v0/b/mc-apps-9477a.firebasestorage.app/o/apps%2Fminetunes%2F/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DownloadService = retrofit.create(DownloadService::class.java)
    var isPlaying = false

    private var track: Track? = null

    fun setTrack(track: Track) {
        this.track = track
    }

    override fun onCreate() {
        super.onCreate()

        val track = track ?: return
        presenter.setTrack(track)

        presenter.addPlayListener(object : Listener {
            override fun invoke() {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = service.download("https://firebasestorage.googleapis.com/v0/b/mc-apps-9477a.firebasestorage.app/o/apps%2Fminetunes%2F${track.downloadUrl}")

                    var input: InputStream? = null
                    try {
                        input = response.body()?.byteStream() ?: return@launch

                        val fileName = "${track.artist.replace("/", "%!")} - ${track.song}.nbs"
                        val fos = File(resources.dataFolder(), "${File.separator}Mine Tunes${File.separator}Songs${File.separator}$fileName")
                        fos.outputStream().use { output ->
                            val buffer = ByteArray(4 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }

                        val song = musicRepository.loadSong(fileName) ?: return@launch

                        CoroutineScope(DudeDispatcher()).launch {
                            isPlaying = !isPlaying

                            if (isPlaying) {
                                musicPlayer.playSong(song, player)
                                    .collectOn(DudeDispatcher())
                                    .collectLatest {
                                        presenter.setProgress(it, song.length, song.speed)
                                    }
                                    .disposeOn(disposer = this@TrackInteractor)
                            } else {
                                musicPlayer.pauseSong()
                            }

                            presenter.setPlayingState(isPlaying)
                        }

                    } catch (e: Throwable) {
                        println("Error: $e")
                    } finally {
                        input?.close()
                    }
                }
            }
        })
    }
}
