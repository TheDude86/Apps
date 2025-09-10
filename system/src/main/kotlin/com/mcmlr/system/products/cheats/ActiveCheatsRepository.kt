package com.mcmlr.system.products.cheats

import com.mcmlr.blocks.core.*
import com.mcmlr.system.EnvironmentScope
import com.mcmlr.system.products.data.ServerEventsRepository
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import javax.inject.Inject

@EnvironmentScope
class ActiveCheatsRepository @Inject constructor(
    private val serverEventsRepository: ServerEventsRepository,
): FlowDisposer() {
    companion object {
        private const val SPAWNER = "spawner"
    }

    private val activePlayersMap = HashMap<Player, PlayerCheatsModel>()

    fun activateSpawnerCheat(player: Player, entity: EntityType) {
        registerCheat(player, SpawnerCheatModel(entity))

        serverEventsRepository.setCancelBlock(player)
        serverEventsRepository
            .getPlayerInteractEventStream()
            .collectOn(DudeDispatcher())
            .collectLatest {

                if (it.clickedBlock?.type == Material.SPAWNER) {
                    val spawner = it.clickedBlock?.state as? CreatureSpawner
                    spawner?.spawnedType = entity
                    spawner?.update()
                }

                serverEventsRepository.removeCancelBlock(player)
                unregisterCheat(player, ActiveCheatType.SPAWNER)
                clear("$SPAWNER${player.uniqueId}")
            }
            .disposeOn(collection = "$SPAWNER${player.uniqueId}", disposer = this)
    }

    private fun unregisterCheat(player: Player, cheat: ActiveCheatType) {
        val activePlayer = activePlayersMap[player] ?: return
        activePlayer.activeCheats = activePlayer.activeCheats.filter { it.cheat != cheat }.toMutableList()

        if (activePlayer.activeCheats.isEmpty()) {
            activePlayer.scoreboard.getObjective("cheats")?.unregister()
            activePlayersMap.remove(player)
        } else {
            drawScoreboard(player)
        }
    }

    private fun registerCheat(player: Player, cheat: CheatModel) {
        val activePlayer = activePlayersMap[player]
        if (activePlayer != null) {
            activePlayer.activeCheats.add(cheat)
        } else {
            val scoreboard = createBoard() ?: return
            player.scoreboard = scoreboard
            activePlayersMap[player] = PlayerCheatsModel(mutableListOf(cheat), scoreboard)
        }

        drawScoreboard(player)
    }

    private fun drawScoreboard(player: Player) {
        val playerModel = activePlayersMap[player] ?: return
        playerModel.scoreboard.getObjective("cheats")?.unregister()

        val objective = playerModel.scoreboard.registerNewObjective("cheats", "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = "${ChatColor.RED}${ChatColor.BOLD}Active Cheats"

        playerModel.activeCheats.forEachIndexed { index, cheatModel ->
            objective.getScore(cheatModel.title()).score = index
            objective.getScore("").score = index + 1
        }
    }

    private fun createBoard(): Scoreboard? {
        val scoreboardManager = Bukkit.getScoreboardManager() ?: return null
        val scoreboard = scoreboardManager.newScoreboard

        return scoreboard
    }
}

data class PlayerCheatsModel(var activeCheats: MutableList<CheatModel>, val scoreboard: Scoreboard)

data class SpawnerCheatModel(val entity: EntityType): CheatModel(ActiveCheatType.SPAWNER) {
    override fun title(): String = "${ChatColor.BOLD}Spawner ${ChatColor.GREEN}${entity.name.lowercase().titlecase()}"
}

abstract class CheatModel(val cheat: ActiveCheatType) {
    abstract fun title(): String
}

enum class ActiveCheatType {
    SPAWNER,
}
