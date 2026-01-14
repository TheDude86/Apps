package com.mcmlr.system.products.minetunes.blocks

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.system.products.minetunes.MusicPlayerRepository
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max

class VolumeBlock @Inject constructor(
    player: Player,
    origin: Origin,
    musicPlayerRepository: MusicPlayerRepository,
): Block(player, origin) {
    private val view = VolumeViewController(player, origin)
    private val interactor = VolumeInteractor(player, view, musicPlayerRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class VolumeViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), VolumePresenter {

    private lateinit var volume: TextView
    private lateinit var volumeBar: ViewContainer
    private lateinit var volumePercentage: ViewContainer

    private lateinit var volumeCallback: (Float) -> Unit

    override fun setVolume(volume: Float) {
        this.volume.update(text = "${(100 * volume).toInt()}%")
        volumePercentage.update(
            modifier = Modifier()
                .size(max(9, (900f * volume).toInt()), 30)
                .alignStartToStartOf(volumeBar)
                .alignTopToTopOf(volumeBar)
                .alignBottomToBottomOf(volumeBar),
        )
    }

    override fun setVolumeCallback(callback: (Float) -> Unit) {
        volumeCallback = callback
    }

    override fun createView() {
        super.createView()
        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Volume",
            size = 16,
        )


        volumeBar = addViewContainer(
            modifier = Modifier()
                .size(900, 30)
                .centerVertically()
                .margins(top = 100),
            background = Color.fromARGB(255, 190, 190, 190),
        )

        volumePercentage = addViewContainer(
            modifier = Modifier()
                .size(1, 30)
                .alignStartToStartOf(volumeBar)
                .alignTopToTopOf(volumeBar)
                .alignBottomToBottomOf(volumeBar),
            height = 1,
            teleportDuration = 1,
            background = Color.YELLOW,
        )

        volume = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignBottomToTopOf(volumeBar)
                .alignStartToStartOf(volumeBar)
                .alignEndToEndOf(volumeBar)
                .margins(bottom = 50),
            size = 20,
            text = "100%",
        )

        val zeroVolumeButton = addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToBottomOf(volumeBar)
                .margins(top = 30),
            size = 6,
            text = "0%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 360),
            size = 6,
            text = "10%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.1f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 720),
            size = 6,
            text = "20%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.2f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 1080),
            size = 6,
            text = "30%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.3f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 1440),
            size = 6,
            text = "40%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.4f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 1800),
            size = 6,
            text = "50%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.5f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 2160),
            size = 6,
            text = "60%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.6f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 2520),
            size = 6,
            text = "70%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.7f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 2880),
            size = 6,
            text = "80%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.8f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToStartOf(volumeBar)
                .alignEndToStartOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton)
                .margins(start = 3240),
            size = 6,
            text = "90%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(0.9f)
                }
            }
        )

        addButtonView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignStartToEndOf(volumeBar)
                .alignEndToEndOf(volumeBar)
                .alignTopToTopOf(zeroVolumeButton),
            size = 6,
            text = "100%",
            callback = object : Listener {
                override fun invoke() {
                    volumeCallback.invoke(1f)
                }
            }
        )
    }

}

interface VolumePresenter: Presenter {

    fun setVolume(volume: Float)

    fun setVolumeCallback(callback: (Float) -> Unit)
}

class VolumeInteractor(
    private val player: Player,
    private val presenter: VolumePresenter,
    private val musicPlayerRepository: MusicPlayerRepository,
): Interactor(presenter) {

    val musicPlayer = musicPlayerRepository.getMusicPlayer(player)

    override fun onCreate() {
        super.onCreate()

        presenter.setVolume(musicPlayer.playerVolume)

        presenter.setVolumeCallback { volume ->
            presenter.setVolume(volume)
            musicPlayer.playerVolume = volume
        }
    }
}
