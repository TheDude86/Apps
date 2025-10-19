package com.mcmlr.system.products.homes

import com.mcmlr.blocks.api.app.ConfigurableApp
import com.mcmlr.blocks.api.app.ConfigurableEnvironment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.HomesAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class HomesEnvironment(): ConfigurableEnvironment<HomesApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): HomesApp = HomesApp(player)

    override fun name(): String = "Homes"

    override fun icon(): String = "http://textures.minecraft.net/texture/ff812112dd187e7c8ddb5c3b8e854e82f19197414a8cdb542021f1a491897e53"

    override fun permission(): String? = PermissionNode.HOME.node

    override fun summary(): String = "This app allows players to save home locations on the server and teleport to those locations."
}

class HomesApp(player: Player): ConfigurableApp(player) {
    private lateinit var appComponent: HomesAppComponent

    @Inject
    lateinit var homesBlock: HomesBlock

    @Inject
    lateinit var homesConfigBlock: HomeConfigBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .homeSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }

    override fun root(): Block = homesBlock

    override fun config(): Block = homesConfigBlock

}
