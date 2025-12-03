package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.MarketAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class MarketEnvironment(): ConfigurableEnvironment<MarketApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): MarketApp = MarketApp(player)

    override fun name(): String = "Market"

    override fun icon(): String = "http://textures.minecraft.net/texture/533fc9a45be13ca57a78b21762c6e1262dae411f13048b963d972a29e07096ab"

    override fun permission(): String? = PermissionNode.MARKET.node

    override fun summary(): String = "A simple economy plugin where players can list in game items to sell for other players can purchase."
}

class MarketApp(player: Player): ConfigurableApp(player) {
    private lateinit var appComponent: MarketAppComponent

    @Inject
    lateinit var marketBlock: MarketBlock

    @Inject
    lateinit var marketConfigBlock: MarketConfigBlock

    override fun root(): Block = marketBlock

    override fun config(): Block = marketConfigBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .marketSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
