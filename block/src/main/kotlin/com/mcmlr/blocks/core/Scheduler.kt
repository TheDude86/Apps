package com.mcmlr.blocks.core

import com.mcmlr.folia.FoliaScheduler
import com.mcmlr.folia.FoliaTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class Scheduler(private val plugin: Plugin) {
    private val foliaScheduler = FoliaScheduler(plugin)

    fun run(runnable: Runnable, player: Player) {
        if (isFolia()) {
            foliaScheduler.run(runnable, player)
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable)
        }
    }

    fun run(runnable: Runnable) {
        if (isFolia()) {
            foliaScheduler.run(runnable)
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable)
        }
    }

    fun runLater(runnable: Runnable, delayTicks: Long): Task = if (isFolia()) {
        Task(foliaScheduler.runLater(runnable, delayTicks))
    } else {
        Task(Bukkit.getScheduler().runTask(plugin, runnable))
    }

    fun runTimer(runnable: Runnable, delayTicks: Long, periodTicks: Long) = if (isFolia()) {
        Task(foliaScheduler.runTimer(runnable, delayTicks, periodTicks))
    } else {
        Task(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks))
    }

    private fun isFolia(): Boolean {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
}

class Task {

    private val bukkitTask: BukkitTask?
    private val foliaTask: FoliaTask?

    constructor(bukkitTask: BukkitTask) {
        this.bukkitTask = bukkitTask
        this.foliaTask = null
    }

    constructor(foliaTask: FoliaTask) {
        this.foliaTask = foliaTask
        this.bukkitTask = null
    }

    fun cancel() {
        bukkitTask?.cancel()
        foliaTask?.cancel()
    }

}