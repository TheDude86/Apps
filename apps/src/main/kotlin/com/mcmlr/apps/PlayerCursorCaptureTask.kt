package com.mcmlr.apps

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.data.CursorRepository
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerCursorCaptureTask @Inject constructor(
    private val cursorRepository: CursorRepository,
): BukkitRunnable() {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            cursorRepository.updateStream(CursorModel(it.uniqueId, it.eyeLocation, CursorEvent.MOVE))
        }
    }
}
