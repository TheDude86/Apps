package com.mcmlr.folia

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.RegionAccessor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.math.max

class FoliaScheduler(private val plugin: Plugin) {
    fun run(runnable: Runnable, player: Player) {
        player.scheduler.run(plugin, { runnable.run() }, runnable)
    }

    fun run(runnable: Runnable) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, runnable)
    }

    fun runLater(runnable: Runnable, delayTicks: Long): FoliaTask = FoliaTask(
        Bukkit
            .getGlobalRegionScheduler()
            .runDelayed(
                plugin,
                { runnable.run() },
                max(1, delayTicks)
            )
    )

    fun runTimer(runnable: Runnable, delayTicks: Long, periodTicks: Long): FoliaTask = FoliaTask(
        Bukkit
            .getGlobalRegionScheduler()
            .runAtFixedRate(
                plugin,
                { runnable.run() },
                max(1, delayTicks),
                periodTicks
            )
    )
}

class FoliaTask(private val task: ScheduledTask) {
    fun cancel() {
        task.cancel()
    }
}

class FoliaFoo {

    fun spawn() {
//        Bukkit.getWorlds().first().spawn
    }

}
