package com.mcmlr.system.products.teleport

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.blocks.api.data.TeleportConfigModel
import com.mcmlr.system.EnvironmentScope
import javax.inject.Inject

@EnvironmentScope
class TeleportConfigRepository @Inject constructor(
    resources: Resources,
): Repository<TeleportConfigModel>(resources.dataFolder()) {

    init {
        loadModel("Teleport", "config", TeleportConfigModel())
    }

    fun updateTeleportCooldown(cooldown: Int) = save {
        model.cooldown = cooldown
    }

    fun updateTeleportDelay(delay: Int) = save {
        model.delay = delay
    }

    fun delay(): Int = model.delay

    fun cooldown(): Int = model.cooldown
}