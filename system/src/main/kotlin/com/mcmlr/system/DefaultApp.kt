package com.mcmlr.system

import com.mcmlr.system.products.base.AppEventHandlerFactory
import com.mcmlr.blocks.api.App
import com.mcmlr.blocks.api.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

class DefaultApp(player: Player): App(player) {

    private lateinit var defaultAppComponent: DefaultAppComponent

    @Inject
    lateinit var rootBlock: LandingBlock

    @Inject
    lateinit var eventHandler: AppEventHandlerFactory

    override fun onCreate(child: Boolean) {
        super.onCreate(child)

        defaultAppComponent = (environment as DefaultEnvironment)
            .environmentComponent
            .subcomponent()
            .app(this)
            .build()

        defaultAppComponent.inject(this)

        registerEvents(eventHandler)
    }

    override fun root(): Block = rootBlock
}