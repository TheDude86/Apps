package com.mcmlr.system.products.homes

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.HomeConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.dagger.EnvironmentScope
import javax.inject.Inject

@EnvironmentScope
class HomesConfigRepository @Inject constructor(
    resources: Resources,
): Repository<HomeConfigModel>(resources.dataFolder()) {

    init {
        loadModel("Homes", "config", HomeConfigModel())
    }

    fun updateHomesCooldown(cooldown: Int) = save {
        model.cooldown = cooldown
    }

    fun updateHomesDelay(delay: Int) = save {
        model.delay = delay
    }

    fun updateMaxHomes(maxHomes: Int) = save {
        model.maxHomes = maxHomes
    }

    fun delay(): Int = model.delay

    fun cooldown(): Int = model.cooldown

    fun maxHomes(): Int = model.maxHomes
}