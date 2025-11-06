package com.mcmlr.system.products.heads

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.HeadsAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class HeadsEnvironment(): Environment<HeadsApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): HeadsApp = HeadsApp(player)

    override fun name(): String = "Heads"

    override fun icon(): String = ""

    override fun permission(): String? = PermissionNode.WORKBENCH.node

    override fun summary(): String = "Opens various Heads for the player to use."
}

class HeadsApp(player: Player): App(player) {
    private lateinit var appComponent: HeadsAppComponent

    @Inject
    lateinit var HeadsBlock: HeadsBlock

    override fun root(): Block = HeadsBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .headsSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
