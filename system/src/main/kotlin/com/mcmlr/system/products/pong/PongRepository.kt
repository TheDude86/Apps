package com.mcmlr.system.products.pong

import com.mcmlr.blocks.api.views.Coordinates
import com.mcmlr.blocks.core.*
import com.mcmlr.system.dagger.AppScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bukkit.entity.Player
import org.joml.Vector2i
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@AppScope
class PongRepository @Inject constructor(

): FlowDisposer() {
    companion object {
        private const val PADDLE_LENGTH = 160
        private const val X_VELOCITY_RANGE = 40f
        private const val X_MAX_VELOCITY = 70
        private const val AI_MAX_VELOCITY = 20
        private const val X_CONSTANT = X_VELOCITY_RANGE / PADDLE_LENGTH
        private const val GAME_DISPOSAL = "game"
    }

    private val ballPositionFlow = MutableStateFlow<PongBallModel?>(null)
    private val playerPaddlePositionFlow = MutableStateFlow<Int?>(null)
    private val opponentPaddlePositionFlow = MutableStateFlow(0)
    private val gameStateFlow = MutableStateFlow(PongGameState.START)
    private val scoreFlow = MutableStateFlow(Pair(0, 0))
    private var difficulty = 0

    var score: PongScore? = null

    fun startGame(player: Player, x: Int, y: Int) {
        ballPositionFlow.emitBackground(PongBallModel(position = Coordinates(x, y), velocity = Vector2i(-20, -20)))

        gameTickFlow()
            .withLatestFrom(ballPositionFlow.filterNotNull())
            .collectOn(Dispatchers.IO)
            .collectLatest {

                when (gameStateFlow.first()) {
                    PongGameState.PLAY -> playTick(it.second.position, it.second.velocity)
                    else -> {}
                }

            }
            .disposeOn(collection = GAME_DISPOSAL, disposer = this)

//        cursorRepository.cursorStream(player.uniqueId)
//            .filter { it.event != CursorEvent.CLEAR }
//            .collectOn(Dispatchers.IO)
//            .collectLatest { model ->
//                val rotation = model.data.pitch
//                val newY = (-24.5 * (rotation - 3.5)).toInt()
//                val finalY = min(600, max(-600, newY))
//                playerPaddlePositionFlow.emit(finalY)
//            }
//            .disposeOn(collection = GAME_DISPOSAL, disposer = this)
    }

    fun restart() {
        clear(GAME_DISPOSAL)
        scoreFlow.emitBackground(Pair(0, 0))
    }

    private suspend fun playTick(position: Coordinates, velocity: Vector2i) {
        updateAIPaddle(position, velocity)
        val newPosition = Coordinates(position.x + (velocity.x), position.y + (velocity.y))

        val xVelocity: Int
        val yVelocity: Int
        if (newPosition.x <= -600) {
            val paddle = playerPaddlePositionFlow.first() ?: 0
            when (val diff = paddle - newPosition.y) {
                in -PADDLE_LENGTH..-1 -> {
                    xVelocity = (X_MAX_VELOCITY + (X_CONSTANT * diff)).toInt()
                    yVelocity = (X_CONSTANT * -diff).toInt()
                }
                in 1..PADDLE_LENGTH -> {
                    xVelocity = (X_MAX_VELOCITY - (X_CONSTANT * diff)).toInt()
                    yVelocity = (X_CONSTANT * -diff).toInt()
                }
                0 -> {
                    xVelocity = X_MAX_VELOCITY
                    yVelocity = 0
                }
                else -> {
                    xVelocity = 0
                    yVelocity = 0
                    score = PongScore.OPPONENT
                    val score = scoreFlow.first()
                    val newScore = Pair(score.first, score.second + 1)
                    scoreFlow.emit(newScore)
                    if (newScore.second == 10) {
                        gameStateFlow.emit(PongGameState.FINISH)
                    } else {
                        gameStateFlow.emit(PongGameState.POINT)
                    }
                }
            }

        } else if (newPosition.x >= 600) {
            val paddle = opponentPaddlePositionFlow.first()
            when (val diff = paddle - newPosition.y) {
                in -PADDLE_LENGTH..-1 -> {
                    xVelocity = -(X_MAX_VELOCITY + (X_CONSTANT * diff)).toInt()
                    yVelocity = (X_CONSTANT * -diff).toInt()
                }
                in 1..PADDLE_LENGTH -> {
                    xVelocity = -(X_MAX_VELOCITY - (X_CONSTANT * diff)).toInt()
                    yVelocity = (X_CONSTANT * -diff).toInt()
                }
                0 -> {
                    xVelocity = -X_MAX_VELOCITY
                    yVelocity = 0
                }
                else -> {
                    xVelocity = 0
                    yVelocity = 0
                    score = PongScore.PLAYER
                    val score = scoreFlow.first()
                    val newScore = Pair(score.first + 1,  score.second)
                    scoreFlow.emit(newScore)
                    if (newScore.first == 10) {
                        gameStateFlow.emit(PongGameState.FINISH)
                    } else {
                        gameStateFlow.emit(PongGameState.POINT)
                    }
                }
            }
        } else if (newPosition.y <= -680) {
            xVelocity = velocity.x
            yVelocity = abs(velocity.y)
        } else if ( newPosition.y >= 680) {
            xVelocity = velocity.x
            yVelocity = -abs(velocity.y)
        } else {
            xVelocity = velocity.x
            yVelocity = velocity.y
        }

        val newVelocity = Vector2i(xVelocity, yVelocity)
        ballPositionFlow.emit(PongBallModel(position = newPosition, newVelocity))
    }

    private suspend fun updateAIPaddle(position: Coordinates, velocity: Vector2i) {
        if (velocity.x < 0) return
        val next = Coordinates(position.x + (velocity.x), position.y + (velocity.y))
        val current = opponentPaddlePositionFlow.first()
        val deltaY = min((AI_MAX_VELOCITY + difficulty), max(-(AI_MAX_VELOCITY + difficulty), next.y - current))
        val finalY = min(600, max(-600, current + deltaY))
        opponentPaddlePositionFlow.emit(finalY)
    }

    fun stopGame() {
        clear()
    }

    fun startNextPoint(x: Int, y: Int, direction: Int) {
        ballPositionFlow.emitBackground(PongBallModel(position = Coordinates(x, y), velocity = Vector2i(-20 * direction, -20)))

        CoroutineScope(Dispatchers.IO).launch {
            delay(1.seconds)
            gameStateFlow.emit(PongGameState.PLAY)
        }
    }

    private fun gameTickFlow() = flow {
        var tick = 0
        gameStateFlow.emit(PongGameState.PLAY)

        while (gameStateFlow.first() == PongGameState.PLAY || gameStateFlow.first() == PongGameState.POINT) {
            delay(50)
            emit(tick++)
        }
    }.flowOn(Dispatchers.IO)

    fun ballPositionStream(): Flow<PongBallModel> = ballPositionFlow.filterNotNull()

    fun playerPaddlePositionStream(): Flow<Int> = playerPaddlePositionFlow.filterNotNull()

    fun opponentPaddlePositionStream(): Flow<Int> = opponentPaddlePositionFlow

    fun scoreStream(): Flow<Pair<Int, Int>> = scoreFlow

    fun gameStateStream(): Flow<PongGameState> = gameStateFlow
}

data class PongBallModel(val position: Coordinates, val velocity: Vector2i)

enum class PongScore {
    PLAYER,
    OPPONENT
}

enum class PongGameState {
    START,
    PLAY,
    POINT,
    PAUSE,
    FINISH,
}