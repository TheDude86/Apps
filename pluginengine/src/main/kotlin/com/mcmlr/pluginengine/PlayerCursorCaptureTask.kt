package com.mcmlr.pluginengine

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.data.InputRepository
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerCursorCaptureTask @Inject constructor(
    private val inputRepository: InputRepository,
): BukkitRunnable() {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            inputRepository.updateStream(CursorModel(it.uniqueId, it.eyeLocation, CursorEvent.MOVE))
        }
    }
}
