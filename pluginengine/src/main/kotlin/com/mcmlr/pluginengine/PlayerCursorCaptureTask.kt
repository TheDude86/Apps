package com.mcmlr.pluginengine

import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.data.BillboardModel
import com.mcmlr.blocks.api.data.InputRepository
import com.mcmlr.blocks.api.log
import com.mcmlr.system.SystemConfigRepository
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerCursorCaptureTask @Inject constructor(
    private val inputRepository: InputRepository,
    private val systemConfigRepository: SystemConfigRepository,
): Runnable {
    override fun run() {
        val ledger = mutableListOf<Pair<Player, BillboardModel>>()

        Bukkit.getOnlinePlayers().forEach {
            inputRepository.updateStream(CursorModel(it.uniqueId, it.eyeLocation, CursorEvent.MOVE))
            systemConfigRepository.model.billboards.billboards.forEach { billboard ->
                if (it.location.distance(billboard.location) < 10.0) {
                    ledger.add(Pair(it, billboard))
                }
            }
        }

        systemConfigRepository.updateBillboardsStream(ledger)
    }
}
