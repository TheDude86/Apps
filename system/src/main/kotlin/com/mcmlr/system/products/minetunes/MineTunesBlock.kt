package com.mcmlr.system.products.minetunes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.ContextListener
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.Listener
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.data.Origin
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.ViewContainer
import com.mcmlr.blocks.core.bolden
import com.mcmlr.system.products.minetunes.player.MusicPlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import javax.inject.Inject

class MineTunesBlock @Inject constructor(
    player: Player,
    origin: Origin,
    resources: Resources,
    musicRepository: MusicRepository,
): Block(player, origin) {
    private val view = MineTunesViewController(player, origin)
    private val interactor = MineTunesInteractor(player, resources, view, musicRepository)

    override fun interactor(): Interactor = interactor

    override fun view(): ViewController = view
}

class MineTunesViewController(
    private val player: Player,
    origin: Origin,
): NavigationViewController(player, origin), MineTunesPresenter {

    private lateinit var feedContainer: ViewContainer
    private lateinit var play: ButtonView

    override fun setPlayListener(listener: Listener) {
        play.addListener(listener)
    }

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = R.getString(player, S.MINE_TUNES_TITLE.resource()),
            size = 16,
        )

        feedContainer = addViewContainer(
            modifier = Modifier()
                .size(800, FILL_ALIGNMENT)
                .alignTopToBottomOf(title)
                .alignBottomToBottomOf(this)
                .centerHorizontally()
                .margins(top = 150, bottom = 450),
            content = object : ContextListener<ViewContainer>() {
                override fun ViewContainer.invoke() {
                    play = addButtonView(
                        modifier = Modifier()
                            .size(WRAP_CONTENT, WRAP_CONTENT)
                            .center(),
                        text = "${ChatColor.GOLD}Play",
                        highlightedText = "${ChatColor.GOLD}Play".bolden(),
                    )
                }
            }
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

        addButtonView(
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
    }
}

interface MineTunesPresenter: Presenter {
    fun setPlayListener(listener: Listener)
}

class MineTunesInteractor(
    private val player: Player,
    private val resources: Resources,
    private val presenter: MineTunesPresenter,
    private val musicRepository: MusicRepository,
): Interactor(presenter) {

    val musicPlayer = MusicPlayer()

    override fun onCreate() {
        super.onCreate()

        presenter.setPlayListener(object : Listener {
            override fun invoke() {
                val song = musicRepository.loadSong() ?: return
                musicPlayer.playSong(song, player)
            }
        })
    }
}
