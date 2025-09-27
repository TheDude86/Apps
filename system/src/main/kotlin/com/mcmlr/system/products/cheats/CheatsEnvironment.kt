package com.mcmlr.system.products.cheats

import com.mcmlr.blocks.api.app.App
import com.mcmlr.blocks.api.app.Environment
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.system.SystemApp
import com.mcmlr.system.dagger.CheatsAppComponent
import com.mcmlr.system.products.data.PermissionNode
import org.bukkit.entity.Player
import javax.inject.Inject

class CheatsEnvironment(): Environment<CheatsApp>() {
    override fun build() {
        //Do nothing
    }

    override fun getInstance(player: Player): CheatsApp = CheatsApp(player)

    override fun name(): String = "Cheats"

    override fun icon(): String = "http://textures.minecraft.net/texture/1866ef71fb761e07e1084143cb86960674b18f0881a1190cf15a8e21d9dd9556"

    override fun permission(): String? = PermissionNode.CHEATS.node

    override fun summary(): String = "A collection of utility and fun cheats for server staff and players to play with."
}

class CheatsApp(player: Player): App(player) {
    private lateinit var appComponent: CheatsAppComponent

    @Inject
    lateinit var cheatsBlock: CheatsBlock

    override fun root(): Block = cheatsBlock

    override fun onCreate(child: Boolean) {
        appComponent = (parentApp as SystemApp)
            .systemAppComponent
            .cheatsSubcomponent()
            .app(this)
            .build()

        appComponent.inject(this)
    }
}
