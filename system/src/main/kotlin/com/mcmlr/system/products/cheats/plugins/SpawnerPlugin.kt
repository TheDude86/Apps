package com.mcmlr.system.products.cheats.plugins

import com.mcmlr.blocks.api.plugin.Plugin
import com.mcmlr.system.products.cheats.CheatType
import com.mcmlr.system.products.cheats.CheatsEventListener

class SpawnerPlugin(
    private val cheatsEventListener: CheatsEventListener,
): Plugin<CheatType> {
    override fun isApplicable(data: CheatType): Boolean = data == CheatType.SPAWNER

    override fun execute(data: CheatType) {
        cheatsEventListener.setSpawnerMode()
    }
}