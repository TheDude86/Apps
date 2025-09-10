package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.data.MarketConfigModel
import com.mcmlr.blocks.api.data.Repository
import com.mcmlr.system.EnvironmentScope
import javax.inject.Inject

@EnvironmentScope
class MarketConfigRepository @Inject constructor(
    resources: Resources,
): Repository<MarketConfigModel>(resources.dataFolder()) {

    init {
        loadModel("Market", "config", MarketConfigModel())
    }

    fun updateMarketMaxOrders(maxOrders: Int) = save {
        model.maxOrders = maxOrders
    }

    fun maxOrders(): Int = model.maxOrders
}