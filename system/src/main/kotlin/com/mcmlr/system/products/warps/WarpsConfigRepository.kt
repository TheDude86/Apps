package com.mcmlr.system.products.warps

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.api.data.WarpConfigModel
import com.mcmlr.system.EnvironmentScope
import javax.inject.Inject

@EnvironmentScope
class WarpsConfigRepository @Inject constructor(
    resources: Resources,
): Repository<WarpConfigModel>(resources.dataFolder()) {

    init {
        loadModel("Warps", "config", WarpConfigModel())
    }

    fun updateWarpsCooldown(cooldown: Int) = save {
        model.cooldown = cooldown
    }

    fun updateWarpsDelay(delay: Int) = save {
        model.delay = delay
    }

    fun delay(): Int = model.delay

    fun cooldown(): Int = model.cooldown
}