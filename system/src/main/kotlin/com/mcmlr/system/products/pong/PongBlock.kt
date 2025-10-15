package com.mcmlr.system.products.pong

import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Interactor
import com.mcmlr.blocks.api.block.NavigationViewController
import com.mcmlr.blocks.api.block.Presenter
import com.mcmlr.blocks.api.block.ViewController
import com.mcmlr.blocks.api.views.ButtonView
import com.mcmlr.blocks.api.views.ItemView
import com.mcmlr.blocks.api.views.Modifier
import com.mcmlr.blocks.api.views.TextView
import com.mcmlr.blocks.core.DudeDispatcher
import com.mcmlr.blocks.core.collectLatest
import com.mcmlr.blocks.core.collectOn
import com.mcmlr.blocks.core.disposeOn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class PongBlock @Inject constructor(
    player: Player,
    origin: Location,
    pongRepository: PongRepository,
): Block(player, origin) {
    private val view = PongViewController(player, origin)
    private val interactor = PongInteractor(player, view, pongRepository)

    override fun view(): ViewController = view
    override fun interactor(): Interactor = interactor
}

class PongViewController(
    player: Player,
    origin: Location
): NavigationViewController(player, origin), PongPresenter {

    private lateinit var point: TextView
    private lateinit var score: TextView
    private lateinit var play: ButtonView
    private lateinit var playerPaddle: ItemView
    private lateinit var opponentPaddle: ItemView
    private lateinit var ball: ItemView

    override fun addStartListener(listener: () -> Unit) = play.addListener(listener)

    override fun setPaddlePosition(y: Int) {
        playerPaddle.setPositionView(y = y)
    }

    override fun setOpponentPaddlePosition(y: Int) {
        opponentPaddle.setPositionView(y = y)
    }

    override fun setBallPosition(x: Int, y: Int) {
        ball.setPositionView(x, y)
    }

    override fun playPoint(score: PongScore, callback: () -> Unit) {
        val title = if (score == PongScore.PLAYER) "${ChatColor.GREEN}${ChatColor.BOLD}${ChatColor.ITALIC}GOAL!!!" else "${ChatColor.RED}${ChatColor.BOLD}${ChatColor.ITALIC}GOAL..."
        point.setTextView(title)

        CoroutineScope(Dispatchers.IO).launch {
            delay(3.seconds)
            callback.invoke()

            CoroutineScope(DudeDispatcher()).launch {
                point.setTextView("")
            }
        }
    }

    override fun playGameOver(winner: Boolean, callback: () -> Unit) {
        val title = if (winner) "${ChatColor.GREEN}${ChatColor.BOLD}${ChatColor.ITALIC}Winner!!!" else "${ChatColor.RED}${ChatColor.BOLD}${ChatColor.ITALIC}You Lost..."
        point.setTextView(title)

        CoroutineScope(Dispatchers.IO).launch {
            delay(3.seconds)

            CoroutineScope(DudeDispatcher()).launch {
                callback.invoke()
                point.setTextView("")
            }
        }
    }

    override fun setScore(score: Pair<Int, Int>) {
        this.score.setTextView("${ChatColor.BOLD}${score.first} - ${score.second}")
    }

    override fun restart() {
        play.setVisibleView(true)
        setScore(Pair(0, 0))
        setPaddlePosition(0)
        setOpponentPaddlePosition(0)
        setBallPosition(0, 450)
    }

    override fun createView() {
        super.createView()

        val title = addTextView(
            modifier = Modifier()
                .size(WRAP_CONTENT, WRAP_CONTENT)
                .alignTopToTopOf(this)
                .alignStartToEndOf(backButton!!)
                .margins(top = 250, start = 400),
            text = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}${ChatColor.ITALIC}${ChatColor.UNDERLINE}Pong!",
            size = 16,
        )

        val field = addViewContainer(
            modifier = Modifier()
                .size(700, 700)
                .alignBottomToBottomOf(this)
                .margins(bottom = 300),
        ) {

            score = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .alignTopToTopOf(this)
                    .centerHorizontally(),
                text = "${ChatColor.BOLD}0 - 0",
            )

            play = addButtonView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .center(),
                text = "Play",
                size = 24,
            ) {
                startGame()
            }

            point = addTextView(
                modifier = Modifier()
                    .size(WRAP_CONTENT, WRAP_CONTENT)
                    .center(),
                text = "",
                size = 24,
            )

            playerPaddle = addItemView(
                modifier = Modifier()
                    .size(10, 100)
                    .alignStartToStartOf(this)
                    .centerVertically()
                    .margins(start = 100),
                item = Material.SMOOTH_QUARTZ,
                teleportDuration = 2,
            )

            opponentPaddle = addItemView(
                modifier = Modifier()
                    .size(10, 100)
                    .alignEndToEndOf(this)
                    .centerVertically()
                    .margins(end = 100),
                item = Material.SMOOTH_QUARTZ
            )

            ball = addItemView(
                modifier = Modifier()
                    .size(40, 40)
                    .position(0, 450),
                item = Material.LIME_CONCRETE,
            )

        }
    }

    private fun startGame() {
        play.setVisibleView(false)
    }
}

interface PongPresenter: Presenter {
    fun addStartListener(listener: () -> Unit)
    fun setPaddlePosition(y: Int)
    fun setOpponentPaddlePosition(y: Int)
    fun setBallPosition(x: Int, y: Int)
    fun playPoint(score: PongScore, callback: () -> Unit)
    fun playGameOver(winner: Boolean, callback: () -> Unit)
    fun setScore(score: Pair<Int, Int>)
    fun restart()
}

class PongInteractor(
    private val player: Player,
    private val presenter: PongPresenter,
    private val pongRepository: PongRepository,
): Interactor(presenter) {
    companion object {
        const val GAME_DISPOSAL = "game"
    }

    override fun onCreate() {
        super.onCreate()

        presenter.addStartListener {
            pongRepository.startGame(player, 0, 450)
            startGame()
        }
    }

    private fun startGame() {
        context.cursorStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                pongRepository.updatePlayerPaddlePosition(min(600, max(-600, ((-it.data.pitch + 3.1) * 19.3).toInt())))
            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

        pongRepository.playerPaddlePositionStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                presenter.setPaddlePosition(it)
            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

        pongRepository.ballPositionStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                presenter.setBallPosition(it.position.x, it.position.y)
            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

        pongRepository.opponentPaddlePositionStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                presenter.setOpponentPaddlePosition(it)
            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

        pongRepository.gameStateStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                if (it == PongGameState.POINT) {
                    val score = pongRepository.score ?: return@collectLatest
                    presenter.playPoint(score) {
                        pongRepository.startNextPoint(0, 450, if (score == PongScore.PLAYER) 1 else -1)
                    }
                } else if (it == PongGameState.FINISH) {
                    presenter.playGameOver(pongRepository.scoreStream().first().first == 10) {
                        clear(GAME_DISPOSAL)
                        pongRepository.restart()
                        presenter.restart()
                    }
                }

            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

        pongRepository.scoreStream()
            .collectOn(DudeDispatcher())
            .collectLatest {
                presenter.setScore(it)
            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)
    }

    override fun onClose() {
        super.onClose()
        pongRepository.stopGame()
    }
}
